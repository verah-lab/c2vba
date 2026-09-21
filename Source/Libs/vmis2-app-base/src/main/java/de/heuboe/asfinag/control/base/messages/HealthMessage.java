package de.heuboe.asfinag.control.base.messages;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 * Health message of actor.
 */
public class HealthMessage {
    /**
     * status of system or sub-system
     */
    public enum Status {OK, NOT_OK}

    /**
     * overall status message as return of the AskStatus message
     */
    @Value
    @Builder
    public static class AppStatus {
        Status status;
        String failureMessage;
    }

    /**
     * ask actor for system status
     */
    @Data
    @Builder
    public static class AskStatus {
    }
}
