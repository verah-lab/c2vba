package de.heuboe.tls.ifacewancom;

import de.heuboe.tls.cfglib.INotificationToApp;
import de.heuboe.tls.cfglib.IOsi7Cfg;
import de.heuboe.tls.iface.iface.IfaceApplication;

/**
 * Object of this class can handle changes in the configuration (TLS)
 */
public class ConfigChangeHandler implements INotificationToApp {
    
    private IfaceApplication ifaceApp = null;

    @Override
    public void configChanged( IOsi7Cfg oldCfg, IOsi7Cfg newCfg ) {
        if( null == ifaceApp ) {
            throw new IllegalStateException( "Need valid IfaceApplication in ConfigChangeHandler" );
        }
    }
    
    public void setIfaceApplication( IfaceApplication ifaceApp ) {
        this.ifaceApp = ifaceApp;
    }

}
