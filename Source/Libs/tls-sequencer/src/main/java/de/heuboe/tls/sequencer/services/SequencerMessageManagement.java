package de.heuboe.tls.sequencer.services;

/**
 * The sequencer message management interface that must be implemented by the implementing projects.
 */
public interface SequencerMessageManagement {

    /**
     * Sends the passed message to the message management.
     *
     * @param message String that should be sent to message management.
     */
    void sendMessage(String message);

    /**
     * Sends the passed message and object id to the message management.
     *
     * @param message  String that should be sent to message management.
     * @param objectId The location object that is involved in the message.
     */
    void sendMessage(String message, String objectId);

}
