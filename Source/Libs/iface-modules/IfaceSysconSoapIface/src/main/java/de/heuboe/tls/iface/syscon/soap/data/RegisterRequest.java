package de.heuboe.tls.iface.syscon.soap.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterRequest", propOrder = {
    "url",
    "receiveAll"
})
public class RegisterRequest {

	private String url;
	private boolean receiveAll;

	public RegisterRequest() {
		this(null, false);
	}

    public RegisterRequest(String url) {
		this(url, false);
	}

    public RegisterRequest(String url, boolean receiveAll) {
		super();
		this.url = url;
		this.receiveAll = receiveAll;
	}

    public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isReceiveAll() {
		return receiveAll;
	}
	public void setReceiveAll(boolean receiveAll) {
		this.receiveAll = receiveAll;
	}

}
