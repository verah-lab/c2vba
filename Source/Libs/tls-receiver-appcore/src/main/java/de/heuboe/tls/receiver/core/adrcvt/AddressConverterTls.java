package de.heuboe.tls.receiver.core.adrcvt;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.tlstele.meta.Osi7Id;

/**
 * Implementation of an AddressConverter for the TLS receiver using the current config service
 * @author ronald
 *
 */
public class AddressConverterTls implements de.heuboe.tls.receiver.interfaces.AddressConverter {

    @Autowired
    private Osi7Cfg cfg;

    /**
     * Convert the given single parameters to a permanent identifier in a VMIS context
     * @param node node number (Knotennummer) the combination of location and distance
     * @param fg function group (Funktionsgruppe)
     * @param de de number (DE-Nummer)
     * @return Permanent identifier of the given de 
     */
    @Override
    public String convert( int node, int fg, int de ) {
        if (0 > node || (256*256*256) <= node) {
            throw new IllegalArgumentException( "OSI7 number out of range of [0 ... 256^3-1]: " + node );
        }
        if (255 < de || 0 > de) { // de 0 allowed? yes, in fg 254 (systemsteuerung) de 0
            throw new IllegalArgumentException( "DE number out of range of [0 ... 255]: " + de );
        }
        if (0 > fg || 255 < fg) { // implement list of allowed fgs?
            throw new IllegalArgumentException( "FG number out of range of [0 ... 255]: " + fg );
        }
        
        Osi7Id id = new Osi7Id( node, (short) de, (short) fg );
        return cfg.getEaPermId( id );
    }

    @Override
    public Collection<Integer> descendants( int realAddress ) {
        return cfg.getDescendants( realAddress, false, null );
    }

}
