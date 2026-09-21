package de.heuboe.asfinag.control.base.services;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@EnableKafka // Required for @KafkaListener
public class KafkaConfig {

    /**
     * Returns a new Kafka {@link AdminClient} which can be used to configure topics.
     *
     * @param admin The kafka admin instance to get the config from.
     * @return The newly created Kafka AdminClient
     */
    @Bean
    public AdminClient kafkaAdminClient(final KafkaAdmin admin) {
        return AdminClient.create(admin.getConfigurationProperties());
    }

    @Bean
    public RuntimeBeanFactory runtimeBeanFactory() {
        return new RuntimeBeanFactory() {
            public Runner createRuntimeBean(Runner.CallbackInterface callback, boolean lastForEachKey) {
                return runnerBean(callback, lastForEachKey);
            }
        };
    }

    @Bean
    public RuntimeBeanFactoryNotCompacted runtimeBeanFactoryNotCompacted() {
        return new RuntimeBeanFactoryNotCompacted() {
            public RunnerNotCompacted createRuntimeBean(RunnerNotCompacted.CallbackInterface callback, boolean lastForEachKey) {
                return runnerNotCompactedBean(callback, lastForEachKey);
            }
        };
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Runner runnerBean(Runner.CallbackInterface callback, boolean lastForEachKey) {
        return new Runner(callback, lastForEachKey);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    RunnerNotCompacted runnerNotCompactedBean(RunnerNotCompacted.CallbackInterface callback, boolean lastForEachKey) {
        return new RunnerNotCompacted(callback, lastForEachKey);
    }

}
