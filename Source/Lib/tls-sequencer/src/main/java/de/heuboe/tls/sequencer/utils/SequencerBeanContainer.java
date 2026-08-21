package de.heuboe.tls.sequencer.utils;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.grammar.sequencer.flops.FlopStorage;
import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import de.heuboe.tls.sequencer.services.SequencerSendingService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * This container hold beans that can be used in some non bean components.
 */
@Getter
@Setter
@Component
public class SequencerBeanContainer {

    private SequencerSendingService sequencerSendingService;
    private FlopStorage flopStorage;
    private Osi7Cfg osi7Cfg;
    private SequencerMessageManagement sequencerMessageManagement;

}
