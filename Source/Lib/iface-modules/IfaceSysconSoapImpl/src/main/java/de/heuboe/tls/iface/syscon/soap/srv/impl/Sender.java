package de.heuboe.tls.iface.syscon.soap.srv.impl;

import javax.jws.WebService;

import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.syscon.soap.data.RegisterRequest;
import de.heuboe.tls.iface.syscon.soap.data.SendTelegramRequest;
import de.heuboe.tls.iface.syscon.soap.srv.TlsSender;

@WebService(
		name="TlsSender", 
		serviceName="TlsSender",
		targetNamespace="http://srv.soap.syscon.iface.tls.heuboe.de/"
		)
public class Sender implements TlsSender {

	private IfaceApplication ifaceApplication;
	private SystemConnector systemConnector;
		
	public void setSystemConnector(SystemConnector systemConnector) {
		this.systemConnector = systemConnector;
	}

	public void setIfaceApplication(IfaceApplication ifaceApplication) throws IfaceException {
		this.ifaceApplication = ifaceApplication;
	}

	@Override
	public void sendTelegram(SendTelegramRequest request) {
		ifaceApplication.sendTelegram(request.getTelegram().getData(), request.getNode());		
	}

	@Override
	public void registerReceiver(RegisterRequest request) {
		systemConnector.registerReceiver(request.getUrl());
	}

}
