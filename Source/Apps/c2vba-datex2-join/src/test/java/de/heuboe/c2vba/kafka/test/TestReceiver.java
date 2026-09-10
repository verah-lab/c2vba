package de.heuboe.c2vba.kafka.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.c2vba.datex2.kafka.PublicationResolver;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;

public class TestReceiver extends BaseT {
	
	@Autowired
	JAXBUtil jaxbUtil;
	
	@Autowired
	Producer<D2LogicalModel> producer;
	
	@Autowired
	ReceiverT receiver;

	@Autowired
	PublicationResolver<D2LogicalModel> resolver;

	@Test
	public void testReceiver() { // NOSONAR
		receiver.start();
		
		while( !receiver.isStateFinished() ) {
			try {
				Thread.sleep( 1000L ); // NOSONAR
			} catch (InterruptedException e) {
			}
		}
		
		try {
			Thread.sleep( 3000L );   // NOSONAR
		} catch (InterruptedException e) {
		
		}
	}
}
