package de.heuboe.tls.receiver.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Configuration
@Slf4j
public class CfgSvcCfg {

    @GrpcClient("CfgSvc")
    private ConfigServiceBlockingStub cfgSvc;

    @Bean
    public ConfigServiceBlockingStub cfgSvcStub() {
//      alternate way
//        Client client = new Client( @Value( "${grpc.client.cfgsvc.address:static://oper-w7v.heuboe.hbintern:9890" )
//        return client.getStub()
        log.info( "Config: cfgSvcStub {}", cfgSvc );
        return cfgSvc;
    }

}
