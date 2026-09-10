package de.heuboe.mst.config.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;


/**
 * 
 * The internal representation of complete MST data
 * 
 * @author peters
 *
 */
public class MstContent
{
	private String 		   	mst;
	private String         	d2Version;
	private String         	defVersion;
	private String         	version;
	
	private String			description;
	
	private List<D2MeasureDataSource>   sources = new ArrayList<>();	
	private List<D2MeasureLoc> 			locations = new ArrayList<>();
	private List<D2MeasureItem>     	items = new ArrayList<>();
	
	public MstContent( String d2Version, String mst, String defVersion, String version )
	{
		this.mst			= mst;
		this.d2Version		= d2Version;
		this.defVersion		= defVersion;
		this.version		= version;
	}
	
	public String getMst()
	{
		return mst;
	}
	public void setMst(String mst)
	{
		this.mst = mst;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
	public String getD2Version()
	{
		return d2Version;
	}
	public void setD2Version(String d2Version)
	{
		this.d2Version = d2Version;
	}
	public String getDefVersion()
	{
		return defVersion;
	}
	public void setDefVersion(String defVersion)
	{
		this.defVersion = defVersion;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	
	public List<D2MeasureDataSource> getSources()
	{
		return sources;
	}
	public void setSources(List<D2MeasureDataSource> sources)
	{
		this.sources = sources;
	}
	
	public List<D2MeasureLoc> getLocations()
	{
		return locations;
	}
	
	public void addLocations( Collection<D2MeasureLoc> locations )
	{
		this.locations.addAll( locations );
	}
	
	public List<D2MeasureItem> getItems()
	{
		return items;
	}
	
	public void addItems( Collection<D2MeasureItem> items )
	{
		this.items.addAll( items );
	}
	
	public void setLocations(List<D2MeasureLoc> locations) {
		this.locations = locations;
	}

	public void setItems(List<D2MeasureItem> items) {
		this.items = items;
	}

}

