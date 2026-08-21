package de.heuboe.tls.wancom.kcfg.test.config;

import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@TestConfiguration
@Component
@Slf4j
public class MockedCfgGetterBean
{
    @Bean
    TlsCfgGetter getCfgGetter() throws Exception {
        return new MockedCfgGetter( "UZ_Kaernten" );
    }

}
