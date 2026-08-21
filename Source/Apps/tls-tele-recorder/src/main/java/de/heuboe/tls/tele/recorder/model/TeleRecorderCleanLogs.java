package de.heuboe.tls.tele.recorder.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class for log cleaning.
 */
@Getter
@Setter
public class TeleRecorderCleanLogs {

    /* Determine the clean log strategy. Depending on this value one of the following parameter will be used. */
    private TeleRecorderCleanLogsStrategy strategy;

    /* Number of telegrams that should be kept (0 means unlimited telegram files). */
    private int number;

    /* Size in Megabyte that should be kept (0 means unlimited telegram files). */
    private float size;
}
