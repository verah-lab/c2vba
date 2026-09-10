package de.heuboe.c2vba.kafka.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.datex2.kafka.C2VbaException;
import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.c2vba.datex2.kafka.PublicationResolver;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaManager;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;

public class TestProducer extends BaseT {
	
	@Autowired
	KafkaManager kafkaManager;
	
	@Autowired
	JAXBUtil jaxbUtil;
	
	@Autowired
	Producer<D2LogicalModel> producer;
	
	@Autowired
	ReceiverT receiver;

	@Autowired
	PublicationResolver<D2LogicalModel> resolver;

	@Test
	public void testProducer() throws IOException, C2VbaException, JAXBException, KafkaException { // NOSONAR
		
		Set<String> keys = kafkaManager.getAllKeysProto( DatexIIContent.class, "C2VBA-datex2-publication", 5000L );
		Map< String, Set<String> > objId2PartKeys = new HashMap<String, Set<String>>();
		objId2PartKeys.put( "PUB", keys );
		producer.setObjId2PartKeys(objId2PartKeys);
		
		String xml = FileUtils.readFileToString( new File("D:\\nrw\\alc\\datex2\\D2SitPub_1623316428652.xml"), StandardCharsets.UTF_8 );
		D2LogicalModel d2lm = (D2LogicalModel)jaxbUtil.getObject( xml );
		producer.send( Arrays.asList( d2lm ), resolver, false );
	}
	

}
