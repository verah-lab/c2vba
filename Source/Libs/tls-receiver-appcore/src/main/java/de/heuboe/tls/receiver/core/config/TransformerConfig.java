package de.heuboe.tls.receiver.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.heuboe.tls.rcv.transf.impl.TransformerImpl;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.receiver.rdr.core.FunctionInval;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TransformerConfig {
    
    
    @Bean
    public Transformer transformer(
             TransformerImpl transformerI,
             TransformationRulesContainer transformationRules,
             AddressConverter addressConverter,
             SystemMessageManagement smm,
             @Qualifier( "timetolerance" ) int timetolerance,
             @Qualifier( "timezoneId" ) String timezoneId,
             @Autowired( required = false ) @Qualifier( "intervallArtIntervallLaenge" ) String intervallArtIntervallLaenge
    ) {
        if( null == transformerI ) {
            throw new IllegalStateException( "Missing Bean of type TransformerImpl" );
        }
        if( null == transformationRules ) {
            throw new IllegalStateException( "Missing Bean of type TransformationRulesContainer" );
        }
        if( null == addressConverter ) {
            throw new IllegalStateException( "Missing Bean of type AddressConverter" );
        }
        Transformer transformer = transformerI;
        if( null != intervallArtIntervallLaenge ) {
            transformerI.setIntervallArtIntervallLaenge( intervallArtIntervallLaenge );
        }
        transformer.setAddressConverter( addressConverter );
        transformer.setTransformationRules( transformationRules );
        transformer.setTimezoneId( timezoneId );
        transformerI.setSmm( smm );
        transformer.init();
        
        TimeGetter.setTimetolerance( timetolerance );
        
        if (null != timezoneId) {
            TimeGetter.setTimeZone( timezoneId );
        } else {
            log.warn( "No timezone for TLS timestamps defined" );
            
            if (null != smm) {
                smm.sendMessage( "No timezone for TLS timestamps defined" );
            }
        }
        
        return transformer;
    }
    
    @Bean( name = "initInvalidFloat" )
    public int initInvalidFloat( @Qualifier("floatInvalid") String floatInvalid ) {
        FunctionInval.setFloatInvalid( floatInvalid );
        return 42;
    }

}
