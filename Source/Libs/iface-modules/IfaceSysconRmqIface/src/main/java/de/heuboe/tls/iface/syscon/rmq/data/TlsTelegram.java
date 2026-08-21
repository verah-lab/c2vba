package de.heuboe.tls.iface.syscon.rmq.data;

public class TlsTelegram {

	public static final String SEND_QUEUE = "TlsTeleToSendQueue";
	public static final String RECEIVE_QUEUE = "TlsTeleToReceiveQueue";
	public static final String RECEIVE_EXCHANGE = "TlsTeleToReceiveExchange";
	
	private byte[] telegram;
	private long node;
	
	public byte[] getTelegram() {
		return telegram;
	}
	public void setTelegram(byte[] telegram) {
		this.telegram = telegram;
	}
	public long getNode() {
		return node;
	}
	public void setNode(long node) {
		this.node = node;
	}
	
	
}
