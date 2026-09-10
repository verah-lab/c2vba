package de.heuboe.datex2.vms.service.table.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.util.JavaObject2JSON;
import eu.datex2.schema._2._2_0.GroupOfLocations;


public class D2VMSUnit 
{
	public static enum UnitType {
		AQ,
		WWW,
		DWISTA,
	}
	
	private Date time;
	private String d2Id;
	private String internalId;
	private UnitType unitType;
	private String name;
	private GroupOfLocations location = null;
	
	private List<D2VMSDisplay> displays = new ArrayList<D2VMSDisplay>();
	
	public D2VMSUnit()
	{
	}
	
	public D2VMSUnit( String name,
					  List<D2VMSDisplay> displays )
	{
		this.name = name;
		this.displays = displays;
	}
	
	public Date getTime() 
	{
		return this.time;
	}
	
	public void setTime(Date time) 
	{
		this.time = time;
	}

	public ObjectKey getObjectKey()
	{
		return new ObjectKey( ObjectKey.TYPE_AQ, internalId); 
	}
	
	public String getD2Id()
	{
		return d2Id;
	}

	public void setD2Id(String d2Id)
	{
		this.d2Id = d2Id;
	}

	public String getInternalId()
	{
		return internalId;
	}
	
	public void setInternalId( String internalId ) 
	{
		this.internalId = internalId;
	}

	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()        // NOSONAR
	{
		return name;
	}
	
	public void setDescription( String description )
	{
		this.name = description;
	}

	public void setName(String name) 
	{
		this.name = name;
	}
	
	public int getNumVMS()
	{
		return displays.size();
	}
	
	public void setNumVMS( int numVMS )
	{
		// JPA requirement
	}
	
	public void setDisplays( List<D2VMSDisplay> displays )
	{
		this.displays = displays;
	}

	public List<D2VMSDisplay> getDisplays()
	{
		return displays;
	}
	
	public void addDisplay( D2VMSDisplay display )
	{
		if( displays == null )
			displays = new ArrayList<D2VMSDisplay>();
		
		displays.add( display );
	}
	
	public GroupOfLocations getLocation()
	{
		return location;
	}
	
	public void setLocation(GroupOfLocations location)
	{
		this.location = location;
	}
	
	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public void setUnitType(UnitType unitType)
	{
		this.unitType = unitType;
	}
}
