package de.heuboe.tls.ifacewancom.config;

import java.util.Objects;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Contains all relevant configuration for metrics. However it does not define library specific
 * metrics.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
public class MetricsConfig {

    /**
     * Adds some optional tags to all metrics. Useful when running multiple variants of the same
     * application (e.g. with different parameters).
     *
     * @return The MeterRegistryCustomizer bean.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        final Package pack = getClass().getPackage();
        return registry -> registry.config()
            .commonTags(
            // @formatter:off
"implementation-title", Objects.toString(pack.getImplementationTitle(), "Kafka-Template"),
"implementation-version", Objects.toString(pack.getImplementationVersion(), "DEV")
            // @formatter:on
            );
    }

}
