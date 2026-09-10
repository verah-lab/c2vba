package de.heuboe.datex2.mst.builder.client;

/**
 * @author peters
 *
 * Builds database supply of a MST by calling process mstBuilder
 * (communication via CORBA interface)
 *
 */

import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import de.heuboe.datex2.base.D2BaseUtil;
import de.heuboe.datex2.mst.builder.ws.D2MSTPubService;
import de.heuboe.datex2.mst.builder.ws.D2MSTSvcException;

public class MSTBuilderClient 
{
	private static int CONN_TIMEOUT = 300000;			// 5 min
	private static int RECEIVE_TIMEOUT = 300000;   		// 5 min
	
	private static D2MSTPubService createPubService( String mstBuilderSvcUrl )
	{
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass( D2MSTPubService.class );
		factory.setAddress( mstBuilderSvcUrl );
		
		factory.setEndpointName( new QName( "http://ws.builder.mst.datex2.heuboe.de/",
				    						"D2MSTPubServiceWSPort") );
		
		D2MSTPubService pubService = (D2MSTPubService)factory.create();

        Client client = ClientProxy.getClient(pubService);
        
        if (client != null) 
        {
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout( CONN_TIMEOUT );
			policy.setReceiveTimeout( RECEIVE_TIMEOUT );
			conduit.setClient(policy);
        }
        
        pubService.ping();
        
        return pubService;
	}
	
	public static void buildMSTFile( String mstId, 
									 String mstVersion, 
									 String defVersion,
									 String mstBuilderSvcUrl, 
									 String pubNatId, 
									 String  preferredLocEncoding )
			throws D2MSTSvcException		
	{
		D2MSTPubService pubBuilder = createPubService( mstBuilderSvcUrl );
		if( pubBuilder == null )
		{
			System.out.println("MST Datei kann nicht erstellt werden !");
			D2BaseUtil.exit(-1);
		}
		
		pubBuilder.createMSTPublicationFileDef( new Date(), 
											 	mstId, 
											 	mstVersion, 
											 	defVersion,
											 	pubNatId, 
											 	preferredLocEncoding );			
	}
}
