package de.heuboe.tls.receiver.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.heuboe.asfinag.tls.cfgchgdetector.ConfigChangeDetector;
import de.heuboe.tls.cfglib.INotificationToApp;
import de.heuboe.tls.cfglib.IOsi7Cfg;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigOsi7Config implements INotificationToApp {
    
//    @Value("#{changeTopic}")
//    String chgTopic; // left here for debug purposes
//
//    @Value("#{changeTopicGroupId}")
//    String chgTopicGroupId;  // left here for debug purposes

    @Bean( name = "changeTopic" )
    public String getChgTopic( @Value( "${de.heuboe.tls.receiver.config.chg.changetopic}" ) String topName ) {
        return topName;
    }

    @Bean( name = "changeTopicGroupId" )
    public String getChgTopicGrpId( @Value( "${de.heuboe.tls.receiver.config.chg.changetopicgroupid}" ) String topGroupIdName ) {
        return topGroupIdName;
    }

    @Bean( name = "configChangeProvider" )
    public ConfigChangeProvider getChgProvider() {
        return new ConfigChangeDetector();
    }

    @Bean
    Osi7Cfg getOsi7Cfg( @Qualifier( "uzId" ) String uzId, TlsCfgGetter cfgGetter, ConfigChangeProvider chgProv ) {
        Osi7Cfg osi7Cfg = new Osi7Cfg();
        osi7Cfg.setCfgSvc( cfgGetter );
        osi7Cfg.buildUZConfig( uzId );

        chgProv.register( osi7Cfg ); // now config change notification detected go to Osi7Cfg
        
        osi7Cfg.setAppNotification( this );

        return osi7Cfg;
    }

    @Override
    public void configChanged( IOsi7Cfg oldCfg, IOsi7Cfg newCfg ) {
        if ( (null != oldCfg.getCfgServiceVersion()) && (null != newCfg.getCfgServiceVersion()) ) {
            log.info( "config changed: {} -> {}", oldCfg.getCfgServiceVersion(), newCfg.getCfgServiceVersion() );
        } else {
            log.info( "config changed" );
        }
        
    }

}
