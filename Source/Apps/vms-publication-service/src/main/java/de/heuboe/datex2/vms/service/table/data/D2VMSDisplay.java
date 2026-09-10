package de.heuboe.datex2.vms.service.table.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.util.JavaObject2JSON;
import eu.datex2.schema._2._2_0.Location;

/**
 * 
 * Pendant zum DAtex II Vms (oder VMSRecord)
 * 
 * 
 * @author peters
 *
 */
public class D2VMSDisplay
{
	public static enum DisplayType
	{
		WZG,				
		AQ,					// Vms mit einem Pictogram 
		WWW,				// Vms mit einem Pictogram
		AQWithWZGs,			// Zu jedem Wzg eigenes Piktogramm 
		WZGGroup,			// Zu jedem Wzg eigenes Piktogramm
		DWISTA,		
		DWISTA_TXT_WZG,		
		DWISTA_TXT_GRP
	}
	
	public static enum FunctionalType {
		OVERVIEW,
		FUNCTIONAL_DETAIL,
		DETAIL,
		OTHER;
	}
	
	public static enum AddressedTrafficFlow {
		ALL,
		MAIN_CARRIAGEWAY_FOLLOWING_TRAFFIC,
		RIGHT_DIRECTION_FOLLOWING_TRAFFIC,
		LEFT_DIRECTION_FOLLOWING_TRAFFIC,
		OTHER,
	}
	
	
	public static enum DisplayPosition {
		
		LEFT(1),
		RIGHT(2),
		TOP(3),
		BOTTOM(4),
		TOP_LEFT(5),
		TOP_RIGHT(6),
		BOTTOM_LEFT(7),
		BOTTOM_RIGHT(8);
		
		DisplayPosition( int v )
		{
			this.value = v;
		}
		
		public String toString()
		{
			switch( value )
			{
				case 2:
					return "RIGHT";
				case 3:
					return "TOP";
				case 4:
					return "RIGHT";
				case 5:
					return "TOP_LEFT";
				case 6:
					return "TOP_RIGHT";
				case 7:
					return "BOTTOM_LEFT";
				case 8:
					return "BOTTOM_RIGHT";
				case 1:
				default:	
					return "LEFT";
			}
		}
		
		private int value;
	}	
	
	
	private Date time;
	private String description;
	private String internalId;
	private int de = -1;
	private int vmsIndex;
	
	private FunctionalType functionalType = FunctionalType.DETAIL;
	private AddressedTrafficFlow addressedTrafficFlow = AddressedTrafficFlow.ALL;
	private String addressedDestinations;
	
	private DisplayType displayType;
	
	private DisplayPosition displayPosition;
	
	private Location location;
	private List<D2VMSLocationPart> locationParts = new ArrayList<>();
	
	private List<D2VMSDisplayPart> displayParts = new ArrayList<>();
	
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
		if( displayType == DisplayType.WZG )
			return new ObjectKey( ObjectKey.TYPE_WZG, internalId); 
		else if( displayType == DisplayType.DWISTA )
			return new ObjectKey( ObjectKey.TYPE_AQ, internalId); 
		else
			return new ObjectKey( ObjectKey.TYPE_AQ, internalId); 
	}
	
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public String getInternalId()
	{
		return internalId;
	}
	public void setInternalId(String internalId)
	{
		this.internalId = internalId;
	}
	
	public DisplayType getDisplayType()
	{
		return displayType;
	}
	
	public void setDisplayType(DisplayType type)
	{
		this.displayType = type;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public void setLocation(Location location)
	{
		this.location = location;
	}

	public void setDisplayParts( List<D2VMSDisplayPart> displayParts )
	{
		this.displayParts = displayParts;
	}
	
	public List<D2VMSDisplayPart> getDisplayParts()
	{
		return displayParts;
	}
	
	public void setVmsIndex( int vmsIndex )
	{
		this.vmsIndex = vmsIndex;
	}
	
	public int getVmsIndex()
	{
		return vmsIndex;
	}
	
	public List<D2VMSLocationPart> getLocationParts()
	{
		return locationParts;
	}

	public void setLocationParts(List<D2VMSLocationPart> locationParts)
	{
		this.locationParts = locationParts;
	}

	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}

	public int getDe()
	{
		return de;
	}

	public void setDe(int de)
	{
		this.de = de;
	}
	
	public DisplayPosition getDisplayPosition()
	{
		return displayPosition;
	}

	public void setDisplayPosition(DisplayPosition displayPosition)
	{
		this.displayPosition = displayPosition;
	}

	public FunctionalType getFunctionalType()
	{
		return functionalType;
	}

	public void setFunctionalType(FunctionalType functionalType)
	{
		this.functionalType = functionalType;
	}

	public AddressedTrafficFlow getAddressedTrafficFlow()
	{
		return addressedTrafficFlow;
	}

	public void setAddressedTrafficFlow(AddressedTrafficFlow addressedTrafficFlow)
	{
		this.addressedTrafficFlow = addressedTrafficFlow;
	}

	public String getAddressedDestinations()
	{
		return addressedDestinations;
	}

	public void setAddressedDestinations( String addressedDestinations )
	{
		this.addressedDestinations = addressedDestinations;
	}
}
