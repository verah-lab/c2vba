package de.heuboe.datex2.mst.builder.ws;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.mst.builder.D2MSTDataReceiver;
import de.heuboe.log.Logger;

/**
 * 
 * D2MSTPubService
 * 
 * @author peters
 *
 */
public class D2MSTPubService
{
	private static final Logger LOGGER = Logger.getLogger( D2MSTPubService.class );
	
	private D2MSTDataReceiver dataReceiver = null;
	
	
	public D2MSTPubService( D2MSTDataReceiver dataReceiver )
	{
		this.dataReceiver = dataReceiver;
	}
	
	public void createMSTPublicationFileDef( String mstId, 
			                                 String mstVersion,
			                                 String defVersion,
			                                 String pubNatId,
			                                 String preferredLocEncoding )
			throws D2MSTSvcException
	{
		LOGGER.info( "" );
		LOGGER.info( "Create MST for mstId=" + mstId + ", version=" + mstVersion + ", defVersion=" + defVersion + " ...");
		
		try
		{
			dataReceiver.writeMST( mstId, mstVersion, defVersion, pubNatId, preferredLocEncoding );
		} catch( D2ExceptionBase ex ) {
			throw new D2MSTSvcException( "Error building MST file", ex );
		}

		LOGGER.info( "MST for mstId=" + mstId + ", version=" + mstVersion + " created !");
	}
}
