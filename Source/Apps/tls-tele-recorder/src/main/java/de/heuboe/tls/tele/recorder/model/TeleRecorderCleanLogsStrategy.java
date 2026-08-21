package de.heuboe.tls.tele.recorder.model;

/**
 * An enum class that hold the possible log cleaning strategies that can be configured via application.yml.
 */
public enum TeleRecorderCleanLogsStrategy {
    NUMBER,
    SIZE,
    NONE
}
