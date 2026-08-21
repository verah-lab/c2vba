package de.heuboe.asfinag.tls.tele.in;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.Vmis2SystemMessageManagement;
import de.heuboe.tls.receiver.core.adrcvt.AddressConverterTls;
import de.heuboe.tls.receiver.core.telein.TlsKafkaTelgramReceiver;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(KafkaProperties.class)
@EnableKafka // Required for @KafkaListener
public class SpringConfig {
//    @Bean
//    ProtoPojoKafkaMessageConverter messageConverter() {
//        return new ProtoPojoKafkaMessageConverter();
//    }
//    @Bean
    TlsKafkaTelgramReceiver  receiver () {
        return new TlsKafkaTelgramReceiver();
    }
    
//    @Bean
//    AddressConverter getAddressConverter() {
//        return new AddressConverterTls();
//    }
    
    @Value("#{changeTopic}")
    String chgTopic;
    
    @Value("#{changeTopicGroupId}")
    String chgTopicGroupId;

    @Bean( name = "changeTopic" )
    public String getChgTopic( @Value( "${de.heuboe.tls.receiver.config.chg.changetopic}" ) String topName ) {
        return topName;
    }

    @Bean( name = "changeTopicGroupId" )
    public String getChgTopicGrpId( @Value( "${de.heuboe.tls.receiver.config.chg.changetopicgroupid}" ) String topName ) {
        return topName;
    }

    @Bean
    public SystemMessageManagement getMessageManagement() {
        return new Vmis2SystemMessageManagement();
    }
}
