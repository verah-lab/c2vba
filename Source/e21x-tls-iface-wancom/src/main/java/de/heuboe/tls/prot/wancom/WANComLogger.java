package de.heuboe.tls.prot.wancom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.lib.Util;

/**
 * Provides functionality for logging connection traffic and events in the style TLS 2012 prefers it
 */
public class WANComLogger {

	private static final Logger LOGGER = Logger.getLogger(WANComLogger.class);

	private static Map<String, WANComLogger> logMap = new HashMap<>();

	/**
	 * enumeration for events during communication
	 */
	public enum Msg   {
		// Errors
		ConnectionRefused(1),          // NOSONAR intended this way
		TimeoutKeepalive(201),         // NOSONAR intended this way
		TimeoutQuittung(202),          // NOSONAR intended this way
		InvalidTelType(203),           // NOSONAR intended this way
		InvalidSeqNum(204),            // NOSONAR intended this way
		InvalidLenTls(205),            // NOSONAR intended this way
		// important information
		ConnectionAccept(1001),        // NOSONAR intended this way
		ConnectionClose(1002),         // NOSONAR intended this way
		ConnectionBroken(1003),        // NOSONAR intended this way
		ConnectionIdentified(1004),    // NOSONAR intended this way
		// useful information
		SwitchLogFile(2050),           // NOSONAR intended this way
		SendDataOsiWANCom(2801),       // NOSONAR intended this way
		RecvDataOsiWANCom(2802),       // NOSONAR intended this way
		SendDataOsi2(2201),            // NOSONAR intended this way
		RecvDataOsi2(2202),            // NOSONAR intended this way
		SendDataOsi3(2301),            // NOSONAR intended this way
		RecvDataOsi3(2302),            // NOSONAR intended this way
		SendDataOsi7(2701),            // NOSONAR intended this way
		RecvDataOsi7(2702),            // NOSONAR intended this way
		ConnectionAcc(88);             // NOSONAR intended this way
		private int val;
		Msg(int value) { val = value; }
		int getValue() { return val; }		
	}

	/**
	 * Provision of a {@link WANComLogger}
	 * @param filename Filename template to write log to
	 * @param maxLines approximate maximum of lines in one log file
	 * @param rotate number of files to keep
	 * @return A WANCom logger
	 * @throws IOException Exception if there is a faulty path for instance
	 */
	public static WANComLogger getWANComLogger( String filename, int maxLines, int rotate ) throws IOException {
        if ( filename == null ) {
            return new WANComLogger();
        }

//        WANComLogger res = logMap.computeIfAbsent( filename, key -> {     // NOSONAR example functional code
//            try {                                                         // NOSONAR example functional code
//                return new WANComLogger( key, maxLines, rotate );         // NOSONAR example functional code
//            }                                                             // NOSONAR example functional code
//            catch ( IOException e ) {                                     // NOSONAR example functional code
//                e.printStackTrace();                                      // NOSONAR example functional code
//                return null;                                              // NOSONAR example functional code
//            }                                                             // NOSONAR example functional code
//        } );                                                              // NOSONAR example functional code
//                                                                          // NOSONAR example functional code
//        if (null == res) {                                                // NOSONAR example functional code
//            throw new IOException( "Could not create WANCom logger" );    // NOSONAR example functional code
//        }                                                                 // NOSONAR example functional code
//                                                                          // NOSONAR example functional code
//        return res;                                                       // NOSONAR example functional code

		WANComLogger logger = logMap.get(filename); // NOSONAR functional code is even worse
		if (logger == null) {
			logger = new WANComLogger(filename, maxLines, rotate);
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
	
	private WANComLogger() {		
	}
	
	private WANComLogger(String filename, int maxLines, int rotate) throws IOException {
		try {
			this.filename = filename;
			this.maxLines = maxLines;
			this.rotate = rotate;
			this.cntLines = 0;
			this.cntRotate = getFirstRotate();
			createNewLogFile();
		} catch (IOException e) {
			LOGGER.error("got Exception creating WANCom log: " + e);
			throw e;
		}
	}
	
	private void msg(int msgclass, int osilevel, int num, String text, short osi2port, short osi2partner) {
		int code = msgclass*1000 + osilevel*100 + num;
		String codeStr = String.format("%04d", code);
		LOGGER.debug("C " + msgclass + ", I " + osi2port + "/" + osi2partner + ", L" + osilevel + ", C " + codeStr + ": " + text); 
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
				LOGGER.error("got exception on writing message to tls over ip log file: " + e);
			}
		}
	}

	/**
	 * Helper for construction of a message (line) in the log file
	 * @param num code of the message
	 * @param text text of the message
	 * @param osi2port osi2 parent port
	 * @param osi2partner osi2 child port
	 */
	public void msg(Msg num, String text, short osi2port, short osi2partner) {
		int msgclass = num.getValue() / 1000; 
		int osilevel = (num.getValue() / 100) % 10;
		int msgnum = num.getValue() % 100;
		msg(msgclass, osilevel, msgnum, text, osi2port, osi2partner);
	}

	/**
	 * Helper for construction of a message (line) in the log file
	 * @param num code of the message
	 * @param data addition data for the massage (converted to a hex string)
	 * @param osi2port osi2 parent port
	 * @param osi2partner osi2 child port
	 */
	public void msg(Msg num, byte[] data, short osi2port, short osi2partner) {
		msg(num, data, 0, data.length, osi2port, osi2partner);
		
	}

	/**
	 * Helper for construction of a message (line) in the log file
	 * @param num code of the message
	 * @param data addition data for the massage (converted to a hex string
	 * @param offset offset for beginning within data
	 * @param length number of bytes to convert
	 * @param osi2port osi2 parent port
	 * @param osi2partner osi2 child port
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

}
