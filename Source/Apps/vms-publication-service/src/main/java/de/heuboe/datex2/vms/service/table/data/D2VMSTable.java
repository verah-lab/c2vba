package de.heuboe.datex2.vms.service.table.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.annotation.Id;

import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType;
import de.heuboe.util.JavaObject2JSON;

public class D2VMSTable
{
	@Id
	private String id;
	private Date time;
	private String d2Id;
	private String d2Version;
	private boolean active;
	
	private List<D2VMSUnit> units = new ArrayList<>();
	
	private D2VMSTableConfig configuration;
	
	public void createId() {
		this.id = d2Id + "__" + d2Version;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTime() 
	{
		return this.time;
	}
	
	public void setTime(Date time) 
	{
		this.time = time;
	}
	
	public String getD2Id()
	{
		return d2Id;
	}

	public void setD2Id(String d2Id)
	{
		this.d2Id = d2Id;
	}

	public String getD2Version()
	{
		return d2Version;
	}

	public void setD2Version(String d2Version)
	{
		this.d2Version = d2Version;
	}

	public List<D2VMSUnit> getUnits()
	{
		return units;
	}

	public void setUnits(List<D2VMSUnit> units)
	{
		this.units = units;
	}
	
	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}
	
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	public Set<ObjectKey> getAllObjectKeys()
	{
		Set<ObjectKey> objKeys = new TreeSet<>();
		
		for( D2VMSUnit unit : units )
		{
			objKeys.add( unit.getObjectKey() );

			List<D2VMSDisplay> displays = unit.getDisplays();
			for( D2VMSDisplay display : displays )
			{
				objKeys.add( display.getObjectKey() );
				
				List<D2VMSDisplayPart> displayParts =display.getDisplayParts();
				if( displayParts != null )
				{
					for( D2VMSDisplayPart displayPart : displayParts )
					{
						objKeys.add( displayPart.getObjectKey() );
					}
				}
			}
		}
		
		return objKeys;
	}
	
	// Returns object with relevant data
	// (E.g. the WZG sub-objects of WWWs are discarded) 
	public Set<ObjectKey> getAllDataObjectKeys()
	{
		Set<ObjectKey> objKeys = new TreeSet<>();
		
		for( D2VMSUnit unit : units )
		{
			objKeys.add( unit.getObjectKey() );

			List<D2VMSDisplay> displays = unit.getDisplays();
			for( D2VMSDisplay display : displays )
			{
				DisplayType displayType = display.getDisplayType();
				if( display.getDisplayType() != DisplayType.DWISTA_TXT_GRP ) {
					objKeys.add( display.getObjectKey() );
				}
				
				List<D2VMSDisplayPart> displayParts = display.getDisplayParts();
				if( ( displayParts != null ) && ( displayType != DisplayType.WWW ) )
				{
					for( D2VMSDisplayPart displayPart : displayParts )
					{
						objKeys.add( displayPart.getObjectKey() );
					}
				}
			}
		}
		
		return objKeys;
	}

	public D2VMSTableConfig getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(D2VMSTableConfig configuration)
	{
		this.configuration = configuration;
	}
	
}
