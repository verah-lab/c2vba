package de.heuboe.datex2.location.service;

import java.util.List;

import eu.datex2.schema._2._2_0.CarriagewayEnum;
import eu.datex2.schema._2._2_0.ExternalReferencing;
import eu.datex2.schema._2._2_0.GroupOfLocations;
import eu.datex2.schema._2._2_0.LaneEnum;


public class D2MeasurementSite 
{
	// Corresponds to Datex2 'MeasurementSpecificCharacteristics'
	
	public static class D2SubSite
	{
		// Mit dieser ID wird eine D2SubSite in Attribut-Datenarten referenziert 
		private String id;
		
		private String name = "";
		private LaneEnum lane = null;
		
		// Datenerfassungszyklus
		private int period = -1;

		
		public String getId()
		{
			return id;
		}
		public void setId(String id)
		{
			this.id = id;
		}
		
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
		public LaneEnum getLane()
		{
			return lane;
		}
		public void setLane(LaneEnum lane)
		{
			this.lane = lane;
		}
		public int getPeriod()
		{
			return period;
		}
		public void setPeriod(int period)
		{
			this.period = period;
		}
	}
	
	private String objId = "";
	private String name = "";
	private int geoDynId = -1;
	private CarriagewayEnum carriageway;
	private String equipment = "";
	private GroupOfLocations alertCLocation;
	private GroupOfLocations openLRLocation;
	private GroupOfLocations coordinateLocation;
	private ExternalReferencing externalReference;
	private List<D2SubSite> subSites;

	public D2MeasurementSite()
	{
	}

	public D2MeasurementSite( String objId, 
							  String name,
							  int geoDynId,
							  CarriagewayEnum carriageway,
							  String equipment,
			                  GroupOfLocations alertCLocation,
			                  GroupOfLocations coordinateLocation,
			                  ExternalReferencing externalReference,
			                  List<D2SubSite> subSites )
	{
		super();
		this.objId = objId;
		this.name = name;
		this.geoDynId = geoDynId;
		this.carriageway = carriageway;
		this.equipment = equipment;
		this.alertCLocation = alertCLocation;
		this.coordinateLocation = coordinateLocation;
		this.externalReference = externalReference;
		this.subSites = subSites;
	}
	
	public String getObjId()
	{
		return objId;
	}

	public void setObjId(String objId)
	{
		this.objId = objId;
	}

	public String getName()
	{
		return name;
	}
	
	public void Name(String name) 
	{
		this.name = name;
	}
	
	public void setEquipment(String equipment)
	{
		this.equipment = equipment;
	}

	public void setName(String name)
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

	public ExternalReferencing getExternalReference()
	{
		return externalReference;
	}

	public void setExternalReference(ExternalReferencing externalReference)
	{
		this.externalReference = externalReference;
	}

	public List<D2SubSite> getSubSites()
	{
		return subSites;
	}

	public void setSubSites(List<D2SubSite> subSites)
	{
		this.subSites = subSites;
	}
	public CarriagewayEnum getCarriageway()
	{
		return carriageway;
	}
	public void setCarriageway(CarriagewayEnum carriageway)
	{
		this.carriageway = carriageway;
	}
	public String getEquipment()
	{
		return equipment;
	}

	public int getGeoDynId()
	{
		return geoDynId;
	}

	public void setGeoDynId(int geoDynId)
	{
		this.geoDynId = geoDynId;
	}
	
	public GroupOfLocations getOpenLRLocation() {
		return openLRLocation;
	}

	public void setOpenLRLocation(GroupOfLocations openLRLocation) {
		this.openLRLocation = openLRLocation;
	}


}
