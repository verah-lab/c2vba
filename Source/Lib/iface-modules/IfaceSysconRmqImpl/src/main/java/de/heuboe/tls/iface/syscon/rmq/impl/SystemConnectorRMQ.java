package de.heuboe.tls.iface.syscon.rmq.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;
import de.heuboe.tls.iface.syscon.rmq.data.TlsTelegram;
import de.heuboe.tls.iface.syscon.rmq.json.JsonConverter;

public class SystemConnectorRMQ implements IfaceSystemConnector {

	private static final Logger LOGGER = Logger.getLogger(SystemConnectorRMQ.class);

	private IfaceApplication ifaceApplication;
	private JsonConverter json;
	private Connection connection;
	private Channel channel;
	
	public SystemConnectorRMQ(String host, int port) throws IfaceException {
		json = new JsonConverter();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
			connection = factory.newConnection();
	        channel = connection.createChannel();
	        channel.exchangeDeclare(TlsTelegram.RECEIVE_EXCHANGE, "fanout");
		} catch (IOException | TimeoutException e) {
			LOGGER.fatal("cannot connect to rabbitmq: " + e.toString());
			throw new IfaceException(e);
		}
	}
	
	@Override
	public void setIfaceApplication(IfaceApplication ifaceApplication) throws IfaceException {
		this.ifaceApplication = ifaceApplication;
	}

	@Override
	public void recvTelegram(byte[] tele, int node) throws IfaceException {
        String ifacekey = Integer.toString(ifaceApplication.getIfaceKey());
        String message = createMessage(tele, node);
        if (message == null || message.isEmpty()) {
			LOGGER.fatal("cannot create message");
			throw new IfaceException("cannot create message");
        }
        try {
			channel.basicPublish(TlsTelegram.RECEIVE_EXCHANGE, ifacekey, null, message.getBytes());
		} catch (IOException e) {
			LOGGER.fatal("cannot send message to exchange: " + e.toString());
			throw new IfaceException(e);
		}
	}

	@Override
	public void recvCommState(int node, boolean alive, boolean queried)	throws IfaceException {
		
	}

	public void close() throws IfaceException {
        try {
			channel.close();
	        connection.close();
		} catch (IOException | TimeoutException e) {
			LOGGER.fatal("cannot close connection to rabbitmq: " + e.toString());
			throw new IfaceException(e);
		}
	}

	private String createMessage(byte[] tele, int node) {
		TlsTelegram tlstele = new TlsTelegram();
		tlstele.setTelegram(tele);
		tlstele.setNode(node);
		return json.toJson(tlstele);
	}

}
