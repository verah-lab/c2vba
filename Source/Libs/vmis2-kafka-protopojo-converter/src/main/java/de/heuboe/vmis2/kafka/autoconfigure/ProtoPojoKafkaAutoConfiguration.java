package de.heuboe.vmis2.kafka.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;

/**
 * Auto configuration for the kafka message converter.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@AutoConfiguration(before = {KafkaAutoConfiguration.class})
public class ProtoPojoKafkaAutoConfiguration {

    /**
     * Automatically create a kafka message converter, if it does not yet exist in the context.
     *
     * @param producerName The optional name of the producer to use. Useful to disambiguate the sender
     *        of a message in kafka.
     * @return The kafka message converter.
     */
    @Bean
    @ConditionalOnMissingBean(RecordMessageConverter.class)
    public ProtoPojoKafkaMessageConverter protoPojoKafkaMessageConverter(
            @Value("${de.heuboe.kafka.producer.name:#{null}}") final String producerName) {

        return new ProtoPojoKafkaMessageConverter(producerName);
    }

}
