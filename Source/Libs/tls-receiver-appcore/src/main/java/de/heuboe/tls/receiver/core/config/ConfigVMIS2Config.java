package de.heuboe.tls.receiver.core.config;
//package de.heuboe.tls.receiver.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import de.heuboe.asfinag.tls.cfgchgdetector.ConfigChangeDetector;
//import de.heuboe.asfinag.tls.cfggetter.Vmis2TlsCfgGetter;
//import de.heuboe.tls.cfglib.INotificationToApp;
//import de.heuboe.tls.cfglib.IOsi7Cfg;
//import de.heuboe.tls.cfglib.Osi7Cfg;
//import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
//import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
//import lombok.extern.slf4j.Slf4j;
//import net.devh.boot.grpc.client.inject.GrpcClient;
////import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.MockCfgSvcCfg2UzA2;
//
//@Configuration
//@EnableAutoConfiguration( exclude = MongoAutoConfiguration.class )
//@Slf4j
//public class ConfigVMIS2Config implements INotificationToApp {
//    
////    @Autowired
////    ConfigReceiverVmis2AlarmProperties alarmProps;
//
//    @GrpcClient( "CfgSvc" )
//    ConfigServiceBlockingStub cfgSvc;
//
//    @Autowired
//    ConfigChangeProvider      chgProv;
//
//    @Value( "#{changeTopic}" )
//    String                    chgTopic;
//
//    @Bean( name = "changeTopic" )
//    public String getChgTopic( @Value( "${de.heuboe.tls.sender.config.changetopic}" ) String topName ) {
//        return topName;
//    }
//
//    @Bean( name = "changeTopicGroupId" )
//    public String getChgTopicGrpId( @Value( "${de.heuboe.tls.sender.config.changetopicgroupid}" ) String topName ) {
//        return topName;
//    }
//
//    @Bean( name = "configChangeProvider" )
//    public ConfigChangeProvider getChgProvider() {
//        return new ConfigChangeDetector();
//    }
//
//    @Bean
//    TlsCfgGetter getCfgGetter( /* @Qualifier( "configServiceAddress" ) String cfgSvcAddress */ ) {
//
//        Vmis2TlsCfgGetter cfgGetter = new Vmis2TlsCfgGetter();
//        // log.info( "Config: cfgSvc {}", cfgSvcAddress );
//
//        cfgGetter.setCfgSvc( cfgSvc );
//        return cfgGetter;
//    }
//
//    @Bean
//    Osi7Cfg getOsi7Cfg( @Qualifier( "uzId" ) String uzId, TlsCfgGetter cfgGetter ) throws Exception {
//        Osi7Cfg osi7Cfg = new Osi7Cfg();
//        osi7Cfg.setCfgSvc( cfgGetter );
//        // osi7Cfg.setDebugDevices( true )
//        osi7Cfg.buildUZConfig( uzId );
//
//        osi7Cfg.setAppNotification( this );
//
//        return osi7Cfg;
//    }
//
//    @Override
//    public void configChanged( IOsi7Cfg oldCfg, IOsi7Cfg newCfg ) {
//        log.info( "Configuration changed." );
//    }
//
//}
