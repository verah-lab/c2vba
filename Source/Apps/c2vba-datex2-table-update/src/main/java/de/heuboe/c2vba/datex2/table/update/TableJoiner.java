package de.heuboe.c2vba.datex2.table.update;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import de.heuboe.datex2.exception.D2CommException;
import de.heuboe.datex2.pull.PullClient;
import de.heuboe.datex2.push.PushClient;

public interface TableJoiner<T> {

	
	JoinResult<T> joinTables( T table1, T table2, 
			                  String versionMode1, String versionMode2,  
			                  String resultFile ) throws JAXBException, IOException;

	/**
	 * 
	 * Reads table from file
	 * 
	 * @param fileName			File name
	 * @return					Table publication
	 * @throws JAXBException	Error
	 */
	T readTable( String region, String fileName ) throws IOException, JAXBException;
	
	/**
	 * 
	 * Pulls table from remote DATEX-II SOAP service (MDM)
	 * 
	 * @param pullClient			File name
	 * @return 					  	Table publication
	 * @throws JAXBException		Error
	 */
	T pullTable( PullClient<?> pullClient ) throws JAXBException, D2CommException;
	void pushJoinTable( T joinTable, PushClient pushClient ) throws D2CommException, JAXBException;
}
