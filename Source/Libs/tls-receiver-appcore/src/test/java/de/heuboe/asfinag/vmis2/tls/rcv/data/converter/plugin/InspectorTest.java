package de.heuboe.asfinag.vmis2.tls.rcv.data.converter.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.heuboe.tls.rcv.data.converter.plugin.Inspector;
import de.heuboe.tls.rcv.data.converter.registry.Registry;
import de.heuboe.tls.rcv.data.cvtinterface.ConversionRegistryIf;
import de.heuboe.tls.receiver.interfaces.helpers.TlsDatatypeId;

public class InspectorTest {
    
    @Test
    public void pluginReaderTest() {
        ConversionRegistryIf registry = new Registry();
//        String cwd = Paths.get(".").toAbsolutePath().toString()
        Inspector.initRegistry( "target/test-classes/de/heuboe/asfinag/vmis2/tls/rcv/data/converter/plugin", "de.heuboe.asfinag.vmis2.tls.rcv.data.converter.plugin", registry, ".*\\.jar"  );
        Set<TlsDatatypeId> datatypes = registry.getKeys();
        assertEquals( 0, datatypes.size() );
    }

}
