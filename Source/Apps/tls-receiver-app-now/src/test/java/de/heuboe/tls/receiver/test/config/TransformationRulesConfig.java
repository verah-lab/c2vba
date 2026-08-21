package de.heuboe.tls.receiver.test.config;

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
    
    private static class MsgHdl implements SystemMessageManagement {

        @Override
        public void sendMessage( String message ) {
            System.out.println( "MessageManagement: " + message );
        }

        @Override
        public void sendMessage( String message, String objectId ) {
            System.out.println( "MessageManagement: " + message + " ObjectId: " + objectId );
        }
        
    }

    @Bean
    TransformationRulesContainer getTransformationRules() throws IOException {
        TransformationReader rdr = new TransformationReaderImpl();
        
        return rdr.createTransformationRules( new NowRcvScriptGetter().concatAllInputStreams()/*new
         File( specFile
         )*/, new MsgHdl() );
    }
}
