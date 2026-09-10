package de.heuboe.datex2.location.service.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.math.NumberUtils;

import de.heuboe.datex2.base.data.D2Coordinate;
import de.heuboe.datex2.location.service.D2MeasurementSite;
import de.heuboe.datex2.location.service.SiteType;
import de.heuboe.datex2.location.service.D2MeasurementSite.D2SubSite;
import de.heuboe.datex2.location.service.server.Util.IdGenerator;
import de.heuboe.datex2.location.service.server.Util.SubIdGenerator;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import eu.datex2.schema._2._2_0.AlertCDirectionEnum;
import eu.datex2.schema._2._2_0.AlertCLinear;
import eu.datex2.schema._2._2_0.AlertCMethod2Point;
import eu.datex2.schema._2._2_0.AlertCMethod2PrimaryPointLocation;
import eu.datex2.schema._2._2_0.AlertCMethod4Linear;
import eu.datex2.schema._2._2_0.AlertCMethod4Point;
import eu.datex2.schema._2._2_0.AlertCMethod4PrimaryPointLocation;
import eu.datex2.schema._2._2_0.AlertCMethod4SecondaryPointLocation;
import eu.datex2.schema._2._2_0.AlertCPoint;
import eu.datex2.schema._2._2_0.ExternalReferencing;
import eu.datex2.schema._2._2_0.GroupOfLocations;
import eu.datex2.schema._2._2_0.ItineraryByIndexedLocations;
import eu.datex2.schema._2._2_0.Linear;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0.PointByCoordinates;
import eu.datex2.schema._2._2_0._LocationContainedInItinerary;

/**
 * 
 * @author peters
 * 
 * All measurement sites of one SiteType
 *
 */
public class Sites
{
	private IdGenerator 		locIdGen;
	private IdGenerator 		itemIdGen;
	private SubIdGenerator		itemIndexGen;
	
	private SiteType type;
	private Map< String, D2MeasurementSite> m_sites = new TreeMap< String, D2MeasurementSite>();
	private Map< String, D2MeasureLoc> m_locations = new TreeMap<>();;
	private List<D2MeasureItem> m_items = new ArrayList<D2MeasureItem>();
	
	
	private void setDir( D2MeasureLoc loc, AlertCDirectionEnum alcDir )
	{
		char dir ='B';
		switch( alcDir )
		{
			case POSITIVE:
				dir = 'P';
				break;
			case NEGATIVE:
				dir = 'N';
				break;
			case BOTH:
				dir = 'B';
				break;
			default:	
				dir = 'U';
				break;
		}
		loc.setRdsDirection( dir );
	}
	
	private void setPL( D2MeasureLoc loc, AlertCMethod4PrimaryPointLocation pl )
	{
		loc.setRdsLocCodePL( pl.getAlertCLocation().getSpecificLocation().intValue() );
		loc.setRdsPLDist( pl.getOffsetDistance().getOffsetDistance().intValue() );
	}
	
	private void setSL( D2MeasureLoc loc, AlertCMethod4SecondaryPointLocation sl )
	{
		loc.setRdsLocCodeSL( sl.getAlertCLocation().getSpecificLocation().intValue() );
		loc.setRdsSLDist( sl.getOffsetDistance().getOffsetDistance().intValue() );
	}
	
	
	public Sites( SiteType type, 
			      List<D2MeasurementSite> sites,
			      IdGenerator locIdGen, 
			      IdGenerator itemIdGen,
			      SubIdGenerator itemIndexGen,
			      boolean objectIdAsD2SiteId )
	{
		this.type = type;
		this.locIdGen = locIdGen;
		this.itemIdGen = itemIdGen;
		this.itemIndexGen = itemIndexGen;
		
		String extType = type.getExternalType();
		boolean isExtRefType =  ( extType != null ) && !extType.isEmpty();
		
		for( D2MeasurementSite site : sites )
		{
			m_sites.put( site.getObjId(), site );
			
			D2MeasureLoc loc = new D2MeasureLoc( this.locIdGen.getNextId() );
			
			if( objectIdAsD2SiteId ) {
				loc.setD2Id( site.getObjId() );
			} else {
				loc.setD2Id( "MS" + loc.getId() );
			}
			loc.setLocName( site.getName() );
			loc.setEquipment( site.getEquipment() );
			loc.setGeodynId( site.getGeoDynId() );
			
			if( isExtRefType )
			{
				ExternalReferencing er = site.getExternalReference();
				if( er != null )
				{
					loc.setD2ExtRefSystem( er.getExternalReferencingSystem() );
					loc.setD2ExtLocCode( er.getExternalLocationCode() );
				}
			}
			
			if( !isExtRefType )
			{
				GroupOfLocations alcLoc = site.getAlertCLocation();
				if( alcLoc != null )
				{
					if( alcLoc instanceof Point )
					{
						Point point = (Point)alcLoc;
						AlertCPoint alcPoint = point.getAlertCPoint();
						if( alcPoint != null )
						{
							if( alcPoint instanceof AlertCMethod4Point ) {
								AlertCMethod4Point alc4Point = (AlertCMethod4Point)alcPoint;
								String cc = alc4Point.getAlertCLocationCountryCode();
								String tn = alc4Point.getAlertCLocationTableNumber();
								String tv = alc4Point.getAlertCLocationTableVersion();
								
								loc.setRdsLocTblRef( cc + tn );
								loc.setRdsLocTblVer( tv );
								
								AlertCDirectionEnum alcDir = alc4Point.getAlertCDirection().getAlertCDirectionCoded();
								setDir( loc, alcDir );
								
								AlertCMethod4PrimaryPointLocation pl = alc4Point.getAlertCMethod4PrimaryPointLocation();
								setPL( loc, pl );
							} else if( alcPoint instanceof AlertCMethod2Point ) {
								AlertCMethod2Point alc2Point = (AlertCMethod2Point)alcPoint;
								String cc = alc2Point.getAlertCLocationCountryCode();
								String tn = alc2Point.getAlertCLocationTableNumber();
								String tv = alc2Point.getAlertCLocationTableVersion();
								
								loc.setRdsLocTblRef( cc + tn );
								loc.setRdsLocTblVer( tv );
								
								AlertCDirectionEnum alcDir = alc2Point.getAlertCDirection().getAlertCDirectionCoded();
								setDir( loc, alcDir );
								
								AlertCMethod2PrimaryPointLocation pl = alc2Point.getAlertCMethod2PrimaryPointLocation();
								loc.setRdsLocCodePL( pl.getAlertCLocation().getSpecificLocation().intValue() );
							}
						}
					}
					else if( alcLoc instanceof Linear )
					{
						Linear linear = (Linear)alcLoc;
						AlertCLinear alcLinear = linear.getAlertCLinear();
						if( alcLinear != null )
						{
							if( alcLinear instanceof AlertCMethod4Linear )
							{
								AlertCMethod4Linear alc4Linear = (AlertCMethod4Linear)alcLinear;
								String cc = alc4Linear.getAlertCLocationCountryCode();
								String tn = alc4Linear.getAlertCLocationTableNumber();
								String tv = alc4Linear.getAlertCLocationTableVersion();
								
								loc.setRdsLocTblRef( cc + tn );
								loc.setRdsLocTblVer( tv );
								
								AlertCDirectionEnum alcDir = alc4Linear.getAlertCDirection().getAlertCDirectionCoded();
								setDir( loc, alcDir );
								
								AlertCMethod4PrimaryPointLocation pl = alc4Linear.getAlertCMethod4PrimaryPointLocation();
								setPL( loc, pl );
	
								AlertCMethod4SecondaryPointLocation sl = alc4Linear.getAlertCMethod4SecondaryPointLocation();
								setSL( loc, sl );
							}
						}
					}
				}
			}
			
			GroupOfLocations coorLoc = site.getCoordinateLocation();
			if( coorLoc != null )
			{
				if( coorLoc instanceof ItineraryByIndexedLocations )
				{
					ItineraryByIndexedLocations ibils = (ItineraryByIndexedLocations)coorLoc;
					List<_LocationContainedInItinerary> lciis = ibils.getLocationContainedInItinerary();
					if( lciis != null )
					{
						if( lciis.size() > 0 )
						{
							_LocationContainedInItinerary lcii = lciis.get(0);
							Location l = lcii.getLocation();
							
							if( l instanceof Point )
							{
								Point point = (Point)l;
								PointByCoordinates pbcs = point.getPointByCoordinates();
								if( pbcs != null )
								{
									loc.setStartCoordX( pbcs.getPointCoordinates().getLongitude() );
									loc.setStartCoordY( pbcs.getPointCoordinates().getLatitude() );
									if( pbcs.getBearing() != null ) {
										loc.setBearing( pbcs.getBearing().intValue() );
									}
								}
							}
						}
						
						if( !isExtRefType )
						{
							if( lciis.size() > 1 )
							{
								_LocationContainedInItinerary lcii = lciis.get( lciis.size() - 1 );
								Location l = lcii.getLocation();
								
								if( l instanceof Point )
								{
									Point point = (Point)l;
									PointByCoordinates pbcs = point.getPointByCoordinates();
									if( pbcs != null )
									{
										loc.setEndCoordX( pbcs.getPointCoordinates().getLongitude() );
										loc.setEndCoordY( pbcs.getPointCoordinates().getLatitude() );
									}
								}
								
							}
						}
						
						if( !isExtRefType )
						{
							List<D2Coordinate> coors = new ArrayList<>();
							for( _LocationContainedInItinerary lcii : lciis )
							{
								Location l = lcii.getLocation();
								
								if( l instanceof Point )
								{
									Point point = (Point)l;
									PointByCoordinates pbcs = point.getPointByCoordinates();
									if( pbcs != null )
									{
										D2Coordinate coor = new D2Coordinate();
										
										coor.setX( pbcs.getPointCoordinates().getLongitude() );
										coor.setY( pbcs.getPointCoordinates().getLatitude() );
									
										coors.add( coor );
									}
								}
							}
							
							loc.setCoordinates( coors );
						}
					}
				}
			}
			
		   	m_locations.put( site.getObjId(), loc );
		}
	}
	
	public SiteType getObjType()
	{
		return type;
	}
	
	private boolean d2SubTypeMatch( String d2LaneName, Set<String> subTypes ) {
		boolean match = subTypes.contains( d2LaneName );
		if( !match ) {
			if( d2LaneName.matches( "lane\\d" ) ) {
				String shortName = d2LaneName.substring( d2LaneName.length() - 1 ); 
				match = subTypes.contains( shortName );
			}
		}
		
		return match;
	}
	
	// Adds items for DataSource
	public void addDataSource( DataSource src )
	{
		Set<String> subTypes = src.getSubTypes();
		for( D2MeasurementSite site : m_sites.values() )
		{
			D2MeasureLoc loc = m_locations.get( site.getObjId() );
			
			List<D2SubSite> subSites = site.getSubSites();
			for( D2SubSite subSite :  subSites )
			{
				String lane = subSite.getLane().value();
				if( ( subTypes.size() == 0 ) || d2SubTypeMatch( lane, subTypes ) )
				{
					D2MeasureItem item = new D2MeasureItem( itemIdGen.getNextId() );
					
					item.setLocId( loc.getId() );
					item.setIndex( itemIndexGen.getNextId( loc.getId() ) );
					item.setPeriod( subSite.getPeriod() );
					
					item.setDkRef( src.getSrc().getId() );
					item.setDkKey( subSite.getId() );
					if( NumberUtils.isDigits( subSite.getId() )) {
						item.setDkId( Integer.parseInt( subSite.getId() ) );
					}
					
					item.setName( subSite.getName() );
					item.setLane( subSite.getLane().value() );
					
					item.setCarriageway( site.getCarriageway().value() );

					m_items.add( item );
					
				}
			}
		}
	}
	
	public Collection<D2MeasurementSite> getMeasurementSites()
	{
		return m_sites.values();
	}
	
	public Collection<D2MeasureLoc> getLocations()
	{
		return m_locations.values();
	}
	
	public Collection<D2MeasureItem> getItems()
	{
		return m_items;
	}
}
