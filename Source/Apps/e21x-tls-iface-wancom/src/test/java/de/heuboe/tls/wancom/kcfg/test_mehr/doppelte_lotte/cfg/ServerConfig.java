package de.heuboe.tls.wancom.kcfg.test_mehr.doppelte_lotte.cfg;

import java.io.IOException;
import java.time.Instant;

import de.heuboe.tls.ifacewancom.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import de.heuboe.tls.cfgifacerouting.RoutingProvider;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceProtocol;
import de.heuboe.tls.iface.iface.IfaceRouting;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import de.heuboe.tls.ifacewancom.GenericTimeSyncGenerator;
import de.heuboe.tls.prot.wancom.ConfigReader;
import de.heuboe.tls.prot.wancom.ConnectionConfig;
import de.heuboe.tls.prot.wancom.WANCom;
import de.heuboe.tls.prot.wancom.WANComConfig;
import de.heuboe.tls.sysconkafka.SysconKafka;
import de.heuboe.tls.sysconkafka.Tele2SendReceiver;
import de.heuboe.tls.wancom.kcfg.test_mehr.doppelte_lotte.IfaceAppKRI;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ronald
 */

//@SpringBootTest(
//        properties = {
//                "de.heuboe.tls.tlsoip.config.tls_over_ip_configfile_fakesrv=src/test/resources/oipA2fakeServer.cfg" // wins over property from file
//        }
//        )

@DirtiesContext // Kafka
//// @formatter:off
//@EmbeddedKafka(
//        partitions = 1, 
//        controlledShutdown = false, 
//                ports = {55777},
//                zookeeperPort = 55771,
////                ports = {50637},
//        // ${random.int} => always begin with a fresh directory. i.e. no messages in broker
//        // keep directory for instance tin order to preload
//        brokerProperties = {"log.dir=target/kafka${random.int}"} 
//        ) // @formatter:on

//@TestPropertySource(
//        properties = {
//                "spring.kafka.bootstrap-servers=${ifaceEmbedded.brokers}",
//                "spring.kafka.consumer.group-id=iface1EmbeddedKafkaTestServer",
//                "spring.kafka.listener.missing-topics-fatal=false",
//                "de.heuboe.tls.tlsoip.config.tls_over_ip_configfile_fakesrv=src/test/resources/oipA2fakeServer.cfg",
//        },
//        locations="classpath:test1.properties"               // !!! properties
//        )

//// @formatter:off
//@ContextConfiguration( classes = {
//        de.heuboe.tls.tlsoip.kcfg.config.Config.class,
//        de.heuboe.tls.tlsoip.kcfg.config.JacksonConfig.class,
//        de.heuboe.tls.tlsoip.kcfg.config.KafkaConfig.class,
//        de.heuboe.tls.tlsoip.kcfg.config.MetricsConfig.class,
//        
//        de.heuboe.tls.tlsoip.kcfg.test.config.MockedCfgUZA2.class,
////        de.heuboe.tls.tlsoip.kcfg.config.Properties.class,
//    } ) // @formatter:on

//@Configuration
@EnableKafka
@EnableConfigurationProperties( Properties.class )
@Slf4j
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
@ComponentScan( 
        basePackageClasses = { 
                de.heuboe.tls.ifacewancom.config.Config.class, 
                de.heuboe.tls.wancom.kcfg.test.config.MockedCfgUZA2.class ,
                de.heuboe.tls.ifacewancom.config.KafkaConfig.class
        },
        excludeFilters={ 
                @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=Config.class),
                @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=ConfigIfaceApp.class),
                @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value= ConfigServiceTlsCfgGgetterBean.class),
                @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value= ConfigGetTLSOsi7Config.class)
                }
// @formatter:off
//        @ContextConfiguration( classes = {
//         de.heuboe.tls.ifacewancom.config.Config.class,
//         de.heuboe.tls.ifacewancom.config.JacksonConfig.class,
//         de.heuboe.tls.ifacewancom.config.KafkaConfig.class,
//         de.heuboe.tls.ifacewancom.config.MetricsConfig.class,
//
//         de.heuboe.tls.wancom.kcfg.test.config.MockedCfgUZA2.class,
//         } ) // @formatter:on
)
public class ServerConfig {
    
    @Autowired
    Osi7Cfg cfg;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplateBean; // NOSONAR better debug

    /**
     * config bean for the id of theUZ this process will use the config of
     * @param props Object carrying the properties for this process
     * @return the name of the topic
     */
    @Bean( name = "uzId" )
    public String getValueUzId( final Properties props ) {
        log.info( "Config: uzId {}", props.getUzId() );
        log.debug( " debug Config: uzId {}", props.getUzId() );
        log.trace( " trace Config: uzId {}", props.getUzId() );
        return props.getUzId();
    }

    /**
     * config bean for the name of the telegram received topic
     * @param props Object carrying the properties for this process
     * @return the name of the topic
     */
    @Bean( name = "rcvTopic" )
    public String getTopicR( final Properties props ) {
        log.info( "Config: topic {}", props.getRcvTopic() );
        return props.getRcvTopic();
    }

    /**
     * config bean for the name of the telegram to send topic
     * @param props Object carrying the properties for this process
     * @return the name of the topic
     */
    @Bean( name = "sndTopic" )
    public String getTopicS( final Properties props ) {
        log.info( "Config: topic {}", props.getSndTopic() );
        return props.getSndTopic();
    }
    
    /**
     * config bean for the name of the communication state topic
     * @param props Object carrying the properties for this process
     * @return the name of the topic
     */
    @Bean( name = "commStateTopic" )
    public String getCommStateTopic( final Properties props ) {
        log.info( "Config: commStateTopic {}", props.getCommStateTopic() );
        return props.getCommStateTopic();
    }

    /**
     * config bean for the iface key
     * @param props Object carrying the properties for this process
     * @return the iface key this process shall work for
     */
    @Bean( name = "ifaceKeySrv" )
    public int getIfaceKey( final Properties props ) {
        log.info( "Config: ifaceKey {}", props.getIfaceKey() );
        return props.getIfaceKey();
    }
    
    @Bean( name = "WANComCfgFileSrv" )
//    public String getTlsOipCfgFileServer(@Value("${de.heuboe.tls.tlsoip.config.tls_over_ip_configfile_fakesrv}") String cfgFile) {
//    public String getTlsOipCfgFileServer(@Value("${de.heuboe.tls.tlsoip.config.WANComConfigFile}") String cfgFile, Properties props) {
    public String getConfigFileWANCom( Properties props ) {
        log.info( "server config file: {}", props.getConfigFileWANCom() );
        return props.getConfigFileWANCom();
    }
    
    /**
     * config bean for the TimeSyncGenerator
     * @param props Object carrying the properties for this process
     * @return the TimeSyncGenerator to be used on every connection
     */
    @Bean( name = "TimeSyncGenerator" )
    public GenericTimeSyncGenerator getGenericTimeSyncGenerator( final Properties props ) {
        log.info( "Config: timezone4Sync {}", props.getTimezone4Sync() );
        log.info( "Config: useDstBit {}", props.isUseDstBit() );
        GenericTimeSyncGenerator res = new GenericTimeSyncGenerator();
        // Europe/Berlin (default) | GMT:01:00 | UTC
        res.setTimeZone( props.getTimezone4Sync() );
        res.setUseDstBit( props.isUseDstBit() );
        
        return res;
    }
    
    // this thing has to be replaced, when the configuration is read from ConfigService
    /**
     * config bean for the tls over ip configurations 
     * @param props Object carrying the properties for this process
     * @param gtsg TimeSyncGenerator to be used on every connection
     * @return the tls over ip config object
     * @throws IOException if the config file for partner configuration is missing or leads to errors
     */
    @Bean( name = "WANComConfigSrv" )
    public WANComConfig getWANComConfigFile( final GenericTimeSyncGenerator gtsg, @Qualifier( "WANComCfgFileSrv" ) final String WANComCfgFileSrv ) throws IOException {
        log.info( "Config: TlsOIpCfgFile {}", WANComCfgFileSrv );
        ConfigReader cr = new ConfigReader( WANComCfgFileSrv );
        WANComConfig oipCfg = cr.getWANComConfig();
        
        log.info( "Config:   Tls default config {}", oipCfg.getDefaultConfig() );
        for ( ConnectionConfig cfg : oipCfg.getConnectionConfigList() ) {
            cfg.setTimeSyncMode( TimeSyncMode.USERDELIVERED );
            cfg.setTimeSyncGenerator( gtsg );
            log.info( "Config:   Tls connection config {}", cfg );
            
        }
        
        return oipCfg;
    }
    
    /**
     * config bean for the iface protocol
     * @param cfg config object carrying tl over ip configs for partners
     * @return the protocol configured with WANComConfig
     */
    @Bean( name = "ifaceProtocolSrv" )
    public IfaceProtocol getIfaceProtocol( @Qualifier( "WANComConfigSrv" ) WANComConfig cfg ) {
        return new WANCom( cfg );
    }
    
    @Bean( name = "tele2SendReceiverSrv" )
    public Tele2SendReceiver getTele2SendReceiver( final Properties props ) {
        Tele2SendReceiver res = new Tele2SendReceiver( Instant.now().toEpochMilli() );
        res.setIfaceKey( props.getIfaceKey() );
        res.setCheckLimits( props.isCheckLimits() );
        res.setJobNrMin( props.getMinJobNr() );
        res.setJobNrMax( props.getMaxJobNr() );
        
        return res;
    }
    
    /**
     * config bean for the iface routing, done by processing data of config service
     * @param cfg config likely provided by a grpc config service
     * @param uzId id of UZ working for
     * @param ifaceKey ifaceKey see getApp
     * @return the iface routing object
     */
    @Bean( name = "ifaceRoutingSrv" )
    public IfaceRouting getIfaceRouting( Osi7Cfg cfg, @Qualifier( "uzId" ) String uzId, @Qualifier( "ifaceKeySrv" ) final int ifaceKey ) {
        log.info( "using config {}", cfg );
        RoutingProvider rp = new RoutingProvider();
        rp.setCfg( cfg );
        rp.setIfaceKey( ifaceKey );
        rp.setUzId( uzId );
        rp.init();
        return rp;
    }
    
    /**
     * config bean for the system connector, i.e. communication which is kafka here
     * @param props Object carrying the properties for this process
     * @param uzId id of UZ working for
     * @param rcvTopic topic name for received telegrams
     * @param commStateTopic name of topic for communication states
     * @param ifaceKey ifaceKey see getApp
     * @return the iface system connector
     */
    @Bean( name = "ifaceSystemConnectorSrv" )
    public IfaceSystemConnector getIfaceSystemConnector( 
            // @formatter:off
            final Properties props, 
            @Qualifier( "uzId" ) String uzId, 
            @Qualifier( "rcvTopic" ) String rcvTopic, 
            @Qualifier( "commStateTopic" ) String commStateTopic, 
            @Qualifier( "tele2SendReceiverSrv" ) Tele2SendReceiver tele2SendReceiver, 
            @Qualifier( "ifaceKeySrv" ) final int ifaceKey
            // @formatter:on
            ) {
        log.info( "Config SysconKafka: minJobNr {}", props.getMinJobNr() );
        log.info( "Config SysconKafka: maxJobNr {}", props.getMaxJobNr() );
        SysconKafka syscon = new SysconKafka();
        syscon.setReceiverForTelegramsToSend( tele2SendReceiver );
        tele2SendReceiver.checkObj();
        syscon.setCommStateTopic( commStateTopic );
        syscon.setIfaceKey( ifaceKey );
        syscon.setKafkaTemplate( kafkaTemplateBean );
        syscon.setTopicReceived( rcvTopic );
        syscon.setJobNrMin( props.getMinJobNr() );
        syscon.setJobNrMax( props.getMaxJobNr() );
        return syscon;
    }

    /**
     * config bean for the application
     * @param ifaceKey tag for partners working with
     * @param ifaceProtocol protocol implementation used
     * @param ifaceRouting routing information used
     * @param ifaceSystemConnector system connector (communication and data distribution) used
     * @return the IfaceApp
     * @throws IfaceException potential exceptions raised from components
     */
    @Bean( name = "ifaceAppServer" )
    public IfaceApplication getServerApp( 
            // @formatter:off
            @Qualifier( "ifaceKeySrv" ) final int ifaceKey,
            @Qualifier( "ifaceProtocolSrv" ) final IfaceProtocol ifaceProtocol,
            @Qualifier( "ifaceRoutingSrv" ) final IfaceRouting ifaceRouting,
            @Qualifier( "tele2SendReceiverSrv" ) Tele2SendReceiver tele2SendReceiver, 
            @Qualifier( "ifaceSystemConnectorSrv" ) final IfaceSystemConnector ifaceSystemConnector 
            // @formatter:on
        ) throws IfaceException {
        IfaceAppKRI app = new IfaceAppKRI();
        app.setIfaceKey( ifaceKey );
        app.setIfaceProtocol( ifaceProtocol );
        app.setIfaceRouting( ifaceRouting );
        app.setIfaceSystemConnector( ifaceSystemConnector );
        app.init();
        tele2SendReceiver.checkObj();
        return app;
    }

}
