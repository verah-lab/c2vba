package de.heuboe.tls.receiver.now.impl;

import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * The project specific implementation of the sequencer message management interface.
 */
@Configuration
@Slf4j
public class Vmis2SystemMessageManagement implements SystemMessageManagement {
    
    // made quiet

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendMessage(String message, String objectId) {
    }

}
