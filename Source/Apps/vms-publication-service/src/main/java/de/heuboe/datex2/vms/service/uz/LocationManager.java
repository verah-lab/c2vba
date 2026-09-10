package de.heuboe.datex2.vms.service.uz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.datex2.vms.service.D2Location;
import de.heuboe.datex2.vms.service.VMSServiceException;
import eu.datex2.schema._2._2_0.AffectedCarriagewayAndLanes;
import eu.datex2.schema._2._2_0.CarriagewayEnum;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0.SupplementaryPositionalDescription;


/**
 * 
 * Creates DATEX-II locations for infrastructure objects
 * 
 * @author peters
 *
 */
public abstract class LocationManager
{
	protected boolean useCarriagewayAttr;
	protected Map<String,CarriagewayEnum> objId2Carriageway = new HashMap<>();
	
	/**
	 * 
	 * Initialisation
	 * 
	 */
	public abstract void init();
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param useCarriagewayAttr	true: create DATEX-II location carriageway attribute
	 */
	protected LocationManager( boolean useCarriagewayAttr ) {      
		this.useCarriagewayAttr = useCarriagewayAttr;
	}
	
	/**
	 * 
	 * Creates DATEX-II locations for infrastructure objects
	 * 
	 * @param objType				Object type		
	 * @param objId					Object ID
	 * @param addTmcLocation		true: add TMC location
	 * @param addOpenLRLocation		true: add OpenLR location
	 * @return						DATEX-II locations
	 * @throws VMSServiceException	Error	
	 */
	public abstract Location getLocation( String objType, 
			              				  String objId, 
			              				  boolean addTmcLocation, 
			              				  boolean addOpenLRLocation ) throws VMSServiceException;
	
	/**
	 * 
	 * Creates DATEX-II locations for infrastructure objects
	 * 
	 * @param objType				Object type		
	 * @param objId					Object ID
	 * @param addTmcLocation		true: add TMC location
	 * @param addOpenLRLocation		true: add OpenLR location
	 * @return						DATEX-II locations
	 * @throws VMSServiceException	Error	
	 */
	public abstract D2Location getD2Location( String objType, 
			              				  	  String objId, 
			              				  	  boolean addTmcLocation, 
			              				  	  boolean addOpenLRLocation ) throws VMSServiceException;

	
	/**
	 * 
	 * Creates DATEX-II locations for infrastructure objects
	 * 
	 * @param objType				Object type		
	 * @param objIds				Object IDs
	 * @param addTmcLocation		true: add TMC location
	 * @param addOpenLRLocation		true: add OpenLR location
	 * @return						DATEX-II locations
	 * @throws VMSServiceException	Error	
	 */
	public abstract List<D2Location> getLocations( String objType, 
			                       				   Set<String> objIds, 
			                       				   boolean addTmcLocation, 
			                       				   boolean addOpenLRLocation ) throws VMSServiceException; 
	
	/**
	 * 
	 * Sets DATEX-II carriageway and lane attributes
	 * 
	 * @param objId		Object ID
	 * @param location	DATEX-II location object
	 * @param lanes		Lanes
	 * @param ld		Description of location site 
	 */
	public void setWzgLanes( String objId, Location location, List<LaneEnum> lanes, LocationDescriptorEnum ld )
	{
		Point pt = (Point)location;
		SupplementaryPositionalDescription spd = new SupplementaryPositionalDescription();
		if( ld != null )
		{
			spd.getLocationDescriptor().add( ld );
		}
		
		CarriagewayEnum cw = null;
		if( useCarriagewayAttr ) {
			cw = objId2Carriageway.get( objId );
		}
		
		AffectedCarriagewayAndLanes acwals = new AffectedCarriagewayAndLanes();
		
/*		
		if( location instanceof Point )  
		{
			if( (lanes != null ) &&        // NOSONAR
				( lanes.size() == 1 ) && 
				( cw == null ) && 
				( lanes.get(0) == LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY ) 
			  )
			{
				acwals.getLane().add( LaneEnum.LANE_1 );
				acwals.setCarriageway( CarriagewayEnum.PARALLEL_CARRIAGEWAY );
				spd.getAffectedCarriagewayAndLanes().add( acwals );
				pt.setSupplementaryPositionalDescription( spd );
				
				return;
			}
		}
*/		
		
		if( cw != null ) {
			acwals.setCarriageway( cw );
		} else {
			acwals.setCarriageway( CarriagewayEnum.MAIN_CARRIAGEWAY );
		}
		
		if( lanes != null )
		{
			acwals.getLane().addAll( lanes );
		}
		
		spd.getAffectedCarriagewayAndLanes().add( acwals );
		pt.setSupplementaryPositionalDescription( spd );
	}
	
}
