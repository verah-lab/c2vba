package de.heuboe.datex2.vms.service.uz;

import java.util.List;

import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import de.heuboe.log.Logger;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.guisvc.sitecfg.iface.SiteCfgService;
import de.heuboe.nrw.ims.SiteConfigServiceIface;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.system.CallStack;

public class SiteConfigServiceSoap implements SiteConfigServiceIface {
	
	private static final Logger LOGGER = Logger.getLogger( SiteConfigServiceSoap.class );
	
	private String svcUrl;
	private SiteCfgService siteCfgService;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param svcUrl	Service address
	 */
	public SiteConfigServiceSoap( String svcUrl ) {
		this.svcUrl = svcUrl;
	}
	
	public boolean connect()
	{
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( SiteCfgService.class );
			factory.setAddress( svcUrl );
			
			JAXBDataBinding db = new JAXBDataBinding();
			factory.setDataBinding( db );
				
			siteCfgService = (SiteCfgService)factory.create();
			siteCfgService.getSiteIds();
			
			LOGGER.info( "Successfully connected to SiteCfgService." );
			return true;
		} catch( Throwable ex ) {
			LOGGER.fatal( "Failed to connect to SiteCfgService" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			return false;
		}
	}
	
	@Override
    public List<String> getSiteIds() {
    	return siteCfgService.getSiteIds();
    }
	
	@Override
	public List<CfgQ> getAqs( List<String> aqIds ) throws SiteConfigError {
    	return siteCfgService.getAqs( aqIds );
	}
	
	@Override
    public CfgQs getSiteAqs( String siteId ) throws SiteConfigError {
    	return siteCfgService.getSiteItems( siteId ).getAqs();
    }
}
