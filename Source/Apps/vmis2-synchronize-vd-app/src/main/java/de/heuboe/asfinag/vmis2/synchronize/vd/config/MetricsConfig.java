package de.heuboe.asfinag.vmis2.synchronize.vd.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Contains all relevant configuration for metrics. However it does not define library specific
 * metrics.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
public class MetricsConfig {

    /**
     * Get the MeterRegistryCustomizer bean. 
     * 
     * @return  MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        final Package pack = getClass().getPackage();
        return registry -> registry.config()
                .commonTags(
                // @formatter:off
"implementation-title",	Objects.toString(pack.getImplementationTitle(), "vmis2-synchronize-vd-app"),
"implementation-version", Objects.toString(pack.getImplementationVersion(), "DEV")
				// @formatter:on
                );
    }

}
