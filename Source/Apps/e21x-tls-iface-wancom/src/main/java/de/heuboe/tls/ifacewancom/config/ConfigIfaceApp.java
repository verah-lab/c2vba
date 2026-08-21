package de.heuboe.tls.ifacewancom.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceProtocol;
import de.heuboe.tls.iface.iface.IfaceRouting;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;
import de.heuboe.tls.iface.lib.IfaceApp;

/**
 * Class holding the spring configuration for ths process
 */
@Configuration
public class ConfigIfaceApp {

    /**
     * config bean for the application
     * @param ifaceKey tag for partners working with
     * @param ifaceProtocol protocol implementation used
     * @param ifaceRouting routing information used
     * @param ifaceSystemConnector system connector (communication and data distribution) used
     * @return the IfaceApp
     * @throws IfaceException potential exceptions raised from components
     */
    @Bean( name = "ifaceApp" )
    public IfaceApp getApp( 
            // @formatter:off
            @Qualifier( "ifaceKey" ) final int ifaceKey,
            @Qualifier( "ifaceProtocol" ) final IfaceProtocol ifaceProtocol,
            @Qualifier( "ifaceRouting" ) final IfaceRouting ifaceRouting,
            @Qualifier( "ifaceSystemConnector" ) final IfaceSystemConnector ifaceSystemConnector 
            // @formatter:on
        ) throws IfaceException {
        IfaceApp app = new IfaceApp();
        app.setIfaceKey( ifaceKey );
        app.setIfaceProtocol( ifaceProtocol );
        app.setIfaceRouting( ifaceRouting );
        app.setIfaceSystemConnector( ifaceSystemConnector );
        app.init();
        return app;
    }

}
