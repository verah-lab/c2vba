package de.heuboe.tls.wancom.kcfg.test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import de.heuboe.tls.cfglib.Osi7Cfg;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@Component
@Slf4j
public class MockedCfgUZA2 {

    public String uzId = "UZ_A2";
    
    @Bean ( name = "cfgA2" )
    public Osi7Cfg getOsi7ConfigA2() throws Exception {
        Osi7Cfg res = new Osi7Cfg();
        res.setCfgSvc( new MockedCfgGetter( uzId ) );
        res.buildUZConfig( uzId );
        log.info( "Build mocked config for UZ_A2" );
        return res;
    }
}
