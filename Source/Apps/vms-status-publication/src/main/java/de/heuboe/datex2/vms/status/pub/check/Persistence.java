package de.heuboe.datex2.vms.status.pub.check;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.datex2.vms.status.pub.VMSException;
import de.heuboe.datex2.vms.status.pub.check.DbData.DbDataSet;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGStellzustandList;


/**
 * 
 * Reads data from Kafka
 * 
 * @author peters
 *
 */
public class Persistence
{
	@Value("${de.heuboe.datex2.vms.status.pub.dataTopic:C2VBA-tlsin-WZGStellzustand}") 
	private String dataTopic;
	
	@Value("${de.heuboe.datex2.vms.status.pub.errorTopic:C2VBA-tlsin-WZGDeFehler}") 
	private String errorTopic;
	
	@Value("${spring.kafka.consumer.group-id:}") 
	private String consumerGroupId;
	
    @Autowired
    private KafkaReceiverFactory kafkaReceiverFactory;
	
	public Persistence() {
		//
	}
	
	
	/**
	 * 
	 * Retrieves data from Kafka
	 * 
	 * @param ids				Object IDs
	 * @return					Current data
	 * @throws VMSException		Error
	 */
	public DbDataSet getData( Set<String> ids )
			throws VMSException
	{
		try {
			DataReceiver receiver = new DataReceiver();
			kafkaReceiverFactory.setListener( receiver, 
											  consumerGroupId + "-check", 
                    						  dataTopic, 
                    						  PWZGStellzustandList.class.getName(), 
                    						  null );

			receiver.start();
			
			if( !receiver.waitForStateData( 2000L ) ) {
				return null;
			}
			
			receiver.stopReceiving();
			Map<String,DbData> data = receiver.getData();
			
			ErrorReceiver errorReceiver = new ErrorReceiver();
			kafkaReceiverFactory.setListener( errorReceiver, 
											  consumerGroupId + "-check", 
                    						  errorTopic, 
                    						  WZGDeFehler.class.getName(), 
                    						  null );

			errorReceiver.start();
			
			if( !errorReceiver.waitForStateData( 2000L ) ) {
				return null;
			}
			
			errorReceiver.stopReceiving();
			Set<String> idsWithError = errorReceiver.getIdsWithError();
			
			for( String id : idsWithError ) {
				DbData d = data.get( id );
				if( d != null ) {
					d.setError( true );
				}
			}
			
			return new DbDataSet( data );
			
		} catch (KafkaException ex ) {
			throw new VMSException( VMSException.ERROR_DB, "Error receiving Kafka topic 'VRZ-SignOverlayApp-WZGStellzustandList'", ex );
		}
	}
	
}
