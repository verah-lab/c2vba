package de.heuboe.tls.receiver.core.config;

import de.heuboe.tls.receiver.core.adrcvt.AddressConverterTls;
import de.heuboe.tls.receiver.core.datawriter.DataWriterImpl;
import de.heuboe.tls.receiver.core.receiver.Receiver;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ComponentScan( basePackageClasses = {
        de.heuboe.tls.receiver.core.telein.TlsKafkaTelgramReceiver.class, de.heuboe.tls.rcv.transf.impl.TransformerImpl.class})
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @Bean( name = "topicPrefix" )
    public String getValuep0( @Value( "${de.heuboe.asfinag.tls.receiver.topicPrefix}" ) String value ) {
        LOGGER.info( "Config: topicPrefix {}", value );
        return value;
    }
    
    @Bean( name = "topicPostfix" )
    public String getValuep1( @Value( "${de.heuboe.asfinag.tls.receiver.topicPostfix}" ) String value ) {
        LOGGER.info( "Config: topicPostfix {}", value );
        return value;
    }

    @Bean( name = "packageName" )
    public String getValue2( @Value( "${de.heuboe.asfinag.tls.receiver.plugpack}" ) String value ) {
        LOGGER.info( "Config: packageName {}", value );
        return value;
    }

    @Bean( name = "uzId" )
    public String getValue3( @Value( "${de.heuboe.asfinag.tls.receiver.uzId}" ) String value ) {
        LOGGER.info( "Config: uzId {}", value );
        return value;
    }
    
    @Bean( name = "timezoneId" )
    public String getValue4( @Value( "${de.heuboe.asfinag.tls.receiver.timezoneid}" ) String value ) {
        LOGGER.info( "Config: timezoneId {}", value );
        return value;
    }
    
    @Bean( name = "timetolerance" )
    public int timetolerance( @Value( "${de.heuboe.asfinag.tls.receiver.timetolerance:45}" ) int value ) {
        LOGGER.info( "Config: timetolerance {}", value );
        return value;
    }

    @Bean( name = "plugins" )
    public String getValue1( @Value( "${de.heuboe.asfinag.tls.receiver.plugins}" ) String value ) {
        LOGGER.info( "Config: plugins {}", value );
        return value;
    }

    @Bean( name = "plugJarNameTemplate" )
    public String getValuePlugJarNameTemplate( @Value( "${de.heuboe.asfinag.tls.receiver.plugJarNameTemplate}" ) String value ) {
        LOGGER.info( "Config: plugJarNameTemplate {}", value );
        return value;
    }

    @Bean( name = "floatInvalid" )
    public String floatInvalid( @Value( "${de.heuboe.asfinag.tls.receiver.floatInvalid:#{null}}" ) String value ) {
        LOGGER.info( "Config: floatInvalid {}", value );
        return value;
    }
    
    @ConditionalOnProperty( name = "de.heuboe.asfinag.tls.receiver.intervallArtLaenge" )
    @Bean( name = "intervallArtIntervallLaenge" )
    public String getIntervallArtLaenge(
             @Value( "${de.heuboe.asfinag.tls.receiver.intervallArtLaenge}" ) String value ) {
        LOGGER.info( "Config: intervallArtLaenge {}", value );
        return value;
    }

    @Bean
    public AddressConverter getAdressConverter() {
        return new AddressConverterTls();
    }

    @Bean( name = "writer" )
    public DataWriter hoppa() {
        LOGGER.info( "Config: constructing writer" );
        DataWriter res = new DataWriterImpl();
        LOGGER.info( "Config: constructed writer" );
        return res;
    }

    @Bean( name = "myWuppi" )
    public Receiver wuppi() {
        LOGGER.info( "Config: constructing receiver" );
        return new Receiver();
    }
    
}
