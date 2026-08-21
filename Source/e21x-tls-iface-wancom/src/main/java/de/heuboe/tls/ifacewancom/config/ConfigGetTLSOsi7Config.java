package de.heuboe.tls.ifacewancom.config;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.ifacewancom.ConfigChangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for getting the TLS configuration
 */

@Configuration
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
@Slf4j
public class ConfigGetTLSOsi7Config {

    @Bean // @formatter:off
    Osi7Cfg getOsi7Cfg(
             @Qualifier( "uzId" ) String uzId,
             TlsCfgGetter cfgGetter,            // imported from a bean
             ConfigChangeProvider chgProv,
             ConfigChangeHandler configChangeHandler,
             @Qualifier("specialDevRoot") String devRoot ) throws Exception { // @formatter:on
        Osi7Cfg osi7Cfg = new Osi7Cfg();
        osi7Cfg.setCfgSvc( cfgGetter );
        // osi7Cfg.setDebugDevices( true )

        if (null != devRoot && devRoot.length() > 11) {
            // String uzParentName, String uzParentId, String uzParentCableName, int ifaceKey, int parentPort, int childPort
            String[] vals = devRoot.split( "," );
            if (vals.length != 6) {
                throw new IllegalStateException( "Wrong format for specialDevRoot: " + devRoot
                                                 + ". Expected: <NodeName>,<NodeId>,<CableName>,<ifaceKey>,<OSI2-parentPort>,<OSI2-childPort>");
            }
            String devRootName = vals[0].trim();
            String devRootId = vals[1].trim();
            String devRootCableName = vals[2].trim();
            int ifaceKey = Integer.parseInt( vals[3].trim() );
            int parentPort = Integer.parseInt( vals[4].trim() );
            int childPort  = Integer.parseInt( vals[5].trim() );
            log.info( "new devRoot [" );
            log.info( "  Config devRootName: {}", devRootName );
            log.info( "  Config devRootId: {}", devRootId );
            log.info( "  Config devRootCableName: {}", devRootCableName );
            log.info( "  Config ifaceKey: {}", ifaceKey );
            log.info( "  Config parentPort: {}", parentPort );
            log.info( "  Config childPort: {}", childPort );
            log.info( "] new devRoot" );
            osi7Cfg.buildUZConfig( uzId, devRootName, devRootId, devRootCableName, ifaceKey, parentPort, childPort );
            osi7Cfg.printDevTree( System.out, 3, false, true ); // NOSONAR needs a OutputStream
        } else {
            osi7Cfg.buildUZConfig( uzId );
        }

        chgProv.register( osi7Cfg ); // now config change notification detected go to Osi7Cfg

        osi7Cfg.setAppNotification( configChangeHandler );

        return osi7Cfg;
    }

    @Bean
    ConfigChangeHandler getConfigChangeHandler() {
        return new ConfigChangeHandler();
    }

}
