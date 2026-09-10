package de.heuboe.datex2.vms.service;

import eu.datex2.schema._2._2_0.Location;

public class D2Location
{
	private ObjectKey objKey;
	private String description;
	private Location location;
	
	public D2Location()
	{
	}

	public D2Location( ObjectKey objKey, Location location )
	{
		this.objKey = objKey;
		this.location = location;
	}

	public ObjectKey getObjKey()
	{
		return objKey;
	}
	public void setObjKey(ObjectKey objKey)
	{
		this.objKey = objKey;
	}
	public Location getLocation()
	{
		return location;
	}
	public void setLocation(Location location)
	{
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
