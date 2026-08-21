package de.heuboe.tls.receiver.core.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Contains all relevant configuration for Kafka.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
@EnableKafka // Required for @KafkaListener
public class KafkaConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Map<String, KafkaTemplate<?, ?>> kafkaTemplates;
    
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

    /**
     * Registers all kafka metrics to micrometer.
     */
    @PostConstruct
    protected void initMetrics() {
        final String kafkaPrefix = "kafka.";
        for (final Entry<String, KafkaTemplate<?, ?>> templateEntry : this.kafkaTemplates.entrySet()) {
            final String name = templateEntry.getKey();
            final KafkaTemplate<?, ?> kafkaTemplate = templateEntry.getValue();
            for (final Metric metric : kafkaTemplate.metrics().values()) {
                final MetricName metricName = metric.metricName();
                final Builder<?> gaugeBuilder = Gauge
                        .builder(kafkaPrefix + metricName.name(), asMetricsValueSupplier(metric))
                        .description(metricName.description());
                gaugeBuilder.tag("bean", name);
                gaugeBuilder.register(this.meterRegistry);
            }
        }
    }

    /**
     * Converts the given kafka metric to a supplier for micrometer metrics.
     *
     * @param metric The metrics to convert.
     * @return The supplier for micrometer metrics.
     */
    private Supplier<Number> asMetricsValueSupplier(final Metric metric) {
        return () -> metric.metricValue() instanceof Number
                ? ((Number) metric.metricValue())
                : Double.NaN;
    }

    /**
     * Defines a health indicator for kafka.
     *
     * @param adminClient The kafka admin client to use for fetching the status.
     * @return A newly created health indicator for kafka.
     */
    @Bean
    public HealthIndicator kafkaHealthIndicator(final AdminClient adminClient) {
        final DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions().timeoutMs(1000);
        return () -> {
            final DescribeClusterResult describeCluster = adminClient.describeCluster(describeClusterOptions);
            try {
                final String clusterId = describeCluster.clusterId().get();
                final int nodeCount = describeCluster.nodes().get().size();
                return Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return Health.down()
                        .withException(e)
                        .build();
            } catch (final ExecutionException e) {
                return Health.down()
                        .withException(e)
                        .build();
            }
        };

    }


}
