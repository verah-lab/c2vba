package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType;
import de.heuboe.log.Logger;
import de.heuboe.zst.ZstError;
import de.heuboe.zst.ims.data.IsAq;
import de.heuboe.zst.ims.data.IsSite;
import de.heuboe.zst.ims.data.IsWzg;
import de.heuboe.zst.ims.data.IsAq.Compound;
import de.heuboe.zst.ims.iface.ImageService;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;

public class ConfigManagerIS extends ConfigManager
{
	private static final Logger LOGGER = Logger.getLogger( ImageManager.class );
	public static final int CLIENT_ID = 1;
	
	private ImageService imageService;
	
	public ConfigManagerIS( ImageService imageService ) {
		this.imageService = imageService;
	}

	private List<IsSite> getInfra()
			throws VMSServiceException
	{
		try
		{
			return imageService.getSites( CLIENT_ID, new ArrayList<>() );
		}
		catch( ZstError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE,
										   "ImageService.getSites() failed !", ex ); 
		}
	}
	
	@Override
	public synchronized List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration filter, 
													   LocationManager locationManager )
			throws VMSServiceException
	{
		LOGGER.info( "WS: getD2VMSUnits()" );
		
		List<D2VMSUnit> units = new ArrayList<D2VMSUnit>();
	
		List<IsSite> sites = getInfra();		
		
		Set<String> filterSites = null;
		List<String> filterSiteList = filter.getSbaIds();
		if( ( filterSiteList != null ) && !filterSiteList.isEmpty() )
		{
			filterSites = new HashSet<String>( filterSiteList );
		}
		
		locationManager.init();
		
		for( IsSite site : sites )
		{
			if( ( filterSites == null ) || ( filterSites.contains( site.getId() )) )
			{
				List<IsAq> aqs = site.getAqs( true );
				
				LOGGER.info( "" );
				LOGGER.info( "Site <" + site.getId() + ">:" );
				LOGGER.info( "# AQ: " + aqs.size() );
				
				int siteUnits = 0;
				for( IsAq aq : aqs )
				{
					D2VMSUnit unit = new D2VMSUnit();
					String aqId = aq.getId();
					
					unit.setInternalId( aqId );
					
					String locAQId = aq.getId();
					if( aq.getCompound() == Compound.ISA )
					{
						List<String> memberIds = aq.getMemberIds();
						if( ( memberIds != null ) && !memberIds.isEmpty() )
						{
							locAQId = memberIds.get(0);
						}
					}
					
					if( ConfigService.isWWW( aq.getType() ) )
					{
						D2VMSDisplay display = new D2VMSDisplay();
						
						display.setDisplayType( DisplayType.WWW );
						display.setDescription( aqId );
						display.setInternalId( aqId );
						
						Location location = locationManager.getLocation( Util.CFG_AQ_TYPE, 
																		 locAQId,
																		 filter.isAddTmcLocations(),
																		 filter.isAddOpenLRLocations() );
						
						if( location == null ) 
						{
							LOGGER.error( "No location for AQ with ID <" + aq.getId() + ">" );
							continue;
						}
		
						display.setLocation( location );
						
						for( IsWzg wzg : aq.getWzgs() )
						{
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( wzg.getId() );
							displayPart.setDisplayPartType( ConfigService.getDisplayPartType( wzg.getDisplayType() ) );
							display.getDisplayParts().add( displayPart );
						}
						
						unit.addDisplay( display );
					}
					else
					{
						
						for( IsWzg wzg : aq.getWzgs() )
						{
							D2VMSDisplay display = new D2VMSDisplay();
							
							String wzgId = wzg.getId();
							display.setDescription( wzgId );
							display.setInternalId( wzgId );
							display.setDisplayType( DisplayType.WZG ); 
							
							Location location = locationManager.getLocation( Util.CFG_WZG_TYPE, locAQId, 
																			 filter.isAddTmcLocations(),
																			 filter.isAddOpenLRLocations() );
							
							if( location == null ) 
							{
								LOGGER.error( "No location for WZG with ID <" + wzg.getId() + ">" );
								continue;
							}
							
							List<LaneEnum> lanes = ConfigService.getLanes( wzg.getLanePos() );
							LocationDescriptorEnum ld = ConfigService.getLocationDescriptor( wzg.getLanePos() );
							
							if( ( lanes == null || lanes.isEmpty() ) && ld == null ) 
							{
								LOGGER.error( "No lane localization for WZG with ID <" + wzg.getId() + ">" );
								continue;
							}
							locationManager.setWzgLanes( aqId, location, lanes, ld );
							
							display.setLocation( location );
							
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( wzg.getId() );
							displayPart.setDisplayPartType( ConfigService.getDisplayPartType( wzg.getWzgType() ) );
							display.getDisplayParts().add( displayPart );
							
							unit.addDisplay( display );
						}
					}
			
					if( unit.getDisplays().isEmpty() ) 
					{
						LOGGER.error( "No displays for unit with ID <" + unit.getInternalId() + ">" );
					} else {
						siteUnits++;
						units.add( unit );
					}
				}
				
				LOGGER.info( "Site <" + site.getId() + ">:" );
				LOGGER.info( "# Units: " + siteUnits );
			}
		}
		
		return units;
	}
}
