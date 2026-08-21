package de.heuboe.tls.receiver.test.config;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import lombok.extern.slf4j.Slf4j;

/**
 * The project specific implementation of the sequencer message management interface.
 */
@Slf4j
public class Vmis2SystemMessageManagement implements SystemMessageManagement {


    @Autowired
    private Vmis2SystemMessageManagementProperties properties;

    @Override
    public void sendMessage(String message) {
        log.info("Message(1) sent to message management: {}", message);
    }

    @Override
    public void sendMessage(String message, String objectId) {
        log.info("Message(2) sent to message management: {}", message);
    }

    /**
     * Sends a message to the message management.
     *
     * @param message  The message that should be sent.
     * @param objectId The object id the message references.
     * @param groupId  The group id the message belongs to.
     * @param line 
     * @param callingMethod 
     * @param classname 
     */
    private void sendMessage(String message, String objectId, int groupId, String classname, String callingMethod, int line) {
        log.info("Message(4) sent to message management: {}", message);
    }

}
