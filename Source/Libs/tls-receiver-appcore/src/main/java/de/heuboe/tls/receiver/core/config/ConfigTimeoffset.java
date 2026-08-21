package de.heuboe.tls.receiver.core.config;

import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up time offset properties in the application context.
 *
 * This class defines beans and configurations related to time offset management.
 * The time offset is an optional property used within the receiver, which can be
 * set via application properties or defaulted to zero if not provided.
 * <p>
 * Beans:
 * - "timeoffset0": This bean retrieves the configured time offset value from application properties. It is only initialized if the property "de.heuboe.asfinag.tls.receiver.timeoffset
 * " is explicitly set.
 * - "timeoffset": This bean represents the effective time offset. It is based on the "timeoffset0" bean if it exists, or defaults to zero otherwise.
 * <p>
 * Conditional Annotations:
 * - {@code @ConditionalOnProperty}: Ensures that a bean is created only if the corresponding property is present in the application's configuration.
 * - {@code @ConditionalOnBean}: Ensures that a bean is created only if another bean exists in the context.
 * - {@code @ConditionalOnMissingBean}: Ensures that a bean is created only if another bean does not exist in the context.
 * <p>
 * Logging:
 * - Logs the configuration values during bean initialization for debugging and traceability.
 * <p>
 * An alternative could be:
 * public String getTimeoffset( @Value( "${de.heuboe.asfinag.tls.receiver.timeoffset:0}" ) int value) {
 * <p>
 * I leave this configuration as it is now. This way it is in my opinion also possible to supply null-values
 * where they can be specified otherwise as properties/beans. This may be helpful.
 * <p>
 * The construct also demonstrates some conditional uses concerning beans.
 */

@Configuration
@EnableKafka
public class ConfigTimeoffset {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTimeoffset.class);
    
    // handle an optional property timeoffset to be used in receiver (autowired)
    
    /**
     * Retrieves the configured time offset value. If no specific value is provided
     * in the configuration, a default value of 0 will be used.
     *
     * @param value the time offset value retrieved from the property
     *              {@code de.heuboe.asfinag.tls.receiver.timeoffset}. If the property
     *              is not defined, the default value is 0.
     * @return the configured time offset, or 0 if no value is specified in the properties.
     */
    @Bean( name = "timeoffset" )
    public int getTimeoffset(
             @Value( "${de.heuboe.asfinag.tls.receiver.timeoffset:0}" ) int value ) {
        LOGGER.info( "Config: timeoffset {}", value );
        
        return value;
    }
    
    // === offsets specific to certain datatypes ....
    
    /**
     * Processes a string representation of time offsets and maps them to {@code TlsDatatypeId} objects.
     * The input string contains entries in the format "fg/id/typ!offset" separated by commas.
     * Whitespace is eliminated, and invalid entries result in an {@link IllegalArgumentException}.
     * If no specific value is provided in the configuration, a default value of "" (wmpty string) will be used.
     * <p>
     * @param value the string representation of time offsets, provided as a Spring {@code @Value}.
     *              Each entry in the string must have the format "fg/id/typ!offset", where:
     *              - {@code fg}, {@code id}, and {@code typ} represent components of a {@code TlsDatatypeId}.
     *              - {@code offset} represents the time offset value.
     * @return a map where the keys are {@code TlsDatatypeId} objects representing the parsed components,
     *         and the values are integers representing the corresponding time offsets.
     * @throws IllegalArgumentException if the input string contains invalid formatting,
     *                                  invalid numbers, or missing components.
     */
    // handle an optional property timeoffsetmap to be used in receiver (autowired)
    @Bean( name = "timeoffsetmap" )
    public Map< TlsDatatypeId, Integer /*timeoffset*/ > getTimeoffsetmap(
             @Value( "${de.heuboe.asfinag.tls.receiver.timeoffset-map:}" ) String value ) { // default empty string
        LOGGER.info( "Config: timeoffsetmap {}", value );

        // process the collection // format: fg/id/typ!offset, fg/id/typ!offset, ...
        // whitespace will be eliminated
        Map<TlsDatatypeId, Integer> map = new HashMap<>();
        String[] typeShifts = value.split(",");
        for (int i = 0; i < typeShifts.length; i++) {
            String current = typeShifts[i].trim();
            if (current.isEmpty()) {
                continue;
            }
            String[] parts = current.split("!");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format for timeoffset-map (TlsParams!): " + current);
            }
            String[] tlsParams = parts[0].split("/");
            if (tlsParams.length != 3) {
                throw new IllegalArgumentException("Invalid format for timeoffset-map (TlsParams): " + current);
            }
            try {
                short fg = Short.parseShort(tlsParams[0].trim());
                short id = Short.parseShort(tlsParams[1].trim());
                short typ = Short.parseShort(tlsParams[2].trim());
                int offset = Integer.parseInt(parts[1].trim());

                map.put(new TlsDatatypeId(fg, id, typ), offset);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number in timeoffset-map: " + current, e);
            }
        }
        return map;
    }
    
}
