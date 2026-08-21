package de.heuboe.tls.receiver.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Contains all relevant configuration for metrics. However it does not define library specific
 * metrics.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
public class MetricsConfig {
    
    @Autowired @Qualifier(  "uzId" )
    public String uzId;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "uzId", uzId
                );
    }
    
}
