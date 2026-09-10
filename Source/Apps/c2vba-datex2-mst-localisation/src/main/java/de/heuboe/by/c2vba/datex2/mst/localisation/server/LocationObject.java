package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import eu.datex2.schema._2._2_0.ExternalReferencing;
import eu.datex2.schema._2._2_0.GroupOfLocations;

/**
 * 
 *
 * Location object
 * 
 * @author peters
 * 
 */
public class LocationObject
{
	private String type;
	private String id;
	private String name;
	private int geoDynId;
	private String carriageway;
	
	private GroupOfLocations alertCLocation = null;
	private GroupOfLocations openLRLocation = null;
	private GroupOfLocations coordinateLocation = null;
	private ExternalReferencing externalReference = null;

	/**
	 * 
	 * Checks non-emptiness
	 * 
	 * @return true: has valid ID 
	 */
	public boolean valid()
	{
		return ( id != null ) && !id.isEmpty();
	}
	
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getId()
	{
		return id;
	}
	public void setId( String id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}
	public void setName( String name )
	{
		this.name = name;
	}
	
	public GroupOfLocations getAlertCLocation()
	{
		return alertCLocation;
	}
	public void setAlertCLocation(GroupOfLocations alertCLocation)
	{
		this.alertCLocation = alertCLocation;
	}
	public GroupOfLocations getCoordinateLocation()
	{
		return coordinateLocation;
	}
	public void setCoordinateLocation(GroupOfLocations coordinateLocation)
	{
		this.coordinateLocation = coordinateLocation;
	}

	public GroupOfLocations getOpenLRLocation() {
		return openLRLocation;
	}

	public void setOpenLRLocation(GroupOfLocations openLRLocation) {
		this.openLRLocation = openLRLocation;
	}
	
	public ExternalReferencing getExternalReference()
	{
		return externalReference;
	}
	public void setExternalReference(ExternalReferencing externalReference)
	{
		this.externalReference = externalReference;
	}

    public int getGeoDynId() {
        return geoDynId;
    }

    public void setGeoDynId(int geoDynId) {
        this.geoDynId = geoDynId;
    }

    public String getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(String carriageway) {
        this.carriageway = carriageway;
    }
}
