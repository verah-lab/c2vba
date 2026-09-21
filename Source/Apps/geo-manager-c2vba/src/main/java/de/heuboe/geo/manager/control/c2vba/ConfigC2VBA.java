package de.heuboe.geo.manager.control.c2vba;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import de.heuboe.geo.manager.base.map.update.GeoManagerMapComponentManagement;
import de.heuboe.geo.manager.map.MapSupplyProviderBaseTypes;
import de.heuboe.wls.util.WlsException;

/**
 * 
 * Adding strategies as map component
 * 
 * @author peters
 *
 */
@Configuration
public class ConfigC2VBA {
    
    @Autowired
    private GeoManagerMapComponentManagement mcm;

    @PostConstruct
    void postConstruct() throws WlsException {
        mcm.addMapComponent( MapSupplyProviderBaseTypes.STRATEGY, null );
    }
}
