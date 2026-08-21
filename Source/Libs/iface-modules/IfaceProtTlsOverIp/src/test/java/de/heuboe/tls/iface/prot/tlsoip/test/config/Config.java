package de.heuboe.tls.iface.prot.tlsoip.test.config;

import de.heuboe.tls.iface.iface.IfaceProtocol;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import de.heuboe.tls.iface.prot.tlsoip.ConfigReader;
import de.heuboe.tls.iface.prot.tlsoip.ConnectionConfig;
import de.heuboe.tls.iface.prot.tlsoip.TlsOverIp;
import de.heuboe.tls.iface.prot.tlsoip.TlsOverIpConfig;
import de.heuboe.tls.iface.prot.tlsoip.test.GenericTimeSyncGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

/**
 * Class holding the spring configuration for ths process
 */
@Configuration
@Slf4j
public class Config {

    /**
     * config bean for the TimeSyncGenerator
     * @return the TimeSyncGenerator to be used on every connection
     */
    @Bean( name = "TimeSyncGenerator" )
    public GenericTimeSyncGenerator getGenericTimeSyncGenerator() {
        log.info( "Config: timezone4Sync {}", "Europe/Berlin" );
        log.info( "Config: useDstBit {}", true );
        GenericTimeSyncGenerator res = new GenericTimeSyncGenerator();
        // Europe/Berlin (default) | GMT:01:00 | UTC
        res.setTimeZone( "Europe/Berlin" );
        res.setUseDstBit( true );
        
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
    @Bean( name = "TlsOverIpConfig" )
    public TlsOverIpConfig getTlsOverIpConfigFile( final GenericTimeSyncGenerator gtsg, final Properties props ) throws IOException {
        String cfgFile = "src/test/resources/oipFakeClient.cfg";
        log.info( "Config: TlsOIpCfgFile {}", cfgFile );
        ConfigReader cr = new ConfigReader( cfgFile );
        if ( null == cr.getReceiptGraceTime() || 0 == cr.getReceiptGraceTime().intValue() ) {
            cr.setReceiptGraceTime( 3000 );
            cr.checkAndSpreadDefaults();
        }
        TlsOverIpConfig oipCfg = cr.getTlsOverIpConfig();
        
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
     * @return the protocol configured with TlsOverIpConfig
     */
    @Bean( name = "ifaceProtocol" )
    public IfaceProtocol getIfaceProtocol( @Qualifier( "TlsOverIpConfig" ) TlsOverIpConfig cfg ) {
        return new TlsOverIp( cfg );
    }
    
    /**
     * config bean for the iface routing, done by processing data of config service
     * @param cfg config likely provided by a grpc config service
     * @param uzId id of UZ working for
     * @param ifaceKey ifaceKey see getApp
     * @return the iface routing object
     */
//    @Bean( name = "ifaceRouting" )
//    public IfaceRouting getIfaceRouting( Osi7Cfg cfg, @Qualifier( "uzId" ) String uzId, @Qualifier( "ifaceKey" ) final int ifaceKey ) {
//        log.info( "using config {}", cfg );
//        RoutingProvider rp = new RoutingProvider();
//        rp.setCfg( cfg );
//        rp.setIfaceKey( ifaceKey );
//        rp.setUzId( uzId );
//        rp.init();
//        return rp;
//    }
    
    /**
     * config bean for the system connector, i.e. communication which is kafka here
     * @param props Object carrying the properties for this process
     * @param uzId id of UZ working for
     * @param rcvTopic topic name for received telegrams
     * @param commStateTopic name of topic for communication states
     * @param ifaceKey ifaceKey see getApp
     * @return the iface system connector
     */
//    @Bean( name = "ifaceSystemConnector" )
//    public IfaceSystemConnector getIfaceSystemConnector( 
//            // @formatter:off
//            final Properties props, 
//            @Qualifier( "uzId" ) String uzId, 
//            @Qualifier( "rcvTopic" ) String rcvTopic, 
//            @Qualifier( "commStateTopic" ) String commStateTopic, 
//            @Qualifier( "tele2SendReceiver" ) Tele2SendReceiver tele2SendReceiver, 
//            @Qualifier( "ifaceKey" ) final int ifaceKey
//            // @formatter:on
//            ) {
//        log.info( "Config SysconKafka: minJobNr {}", props.getMinJobNr() );
//        log.info( "Config SysconKafka: maxJobNr {}", props.getMaxJobNr() );
//        SysconKafka syscon = new SysconKafka();
//        tele2SendReceiver.checkObj();
//        syscon.setReceiverForTelegramsToSend( tele2SendReceiver );
//        syscon.setCommStateTopic( commStateTopic );
//        syscon.setIfaceKey( ifaceKey );
//        syscon.setKafkaTemplate( kafkaTemplateBean );
//        syscon.setTopicReceived( rcvTopic );
//        syscon.setCheckLimits( props.isCheckLimits() );
//        syscon.setJobNrMin( props.getMinJobNr() );
//        syscon.setJobNrMax( props.getMaxJobNr() );
//        return syscon;
//    }

}
