package de.heuboe.datex2.mdp.builder;

import java.time.Instant;

import com.mongodb.lang.Nullable;

import io.vavr.collection.Map;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

/**
 * Class to hold mdp data
 */
@Value
public class SensorData {

    String id;
    Instant prozessTime;
    int validInterval;
    int interval;
    Map<String, Integer> data;
    @Nullable
    @NonFinal
    @Setter
    DeError deError;

    /**
     * Class to hold de errors
     */
    @Value
    public static class DeError {
        int errorCode;
        Instant time;
    }
}
