package de.heuboe.datex2.location.service;

public class LocationFilter
{
	public static final String LOC_SUB_TYPE_DRIVERS = "DRIVERS";
	
	private String locSubType = null;

	public LocationFilter()
	{
	}
	
	public LocationFilter( String locSubType )
	{
		this.locSubType = locSubType;
	}
	
	public String getLocSubType()
	{
		return locSubType;
	}
	
	public void setLocSubType( String locSubType )
	{
		this.locSubType = locSubType;
	}
	
	@Override
	public String toString()
	{
		if( locSubType != null )
			return "LOCATION_SUB_TYPE: " + locSubType;
					
		return "<NONE>";
	}
}
