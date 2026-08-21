package de.heuboe.tls.receiver.main;

import java.io.File;
import java.io.IOException;

import de.heuboe.tls.receiver.app.TlsReceiver;
import de.heuboe.tls.receiver.impl.AddressConverterGenericImpl;
import de.heuboe.tls.receiver.impl.DataWriterDummyImpl;
import de.heuboe.tls.receiver.impl.TransformationReaderImpl;
import de.heuboe.tls.receiver.impl.TransformerImpl;
import de.heuboe.tls.receiver.impl.rmq.TeleReceiverRmqImpl;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import de.heuboe.tls.receiver.interfaces.TeleReceiver;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.Transformer;

public class Main {

	private static String host = "localhost";
	private static int port = 5672;

	public static void main(String[] args) {
		AddressConverter addressConverter = new AddressConverterGenericImpl(); 
		DataWriter dataWriter = new DataWriterDummyImpl();
		try {
			TeleReceiver teleReceiver = new TeleReceiverRmqImpl(host, port);
			
			TransformationReader transformationReader = new TransformationReaderImpl();
			
			Transformer transformer = new TransformerImpl();
			
			TlsReceiver tlsReceiver = new TlsReceiver(addressConverter, dataWriter, teleReceiver,
					transformationReader, transformer, new File("src/test/resources/rcv.txt"));
			
			tlsReceiver.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
