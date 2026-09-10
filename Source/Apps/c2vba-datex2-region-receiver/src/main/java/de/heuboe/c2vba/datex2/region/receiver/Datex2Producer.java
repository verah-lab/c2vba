package de.heuboe.c2vba.datex2.region.receiver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;

import de.heuboe.c2vba.datex2.kafka.C2VbaException;
import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.datex2.base.Worker;
import de.heuboe.datex2.base.comm.Datex2ConsumerServer;
import de.heuboe.datex2.exception.D2CommException;
import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import de.heuboe.wls.basic.util.SimpleDateFormatter;
import eu.datex2.schema._2._2_0.D2LogicalModel;
import eu.datex2.schema._2._2_0.UpdateMethodEnum;


/**
 * 
 * DATEX-II worker: puts publications onto Kafka
 * 
 * 
 * @author peters
 *
 */
public class Datex2Producer extends Worker {
	
	/**
	 * 
	 * Observer
	 * 
	 * @author peters
	 *
	 */
	public interface Observer {
		/**
		 * 
		 * Update notification
		 * 
		 * @param time			Update time	
		 * @param region		Region
		 */
		void notifyUpdate( Date time, String region );
	}
	
	private static final Logger LOGGER = Logger.getLogger( Datex2Producer.class );
	private static final SimpleDateFormatter sdf = new SimpleDateFormatter( "yyyy-MM-dd HH:mm:ss" );
	
	private JAXBUtil jaxbUtil;
	private String pubFileDir;
	
	private Producer<D2LogicalModel> producer;
	private PubResolver resolver;
	
	private String region;
	private Observer observer = null;
	
	private Date lastPubTime = null;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param producer				Producer
	 * @param resolver				Resolver
	 * @param pubFileDir			Publication directory
	 * @param generalD2Schema		Schema file of general DATEX-II profile 
	 * @throws D2CommException		Communication error
	 */
	public Datex2Producer( Producer<D2LogicalModel> producer, 
			               PubResolver resolver, 
			               String pubFileDir,
			               String generalD2Schema ) throws D2CommException {
		this.pubFileDir = pubFileDir;
		this.producer = producer;
		this.resolver = resolver;
		
		try {
			jaxbUtil = new JAXBUtil( generalD2Schema, 
					 				 "eu.datex2.schema._2._2_0", 
					 				 SchemaUtil.DATEX2_SCHEMA_NS,
					 				 false );
		} catch (JAXBException ex ) {
			throw new D2CommException( D2Exception.D2_ERR_INIT_D2_ENV, "Error creating JAXBUtil", ex );
		}
	}

	@Override
	public void process(Date time, int cycle, String cycleName, D2LogicalModel d2lm) {
		
		synchronized( Datex2ConsumerServer.class ) {
		
			try {
				
				Date pubTime = d2lm.getPayloadPublication().getPublicationTime();
				boolean isSnapshot = true;
				
				if( observer != null ) {
					observer.notifyUpdate( pubTime, region );
				}
				
				if( d2lm.getExchange().getSubscription() != null ) {
					if( d2lm.getExchange().getSubscription().getUpdateMethod() != UpdateMethodEnum.SNAPSHOT ) {    // NOSONAR
						isSnapshot = false; 
					}
				}
				
				LOGGER.info( "" );
				LOGGER.info( isSnapshot ? "Snapshot": "Update" );
				if( pubTime.equals( lastPubTime ) ) {
					LOGGER.info( "Kafka producer '" + producer.getName() + "': publication already seen, skip");
				} else {
					lastPubTime = pubTime;
					LOGGER.info( "Kafka producer '" + producer.getName() + "': send publication");
					String tableId = resolver.getTableId( d2lm );
					String tableVersion = resolver.getTableVersion( d2lm );
					if( !tableId.isEmpty() ) {
						LOGGER.info( "    Table ID:      " + tableId );
					}
					if( !tableVersion.isEmpty() ) {
						LOGGER.info( "    Table Version: " + tableVersion );
					}
					LOGGER.info( "    Time: " + sdf.format( pubTime ) );
					LOGGER.info( "    # Elements: " + resolver.getElementCount( d2lm ) );
					writePubFile( d2lm );
					producer.send( Arrays.asList( d2lm ), resolver, false);
					LOGGER.info( "Sent" );
					LOGGER.info( "" );
					LOGGER.info( "" );
				}
			} catch (C2VbaException ex) {
				LOGGER.error( "Error sending message: " + ex.toString() );
			}
		}
	}

	private void writePubFile( D2LogicalModel d2lm ) {
		synchronized( Datex2Producer.class ) {
			if( ( pubFileDir != null ) && !pubFileDir.isEmpty()  ) {
				String fileName = pubFileDir + File.separator + 
						          "D2Pub_" + producer.getName() + "_" + ( (new Date() ).getTime() ) + ".xml";
				
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
	
	public Observer getObserver() {
		return observer;
	}

	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

}
