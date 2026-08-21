package de.heuboe.tls.iface.prot.tlsoip;

import de.heuboe.tls.iface.iface.SystemMessageManagement;
import de.heuboe.tls.iface.lib.Util;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Objects of this clas handle connection dependent logging in the special format
 * indicated by TLS 2012
 */
@Slf4j
public class TlsOverIpLogger {

	private SystemMessageManagement smm;

	private static Map<String, TlsOverIpLogger> logMap = new HashMap<>();
	
	/**
	 * Numerical code for a certain messagge
	 */
	public enum Msg   {
		// Errors
		ConnectionRefused(1), // NOSONAR keep this style for historic reason
		TimeoutKeepalive(201), // NOSONAR keep this style for historic reason
		TimeoutQuittung(202), // NOSONAR keep this style for historic reason
		InvalidTelType(203), // NOSONAR keep this style for historic reason
		InvalidSeqNum(204), // NOSONAR keep this style for historic reason
		InvalidLenTls(205), // NOSONAR keep this style for historic reason
        TimeoutQuittungGrace(206), // Warning, still have grace time // NOSONAR keep this style for historic reason
		// important information
		ConnectionAccept(1001), // NOSONAR keep this style for historic reason
		ConnectionClose(1002), // NOSONAR keep this style for historic reason
		ConnectionBroken(1003), // NOSONAR keep this style for historic reason
		ConnectionIdentified(1004), // NOSONAR keep this style for historic reason
		// useful information
		SwitchLogFile(2050), // NOSONAR keep this style for historic reason
		SendDataOsi2(2201), // NOSONAR keep this style for historic reason
		RecvDataOsi2(2202), // NOSONAR keep this style for historic reason
		SendDataOsi3(2301), // NOSONAR keep this style for historic reason
		RecvDataOsi3(2302), // NOSONAR keep this style for historic reason
		SendDataOsi7(2701), // NOSONAR keep this style for historic reason
		RecvDataOsi7(2702), // NOSONAR keep this style for historic reason
		ConnectionAcc(88); // NOSONAR keep this style for historic reason
		private int val;
		Msg(int value) { val = value; }
		int getValue() { return val; }		
	}
	
	/**
	 * factory method to constuct a logger for tls over ip
	 * @param filename Filename/Path to be used (prefix)
	 * @param maxLines Approx. maximum number of lines per file
	 * @param rotate Number of fiels too keep
	 * @param smm Potential System Message Managemen in charge
	 * @return A logger for TLS over ip (special TLS format)
	 * @throws IOException may be ... file system use
	 */
	public static TlsOverIpLogger getTlsOverIpLogger(String filename, int maxLines, int rotate, SystemMessageManagement smm) throws IOException {
		if (filename == null) {
			return new TlsOverIpLogger();
		}
		
		TlsOverIpLogger logger = logMap.get(filename);  // NOSONAR computeIfAbsent is no good alternative due to exception handling
		if (logger == null) {
			logger = new TlsOverIpLogger(filename, maxLines, rotate, smm);
			logMap.put(filename, logger);
		}
		return logger;
	}
	
	private String filename;
	private int maxLines;
	private int rotate;
	private int cntLines;
	private int cntRotate;
	private BufferedWriter logwriter;
	
	private TlsOverIpLogger() {		
	}
	
	private TlsOverIpLogger(String filename, int maxLines, int rotate, SystemMessageManagement smm) throws IOException {
		try {
			this.filename = filename;
			this.maxLines = maxLines;
			this.rotate = rotate;
			this.cntLines = 0;
			this.smm = smm;
			this.cntRotate = getFirstRotate();
			createNewLogFile();
		} catch (IOException e) {
			log.error("got Exception creating tls over ip log: " + e);
			sysMsg( "TlsOverIpLogger: Got Exception creating tls over ip log: " + e.getMessage() );
			throw e;
		}
	}
	
	private void msg(int msgclass, int osilevel, int num, String text, short osi2port, short osi2partner) {
		int code = msgclass*1000 + osilevel*100 + num;
		String codeStr = String.format("%04d", code);
		log.debug("C " + msgclass + ", I " + osi2port + "/" + osi2partner + ", L" + osilevel + ", C " + codeStr + ": " + text); 
		if (logwriter != null) {
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				logwriter.write(format.format(now) + "\t" + msgclass + "\t" + osi2partner + "\t" + osilevel
						+ "\t" + codeStr + "\t" + text);
				logwriter.newLine();
				logwriter.flush();
				++cntLines;
				if (maxLines > 0 && cntLines == maxLines) {
					createNewLogFile();
				}
			} catch (IOException e) {
				log.error("got exception on writing message to tls over ip log file: " + e);
				sysMsg( "TlsOverIpLogger.msg: Got exception on writing message to tls over ip log file: " + e.getMessage() );
			}
		}
	}
	
	/**
	 * Helper to build a message for the log (differing parameters)
	 * @param num Code for the message
	 * @param text text tobe output to log
	 * @param osi2port Own osi2 port
	 * @param osi2partner Osi2 port of partner
	 */
	public void msg(Msg num, String text, short osi2port, short osi2partner) {
		int msgclass = num.getValue() / 1000; 
		int osilevel = (num.getValue() / 100) % 10;
		int msgnum = num.getValue() % 100;
		msg(msgclass, osilevel, msgnum, text, osi2port, osi2partner);
	}
	
	/**
	 * Helper to build a message for the log (differing parameters)
	 * @param num Code for the message
	 * @param data Data to be logged with the message
	 * @param osi2port Own osi2 port
	 * @param osi2partner Osi2 port of partner
	 */
	public void msg(Msg num, byte[] data, short osi2port, short osi2partner) {
		msg(num, data, 0, data.length, osi2port, osi2partner);
		
	}
	
	/**
	 * Helper to build a message for the log (differing parameters)
	 * @param num Code for the message
	 * @param data Data to be logged with the message (restricted by offset and length)
	 * @param offset Offset into data
	 * @param length Number of byte beginning with offset to be logged
	 * @param osi2port Own osi2 port
	 * @param osi2partner Osi2 port of partner
	 */
	public void msg(Msg num, byte[] data, int offset, int length, short osi2port, short osi2partner) {
		String hex = Util.toHex(data, offset, length);
		msg(num, hex, osi2port, osi2partner);
	}

	private void createNewLogFile() throws IOException {
		if (logwriter != null) {
			logwriter.close();
		}
		FileWriter fw = new FileWriter(filename+"-"+cntRotate+".log");
		logwriter = new BufferedWriter(fw);
		logwriter.write("JJJJ-MM-TT HH:MM:SS\tC\tI\tL\tCLnn\tText");
		logwriter.newLine();
		logwriter.flush();
		++cntRotate;
		if (cntRotate == rotate) {
			cntRotate = 0;
		}
		cntLines=0;
	}

	private int getFirstRotate() {
		SortedMap<Long, Integer> tMap = new TreeMap<>();
		for(int r=0; r<rotate; ++r) {
			File f = new File(filename+"-"+r+".log");
			if (!f.exists()) {
				return r;
			}
			tMap.put(f.lastModified(), r);
		}
		Iterator<Integer> it = tMap.values().iterator();
		return it.next();
	}
    
    private void sysMsg( String msg ) {
        if (null != smm) {
            smm.sendMessage( msg );
        }
    }

}
