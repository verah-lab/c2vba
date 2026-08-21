package de.heuboe.tls.receiver.now.config;

import de.heuboe.now.tls.resources.generator.proto.NowRcvScriptGetter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.rdr.impl.TransformationReaderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class TransformationRulesConfig {

    @Bean
    TransformationRulesContainer getTransformationRules( SystemMessageManagement smm ) throws IOException {
        TransformationReader rdr = new TransformationReaderImpl();
        
        return rdr.createTransformationRules( new NowRcvScriptGetter().concatAllInputStreams()
                 /*new File( specFile )*/, smm );
    }
}
