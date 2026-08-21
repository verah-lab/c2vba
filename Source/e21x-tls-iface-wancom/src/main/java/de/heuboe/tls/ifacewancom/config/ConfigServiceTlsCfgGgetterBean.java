package de.heuboe.tls.ifacewancom.config;

import de.heuboe.asfinag.tls.cfggetter.Vmis2TlsCfgGetter;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration in order to get a bean for a {@link TlsCfgGetter}
 */
@Configuration
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
@Slf4j
public class ConfigServiceTlsCfgGgetterBean {

    @GrpcClient( "CfgSvc" )
    ConfigServiceBlockingStub cfgSvc;

    @Bean
    TlsCfgGetter getCfgGetter() {

        Vmis2TlsCfgGetter cfgGetter = new Vmis2TlsCfgGetter();

        cfgGetter.setCfgSvc( cfgSvc );
        return cfgGetter;
    }

}
