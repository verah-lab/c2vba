package de.heuboe.tls.iface.syscon.soap.srv.impl;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;
import de.heuboe.tls.iface.syscon.soap.data.Direction;
import de.heuboe.tls.iface.syscon.soap.data.ReceiveTelegramRequest;
import de.heuboe.tls.iface.syscon.soap.data.TlsTelegram;
import de.heuboe.tls.iface.syscon.soap.rcv.TlsReceiver;
import jakarta.xml.bind.JAXBException;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SystemConnector implements IfaceSystemConnector {

    private static final Logger LOGGER = Logger.getLogger(SystemConnector.class);

    private Sender ifaceServiceImpl;
    private List<TlsReceiver> receivers = new ArrayList<>();

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public SystemConnector(String url) {
        super();
        ifaceServiceImpl = createTlsSenderImpl(url);
        ifaceServiceImpl.setSystemConnector(this);
    }

    private Sender createTlsSenderImpl(String url) {
		Sender sender = new Sender();
		JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
		svrFactory.setServiceClass(Sender.class);
		svrFactory.setAddress(url);
		svrFactory.setServiceBean(sender);
		svrFactory.getInInterceptors().add(new LoggingInInterceptor());
		svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
		svrFactory.create();

		return sender;
	}

	@Override
	public void setIfaceApplication(IfaceApplication ifaceApplication) throws IfaceException {
		ifaceServiceImpl.setIfaceApplication(ifaceApplication);
	}

	@Override
	public void recvTelegram(byte[] tele, int node) throws IfaceException {
		ReceiveTelegramRequest request = new ReceiveTelegramRequest();
		request.setDirection(Direction.Recv);
		request.setNode(node);
		TlsTelegram telegram = new TlsTelegram();
		telegram.setData(tele);
		request.setTelegram(telegram);
		List<TlsReceiver> bad = new ArrayList<>();
		rwLock.readLock().lock();
		for(TlsReceiver r : receivers) {
			try {
				r.receiveTelegram(request );
			} catch(Exception e) {
				LOGGER.error("cannot deliver telegram to receiver", e);
				bad.add(r);
			}
		}
		rwLock.readLock().unlock();
		if (!bad.isEmpty()) {
			rwLock.writeLock().lock();
			receivers.removeAll(bad);
			rwLock.writeLock().unlock();
		}
	}

	@Override
	public void recvCommState(int node, boolean alive, boolean queried) throws IfaceException {
		// TODO Auto-generated method stub

    }

    public void registerReceiver(String url) {
		try {
            TlsReceiver receiver = createReceiver(url);
            rwLock.writeLock().lock();
            receivers.add(receiver);
            rwLock.writeLock().unlock();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(); // NOSONAR dont care, SOAP is not used any more
        }
    }

    private TlsReceiver createReceiver(String url) throws JAXBException {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.setServiceClass(TlsReceiver.class);
        factory.setAddress(url);
        factory.setWsdlLocation(url + "?wsdl");

        return (TlsReceiver) factory.create();
    }
}
