package de.heuboe.tls.iface.syscon.ddp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import de.heuboe.ddp.Connection;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.Parameters;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.SystemMessageManagement;

public class PermanentRunCheck implements IfaceApplication {

	int cnt=0;
	boolean print=false;
	
	@Test
	public void test() throws DDPException, IfaceException, InterruptedException {
		Parameters p = new Parameters("postgres", "jb_1", "dbsrv9-2k12", "postgres", "postgres", "TestJddp", "rz", "th-int2-w7v:5599:uz-jb", 0);
		Connection conn = new Connection(p);
		
		DdpSystemConnector syscon = new DdpSystemConnector(conn);
		syscon.setIfaceApplication(this);
		while(true) {
			Thread.sleep(1000000L);
		}
	}

	@Override
	public void recvTelegramm(byte[] tele, short osi2port, short osi2partner) {
		// TODO Auto-generated method stub
	}

	private long unsigned(byte b) {
		if (b<0) {
			return b + 256;
		}
		return b;
	}

	@Override
	public void sendTelegram(byte[] tele, int node) {
		if ((cnt % 10000) == 0) {
			print = true;
		}		
		if (print && tele.length > 20 && unsigned(tele[10]) == 255 && unsigned(tele[11]) == 251) {
			long t = unsigned(tele[12]) * 0x1000000L;
			t += unsigned(tele[13]) * 0x10000L;
			t += unsigned(tele[14]) * 0x100L;
			t += unsigned(tele[15]);
			t *= 1000L;
			Date tt = new Date(t);
			Date now = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("hh:mm:ss");
			System.out.println(fmt.format(now) + " -> " + fmt.format(tt));
			print = false;
		}
		++cnt;
	}

	@Override
	public void startComm(Short osi2port, Short osi2partner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopComm(Short osi2port, Short osi2partner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void timeSync() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recvCommunicationState(short osi2Port, short osi2Address,
			boolean state, boolean queried) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIfaceKey() {
		return 1017;
	}

    @Override
    public SystemMessageManagement getSystemMessageManagement() {
        // TODO Auto-generated method stub
        return null;
    }
	
	/**
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	@Override
	public void stopCommByOsi7Node( Integer targetNodeNumber ) {
	
	}
	
	/**
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	@Override
	public void startCommByOsi7Node( Integer targetNodeNumber ) {
	
	}
}
