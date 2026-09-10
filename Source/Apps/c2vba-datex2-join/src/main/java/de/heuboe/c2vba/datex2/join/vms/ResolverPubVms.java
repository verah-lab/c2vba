package de.heuboe.c2vba.datex2.join.vms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.heuboe.c2vba.datex2.join.ResolverPub;
import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.vms.D2LogicalModel;
import eu.datex2.schema._2._2_0.vms.PayloadPublication;
import eu.datex2.schema._2._2_0.vms.UpdateMethodEnum;
import eu.datex2.schema._2._2_0.vms.VmsPublication;
import eu.datex2.schema._2._2_0.vms.VmsUnit;
import eu.datex2.schema._2._2_0.vms._VmsUnitVmsIndexVms;


/**
 * 
 * Retrieves basic generic attributes of a Vms publication
 * Updates a Vms publication with content of another
 * 
 * @author peters
 *
 */
public class ResolverPubVms implements ResolverPub<D2LogicalModel> {
	
	private static final Logger LOGGER = Logger.getLogger( ResolverPubVms.class );
	
	@Override
	public boolean isSnapshot( D2LogicalModel d2lm ) {
		eu.datex2.schema._2._2_0.vms.Subscription subscription = d2lm.getExchange().getSubscription();
		if( subscription != null ) {
			if( subscription.getUpdateMethod() != UpdateMethodEnum.SNAPSHOT ) {     // NOSONAR
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int getElementCount( D2LogicalModel d2lm ) {
		return ((VmsPublication)d2lm.getPayloadPublication()).getVmsUnit().size();
	}
	
	@Override
	public Date getPubTime( D2LogicalModel d2lm ) {
		return ((VmsPublication)d2lm.getPayloadPublication()).getPublicationTime();
	}
	
	@Override
	public void updatePub( D2LogicalModel pub, D2LogicalModel updatePub ) {     // NOSONAR
		VmsPublication vmsPub = (VmsPublication)pub.getPayloadPublication();
		VmsPublication vmsUpdatePub = (VmsPublication)updatePub.getPayloadPublication();
		
		Map<String, List<VmsUnit> > id2VmsUnit = new HashMap<>();
		for( VmsUnit vmsUnit : vmsPub.getVmsUnit() ) {
			id2VmsUnit.computeIfAbsent( vmsUnit.getVmsUnitReference().getId(), i -> new ArrayList<>() ).add( vmsUnit );
		}
		
		for( VmsUnit updUnit : vmsUpdatePub.getVmsUnit() ) {
			String vmsUnitId = updUnit.getVmsUnitReference().getId();
			List<VmsUnit> vmsUnits = id2VmsUnit.get( vmsUnitId );
			if( vmsUnits == null ) {
				id2VmsUnit.computeIfAbsent( updUnit.getVmsUnitReference().getId(), i -> new ArrayList<>() ).add( updUnit );
			} else {
				for( _VmsUnitVmsIndexVms updVms : updUnit.getVms() ) {
					boolean found = false;
					
					for( VmsUnit vmsUnit : vmsUnits ) {
						for( _VmsUnitVmsIndexVms vms : vmsUnit.getVms() ) {
							if( vms.getVmsIndex() == updVms.getVmsIndex() ) {
								vms.setVms( updVms.getVms() );
								found = true;
								break;
							}
						}
						
						if( found ) {
							break;
						}
					}
				
					if( !found ) {
						id2VmsUnit.get( vmsUnitId ).get(0).getVms().add( updVms );
						LOGGER.warn( "No Vms with index " + updVms.getVmsIndex() + 
								     " of VmsUnit " +  vmsUnitId ); 
					}
				}
			}
		}
		
		vmsPub.getVmsUnit().clear();
		vmsPub.getVmsUnit().addAll( id2VmsUnit.values().stream().flatMap(List::stream).collect( Collectors.toList() ) );
	}
	
	@Override
	public void updateTableRef( eu.datex2.schema._2._2_0.D2LogicalModel d2lm, String tableId, String tableVersion ) {
		eu.datex2.schema._2._2_0.VmsPublication vmsp = (eu.datex2.schema._2._2_0.VmsPublication)d2lm.getPayloadPublication();
		for( eu.datex2.schema._2._2_0.VmsUnit vu : vmsp.getVmsUnit() ) {
			vu.getVmsUnitTableReference().setId( tableId );
			vu.getVmsUnitTableReference().setVersion( tableVersion );
		}
	}
	
	@Override
	public void clearElements( eu.datex2.schema._2._2_0.D2LogicalModel d2lm ) {
		((eu.datex2.schema._2._2_0.VmsPublication)d2lm.getPayloadPublication()).getVmsUnit().clear();
	}


	@Override
	public D2LogicalModel clone(D2LogicalModel d2lm) {
		return (D2LogicalModel)d2lm.clone();
	}
	
	
	@Override
	public String getTableId( D2LogicalModel d2lm ) { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof VmsPublication && !((VmsPublication)pp).getVmsUnit().isEmpty() ) {
			return ((VmsPublication)pp).getVmsUnit().get(0).getVmsUnitTableReference().getId();
		}
		return "";
	}

	@Override
	public String getTableVersion( D2LogicalModel d2lm ) { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof VmsPublication && !((VmsPublication)pp).getVmsUnit().isEmpty() ) {
			return ((VmsPublication)pp).getVmsUnit().get(0).getVmsUnitTableReference().getVersion();
		}
		return "";
	}

}
