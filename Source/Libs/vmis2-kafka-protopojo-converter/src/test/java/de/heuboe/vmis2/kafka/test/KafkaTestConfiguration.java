package de.heuboe.vmis2.kafka.test;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;

@EnableKafka
@Configuration
public class KafkaTestConfiguration {

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbeded;

    @Bean
    ProtoPojoKafkaMessageConverter messageConverter() {
        return new ProtoPojoKafkaMessageConverter();
    }

    private Map<String, Object> producerProperties() {
        final Map<String, Object> properties = KafkaTestUtils.producerProps(this.kafkaEmbeded);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return properties;
    }

    @Bean
    KafkaListenerContainerFactory<?> kafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setMessageConverter(messageConverter());
        factory.setMissingTopicsFatal(false);
        return factory;
    }

    @Bean
    ProducerFactory<Object, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProperties());
    }

    private Map<String, Object> consumerProperties() {
        final Map<String, Object> properties =
                KafkaTestUtils.consumerProps("junit-test", "true", this.kafkaEmbeded);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return properties;
    }

    @Bean
    ConsumerFactory<Object, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProperties());
    }

    @Bean
    KafkaTemplate<Object, Object> kafkaTemplate() {
        final KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setMessageConverter(messageConverter());
        return kafkaTemplate;
    }

}
