package de.c2vba.tls.sequencer.config;

import de.heuboe.tls.sequencer.config.SequencerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This is the main sequencer configuration class that handle all necessary initialization. The {@link SequencerConfig}
 * this class extends implements all necessary components for a default sequencer setting.
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = {"de.heuboe.tls.sequencer"})
public class C2VBASequencerConfig extends SequencerConfig {
    // currently no project specific implementation necessary
}