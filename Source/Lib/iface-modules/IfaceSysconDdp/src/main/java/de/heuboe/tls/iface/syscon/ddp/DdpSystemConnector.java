package de.heuboe.tls.iface.syscon.ddp;

import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.Datakind;
import de.heuboe.ddp.Dataset;
import de.heuboe.ddp.Modification;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;

public class DdpSystemConnector implements IfaceSystemConnector {

	private Connection connection;
	private Datakind datakind;
	private int ifaceKey;
	
	public DdpSystemConnector(Connection connection) throws DDPException {
		super();
		this.connection = connection;
		datakind = connection.getDatakind("TlsTele2Recv");
	}

	@Override
	public void setIfaceApplication(IfaceApplication ifaceApplication) throws IfaceException {
		this.ifaceKey = ifaceApplication.getIfaceKey();
		DdpReceiver receiver;
		try {
			receiver = new DdpReceiver(connection, ifaceApplication, ifaceKey);
		} catch (DDPException e) {
			throw new IfaceException(e);
		}
		Thread ddp = new Thread(receiver, "DDP-Loop");
		ddp.start();
	}

	@Override
	public void recvTelegram(byte[] tele, int node) throws IfaceException {
		try {
			Dataset ds = new Dataset(datakind);
			Container cont = new Container(datakind);
			ds.value("ifacekey").setInt(ifaceKey);
			ds.value("realaddress").setInt(node);
			ds.value("flags").setInt(0);
			ds.value("len").setInt(tele.length);
			for(int i=0; i<tele.length; ++i) {
				ds.value("osi7tel").get(i).setByte(tele[i]);
			}
			cont.add(ds);
			Modification modification = new Modification();
			modification.put(cont);
			connection.write(modification);
		} catch (DDPException e) {
			throw new IfaceException(e);
		}
	}

	public void recvCommState(int node, boolean alive, boolean queried) throws IfaceException  {
		byte[] tele = new byte[6];
		tele[0] = (byte) (node & 0xff);
		tele[1] = (byte)((node & 0xff00) >> 8);
		tele[2] = (byte)((node & 0xff0000) >> 16);
		tele[3] = 3;                            // Kommunikationstatus
		tele[4] = (byte)(alive   ? 0 : 1);                  
		tele[5] = (byte)(queried ? 1 : 0);
		
		try {
			Dataset ds = new Dataset(datakind);
			Container cont = new Container(datakind);
			ds.value("ifacekey").setInt(0);
			ds.value("realaddress").setInt(0);
			ds.value("flags").setInt(0);
			ds.value("len").setInt(tele.length);
			for(int i=0; i<tele.length; ++i) {
				ds.value("osi7tel").get(i).setByte(tele[i]);
			}
			cont.add(ds);
			Modification modification = new Modification();
			modification.put(cont);
			connection.write(modification);
		} catch (DDPException e) {
			throw new IfaceException(e);
		}
		recvCommStateDetail(node, alive ? 1 : 0);
	}
	
	private void recvCommStateDetail(int node, int detail) throws IfaceException  {
		byte[] tele = new byte[6];
		tele[0] = (byte) (node & 0xff);
		tele[1] = (byte)((node & 0xff00) >> 8);
		tele[2] = (byte)((node & 0xff0000) >> 16);
		tele[3] = 5;                            // Kommunikationstatus Detail
		tele[4] = (byte) detail;                  
		tele[5] = 0;
		
		try {
			Dataset ds = new Dataset(datakind);
			Container cont = new Container(datakind);
			ds.value("ifacekey").setInt(0);
			ds.value("realaddress").setInt(0);
			ds.value("flags").setInt(0);
			ds.value("len").setInt(tele.length);
			for(int i=0; i<tele.length; ++i) {
				ds.value("osi7tel").get(i).setByte(tele[i]);
			}
			cont.add(ds);
			Modification modification = new Modification();
			modification.put(cont);
			connection.write(modification);
		} catch (DDPException e) {
			throw new IfaceException(e);
		}
	}
}
