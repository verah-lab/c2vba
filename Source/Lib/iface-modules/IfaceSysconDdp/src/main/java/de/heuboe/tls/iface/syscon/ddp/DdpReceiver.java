package de.heuboe.tls.iface.syscon.ddp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.DataFilter;
import de.heuboe.ddp.DataReceivable;
import de.heuboe.ddp.DataReceiver;
import de.heuboe.ddp.DatakindFilter;
import de.heuboe.ddp.Dataset;
import de.heuboe.ddp.TimeFilter;
import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.lib.Util;

/**
 * 
 * @author ralfz
 *
 */
public class DdpReceiver extends DataReceiver implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(DdpReceiver.class);
	
	private Connection connection;
	private IfaceApplication ifaceApplication;
	private int ifaceKey;
	private boolean stopReceive = false; // make sonar happy
	
	/**
	 * 
	 * @param connection the connection to ddp
	 * @param ifaceApplication the ifaceApplication this DdpReceiver is linked to 
	 * @param ifaceKey the ifaceKey under concern
	 * @throws DDPException when something goes wrong with the ddp connection
	 */
	public DdpReceiver(Connection connection, IfaceApplication ifaceApplication, int ifaceKey) throws DDPException {
		this.connection = connection;
		this.ifaceApplication = ifaceApplication;
		this.ifaceKey = ifaceKey;
		
		List<DatakindFilter> dkfList = new ArrayList<>();
		DatakindFilter dkf = new DatakindFilter("TlsTele2Send");
		dkf.setTimeFilter(new TimeFilter(DataFilter.NOW, DataFilter.FOREVER, false));
		dkfList.add(dkf);
		
		DataFilter df = new DataFilter(dkfList.toArray(new DatakindFilter[0]));
		connection.startRead(df, this);
	}

	/**
	 * this method is called, when data is received by ddp
	 * @param eventCode the type of action that leaded to this call
	 */
	@Override
	public void callback(int eventCode) {
		switch (eventCode) {
		case DataReceivable.TRANSACTION_END: // fall through
		case DataReceivable.DATABASE_QUERY_FINISHED:
			getDdpdata();
			break;
		default:
			break;
		}
	}

	/**
	 * Get the ddp data.
	 */
	private void getDdpdata() {
		try {
			Container[] results = getResults();
			for (Container result : results) {
				Iterator<Dataset> it = result.iterator();
				while (it.hasNext()) {
					Dataset data = it.next();
					int ifkey = data.value("ifacekey").getInt();
					// is it for me?
					if (ifkey != 0 && ifkey != ifaceKey) {
						continue;
					}
					// extract data
					int len = data.value("len").getInt();
					byte[] tele = new byte[len];
					for(int i=0; i<len; ++i) {
						tele[i] = data.value("osi7tel").get(i).getByte();
					}
					// do command
					if (ifkey == 0) {
						commandToIface(tele);
					}
					// do telegram
					if (ifkey == ifaceKey) {
						int realaddress = data.value("realaddress").getInt();
						ifaceApplication.sendTelegram(tele, realaddress);
					}
				}
			}
		} catch (DDPException e) {
			LOGGER.fatal("DDP-Error: " + e);
		}
	}
	
	private void commandToIface(byte[] tele) throws DDPException {
		switch (tele[0]) {
			case 0: // StartComm
				if (tele.length != 3) {
					throw new DDPException(0, "falsche Laenge bei Steuerbefehl StartComm");
				}
				ifaceApplication.startComm(Util.toUnsignedShort(tele[1]), Util.toUnsignedShort(tele[2]));
				break;

			case 1: // StopComm
				if (tele.length != 3) {
					throw new DDPException(0, "falsche Laenge bei Steuerbefehl StopComm");
				}
				ifaceApplication.stopComm(Util.toUnsignedShort(tele[1]), Util.toUnsignedShort(tele[2]));
				break;

			case 2: // WerLebt
				if (tele.length != 1) {
					throw new DDPException(0, "falsche Laenge bei Steuerbefehl WerLebt");
				}
				ifaceApplication.queryState();
				break;

			case 3: // ComStat (Rueckmeldung Kommunikationstatus, kommt nicht vor)
				break;
				
			case 4: // TimeSync
				if (tele.length != 1) {
					throw new DDPException(0, "falsche Laenge bei Steuerbefehl TimeSync");
				}
				ifaceApplication.timeSync();
				break;

			default:
				LOGGER.error("unbekannter Steuerbefehl " + (int) tele[0]);
		}
	}


	/**
	 * Implementation of the Runnable.run() method.
	 * Calls the ddp loop.
	 */
	@Override
	public void run() {
		while(!stopReceive) {
			try {
				connection.loop();
			} catch (DDPException e) {
				LOGGER.fatal("DDP-Error: " + e);
			}
		}
	}
}
