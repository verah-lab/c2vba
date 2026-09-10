package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;


/**
 * 
 * ConfigService implementation of ConfigManager
 * Provides only SBAs
 * 
 * @author peters
 *
 */
public class ConfigManagerSBA extends ConfigManager 
{
	private static final Logger LOGGER = Logger.getLogger( ConfigManagerSBA.class );
	
	private SiteConfigService siteCfgSvc;
	private ConfigManagerSBANonSite cfgManSBA;
	
	public ConfigManagerSBA( SiteConfigService siteCfgSvc, ConfigManagerSBANonSite cfgManSBA ) {
		this.siteCfgSvc = siteCfgSvc;
		this.cfgManSBA = cfgManSBA;
	}
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration,     // NOSONAR
										  LocationManager locationManager) throws VMSServiceException
	{
		LOGGER.info( "ConfigManagerSitesAndSBA: getD2VMSUnits()" );
		
		if( isSba( configuration ) ) {
			List<D2VMSUnit> units = new ArrayList<>();
			units.addAll( siteCfgSvc.getD2VMSUnits( configuration, locationManager ) );
			units.addAll( cfgManSBA.getD2VMSUnits( configuration, locationManager) );
			return units;
		} else {
	    		return siteCfgSvc.getD2VMSUnits( configuration, locationManager );
		}
	}
 
	private boolean isSba( VMSTableConfiguration configuration ) {
		List<String> sbaIds = configuration.getSbaIds();
		if( ( sbaIds == null ) || sbaIds.isEmpty() ) {
			return true;
		}
		
		return sbaIds.get(0).startsWith( "BY-SBA" );
	}
}
