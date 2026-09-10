package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.by.c2vba.datex2.mst.localisation.server.ConfigService.LaneData;
import de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka.KafkaManager;
import de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka.LVEBetriebsparamterReceiver;
import de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka.UFDBetriebsparamterReceiver;
import de.heuboe.datex2.location.service.D2MeasurementSite;
import de.heuboe.datex2.location.service.D2MeasurementSite.D2SubSite;
import de.heuboe.datex2.location.service.LocationFilter;
import de.heuboe.datex2.location.service.LocationService;
import de.heuboe.datex2.location.service.ServiceException;
import de.heuboe.datex2.location.service.SiteType;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.CarriagewayEnum;
import eu.datex2.schema._2._2_0.GroupOfLocations;
import eu.datex2.schema._2._2_0.LaneEnum;


/**
 *
 * LocationService implementation
 * 
 * @author peters
 * 
 */
@WebService(endpointInterface="de.heuboe.datex2.location.service.LocationService",
            serviceName="LocationService",
            portName="LocationServiceWSPort")
public class LocalisationServiceImpl implements LocationService 
{
	private static final Logger LOGGER = Logger.getLogger( LocalisationServiceImpl.class );
	private static final long KAFKA_RECEIVE_TIMEOUT = 20000L;
	
    private WlsClient wlsClient = null;
    private ConfigService configService = null;
    private String system = null;
    
    @Autowired
    private KafkaManager kafkaManager;
    
    private int carriageMisMatchCount = 0;
    
	/**
	 * 
	 * ping
	 * 
	 * @param clientId		client ID
	 * @return			    ok
	 * 
	 */
    @Override
	public boolean ping( String clientId )
	{
		   return true;
	}
	
	/**
	 * 
	 * Constructor
	 * 
     * @param wlsClient       	WLS proxy
	 * @param configService		ConfigService
	 */
	public LocalisationServiceImpl( WlsClient wlsClient, ConfigService configService, String system )
	{
		this.wlsClient = wlsClient;
		this.configService = configService;
		this.system = system;
	}
	 
	private String getLocationType( SiteType type )
	{
		if( "tr_detection_site".equals( type.getObjType() ) ) {
			return "EQ";
		}
		
		return type.getObjType();
	}
	
	private D2MeasurementSite createTLSMeasurementSite( LocationObject locObject ) {
	    
        D2MeasurementSite ms = new D2MeasurementSite();
        ms.setAlertCLocation( locObject.getAlertCLocation() );
        ms.setOpenLRLocation( locObject.getOpenLRLocation() );
        ms.setCoordinateLocation( locObject.getCoordinateLocation() );
        ms.setExternalReference( locObject.getExternalReference() );
        ms.setObjId( locObject.getId() );
        ms.setName( locObject.getName() );
        ms.setGeoDynId( locObject.getGeoDynId() );
        
        String cw = locObject.getCarriageway();
        if( cw != null ) {
            ms.setCarriageway( CarriagewayEnum.fromValue( cw ) );
        } else {
            ms.setCarriageway( CarriagewayEnum.MAIN_CARRIAGEWAY );
        }
        
        String equipment = "TLS-Erfassung";
        if( ( system != null ) && !system.isBlank() ) {
        	equipment = system + ": " + equipment;
        }
        ms.setEquipment( equipment );

        return ms;
	}
	
    private D2SubSite createMainSubSite( LocationObject locObject, int period, String dataId ) {
	
        D2SubSite subSite = new D2SubSite();
        
        if( dataId != null ) {
        	subSite.setId( dataId );
        } else {
        	subSite.setId( locObject.getId() );
        }
        subSite.setName( locObject.getName() );
        subSite.setLane( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
        subSite.setPeriod( period );
        
        return subSite;
    }
    
    //
    // Carriageway gemäß TLS 2012, 4. Empfehlungen für die Codierung der DE-Kanäle, 4.1 FG 1 "Verkehrsdatenerfassung"
	//    
	//    1 -63 HFB
	//    81 - 87 Parallel
	//    89 Seitenstreifen (HFS)
	//    65 - 95   EXIT
	//
	//
	//    113 - 119 Parallel
	//    121 Seitenstreifen (HFS)
	//    97 - 127 EXIT
	//
	//    145 - 151 Parallel
	//    129 - 159 ENTRY
	//
	//    177 - 183 Parallel
	//    161 - 191 ENTRY
	//
	//    225 - 255 HFB
    //
    private CarriagewayEnum determineCarriageway( List<LaneData> lds ) {   // NOSONAR
    	
    	Map<CarriagewayEnum,Integer> cCount = new HashMap<>();   // NOSONAR
    	
    	int maxCount = 0; 
    	for( LaneData ld : lds ) {
    		Integer de = ld.getDe();
    		if( de != null ) {
    			
        		CarriagewayEnum carriageway = null;
    			if( ( de >= 1 ) && ( de <= 63 ) ) {
    				carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
    			} else if( ( de >= 81 ) && ( de <= 87 ) ) {
    				carriageway = CarriagewayEnum.PARALLEL_CARRIAGEWAY;
    			} else if( de == 89 ) {    // Seitenstreifen
    				carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
    			} else if( ( de >= 65 ) && ( de <= 95 ) ) {
    				carriageway =  CarriagewayEnum.EXIT_SLIP_ROAD;
    				
    			} else if( ( de >= 113 ) && ( de <= 119 ) ) {
    				carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
    			} else if( de == 121 ) {    // Seitenstreifen
    				carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
    			} else if( ( de >= 97 ) && ( de <= 127 ) ) {
    				carriageway =  CarriagewayEnum.EXIT_SLIP_ROAD;
    				
    			} else if( ( de >= 145 ) && ( de <= 151 ) ) {
    				carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
    			} else if( ( de >= 129 ) && ( de <= 159 ) ) {
    				carriageway =  CarriagewayEnum.ENTRY_SLIP_ROAD;
    				
    			} else if( ( de >= 177 ) && ( de <= 183 ) ) {
    				carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
    			} else if( ( de >= 161 ) && ( de <= 191 ) ) {
    				carriageway =  CarriagewayEnum.ENTRY_SLIP_ROAD;
    				
    			} else	if( ( de >= 225 ) && ( de <= 255 ) ) {
        			carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;


    			} else {
    				carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
    			}

    			Integer count = cCount.get( carriageway );
        		if( count == null ) {
        			count = 1;
        		} else {
        			count++;
        		}
        		
        		maxCount = Math.max( count, maxCount );
        		cCount.put( carriageway, count );
    		}
    	}
    	
    	if( !cCount.isEmpty() ) {
    		
    		if( cCount.size() > 1 ) {
    			carriageMisMatchCount++;
    		}
    		
    		for( Map.Entry<CarriagewayEnum,Integer> entry : cCount.entrySet() ) {
    			if( entry.getValue() == maxCount ) {
    				return entry.getKey(); 
    			}
    		}
    	}
    	
    	return null;
    }
	
	/**
	 * 
	 * Retrieves Datex2 locations
	 * 
	 * @param type			           site type
	 * @param filter		           client ID
	 * @return                         Datex2 locations
	 * @exception ServiceException     Service exception             
	 *
	 */
	@Override
	public List<D2MeasurementSite> getDatex2Locations( SiteType type, // NOSONAR
			   										   LocationFilter filter ) throws ServiceException
	{
		LOGGER.info( "Start of getDatex2Locations()");
		
		carriageMisMatchCount = 0;
        
		String locType = getLocationType( type );
        if( locType == null ) {
            throw new ServiceException( "Error in getDatex2Locations(): Unknown object type <" + type.getObjType() + ">" ); 
        }
        
        if( locType.equals( "EQ") || locType.equals( "MQ")  ) {
            
        	LOGGER.info( "" );
            LOGGER.info( "Site-Type: " + type.getObjType() );
				
    		try
    		{
    		    Map<String,Integer> laneId2Period = readLVEDetectionPeriod(); 

                Collection<LocationObject> locObjects = wlsClient.getLocationObjects( filter, locType, null );
    			
    			List<String> qids = locObjects.stream().map( LocationObject::getId ).collect( Collectors.toList() );
    			
    			Map< String, List<LaneData> > qid2LaneDatas = configService.getMqLaneData( qids );
    			
    			List<D2MeasurementSite> mss = new ArrayList<>();
    			for( LocationObject locObject : locObjects )
    			{
    				GroupOfLocations alcLocs = locObject.getAlertCLocation();
    				GroupOfLocations coorLocs = locObject.getCoordinateLocation();
    				if( ( alcLocs == null ) && ( coorLocs == null ) )
    				{
    					LOGGER.error( "No location for object with ID '" + locObject.getId() + "'" );
    					continue;
    				}
    				
    				D2MeasurementSite ms = createTLSMeasurementSite( locObject );
    				if( !type.isHasSubSites() )
    				{
    					D2SubSite subSite = createMainSubSite( locObject, 60, null );
    					ms.setSubSites( Arrays.asList( subSite ) );
    				} else {
    				    List<LaneData> lds = qid2LaneDatas.get( locObject.getId() );
    				    List<D2SubSite> subSites = new ArrayList<>();
    				    if( lds != null ) {
    				        for( LaneData ld : lds ) {
    		                    D2SubSite subSite = new D2SubSite();
    		                    
    		                    subSite.setId( "" + ld.getId() );
    		                    subSite.setName( ld.getName() );
    		                    subSite.setLane( ld.getLane() );
    		                    
    		                    Integer period = getDetectionPeriod( laneId2Period, ld.getId() );
    		                    if( period == null ) {
    		                        period = 60;
    		                    }
                                subSite.setPeriod( period );
    		                    
    		                    subSites.add( subSite );
    				        }
                            CarriagewayEnum carriageway = determineCarriageway( lds );
                            if( carriageway != null ) {
                            	ms.setCarriageway( carriageway );
                            }
    				    }
                        ms.setSubSites( subSites );
                        
    				}
    				
    				mss.add( ms );
    			}
    			
    			return mss;
            } catch( SvcException ex) {
            	LOGGER.error( ex.toString() );
            	if( ex.getCause() != null ) {
                	LOGGER.error( ex.getCause().toString() );
            	}
                throw new ServiceException( "Error in getDatex2Locations()", ex );
            } finally {
                LOGGER.info(")");
                LOGGER.info("carriageMisMatchCount: "  +carriageMisMatchCount );
                LOGGER.info("");
                LOGGER.info("End of getDatex2Locations()");
            }
    		
        } else if( locType.startsWith( "UFD") ) {
            
        	LOGGER.info( "" );
            LOGGER.info( "Site-Type: " + type.getObjType() );
            
            try
            {
                int pos = locType.indexOf( ':' );
                if( pos == - 1 ) {
                    throw new ServiceException( "No UFD type provided" );
                }
                
                String mainType = locType.substring( 0, pos );
                String ufdTypePart = locType.substring( pos + 1 );
                int udfType = -1;
                try {  // NOSONAR
                    udfType = Integer.parseInt( ufdTypePart );
                } catch( NumberFormatException ex ) {
                    throw new ServiceException( "Invalid UFD type <" + ufdTypePart + "> provided");
                }
                
                LOGGER.info( "Site-Type-Name: " + configService.getTypeName(udfType) );
                
                Set<String> udfIds = readUFDIds( udfType );
                
                List<D2MeasurementSite> mss = new ArrayList<>();
                if( udfIds.isEmpty() ) {
                	LOGGER.warn( "" );
                	LOGGER.warn( "No UFD items for type " + udfType  );
                	LOGGER.warn( "" );
                } else {
                
	                Map<String,Integer> ufdId2Period = readUFDDetectionPeriod(); 
	                Collection<LocationObject> locObjects = wlsClient.getLocationObjects( filter, mainType, udfIds );
	                
	                for( LocationObject locObject : locObjects ) {
	                    
	                    if( udfIds.contains( locObject.getId() ) ) {
	                        D2MeasurementSite ms = createTLSMeasurementSite( locObject );
	                        
	                        Integer period = getDetectionPeriod( ufdId2Period, locObject.getId() );
	                        if( period == null ) {
	                            period = 60;
	                        }
	                        D2SubSite subSite = createMainSubSite( locObject, period, locObject.getId() );
	                        ms.setSubSites( Arrays.asList( subSite ) );
	                        
	                        mss.add( ms );
	                    }
	                }
                }
                
                return mss;
                
            } catch( SvcException ex) {
            	LOGGER.error( ex.toString() );
            	if( ex.getCause() != null ) {
                	LOGGER.error( ex.getCause().toString() );
            	}
                throw new ServiceException( "Error in getDatex2Locations()", ex );
            } finally {
                LOGGER.info("End of getDatex2Locations()");
            }
        
        } else {
            throw new ServiceException( "Error in getDatex2Locations(): Unknown object type <" + type.getObjType() + ">" ); 
        }
	}
	
	/**
	 * 
	 * Read LVE detection periods
	 * 
	 * @return					LVE detection periods			
	 * @throws SvcException		Errors
	 */
	public Map<String,Integer> readLVEDetectionPeriod() throws SvcException {
	    
	    LVEBetriebsparamterReceiver receiver;
		try {
			receiver = kafkaManager.createLVEBetriebsparamterReceiver();
		} catch (KafkaException ex ) {
			throw new SvcException( SvcException.ERROR_KAFKA, "Error retrieving LVEBetriebsparamter" );
		}

		receiver.start();
		
		if( !receiver.waitForStateData( KAFKA_RECEIVE_TIMEOUT ) ) {
			throw new SvcException( SvcException.ERROR_KAFKA, "Timeout on retrieving LVEBetriebsparamter" );
		}
		
		receiver.stopReceiving();
		
		return receiver.getDetectionPeriods();
	}
	
	/**
	 * 
	 * Read UFD detection periods
	 * 
	 * @return					UFD detection periods			
	 * @throws SvcException		Errors
	 */
   private Map<String,Integer> readUFDDetectionPeriod() throws SvcException {
	   
	    UFDBetriebsparamterReceiver receiver;
		try {
			receiver = kafkaManager.createUFDBetriebsparamterReceiver();
		} catch (KafkaException ex ) {
			throw new SvcException( SvcException.ERROR_KAFKA, "Error retrieving UFDBetriebsparamter" );
		}

		receiver.start();
		
		if( !receiver.waitForStateData( KAFKA_RECEIVE_TIMEOUT ) ) {
			throw new SvcException( SvcException.ERROR_KAFKA, "Timeout on retrieving UFDBetriebsparamter" );
		}
		
		receiver.stopReceiving();
		
		return receiver.getDetectionPeriods();
    }
   
   private Set<String> readUFDIds( int type ) {
       return configService.getUfdIds( type );
   }

    private Integer getDetectionPeriod( Map<String,Integer> id2Period, String id ) {
        
        Integer period = id2Period.get( id );
        if( ( period != null ) && ( period  > 0 ) && period != 255 ) {
            return period * 15;
        }
        
        return null;
    }
    
    /**
     * 
     * Starts SOAP service
     * 
     * @param servicePort	Service port
     */
    public void start( int servicePort ) {
		String smcURL = "http://0.0.0.0:" + servicePort + "/BY/Datex2LocalisationService";   // NOSONAR:  
		Endpoint.publish( smcURL, this );
        LOGGER.info( "Service available at " + smcURL );
    }
}
