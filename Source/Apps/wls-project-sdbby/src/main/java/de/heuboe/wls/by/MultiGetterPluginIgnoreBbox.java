package de.heuboe.wls.by;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.Parameter;
import de.heuboe.wls.data.wls.ParameterBBox;
import de.heuboe.wls.data.wls.ParameterBBoxWithSRID;
import de.heuboe.wls.data.wls.ParameterFlags;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.utils.MultiGetterPlugin;
import de.heuboe.wls.utils.WlsUtils;


/**
 * 
 * MultiGetterPlugin ignoring ParameterBBox parameter
 * 
 * @author peters
 *
 */
public class MultiGetterPluginIgnoreBbox extends MultiGetterPlugin {
    
	private boolean isExtensionStrategy( String id ) {
		return id.startsWith( "DE-" );
	}
	
    @Override
    public List<Location> getLocations(String targetType, Parameter parameter) throws Fault {
    	
    	List<Location> locations;
    	
        if( parameter instanceof ParameterBBox || parameter instanceof ParameterBBoxWithSRID ) {
        	return  super.getLocations( targetType, null );
        } else {
        	
            if( targetType.equals( SdbbyStrategy.class.getName() ) ) {
    	        ParameterFlags pfs = WlsUtils.getParameterOfType( parameter, ParameterFlags.class);
    			if( ( pfs == null ) || !(new HashSet<>( pfs.getFlag() ) ).contains( "COMPLETE" )  ) {
    				locations = super.getLocations( targetType, parameter );
    				locations = locations.stream()
    						         .filter( l -> !isExtensionStrategy(l.getId() ) )	
    						         .collect( Collectors.toList() );
    				
    				return locations;
    			} else {
    				return  super.getLocations( targetType, null );
    			}
            } 
            return  super.getLocations( targetType, parameter );
        }
    }
}
