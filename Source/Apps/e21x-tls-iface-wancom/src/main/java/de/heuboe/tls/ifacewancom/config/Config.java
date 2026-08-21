package de.heuboe.tls.ifacewancom.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

import de.heuboe.tls.cfgifacerouting.RoutingProvider;
import de.heuboe.tls.cfglib.Osi7Cfg;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Class holding the spring configuration for ths process
 */
@Configuration
@EnableKafka
@EnableConfigurationProperties( Properties.class )
@Slf4j
public class Config {

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
        log.info( "Config: receive topic {}", props.getRcvTopic() );
        return props.getRcvTopic();
    }

    /**
     * config bean for the name of the telegram to send topic
     * @param props Object carrying the properties for this process
     * @return the name of the topic
     */
    @Bean( name = "sndTopic" )
    public String getTopicS( final Properties props ) {
        log.info( "Config: send topic {}", props.getSndTopic() );
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
    @Bean( name = "ifaceKey" )
    public int getIfaceKey( final Properties props ) {
        log.info( "Config: ifaceKey {}", props.getIfaceKey() );
        return props.getIfaceKey();
    }

    /**
     * config bean for special_dev_root
     * @param props Object carrying the properties for this process
     * @return special_dev_root, comma seperated list that itemizes name, id, cable name, ifaceKey, osi2 parent port, osi2 child port
     */
    @Bean(name = "specialDevRoot") // like HE GPRS-UZ
    public String getSpecialDevRoot(  final Properties props  ) {
        log.info( "Config: specialDevRoot {}", props.getSpecialDevRoot() );
        return props.getSpecialDevRoot();
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
     * config bean for the wancom configurations 
     * @param props Object carrying the properties for this process
     * @param gtsg TimeSyncGenerator to be used on every connection
     * @return the wancom config object
     * @throws IOException if the config file for partner configuration is missing or leads to errors
     */
    @Bean( name = "WANComConfig" )
    public WANComConfig getWANComConfigFile( final GenericTimeSyncGenerator gtsg, final Properties props ) throws IOException {
        log.info( "Config: CfgFile {}", props.getConfigFileWANCom() );
        ConfigReader cr = new ConfigReader( props.getConfigFileWANCom() );
        WANComConfig cfgWANCom = cr.getWANComConfig();
        
        log.info( "Config:   Tls default config {}", cfgWANCom.getDefaultConfig() );
        for ( ConnectionConfig cfg : cfgWANCom.getConnectionConfigList() ) {
            cfg.setTimeSyncMode( TimeSyncMode.USERDELIVERED );
            cfg.setTimeSyncGenerator( gtsg );
            log.info( "Config:   Tls connection config {}", cfg );
            
        }
        
        return cfgWANCom;
    }


    /**
     * config bean for the iface protocol
     * @param cfg config object carrying tl over ip configs for partners
     * @return the protocol configured with TlsOverIpConfig
     */
    @Bean( name = "ifaceProtocol" )
    public IfaceProtocol getIfaceProtocol( @Qualifier( "WANComConfig" ) WANComConfig cfg ) {
        return new WANCom( cfg );
    }

    /**
     * config bean for the iface routing, done by processing data of config service
     * @param cfg config likely provided by a grpc config service
     * @param uzId id of UZ working for
     * @param ifaceKey ifaceKey see getApp
     * @return the iface routing object
     */
    @Bean( name = "ifaceRouting" )
    public IfaceRouting getIfaceRouting( Osi7Cfg cfg, @Qualifier( "uzId" ) String uzId, @Qualifier( "ifaceKey" ) final int ifaceKey ) {
        log.info( "using config {}", cfg );
        RoutingProvider rp = new RoutingProvider();
        rp.setCfg( cfg );
        rp.setIfaceKey( ifaceKey );
        rp.setUzId( uzId );
        rp.init();
        return rp;
    }

    /**
     * supply a bean of type {@link Tele2SendReceiver}
     * This one will receive telegrams via kafka, that are intended to be sent out
     * @param props the application properties
     * @return a receiver for telegrams to send
     */
    @Bean( name = "tele2SendReceiver" )
    public Tele2SendReceiver getTele2SendReceiver( final Properties props ) {
        log.info( "Remembered start time for seek after here" ); // show time >before< critical point in time
        Tele2SendReceiver res = new Tele2SendReceiver( Instant.now().toEpochMilli() );
        log.info( "Remembered start time for seek before here" ); // show time >after< critical point in time
        res.setIfaceKey( props.getIfaceKey() );
        res.setJobNrMin( props.getMinJobNr() );
        res.setJobNrMax( props.getMaxJobNr() );
        res.setCheckLimits( props.isCheckLimits() );

        return res;
    }

    /**
     * config bean for the system connector, i.e. communication which is kafka here
     * @param props Object carrying the properties for this process
     * @param uzId id of UZ working for
     * @param rcvTopic topic name for received telegrams
     * @param commStateTopic name of topic for communication states
     * @param tele2SendReceiver receiver for telegram that are to be sent
     * @param ifaceKey ifaceKey see getApp
     * @return the iface system connector
     */
    @Bean( name = "ifaceSystemConnector" )
    public IfaceSystemConnector getIfaceSystemConnector( 
            // @formatter:off
            final Properties props, 
            @Qualifier( "uzId" ) String uzId, 
            @Qualifier( "rcvTopic" ) String rcvTopic, 
            @Qualifier( "commStateTopic" ) String commStateTopic, 
            @Qualifier( "tele2SendReceiver" ) Tele2SendReceiver tele2SendReceiver, 
            @Qualifier( "ifaceKey" ) final int ifaceKey
            // @formatter:on
            ) {
        log.info( "Config SysconKafka: minJobNr {}", props.getMinJobNr() );
        log.info( "Config SysconKafka: maxJobNr {}", props.getMaxJobNr() );
        SysconKafka syscon = new SysconKafka();
        tele2SendReceiver.checkObj();
        syscon.setReceiverForTelegramsToSend( tele2SendReceiver );
        syscon.setCommStateTopic( commStateTopic );
        syscon.setIfaceKey( ifaceKey );
        syscon.setKafkaTemplate( kafkaTemplateBean );
        syscon.setTopicReceived( rcvTopic );
        syscon.setCheckLimits( props.isCheckLimits() );
        syscon.setJobNrMin( props.getMinJobNr() );
        syscon.setJobNrMax( props.getMaxJobNr() );
        return syscon;
    }

    /**
     * config bean for kafka retention time
     * @return retention time 
     */
    @Bean
    public Duration kafkaDataRetentionTime() {
        return Duration.ofDays(2);
    }

}
