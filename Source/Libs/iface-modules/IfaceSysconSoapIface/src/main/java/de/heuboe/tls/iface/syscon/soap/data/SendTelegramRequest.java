package de.heuboe.tls.iface.syscon.soap.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendTelegramRequest", propOrder = {
    "node",
    "telegram"
})
public class SendTelegramRequest {

    private Integer node;
	private TlsTelegram telegram;

    public Integer getNode() {
		return node;
	}
	public void setNode(Integer node) {
		this.node = node;
	}
	public TlsTelegram getTelegram() {
		return telegram;
	}
	public void setTelegram(TlsTelegram telegram) {
		this.telegram = telegram;
	}

}
