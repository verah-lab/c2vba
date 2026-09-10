package de.heuboe.datex2.location.service.server;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.heuboe.datex2.location.service.SiteType;
import de.heuboe.datex2.measure.D2MeasureDataSource;

public class DataSource
{
	private D2MeasureDataSource src;
	private SiteType siteType;
	private Set<String> subTypes = new TreeSet<String>(); 
	
	public DataSource( D2MeasureDataSource src, SiteType siteType, List<String> subTypes)
	{
		this.src = src;
		this.siteType = siteType;
		this.subTypes.addAll( subTypes );
	}
	
	public D2MeasureDataSource getSrc()
	{
		return src;
	}
	public void setSrc(D2MeasureDataSource src)
	{
		this.src = src;
	}
	public SiteType getSiteType()
	{
		return siteType;
	}
	public void setSiteType(SiteType siteType)
	{
		this.siteType = siteType;
	}
	public Set<String> getSubTypes()
	{
		return subTypes;
	}
	public void setSubTypes(List<String> subTypes)
	{
		this.subTypes.clear();
		this.subTypes.addAll( subTypes );
	}
	
	public void setEnumValues( Map< Integer, String > val2Name )
	{
		src.setEnumValues( val2Name );
	}
}
