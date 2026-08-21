package de.heuboe.tls.receiver.now.config;

import de.heuboe.asfinag.tls.cfggetter.Vmis2TlsCfgGetter;
import de.heuboe.idgenerator.autoconfig.IDGeneratorAutoConfiguration;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.iface.iface.SystemMessageManagement;
import de.heuboe.tls.receiver.now.impl.Vmis2SystemMessageManagement;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//(proxyBeanMethods = false)
@EnableAutoConfiguration( exclude = { MongoAutoConfiguration.class, IDGeneratorAutoConfiguration.class } )
@Slf4j
public class ConfigVMIS2Config /* implements INotificationToApp */ {
    
//    @Autowired
//    Vmis2SystemMessageManagementProperties alarmProps;

    @Autowired
    ConfigServiceBlockingStub cfgSvc;

    @Bean
    TlsCfgGetter getCfgGetter( /* @Qualifier( "configServiceAddress" ) String cfgSvcAddress */ ) {

        Vmis2TlsCfgGetter cfgGetter = new Vmis2TlsCfgGetter();
        // log.info( "Config: cfgSvc {}", cfgSvcAddress );

        cfgGetter.setCfgSvc( cfgSvc );
        return cfgGetter;
    }
    
    @Bean
    Vmis2SystemMessageManagement getSMM() {
        return new Vmis2SystemMessageManagement();
    }

}
