package de.heuboe.datex2.vms.status.pub;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.push.D2PubSenderMDM;
import eu.datex2.schema._2._2_0.D2LogicalModel;


/**
 * 
 * Sends DATEX-II publication to receiver
 * 
 * @author peters
 *
 */
public class Sender
{
	@Autowired
	private D2PubSenderMDM mdmSender = null;
	
//	public Sender( Properties properties )
//			throws VMSException
//	{
//		mdmSender = new D2PubSenderMDM( rc );
//		
//		try
//		{
//			String d2SchemaLocation = properties.getD2SchemaLocation();	
//			
//			String mdmAuthenticationFile = 	properties.getMdmAuthenticationFile();	
//			if( ( mdmAuthenticationFile != null ) && !mdmAuthenticationFile.isEmpty() )
//			{
//				ConnectionParameter connParams = ConnectionParameter.fromPropertyFile(  mdmAuthenticationFile );
//				
//				RemoteConfig rc = new RemoteConfig( Publisher.SCHEMA_FILE, AccessType.SOAP, connParams );
//				
//				if( ( d2SchemaLocation != null ) && !d2SchemaLocation.isEmpty() )
//				{
//					Map<String,String> sls = new HashMap<>();
//					sls.put( SchemaUtil.DATEX2_SCHEMA_NS, d2SchemaLocation );
//					rc.setSchemaLocations( sls );
//				}				
//				mdmSender = new D2PubSenderMDM( rc );
//			}
//			
//			String d2ReceiverURL = properties.getD2ReceiverURL();	
//			String d2ReceiverConnPropFile = properties.getD2ReceiverConnPropFile();
//			
//			ConnectionParameter connParams = null;
//			if( ( d2ReceiverURL != null ) && !d2ReceiverURL.isEmpty() )
//			{
//				connParams = new ConnectionParameter();
//				connParams.setUrl( d2ReceiverURL );
//				connParams.setWsTimeout( properties.getConnTimeout() );
//				connParams.setUser( properties.getD2ReceiverUser());
//				connParams.setPassword( properties.getD2ReceiverPassword() );
//				connParams.setProxyServer( properties.getD2ReceiverProxyServer() );
//				connParams.setProxyPort( properties.getD2ReceiverProxyPort() );
//				connParams.setTrustStoreFile( properties.getD2ReceiverTrustStoreFile() );
//				connParams.setTrustStorePassword( properties.getD2ReceiverTrustStorePassword() );
//				connParams.useGzip( properties.isD2ReceiverUseGzip() );
//			}
//			
//			if( ( d2ReceiverConnPropFile != null ) && !d2ReceiverConnPropFile.isEmpty() )
//			{
//				connParams = ConnectionParameter.fromPropertyFile( d2ReceiverConnPropFile );
//			}			
//			
//			if( connParams != null )
//			{
//				if( ( d2SchemaLocation != null ) && !d2SchemaLocation.isEmpty() )
//				{
//					Map<String,String> sls = new HashMap<>();
//					sls.put( SchemaUtil.DATEX2_SCHEMA_NS, d2SchemaLocation );
//					RemoteConfig rc = new RemoteConfig( Publisher.SCHEMA_FILE, sls, connParams );
//					
//					d2PushSender = new D2PubSenderSOAP( rc );
//				} else
//				{	
//					d2PushSender = new D2PubSenderSOAP( connParams );
//				}
//			}
//			
//		} catch( D2Exception | D2BaseException | FtpException ex )
//		{
//			throw new VMSException( VMSException.ERROR_XML, ex.toString(), ex );
//		}
//	}
	
	/**
	 * 
	 * Sends
	 * 
	 * 
	 * @param pubTime		Publication time
	 * @param pubCycle		Publication cycle
	 * @param errMsg		Error message
	 * @param d2lm			DATEX-II publication
	 * @param pubFile		File	
	 * @throws D2Exception	Error
	 */
	public void send( Date pubTime, int pubCycle, 
					  String errMsg,	
					  D2LogicalModel d2lm,
					  String pubFile )
			throws D2Exception
	{
		if( mdmSender != null )
		{
			if( d2lm != null ) {  				// NOSONAR 
				mdmSender.publish( d2lm );
			}
		}
	}
}
