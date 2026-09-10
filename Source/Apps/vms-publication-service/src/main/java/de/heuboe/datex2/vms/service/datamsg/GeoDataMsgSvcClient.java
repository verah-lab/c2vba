package de.heuboe.datex2.vms.service.datamsg;


import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.Empty;

import de.heuboe.datex2.schema.D2BaseException;
import de.heuboe.geo.grpc.AdminMessage;
import de.heuboe.geo.grpc.Data;
import de.heuboe.geo.grpc.DataMessage;
import de.heuboe.geo.grpc.DataSpec;
import de.heuboe.geo.grpc.GeoDataMsgServiceGrpc;
import de.heuboe.geo.grpc.GeoDataMsgServiceGrpc.GeoDataMsgServiceStub;
import de.heuboe.geo.grpc.Property;
import de.heuboe.geo.grpc.Sender;
import de.heuboe.log.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GeoDataMsgSvcClient {
	
	private static final Logger LOGGER = Logger.getLogger( GeoDataMsgSvcClient.class );
	private static final long REINIT_INTERVAL = 60L * 1000L;
	
	private String svcHost;
	private int svcPort;
    private double zoomFactor = -1.0;
	
	private Sender sender;
	
	private StreamObserver<DataMessage> streamObserver;
	private boolean stopped = false;
	
	private Map<String,String> currentAqId2imageUrl = new HashMap<>();
	
	public GeoDataMsgSvcClient( String svcHost, int svcPort, String address, double zoomFactor ) {
		this.svcHost = svcHost;
		this.svcPort = svcPort;
		this.zoomFactor = zoomFactor;
		
    	sender = Sender.newBuilder().setId( "vms-publication-service" ).setName("VMS publication service").setAddress(address).build();
		
	    TimerTask task = new TimerTask() {
	        public void run() {
	        	reinit();
	        }
	    };
	    Timer timer = new Timer("GeoDataMsgSvcClient Reinit timer");
	    
	    timer.schedule(task, REINIT_INTERVAL, REINIT_INTERVAL );

	}
	
	private synchronized void reinit() {
		if( stopped ) {
			try {
				stopped = false;
				init();
				currentAqId2imageUrl.entrySet().forEach( i -> sendDataMsg( i.getKey() , i.getValue() ) );
			} catch( Exception ex ) {
				stopped = true;
			}
		}
	}
	
	public synchronized void init() throws Exception {

        ManagedChannel channel = ManagedChannelBuilder.forAddress( svcHost, svcPort ).usePlaintext().build();
        GeoDataMsgServiceStub stub = GeoDataMsgServiceGrpc.newStub(channel);
        
        DataSpec ds = DataSpec.newBuilder().setLayerName( "AQ" ).setLocationType("GeoFeature").setLocationSubType("AQ").build();
        AdminMessage am = AdminMessage.newBuilder().setSender( sender ).setType( de.heuboe.geo.grpc.AdminMessage.Type.REGISTER ).setDataSpec( ds ).build();
        stub.sendAdminData( am, null );

        Thread.sleep( 200L );
        
        StreamObserver<Empty> soOut = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty dm) {}
            @Override
            public void onError(Throwable ex ) {
            	LOGGER.warn( "GeoDataMsgSvcClient: error on DataMessage channel" );
            	LOGGER.warn( "Stop sending" );
            	synchronized ( this ) {
            		channel.shutdownNow();
            		stopped = true;
				}
            }
            @Override
            public void onCompleted() {}
          };  
        
       	streamObserver = stub.sendData( soOut );
       	if( streamObserver == null ) {
       		throw new D2BaseException( "GeoDataMsgSvcClient: service not available" );
       	}
	}
    
    public synchronized void sendDataMsg( String aqId, String imageUrl ) {
    	
		currentAqId2imageUrl.put( aqId, imageUrl );
    	if( !stopped )  {
    		
    		String iu = imageUrl;
    		if( zoomFactor > 0.0 ) {
    			iu = iu.replaceAll( "zoom=\\d+", "zoom=" +  (int)(zoomFactor * 100.0) );
    		}
	    	Data d = Data.newBuilder().setId( aqId ).addProperty( Property.newBuilder().setName( "imageUrl").setValue( iu ) ).build();
	    	
	    	try {
		    	streamObserver.onNext( DataMessage.newBuilder().setSender( sender )
                        									   .setDataType( "AQ-Image" )
		    			                                       .setLayerName( "AQ" )
		    			                                       .setLocationType( "GeoFeature" )
		    			                                       .setLocationSubType( "AQ" )
		    			                                       .addData( d ).build() );
	    	} catch( Throwable ex ) {
	        	LOGGER.warn( "GeoDataMsgSvcClient: error forwarding DataMessage: " + ex.toString() );
	    	}
    	}
    }
}