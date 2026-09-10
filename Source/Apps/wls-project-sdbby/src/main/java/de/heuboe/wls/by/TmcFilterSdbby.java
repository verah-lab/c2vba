package de.heuboe.wls.by;

import java.util.List;

import de.heuboe.wls.data.tmc.TmcLocation;
import de.heuboe.wls.tmc.TmcFilter;

public class TmcFilterSdbby implements TmcFilter {
    
    private List<String> addRouteNums;
    
    public TmcFilterSdbby( List<String> addRouteNums ) {
        this.addRouteNums = addRouteNums;
    }

	/**
	 * Nur Punkte auf Autobahnen.
	 */
	@Override
	public boolean isValid(TmcLocation tmcLoc) {
		if( ( tmcLoc.getRouteNumber() == null ) || 
		    ( 
		       !tmcLoc.getRouteNumber().startsWith("A") && 
		       !addRouteNums.contains( tmcLoc.getRouteNumber() ) 
		    ) 
		  ) {
			return false;
		}
		if (!tmcLoc.getLocationType().startsWith("P")) {
			return false;
		}
		return true;
	}

}
