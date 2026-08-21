package de.heuboe.tls.ifacewancom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Contains all relevant configuration for Jackson's ObjectMapper.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates a new Jackson ObjectMapper for Json.
     *
     * @return The newly created Jackson ObjectMapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        configureJackson(objectMapper);
        return objectMapper;
    }

    // Add XmlMapper or other Jackson stuff here.

    /**
     * Configures some shared configuration options on the given ObjectMapper.
     *
     * @param objectMapper The objectMapper to apply the default config to.
     */
    private void configureJackson(final ObjectMapper objectMapper) {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
