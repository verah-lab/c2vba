package de.heuboe.c2vba.datex2.join;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.c2vba.datex2.join.ReceiverT.Observer;
import de.heuboe.datex2.base.D2PublicationMerger;
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.util.JAXBUtil;
import de.heuboe.wls.basic.util.SimpleDateFormatter;


/**
 * 
 * Manager
 * 
 * @author peters
 *
 * @param <T>	D2LogicalModel of a profile
 */
public class Manager<T> implements Observer<T> {
	
	class SnapshotTask extends TimerTask {

		@Override
		public void run() {
			
			try {
				LOGGER.info( "" );
				LOGGER.info( "" );
				LOGGER.info( "Cyclic snapshot publication" );
				publish( (T)null );
			} catch( Throwable ex ) {      // NOSONAR
				LOGGER.error( "Fatal error: " + ex.toString()  );
				LOGGER.error( "Terminate app" );
				System.exit( -1 );
			}
		}
		
	}
	
	private static final Logger LOGGER = Logger.getLogger( Manager.class );
	private static final SimpleDateFormatter sdf = new SimpleDateFormatter( "yyyy-MM-dd HH:mm:ss" );
	
	private static final String REGION_ID_SUED = "sued";
	private static final String REGION_ID_NORD = "nord";
    
	private static final Map<String,String> REGION_NAMES = new HashMap<>();
	
	static {    
		REGION_NAMES.put( REGION_ID_SUED, "Süd" );
		REGION_NAMES.put( REGION_ID_NORD, "Nord" );
	}
	
	@Autowired 
	ProfileConverter<T,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter;
	
	@Value("${de.heuboe.c2vba.datex2.join.pubMode:}") 
	private String pubModeProp;
	
	private PubMode pubMode = PubMode.OnUpdateCyclicSnapshot;

	@Value("${de.heuboe.c2vba.datex2.join.pubInterval}") 
	private int pubInterval;
	
	@Value("${de.heuboe.c2vba.datex2.join.outdatedInterval:900}") 
	private int outdatedInterval;

	
	@Value("${de.heuboe.c2vba.datex2.join.pubFileDir}") 
	private String pubFileDir;
	
	@Value("${de.heuboe.c2vba.datex2.join.tableId}") 
	private String tableId;
	
	@Value("${de.heuboe.c2vba.datex2.join.tableVersion}") 
	private String tableVersion;

	@Autowired
	private D2PubSenderMDM senderMDM;
	
	private Date startTime = new Date();
	private JAXBUtil jaxbUtil;
	private JAXBUtil jaxbUtilT;
	
	private D2PublicationMerger merger = new D2PublicationMerger();
	private Map<String,T> region2Pub = new HashMap<>();
	
	ReceiverT<T> receiverNord;
	ReceiverT<T> receiverSued;
	
	private T emptyUpdatePub;
	
	private ResolverPub<T> resolverPub;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param schemaFile			Schema file
	 * @param generalD2SchemaFile	General DATEXT-II schema file
	 * @param schemaPack			Schema package
	 * @param emptyPubFile			Empty publication
	 * @param resolverPub			ResolverPub
	 * @param receiverNord			Receiver of north data
	 * @param receiverSued			Receiver of south data
	 * @throws JAXBException		Error 
	 */
	public Manager( String schemaFile,
					String generalD2SchemaFile,
			        String schemaPack,  
			        String emptyPubFile,
			        ResolverPub<T> resolverPub, 
					ReceiverT<T> receiverNord,
					ReceiverT<T> receiverSued ) throws JAXBException {
		
		this.receiverNord = receiverNord;
		this.receiverSued = receiverSued;
		
		this.resolverPub = resolverPub;
		
		jaxbUtil = new JAXBUtil( generalD2SchemaFile, 
							     Config.D2_PACKAGE, 
				 			     SchemaUtil.DATEX2_SCHEMA_NS,
				 			     false );
		
		jaxbUtilT = new JAXBUtil( schemaFile, 
				                    schemaPack, 
			     					SchemaUtil.DATEX2_SCHEMA_NS,
			     					false );

		try ( Scanner scanner = new Scanner( Manager.class.getResourceAsStream( emptyPubFile ), StandardCharsets.UTF_8) ) {
			String esp = scanner.useDelimiter("\\A").next();
			emptyUpdatePub = jaxbUtilT.getObject( esp );
		}
	}
	
	/**
	 * 
	 * Initialization
	 * 
	 */
	public void init() {
		
		LOGGER.info( "Start Manager");
		if( ( tableId != null ) && !tableId.isBlank() ) {
			LOGGER.info( "Table ID:      "  + tableId );
		}
		if( ( tableVersion != null ) && !tableVersion.isBlank() ) {
			LOGGER.info( "Table version: "  + tableVersion );
		}
		
		if( ( pubModeProp != null ) && !pubModeProp.isBlank() ) {
			pubMode = PubMode.valueOf( pubModeProp );
		}
		
		LOGGER.info( "" );

		
		receiverNord.setObserver( this );
		receiverSued.setObserver( this );

		receiverNord.start();
		receiverSued.start();
		
		if( pubInterval > 0 ) {
		    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		    long delay  = pubInterval * 1000L;
		    long period = pubInterval * 1000L;
		    executor.scheduleAtFixedRate( new SnapshotTask(), delay, period, TimeUnit.MILLISECONDS );
		}
	}
	
	private String toRegionName( String regionId ) {
		String name = REGION_NAMES.get( regionId );
		
		return ( name == null ) ? ( regionId + " ???" ) : name;
	}
	
	@Override
	public synchronized void notifyState(String region, List<T> publications) {    // NOSONAR
		LOGGER.info("Initial publication state of region " + toRegionName(  region ) + " received" );
		if( !publications.isEmpty() ) {
			
			boolean hasSnapshot = false;
			Date lastUpdTime = new Date( 1000000000L );
			LOGGER.info( "    # Initial publications: " + publications.size() );
			for( T publication : publications ) {
				
				boolean isSnapshot =  resolverPub.isSnapshot( publication );
				String type = "Update";
				if( isSnapshot ) {
					type = "Snapshot";
				}
				LOGGER.info( "        " + type + ", " + 
				             "Time: " + sdf.format( resolverPub.getPubTime( publication ) ) + ", " + 
				             "# Elements: " + resolverPub.getElementCount( publication ) );
				
				if( isSnapshot ) {
					hasSnapshot = true;
				}
				
				Date pubTime = resolverPub.getPubTime( publication );
				if( lastUpdTime.before( pubTime ) ) {
					lastUpdTime = pubTime;
				}
			}
			
			if( !hasSnapshot ) {
				LOGGER.info( "No snapshot for " + toRegionName(  region ) + " yet present." );
				return;
			}
			
			Date outdatedTime = new Date( (new Date()).getTime() - ( outdatedInterval * 1000 ) );
			if( lastUpdTime.before( outdatedTime ) ) {
				LOGGER.info( "Present state data for " + toRegionName(  region ) + " is outdated." );
				return;
			}
			
			region2Pub.put( region, publications.get(0) );
			for( T publication : publications.subList( 1, publications.size() ) ) {
				if( resolverPub.isSnapshot( publication ) ) {
					region2Pub.put( region, publication );
				} else {
					T pub = region2Pub.get( region );
					resolverPub.updatePub( pub, publication );
				}
			}
			
			T initPub = region2Pub.get( region );
			
			String tId = resolverPub.getTableId( initPub );
			String tVersion = resolverPub.getTableVersion( initPub );
			if( !tId.isEmpty() ) {
				LOGGER.info( "    Table ID:      " + tId );
			}
			if( !tVersion.isEmpty() ) {
				LOGGER.info( "    Table Version: " + tVersion );
			}
			LOGGER.info("    Time: " + sdf.format( resolverPub.getPubTime( initPub ) ) );
			LOGGER.info("    # Elements: " + resolverPub.getElementCount( initPub ) );
			LOGGER.info("");
			
			publish( (T)null );
		}
	}

	@Override
	public synchronized void notifyUpdate(String region, List<T> publications) {
		
		if( !publications.isEmpty() ) {
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info("Publication(s) of region " + toRegionName(  region ) + " received" );
			for( T publication : publications ) {
				
				String tId = resolverPub.getTableId( publication );
				String tVersion = resolverPub.getTableVersion( publication );
				if( !tId.isEmpty() ) {
					LOGGER.info( "    Table ID:      " + tId );
				}
				if( !tVersion.isEmpty() ) {
					LOGGER.info( "    Table Version: " + tVersion );
				}
				LOGGER.info("    Time: " + sdf.format( resolverPub.getPubTime( publication ) ) );
				LOGGER.info("    # Elements: " + resolverPub.getElementCount( publication ) );
				
				if( resolverPub.isSnapshot( publication ) ) {
					region2Pub.put( region, publications.get(0) );
				} else {
					T pub = region2Pub.get( region );
					resolverPub.updatePub( pub, publication );
				}
				
				if( pubMode != PubMode.CyclicSnapshot ) {
					publish( publication );
				}
			}
		}
	}
	
	private boolean pubTimeOutdated( Date pubTime ) {
		Date outdatedTime = new Date( (new Date()).getTime() - ( outdatedInterval * 1000 ) );
		return pubTime.before( outdatedTime );
	}

	private synchronized void publish( T publication ) {   // NOSONAR
	
		try {
			
			T d2lmVmsS = region2Pub.get( REGION_ID_SUED );
			T d2lmVmsN = region2Pub.get( REGION_ID_NORD );
			
			if( ( publication == null ) 
				&& 
				( ( d2lmVmsS == null ) || ( pubTimeOutdated( resolverPub.getPubTime( d2lmVmsS ) ) ) )
				&& 
				( d2lmVmsN != null ) 
			   ) {
				LOGGER.info( "No Süd publication present (or expired), use empty" );
				d2lmVmsS = resolverPub.clone( emptyUpdatePub);
			}
			if( ( publication == null ) 
				&& 
				( ( d2lmVmsN == null ) || ( pubTimeOutdated( resolverPub.getPubTime( d2lmVmsN ) ) ) ) 
				&& 
				( d2lmVmsS != null ) 
			  ) {
				LOGGER.info( "No Nord publication present (or expired), use empty" );
				d2lmVmsN = resolverPub.clone( emptyUpdatePub);
			}
			
			if( ( d2lmVmsS != null ) && ( d2lmVmsN != null ) 
			    || 
			    ( ( publication != null ) && !resolverPub.isSnapshot( publication ) ) ) {
				
				boolean emptyS = false;
				
				if( ( publication != null ) && !resolverPub.isSnapshot( publication ) ) {
					LOGGER.info("Update" );
					emptyS = true;
					d2lmVmsS = resolverPub.clone( emptyUpdatePub);
					d2lmVmsN = publication;
				} else {
					LOGGER.info("Snapshot" );
				}
				
				LOGGER.info("Publish ..." );
				
				eu.datex2.schema._2._2_0.D2LogicalModel d2lmS = profileConverter.convertS( d2lmVmsS );
				if( emptyS ) {
					resolverPub.clearElements( d2lmS );
				}
				
				LOGGER.info("Converted region Süd publication" );
				
				eu.datex2.schema._2._2_0.D2LogicalModel d2lmN = profileConverter.convertS( d2lmVmsN );
				
				LOGGER.info("Converted region Nord publication" );
				
				eu.datex2.schema._2._2_0.D2LogicalModel d2lmM = merger.merge( d2lmS, d2lmN );
				
				if( d2lmM.getExchange().getSubscription() != null ) {
					d2lmM.getExchange().getSubscription().setSubscriptionStartTime( startTime );
				}
				d2lmM.getPayloadPublication().setPublicationTime( new Date() );
				
				resolverPub.updateTableRef( d2lmM, tableId, tableVersion );
				
				publish( d2lmM );
			} else {
				if( d2lmVmsS == null ) {
					LOGGER.warn("No publication for region Süd yet present!" );
				}
				if( d2lmVmsN == null ) {
					LOGGER.warn("No publication for region Nord yet present!" );
				}
			}
		} catch ( JAXBException ex ) {
			LOGGER.error( "Error sending publication:" );
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			System.exit(-1);
		}
	}
	
	/**
	 * 
	 * Sends publication to MDM
	 * 
	 * @param d2lm	Publication
	 */
	public void publish( eu.datex2.schema._2._2_0.D2LogicalModel d2lm ) {
		d2lm.getPayloadPublication().setPublicationTime( new Date() );
		try {
			senderMDM.publish( d2lm );
			writePubFile( d2lm );
		} catch (D2Exception ex ) {
			LOGGER.error( "Error sending publication:" );
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			System.exit(-1);
		}
	}
	 
	private void writePubFile( eu.datex2.schema._2._2_0.D2LogicalModel d2lm ) {
		if( ( pubFileDir != null ) && !pubFileDir.isEmpty()  ) {
		
			String fileName = pubFileDir + "/D2Pub_" + ( (new Date() ).getTime() ) + ".xml";
			
			try {
				String content = jaxbUtil.getDocument( d2lm, 
				 		 					 		   "d2LogicalModel", 
				 		 					 		   eu.datex2.schema._2._2_0.D2LogicalModel.class );
				FileUtils.write( new File( fileName ), content, StandardCharsets.UTF_8 );
			} catch  (JAXBException | IOException ex ) {
				LOGGER.warn( "Cannot write publication to file <" + fileName + ">: " + ex.toString() );
			}
		}

	}
}
