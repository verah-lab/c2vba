package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import de.heuboe.tls.cfglib.Osi7Cfg;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;

//@TestConfiguration
@Component
public class MockCfgSvcCfg2UzA2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockCfgSvcCfg2UzA2.class);

//    static private ConfigServiceBlockingStub cfgSvc = null;
//    public static void setCfgSvc( ConfigServiceBlockingStub cfgSvcIn ) {
//        cfgSvc = cfgSvcIn;
//    }
//
//    @Bean
//    @Primary
//    public ConfigServiceBlockingStub cfgSvcStub() {
////      alternate way
////        Client client = new Client( @Value( "${grpc.client.cfgsvc.address:static://oper-w7v.heuboe.hbintern:9890" )
////        return client.getStub()
//        LOGGER.info( "Config: cfgSvcStub {}", cfgSvc );
//        return cfgSvc;
//    }
    
//    @Autowired
    public String uzId = "UZ_A2";
//    
//    @Bean ( name = "cfgKaernten" )
//    public Osi7Cfg getOsi7ConfigKtn() throws Exception {
//        Osi7Cfg res = new Osi7Cfg();
//        res.setCfgSvc( new MockedCfgGetter( "UZ_Kaernten" ) );
//        res.buildUZConfig( uzId );
//        return res;
//    }
 // UZ_A2   
    
    @Bean ( name = "cfgA2" )
    public Osi7Cfg getOsi7ConfigA2() throws Exception {
        Osi7Cfg res = new Osi7Cfg();
        res.setCfgSvc( new MockedCfgGetter( uzId ) );
        res.buildUZConfig( uzId );
        return res;
    }
}
