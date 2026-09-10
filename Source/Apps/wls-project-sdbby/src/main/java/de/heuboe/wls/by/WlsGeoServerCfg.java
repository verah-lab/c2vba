package de.heuboe.wls.by;

import de.heuboe.wls.persistence.PersistenceManager;
import de.heuboe.wls.persistence.WlsDbGeoServer;
import de.heuboe.wls.tmc.TmcTableCache;

public class WlsGeoServerCfg {

    public WlsGeoServerCfg( String wlsVersion,
                            Boolean inSupplyMode,
                            int geoServicePort,
                            TmcTableCache tmcTableCache,
                            PersistenceManager wlsPersistenceManager) {
        
        WlsDbGeoServer wlsGeoServer = new WlsDbGeoServer( "Wls SDBBY", wlsVersion, tmcTableCache.getVersion(), inSupplyMode, wlsPersistenceManager );
        wlsGeoServer.setSvcPort( geoServicePort );
        wlsGeoServer.start( "/sdbby" );
    }

}
