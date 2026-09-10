package de.heuboe.datex2.mdp.builder;

import de.heuboe.datex2.base.config.ConnectionParameter;
import de.heuboe.datex2.config.persistence.*;
import de.heuboe.datex2.config.persistence.repository.*;
import de.heuboe.datex2.mdp.builder.writer.D2MDPPubWriter;
import de.heuboe.util.DirectoryCleaner;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import io.vavr.collection.List;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class
 */
@EnableKafka
@EnableAutoConfiguration
@Configuration
@EnableScheduling
@EnableMongoRepositories(basePackages = "de.heuboe.datex2.config.persistence.repository")
public class Config {


    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Bean
    D2MDPPublisherVSInfo getD2MDPPublisherVSInfo(Properties props, ConnectionParameter conProps) {
        return new D2MDPPublisherVSInfo(conProps.getPubPath(), props.getMstId());
    }

    @Bean
    Storage getStorage(@Autowired Datex2MSTRepository mstRepo,
                       @Autowired Datex2MSTDataSourceRepository dSRepo,
                       @Autowired Datex2MSTLocationRepository locRepo,
                       @Autowired Datex2MSTItemRepository itemRepo, Properties props) {
        java.util.List<Datex2MST> byIdVersion = mstRepo.findByIdVersion(props.getMstId(), props.getMstVersion());
        Datex2MST msts = byIdVersion.stream().findFirst().orElseThrow(()-> new IllegalArgumentException("No MST found!"));
        List<Datex2MSTDataSource> dataSources = List.ofAll(dSRepo.findByMstVersion(props.getMstId(), props.getMstVersion()));
        List<Datex2MSTLocation> locations = List.ofAll(locRepo.findByMstVersion(props.getMstId(), props.getMstVersion()));
        List<Datex2MSTItem> items = List.ofAll(itemRepo.findByMstVersion(props.getMstId(), props.getMstVersion()));
        return new Storage(msts, dataSources, locations, items);
    }

    @Bean
    String[] getTopics(Storage storage, @Value("${de.heuboe.datex2.mdp.deFehlerTopic}") String errorTopic) {
        java.util.Set<String> topics = new HashSet<>(storage.getDataSources().map(Datex2MSTDataSource::getDatakind).asJava());
        topics.add(errorTopic);
        return topics.toArray(String[]::new);
    }

    @Bean
    DirectoryCleaner directoryCleaner(ConnectionParameter conParams) throws IOException {
        String pubFileDir = conParams.getPubPath();
        File dir = new File(pubFileDir);
        dir.mkdirs();

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("No access to pubFileDir <" + pubFileDir + "> !");
        }

        try {
            DirectoryCleaner dc = new DirectoryCleaner(pubFileDir);
            dc.start();
            return dc;
        } catch (IOException ex) {
            throw new IOException("DirectoryCleaner: no access to pubFileDir <" + pubFileDir + "> !");
        }
    }

    @Bean
    D2MDPDataReceiver setActivationListener(D2MDPDataReceiver listener, @Autowired String[] topics) {
        KafkaMessageListenerContainer<String, byte[]> container = createContainer(createFactory(), createProperties(listener, topics));
        container.start();
        return listener;
    }

    private ContainerProperties createProperties(D2MDPDataReceiver listener, String[] topics) {
        ContainerProperties containerProps = new ContainerProperties(topics);
        containerProps.setMessageListener(listener);
        return containerProps;
    }

    private KafkaMessageListenerContainer<String, byte[]> createContainer(DefaultKafkaConsumerFactory<String, byte[]> factory, ContainerProperties containerProps) {
        return new KafkaMessageListenerContainer<>(factory, containerProps);
    }

    private DefaultKafkaConsumerFactory<String, byte[]> createFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(properties);
    }

}
