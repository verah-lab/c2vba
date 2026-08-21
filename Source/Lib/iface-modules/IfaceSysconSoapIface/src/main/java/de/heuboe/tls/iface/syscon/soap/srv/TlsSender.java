package de.heuboe.tls.iface.syscon.soap.srv;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import de.heuboe.tls.iface.syscon.soap.data.RegisterRequest;
import de.heuboe.tls.iface.syscon.soap.data.SendTelegramRequest;

@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface TlsSender {

	public void sendTelegram(SendTelegramRequest request);
	
	public void registerReceiver(RegisterRequest request);
}
