package de.heuboe.srb.datex2.srp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.heuboe.datex2.wls.AlertCLocalizerD2Profile.TmcDirectionResolver;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.tmc.TmcLocation;
import de.heuboe.wls.data.wls.Direction;
import de.heuboe.wls.data.wls.GetLocationsRequest;
import de.heuboe.wls.data.wls.GetLocationsResponse;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.iface.WebLocationServer;

public class SrpTmcDirectionResolver implements TmcDirectionResolver
{
	private static Logger LOGGER = Logger.getLogger( SrpTmcDirectionResolver.class );
	public static String ALC_DIR_POS = "P";
	public static String ALC_DIR_NEG = "N";
	
	private WebLocationServer wls;
	
	private static class AlertCLocation
	{
		public Integer locCode = null;
		public String name = null;
		public Integer posOff = null;
		public Integer negOff = null;
	}
	
	private Map< Integer, AlertCLocation > rdsCode2AlcLoc = new TreeMap< Integer, AlertCLocation >();
	
	public SrpTmcDirectionResolver( WebLocationServer wls ) throws Fault
	{
		this.wls = wls;
		
		readAlertCLocations();
		
		LOGGER.info( "Verbindung zum WLS hergestellt." );
	}
	
	public boolean validLocationCode( int locCode ) {
		return ( rdsCode2AlcLoc.get( locCode ) != null );
	}
	
	public String getLocationName( int locCode ) {
		
		AlertCLocation alcLoc = rdsCode2AlcLoc.get( locCode );
		if( alcLoc != null ) {
			return alcLoc.name;
		}
		
		return null;
	}
	
	public List<TmcLocation> readTmcLocations() throws Fault {
		
		GetLocationsRequest getLocationsRequest = new GetLocationsRequest();
		getLocationsRequest.setLocationType( TmcLocation.class.getName() );
		GetLocationsResponse getLocationsResponse = wls.getLocations(getLocationsRequest);
		List<Location> locations = getLocationsResponse.getLocations();
		
		List<TmcLocation> tmcLocations = new ArrayList<>();
		for( Location location : locations ) {
			tmcLocations.add( (TmcLocation)location );
		}
		
		return tmcLocations;
	}
	
	
	private void readAlertCLocations() throws Fault
	{
		List<TmcLocation> tmcLocations = readTmcLocations(); 
        
        for( TmcLocation tmcLocation : tmcLocations )
        {
        	String locType = tmcLocation.getLocationType();
        	if( locType.startsWith( "P" ) ) {
	            AlertCLocation alcLoc = new AlertCLocation();
	            
	            int rdsCode = tmcLocation.getLocationCode();
	            
	            alcLoc.locCode = rdsCode;
	            if( tmcLocation.getPosOffset() != null )
	            	alcLoc.posOff = tmcLocation.getPosOffset();
	            
	            alcLoc.name = tmcLocation.getFirstName();
	            
	            if( tmcLocation.getNegOffset() != null )
	            	alcLoc.negOff = tmcLocation.getNegOffset();
	            
	            rdsCode2AlcLoc.put( rdsCode, alcLoc ); 
        	}
        }
	}
	
	@Override
	public Direction getDirection( int pLoc, int sLoc ) {
		
		LOGGER.debug( "getAlertCDirection()" );

		int posHops = -1;
		int negHops = -1;
		
		LOGGER.debug( "positive direction ..." );
		AlertCLocation alcLoc = rdsCode2AlcLoc.get( sLoc );
		posHops = 0;
		while( true )
		{
			if( alcLoc == null )
			{
				posHops = -1;
				break;
			}
			
			LOGGER.debug( "" + alcLoc.locCode );
			
			if( alcLoc.locCode == pLoc )
				break;
			
			Integer next = alcLoc.posOff;
			if( next == null )
			{
				posHops = -1;
				break;
			}
			
			alcLoc = rdsCode2AlcLoc.get( next );
			posHops++;
		}
		
		LOGGER.debug( "negative direction ..." );
		alcLoc = rdsCode2AlcLoc.get( sLoc );
		negHops = 0;
		while( true )
		{
			if( alcLoc == null )
			{
				negHops = -1;
				break;
			}
			
			LOGGER.debug( "" + alcLoc.locCode );
		
			if( alcLoc.locCode == pLoc )
				break;
			
			Integer next = alcLoc.negOff;
			if( next == null )
			{
				negHops = -1;
				break;
			}
			
			alcLoc = rdsCode2AlcLoc.get( next );
			negHops++;
		}
		
		if( negHops == -1 )
		{
			if( posHops == -1 )
				return null;
			
			return Direction.POSITIVE;
		}
		
		if(  posHops == -1 )
			return Direction.NEGATIVE;
		
		return ( posHops < negHops ) ? Direction.NEGATIVE : Direction.POSITIVE;
		
	}
	
}
