package de.heuboe.srb.datex2.srp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import com.google.common.collect.Sets;

import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.datex2.base.JAXBConverter;
import de.heuboe.log.Logger;
import de.heuboe.srb.datex2.srp.Reader.D2Strategy;
import de.heuboe.srb.datex2.srp.StrategyConfig.StrategyCause;
import de.heuboe.wls.util.WlsException;
import eu.datex2.schema._2._2_0.srp.Cause;
import eu.datex2.schema._2._2_0.srp.Comment;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;
import eu.datex2.schema._2._2_0.srp.GeneralNetworkManagement;
import eu.datex2.schema._2._2_0.srp.LifeCycleManagement;
import eu.datex2.schema._2._2_0.srp.Management;
import eu.datex2.schema._2._2_0.srp.MultilingualString;
import eu.datex2.schema._2._2_0.srp.NonManagedCause;
import eu.datex2.schema._2._2_0.srp.OperatingModeEnum;
import eu.datex2.schema._2._2_0.srp.PayloadPublication;
import eu.datex2.schema._2._2_0.srp.Route;
import eu.datex2.schema._2._2_0.srp.Situation;
import eu.datex2.schema._2._2_0.srp.SituationPublication;
import eu.datex2.schema._2._2_0.srp.SituationRecord;
import eu.datex2.schema._2._2_0.srp.StrategicRouteManagement;
import eu.datex2.schema._2._2_0.srp.Subscription;
import eu.datex2.schema._2._2_0.srp.SubscriptionStateEnum;
import eu.datex2.schema._2._2_0.srp.Target;
import eu.datex2.schema._2._2_0.srp.Trigger;
import eu.datex2.schema._2._2_0.srp.UpdateMethodEnum;
import eu.datex2.schema._2._2_0.srp.VehicleCharacteristics;
import eu.datex2.schema._2._2_0.srp.VehicleTypeEnum;
import eu.datex2.schema._2._2_0.srp.WeightingAndVehicleClassification;
import eu.datex2.schema._2._2_0.srp._GeneralNetworkManagementExtensionType;
import eu.datex2.schema._2._2_0.srp._RouteIndexWeightingAndVehicleClassification;

/**
 * 
 * Builds D2 publicaitons
 * 
 * @author peters
 *
 */
public class Builder
{
	
	private static final Logger LOGGER = Logger.getLogger( Builder.class );
	
	private StrategyConfig strategyConfig = null;
	
	private PublicationMode pubMode;
	private Date subscriptionStartTime = null;
	private Reader reader;
	private Publisher publisher; 
	private Map< String, String > sId2D2Id = new HashMap<>();
	private Map< String, Date > sId2StartTime = new HashMap<>();
	
	private Map<String,D2Strategy> curActiveStrategies = new HashMap<>();
	
	private int pubInterval;
	private String d2Sender; 
	private String d2Target;

	/**
	 * 
	 * Constructor
	 * 
	 * @throws SRPException Error
	 */
	public Builder( Reader reader,      // NOSONAR
			        int pubInterval, 
			        PublicationMode pubMode, 
			        String strategyRuleFile, 
			        String strategyCauseFile, 
			        String d2SchemaLocation,
			        String d2Sender, 
			        String d2Target )
			throws SRPException, IOException
	{
		this.pubInterval = pubInterval;
		this.d2Sender = d2Sender;
		this.d2Target = d2Target;
		this.pubMode = pubMode;
		
		this.reader = reader;
		
		strategyConfig = new StrategyConfig();
		strategyConfig.init( strategyRuleFile, strategyCauseFile );
		
		publisher = new Publisher();
		publisher.init( pubMode, d2SchemaLocation );
	}
	
	/**
	 * 
	 * Builds DATEX II situation
	 * 
	 * @param now						Timestamp now
	 * @param s							Strategy
	 * @param ended						true: expired
	 * @param newSId2D2Id				Strategy ID --> DATEX II ID
	 * @param newSId2StartTime			Start times
	 * @return							Strategy as DATEX II situation
	 * @throws SRPException				Error
	 */
	public Situation buildSituation( Date now,    // NOSONAR
									 D2Strategy s, 
									 boolean ended,
									 Map< String, String > newSId2D2Id,	
									 Map< String, Date > newSId2StartTime
								   ) 
			throws SRPException
	{
		Situation sit = publisher.createSituation();
		
		String sitId = sId2D2Id.get( s.getId() );
		if( sitId == null )
		{
			sitId = "" + Long.toString( (new Date()).getTime() );
			Util.sleep( 10 );
		}
		
		Date sitTime = sId2StartTime.get( s.getId() );
		if( sitTime == null )
		{
			sitTime = now;
		}
		Date expiry = new Date( now.getTime() + (2L * pubInterval * 1000L) );
		
		if( newSId2D2Id != null ) {
			newSId2D2Id.put( s.getId(), sitId );
		}
		if( newSId2StartTime != null ) {
			newSId2StartTime.put( s.getId(), sitTime );
		}
		
		sit.setId( "S" + sitId );
		sit.setVersion( "1" );
		sit.setSituationVersionTime( sitTime );
		
		SituationRecord sitRecord = sit.getSituationRecord().get(0);
		
		sitRecord.setId( "R" + sitId );
		sitRecord.setVersion( "1" );
		sitRecord.setSituationRecordCreationTime( sitTime );
		sitRecord.setSituationRecordVersionTime( sitTime );
		
		sitRecord.getValidity().getValidityTimeSpecification().setOverallStartTime( sitTime ); 
		sitRecord.getValidity().getValidityTimeSpecification().setOverallEndTime( expiry ); 
		
		sitRecord.setGroupOfLocations( s.getLocation() ); 
		
		if( ended ) {
			Management management = new Management();
			LifeCycleManagement lifeCycleManagement = new LifeCycleManagement();
			lifeCycleManagement.setEnd( true );
			management.setLifeCycleManagement( lifeCycleManagement );
			sitRecord.setManagement( management );
		}
		
		StrategyCause sc = strategyConfig.getStrategyCause( s.getId() );
		if( sc == null ) {
			LOGGER.warn( "No cause for strategy <" + s.getId() + ">!" );
			sitRecord.setCause( null );
		} else {
			Cause cause = sitRecord.getCause();
			if( cause instanceof NonManagedCause ) {
				NonManagedCause nmc = (NonManagedCause)cause;
				MultilingualString ms = Util.toD2String( sc.getNameDe(), sc.getNameEn() );
				nmc.setCauseDescription( ms );
			}
		}
		
		String co = strategyConfig.getStrategyComment( s.getId() );
		if( co == null ) {
			co = s.getName();
		}
		if( co != null ) {
			Comment comment = new Comment();
			comment.setComment( Util.toD2String( co ) ); 
			sitRecord.getGeneralPublicComment().add( comment );
		}

		GeneralNetworkManagement gnm = (GeneralNetworkManagement)sitRecord;
		String api = s.getId();
		if( StrategyConfig.isC2VBAStrategy( api ) ) {
			api = StrategyConfig.getC2VBAStrategyIdMainPart( api );
		}
		gnm.setActionPlanIdentifier( api );
		
		_GeneralNetworkManagementExtensionType ext = gnm.getGeneralNetworkManagementExtension();
		
		StrategicRouteManagement srm = s.getStrategicRouteManagement(); 
		if( srm != null ) {
			
			// Set weightingAndVehicleClassification (and remove alternative, not referenced routes)
			
			try {
				srm = JAXBConverter.clone( StrategicRouteManagement.class, srm );
				
				List<Route> refRoutes = new ArrayList<>();
				
				int index = 0;
				for( Route route : srm.getRoute() ) {
					
					_RouteIndexWeightingAndVehicleClassification riwavc = new _RouteIndexWeightingAndVehicleClassification();
					riwavc.setIndex( 0 );
					WeightingAndVehicleClassification wavc = new WeightingAndVehicleClassification();
					
//					wavc.setWeight( 100.0F );
					if( ( srm.getRoute().size() == 1 ) || !route.isOriginalRoute() ) {
						// There is only on route or this is an alternative route
						wavc.setWeight( 100.0F );
					} else {
						wavc.setWeight( 0.0F );
					}
					
					VehicleCharacteristics vc = new VehicleCharacteristics();
					vc.getVehicleType().add( VehicleTypeEnum.ANY_VEHICLE );
					wavc.getValidForVehiclesWithCharacteristics().add( vc );
					
					riwavc.setWeightingAndVehicleClassification( wavc );
					route.getWeightingAndVehicleClassification().add( riwavc );
					
					if( ( srm.getRoute().size() > 2 ) && ( index > 0 ) ) {
						String arn = s.getId(); 
						if( arn != null ) {
							if( arn.endsWith( "A" + index ) ) {    // NOSONAR
								refRoutes.add( route );
							}
						} 
					} else {
						refRoutes.add( route );
					}
					
					index++;
				}
				
				if( ( srm.getRoute().size() > 2 ) && ( refRoutes.size() != 2 ) ) {
					LOGGER.warn( "Did not detect alternative route: " + s.getId() );
				}
				
				srm.getRoute().clear();
				srm.getRoute().addAll( refRoutes );
			} catch ( JAXBException ex ) {     // NOSONAR
				LOGGER.warn( "Error cloning StrategicRouteManagement: "  + ex.toString() );
			}
			ext.setGeneralNetworkManagementExtended( srm ); 
		} else {
			srm = (StrategicRouteManagement)ext.getGeneralNetworkManagementExtended();
	
			srm.setNameOfRouteManagement( Util.toD2String( s.getName() ) );
			
			Trigger trigger = new Trigger();
			trigger.setTriggerDescription( s.getTriggerDescription() );
			trigger.setLocation( s.getTriggerLocation() ); 
			
			srm.getTriggerOrigin().clear();
			srm.getTriggerOrigin().add( trigger ); 
			
			trigger = new Trigger();
			trigger.setTriggerDescription( s.getTriggerDescription() );
			trigger.setLocation( s.getTriggerDestination() ); 

			srm.getTriggerDestination().clear();
			srm.getTriggerDestination().add( trigger );
			
			boolean hasAlternativeRoute = false;
			
			List<Route> routes = srm.getRoute();
			for( Route route : routes )
			{
				if( Boolean.TRUE.equals(route.isOriginalRoute()) ) {
					route.setNameOfRoute( Util.toD2String( s.getOriginalRouteName() ) );
					route.setItinerary( s.getOriginalRoute() );
				} else if( s.getAlternativeRouteName() == null ) {
						LOGGER.info( "Strategy <" + s.getId() + ">:" );
						LOGGER.info( "No alternative route !" );
				} else {
					hasAlternativeRoute = true;
	
					route.setNameOfRoute( Util.toD2String( s.getAlternativeRouteName() ) );
					route.setItinerary( s.getAlternativeRoute() );
				}
			}
			
			if( !hasAlternativeRoute )
			{
				routes.remove( 1 );
			}
		}
		return sit;
	}
	
	private void addSubscription( D2LogicalModel d2lm, Date now, boolean snapshot ) {
		Subscription subscription = new Subscription();
		
		if( subscriptionStartTime == null ) {
			subscriptionStartTime = now;
		}
		subscription.setSubscriptionStartTime( subscriptionStartTime );
		
		Target target = new Target();
		target.setProtocol( "SOAP" );
		target.setAddress( d2Target );
		subscription.getTarget().add( target );
		
		subscription.setOperatingMode( OperatingModeEnum.OPERATING_MODE_1 );  
		subscription.setSubscriptionState( SubscriptionStateEnum.ACTIVE );
	     
		if( snapshot ) {
			subscription.setUpdateMethod( UpdateMethodEnum.SNAPSHOT );
		} else {
			subscription.setUpdateMethod( UpdateMethodEnum.ALL_ELEMENT_UPDATE );
		}
		
		d2lm.getExchange().setSubscription( subscription );    // NOSONAR  (Subscription not part of StrategicRouting.xsd )
	}
	
	/**
	 * 
	 * Builds publication
	 * 
	 * @param now				Timestamp now
	 * @param onChange			true: publish only if some strategy state has changed
	 * @param snapshot			true: publish all strategies
	 * @param strategyStates	Current strategy states
	 * @return					Publication
	 * @throws SRPException		Exception
	 * @throws WlsException 	WLS error	
	 */
	public D2LogicalModel buildPublication( Date now, 
            								boolean onChange, 
			                                boolean snapshot, 
			                                StrategyStates strategyStates ) 
			throws SRPException, WlsException
	{
		D2LogicalModel d2lm = publisher.getPublication();
		if( ( d2Sender != null ) && !d2Sender.isEmpty() ) {
			d2lm.getExchange().getSupplierIdentification().setNationalIdentifier( d2Sender );
		}
		 
		if( pubMode == PublicationMode.OnUpdateCyclicSnapshot ) {
			// Für diesen PublicationMode ist die Kennzeichnung snapshot/allElementUpdate notwendig.
			// Allerdings ist das im Schema StrategicRouting nicht zulässig: das Subscription-Element gehört nicht zum Profil. 
			// ( Es gibt ein um Subscription erweitertes Schema (StrategicRouting_withSubscriptionLifecycle.xsd).)
			addSubscription( d2lm, now, snapshot );
		}
		
		PayloadPublication plp = d2lm.getPayloadPublication();
		SituationPublication sp = (SituationPublication)plp;
		sp.setPublicationTime( now );
		if( ( d2Sender != null ) && !d2Sender.isEmpty() ) {
			sp.getPublicationCreator().setNationalIdentifier( d2Sender );
		}
		 
		sp.getSituation().clear();
		
		List<D2Strategy> ss = reader.getActiveStrategies( publisher, strategyStates );
		 
		Map< String, String > newSId2D2Id = new HashMap<>();	
		Map< String, Date > newSId2StartTime = new HashMap<>();
		
		Map<String,D2Strategy> activeStrategies = new HashMap<>();
		ss.forEach( s -> activeStrategies.put( s.getId(), s ) );
		
		if( !onChange 
				||  
		    !Sets.difference( activeStrategies.keySet(), curActiveStrategies.keySet() ).isEmpty()
				||  
		    !Sets.difference( curActiveStrategies.keySet(), activeStrategies.keySet() ).isEmpty() ) {
		
			List<D2Strategy> ssSorted = ss.stream().sorted( (s1, s2)  -> s1.getId().compareTo( s2.getId() ) ).collect( Collectors.toList() );
			for( D2Strategy s : ssSorted ) {
				if( !curActiveStrategies.containsKey( s.getId() ) || snapshot ) {
					Situation sit = buildSituation( now, s, false,
													newSId2D2Id, newSId2StartTime );
					
					LOGGER.info( "Added situation for strategy <" + s.getId() + ">" );
					sp.getSituation().add( sit );
				}
			}
			
			if( !snapshot ) {
				
				// Aufhebungen ergänzen
				
				for( D2Strategy s : curActiveStrategies.values() ) {
					String id = s.getId();
					if( !activeStrategies.containsKey( id ) ) {
						Situation sit = buildSituation( now, s, true,
													    null, null );
		
						LOGGER.info( "Added ended situation for strategy <" + s.getId() + ">" );
						sp.getSituation().add( sit );
					}
				}
			}
			
			sId2D2Id = newSId2D2Id;
			sId2StartTime = newSId2StartTime;
			curActiveStrategies = activeStrategies; 
	
			return d2lm;
		}
	
		return null;
	}
	
	
	/**
	 * 
	 * D2LogicalModel to String
	 * 
	 * @param publication			D2LogicalModel	
	 * @return						String
	 * @throws SRPException			Error
	 */
	public String toString( D2LogicalModel publication )
			throws SRPException
	{
		return publisher.toString( publication );
	}
}
