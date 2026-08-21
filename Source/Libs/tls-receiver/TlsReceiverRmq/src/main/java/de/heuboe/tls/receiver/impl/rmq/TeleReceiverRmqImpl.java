package de.heuboe.tls.receiver.impl.rmq;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.syscon.rmq.data.TlsTelegram;
import de.heuboe.tls.iface.syscon.rmq.json.JsonConverter;
import de.heuboe.tls.receiver.interfaces.TeleReceiver;
import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.TlsTele.Direction;

public class TeleReceiverRmqImpl implements TeleReceiver {

	private static final Logger LOGGER = Logger.getLogger(TeleReceiverRmqImpl.class);
	
	private JsonConverter json;
	private List<TlsTele> teleBuffer;

	private ReentrantReadWriteLock rwLock;
	
	public TeleReceiverRmqImpl(String host, int port) throws IOException {
		rwLock = new ReentrantReadWriteLock(true);
		teleBuffer = new ArrayList<>();
		json = new JsonConverter();
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(host);
	    factory.setPort(port);
		Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.exchangeDeclare(TlsTelegram.RECEIVE_EXCHANGE, "fanout");
	    String queueName = channel.queueDeclare().getQueue();
	    
	    channel.queueBind(queueName, TlsTelegram.RECEIVE_EXCHANGE, "");

	    Consumer consumer = new DefaultConsumer(channel) {
	        @Override
	        public void handleDelivery(String consumerTag, Envelope envelope,
	                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
	          String message = new String(body, "UTF-8");
	          createTelegram(message);
	        }
	      };
	      channel.basicConsume(queueName, true, consumer);
	}
	
	@Override
	public List<TlsTele> receive() {
		List<TlsTele> teleList = new ArrayList<>();
		rwLock.writeLock().lock();
		teleList.addAll(teleBuffer);
		teleBuffer.clear();
		rwLock.writeLock().unlock();
		return teleList;
	}

	private void createTelegram(String message) {
		LOGGER.info("createTelegram: " + message);
		TlsTelegram telegram = json.toTelegram(message);
		byte[] tele = telegram.getTelegram();
		try {
			TlsTele tlsTele = new TlsTele(new Date(), Direction.RECEIVE, (int) telegram.getNode(), tele, 0, tele.length);
			rwLock.writeLock().lock();
			teleBuffer.add(tlsTele);
			rwLock.writeLock().unlock();
		} catch (TlsBadTele e) {
			logBadData((int) telegram.getNode(), tele, e);
		}
	}
	
	private void logBadData(int node, byte[] data, TlsBadTele e) {
		String realnode = " " + node / 256 + "-" + node % 256; 
		String cause = "";
		if (e != null) {
			if (e.getCause() != null) {
				cause = e.getCause().getMessage();
			} else {
				cause = e.getMessage();
			}
		}
		LOGGER.error(" Bad Data from " + realnode + ": " + cause ); 
	}

	@Override
	public void stopReceive() {
	}
}
