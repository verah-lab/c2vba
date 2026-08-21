package de.heuboe.asfinag.vmis2.synchronize.vd.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;

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
    private Map<String, KafkaTemplate<?, ?>> kafkaTemplateMap;
    @PostConstruct
    protected void initMetrics() {
        final String kafkaPrefix = "kafka.";
        for (final Entry<String, KafkaTemplate<?, ?>> templateEntry : this.kafkaTemplateMap.entrySet()) {
            final String name = templateEntry.getKey();
            final KafkaTemplate<?, ?> kafkaTemplate = templateEntry.getValue();
            for (final Metric metric : kafkaTemplate.metrics().values()) {
                final MetricName metricName = metric.metricName();
                Gauge.builder(kafkaPrefix + metricName.name(), asMetricsValueSupplier(metric))
                     .description(metricName.description())
                     .tag("bean", name)
                     .register(this.meterRegistry);
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
     * @param admin the kafkaAdmin bean.
     * @return A newly created health indicator for kafka.
     */
    @Bean
    public HealthIndicator kafkaHealthIndicator(KafkaAdmin admin) {
        final DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions().timeoutMs(1000);
        return () -> {
            final DescribeClusterResult describeCluster = kafkaAdminClient(admin).describeCluster(describeClusterOptions);
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

    /**
     * Returns a new Kafka {@link AdminClient} which can be used to configure topics.
     *
     * @param admin     the KafkaAdmin bean.
     * @return The newly created Kafka AdminClient
     */
    @Bean
    public AdminClient kafkaAdminClient(KafkaAdmin admin) {
        return AdminClient.create(admin.getConfigurationProperties());
    }
}
