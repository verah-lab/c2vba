package de.heuboe.sdbby.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.google.protobuf.Timestamp;

import de.heuboe.c2vba.data.CommState;
import de.heuboe.c2vba.data.CommStates;
import de.heuboe.c2vba.data.pojo.PCommStates;
import de.heuboe.kafka.producer.KafkaProducer;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.config.data.ConfigItem;
import de.heuboe.sdbby.service.config.data.ConfigSet;
import de.heuboe.sdbby.service.config.kafka.SYSFehlerDUEReceiver;
import de.heuboe.sdbby.service.data.CommStatItem;
import de.heuboe.sdbby.service.data.CommStatSet;
import de.heuboe.system.CallStack;
import de.heuboe.util.JAXBUtil;
import eu.vmis_ehe.vmis2.tls.received.SYSFehlerDUE;

public class CommStatMan implements CommStatMsgConsumer {
	
	private static final Logger LOGGER = Logger.getLogger(CommStatMan.class);
	private static final String JAXB_PACKAGES = "de.heuboe.sdbby.service.config.data";

    @Autowired 
    private SYSFehlerDUEReceiver fehlerReceiver;
    
    @Autowired 
    KafkaProducer<PCommStates> commStatesProducer;
    
    private boolean stateReceived = false;

	private ConfigSet configSet;
	private Map<String, Boolean> commStates;
	
	// KRI-IDs for which a state has been received
	private Set<String> commStatesReceived = new HashSet<>();
	
	private Map<String, CommStatItem> externStates;
	
	/**
	 * Default constructor. should only be used for junit tests.
	 */
	public CommStatMan() {
	}
	
	public void init() {
		fehlerReceiver.registerMsgConsumer( this );
		fehlerReceiver.start();
	}

	public CommStatMan( String filename ) {
		
		configSet = readConfigSetXML(filename);
		commStates = new HashMap<>();
		externStates = new HashMap<>();
		for(ConfigItem c : configSet.getConfigItem()) {
			for(String id : c.getId()) {
				commStates.put(id, true);
			}
		}
		
		LOGGER.info( "CommStatMan initialised" );
	}
	
	@Override
	public synchronized void update( List<SYSFehlerDUE> fehlerList ) {
		
		for( SYSFehlerDUE fehler : fehlerList ) {
			commStatesReceived.add( fehler.getId() );
			commStates.put( fehler.getId(), fehler.getFehlercode() != 0 );
		}
		
		if( stateReceived ) {
			sendCommStates();
		}
	}
	
	@Override
	public synchronized void stateReceived() {
		stateReceived = true;
		
		logMissingStates();
		sendCommStates();
	}
	
	private void logMissingStates() {
		LOGGER.info( "No communication state for KRIs (assume ok):");
		for( String kriId : commStates.keySet() ) {
			if( !commStatesReceived.contains( kriId ) ) {
				LOGGER.info( "    " + kriId );
			}
			
		}
	}

	
	private void sendCommStates() {
		
		CommStatSet csSet = getCommStates();
		
		CommStates.Builder cssb = CommStates.newBuilder();
        Instant time = Instant.now();
		cssb.setTime( Timestamp.newBuilder().setSeconds(time.getEpochSecond()).setNanos(time.getNano()).build() );
		for( CommStatItem csi : csSet.getCommStatSet() ) {
			CommState cs = CommState.newBuilder().setId( csi.getId() ).setAvailability( csi.getAvailability() ).build();
			cssb.addCommState( cs );
		}
		
		cssb.setIid( "1" );
		
		Map<String,String> headers = new HashMap<>();
		headers.put( de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_IID, "1" );
		
		commStatesProducer.sendData( "1", headers, PCommStates.from( cssb.build() ) );
	}

	
	public synchronized CommStatSet getCommStates() {	
		
		CommStatSet commStatSet = new CommStatSet();		
		for(ConfigItem item : configSet.getConfigItem()) {
			int cnt = 0;
			for(String id : item.getId()) {
				Boolean active = commStates.get( id );
				if (active != null && active == true) {
					++cnt;
				}
			}
			int availability = 100 * cnt / item.getId().size();
			commStatSet.getCommStatSet().add(new CommStatItem(item.getName(), availability));
		}
		commStatSet.getCommStatSet().addAll(externStates.values());
		return commStatSet;
	}
	
	void saveConfigSetXML(ConfigSet config, String xmlFilename) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(JAXB_PACKAGES);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			ClassLoader classLoader = getClass().getClassLoader();
			URL xsdUrl = classLoader.getResource("config.xsd");

			SchemaFactory sf = SchemaFactory
					.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(xsdUrl);
			marshaller.setSchema(schema);

			QName name = new QName("http://sdbby.heuboe.de/service/config/data", config.getClass().getSimpleName());
			try (FileOutputStream file = new FileOutputStream(xmlFilename)) {
				JAXBElement<ConfigSet> element = new JAXBElement<>(name, ConfigSet.class, null, config);
				marshaller.marshal(element, file);
			}
		} catch (JAXBException | IOException | SAXException e) {
			System.out.println("cannot save xml configuration: " + e);
		}
	}

	ConfigSet readConfigSetXML(String xmlFilename) {
		try {
			
			JAXBUtil jaxbUtil = new JAXBUtil( "config.xsd", 
                    						  "de.heuboe.sdbby.service.config.data", 
                    						  "http://sdbby.heuboe.de/service/config/data",
                    						  new HashMap<>(),
                    						  true);
			
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream is = classLoader.getResourceAsStream(xmlFilename);
			String content = IOUtils.toString( is );
			ConfigSet cs = jaxbUtil.getObject( content );
			
			return cs;
		} catch (JAXBException | IOException ex ) {
			LOGGER.error("cannot load xml configuration: " + ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			return null;
		}
	}

	public synchronized void setCommState(CommStatItem item) {
		externStates.put(item.getId(), item);
	}

}
