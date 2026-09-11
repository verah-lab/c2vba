package de.heuboe.c2vba.kafka.test;

import java.util.List;

import javax.validation.constraints.NotNull;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.datex2.kafka.C2VbaException;
import de.heuboe.c2vba.datex2.kafka.Receiver;
import de.heuboe.c2vba.datex2.kafka.Resolver;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import eu.datex2.schema._2._2_0.D2LogicalModel;

public class ReceiverT extends Receiver<D2LogicalModel> {

	public ReceiverT( Class<DatexIIContent> clast, 
			             @NotNull KafkaReceiverSubscription rs,
			             Resolver<D2LogicalModel> resolver) throws KafkaException {
		super(clast, rs, resolver);
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleStateMessages(List<D2LogicalModel> messages ) {
		for( D2LogicalModel d2lm : messages ) {
			try {
				boolean s = resolver.isSnapshot( d2lm );
				System.out.println( "snapshot: " + s  );
			} catch (C2VbaException e) {
			}
			System.out.println( d2lm.getPayloadPublication().getPublicationTime() );
		}
	}

	@Override
	public void handleMessages(List<D2LogicalModel> messages) {
		for( D2LogicalModel d2lm : messages ) {
			System.out.println( d2lm.getPayloadPublication().getPublicationTime() );
		}
	}


}
