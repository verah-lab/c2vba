package de.heuboe.tls.iface.syscon.soap.rcv;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import de.heuboe.tls.iface.syscon.soap.data.ReceiveTelegramRequest;

@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface TlsReceiver {

	public void receiveTelegram(ReceiveTelegramRequest request);
}
