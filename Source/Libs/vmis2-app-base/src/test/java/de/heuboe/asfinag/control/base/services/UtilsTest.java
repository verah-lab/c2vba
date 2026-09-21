package de.heuboe.asfinag.control.base.services;

import de.heuboe.asfinag.control.base.config.CheckerHealthIndicator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;


@Import({KafkaConfig.class})
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {"log.dir=target/kafka"}, topics = {

})
@EnableAutoConfiguration
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest", "spring.kafka.listener.missing-topics-fatal=false"})
@SpringJUnitConfig
@Component
@Slf4j
public class UtilsTest {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    private AdminClient kafkaAdminClient;

    @Test
    void createTopicIfNeededTest() {
        String topicname = "Testtopic";

        //topic does not exist
        ListTopicsOptions lto = new ListTopicsOptions();
        ListTopicsResult listTopicsResult = kafkaAdminClient.listTopics(lto);

        try {

            Collection<TopicListing> topicListings = listTopicsResult.listings().get();

            Optional<TopicListing> optionalTopicListing =
                    topicListings.stream().filter(tl -> tl.name().equals(topicname)).findFirst();
            assertFalse(optionalTopicListing.isPresent());
        } catch (InterruptedException e) {
            fail(e);
        } catch (ExecutionException e) {
            fail(e);
        }

        // add topic
        Utils.createTopicIfNeeded(kafkaAdminClient, topicname, true, 5, (short) 1);

        try {
            //topic exists
            listTopicsResult = kafkaAdminClient.listTopics(lto);
            Collection<TopicListing> topicListings = listTopicsResult.listings().get();

            Optional<TopicListing> optionalTopicListing =
                    topicListings.stream().filter(tl -> tl.name().equals(topicname)).findFirst();
            assertTrue(optionalTopicListing.isPresent());
        } catch (InterruptedException e) {
            fail(e);
        } catch (ExecutionException e) {
            fail(e);
        }

        // topic already present
        Utils.createTopicIfNeeded(kafkaAdminClient, topicname, true, 5, (short) 1);

        try {
            //topic exists
            listTopicsResult = kafkaAdminClient.listTopics(lto);
            Collection<TopicListing> topicListings = listTopicsResult.listings().get();

            Optional<TopicListing> optionalTopicListing =
                    topicListings.stream().filter(tl -> tl.name().equals(topicname)).findFirst();
            assertTrue(optionalTopicListing.isPresent());
        } catch (InterruptedException e) {
            fail(e);
        } catch (ExecutionException e) {
            fail(e);
        }
    }

    @Test
    void createActorNameTest() {
        assertEquals("A23_2", Utils.createActorName("A23_2"));
        assertEquals("A23+2", Utils.createActorName("A23 2"));
        assertEquals("A23%282%29", Utils.createActorName("A23(2)"));
        assertEquals("A23%2F2", Utils.createActorName("A23/2"));
        assertEquals("%C3%A4%C3%B6%C3%BC%C3%9FA%C3%96%C3%9C", Utils.createActorName("äöüßAÖÜ"));
    }

    @Test
    void waitForActorsTest() throws TimeoutException, InterruptedException {
        assertThrows(TimeoutException.class, () -> {
            Utils.waitForActors(new CheckerHealthIndicator(100) {
                @Override
                public Health health() {
                    return Health.down().build();
                }
            }, 100);
        });

        Utils.waitForActors(new CheckerHealthIndicator(100) {
            @Override
            public Health health() {
                return Health.up().build();
            }
        }, 100);
    }
}
