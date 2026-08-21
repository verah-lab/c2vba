package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Algorithm parameter for the collecting and time synchronization algorithm
 *
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AlgoParameter extends AbstractInfraParameter {

    /**
     * 
     * Timeout in seconds for interval length
     *
     */
    @Value
    public static class IntervalTimout {
        private IntervalLengthValue intervalLength;
        private Integer timeout;
    }
    
    /**
     * Temporary lower and upper threshold for interval length
     *
     */
    @Value
    public static class IntervalTemporaryThresholds {
        IntervalLengthValue intervalLength;
        Integer lowerThreshold;
        Integer upperThreshold;
    }
    
    /**
     * Time lead for interval length
     *
     */
    @Value
    public static class IntervalTimeLead {
        IntervalLengthValue intervalLength;
        Integer timeLead;
    }

    private final Map<IntervalLengthValue, IntervalTimout> timeouts = new HashMap<>();
    private final Map<IntervalLengthValue, IntervalTemporaryThresholds> thresholds = new HashMap<>();
    private final Map<IntervalLengthValue, IntervalTimeLead> timeLeads = new HashMap<>();
    private int resendTimeSyncWaitTime;
    private int defaultTimeLead;


    /**
     * Constructor
     *
     * @param id Id or corresponding infrastructure object (UZ)
     * @param type Is the parameter a default or is it directly set
     * @param time Time of last change
     * @param resendTimeSyncWaitTime Wait time for resending time synchronization
     * @param defaultTimeLead Default time lead in seconds for all intervals, if not set in
     *        {@code List<TimeLead> }
     * @param timeouts Timeout in seconds for each interval length
     * @param timeLeads Data may be x seconds too early for each interval
     * @param thresholds A lower and a upper temporary threshold in seconds for each interval length
     */
    public AlgoParameter(String id, Type type, Instant time, int resendTimeSyncWaitTime, int defaultTimeLead,
            List<IntervalTimout> timeouts, List<IntervalTimeLead> timeLeads, List<IntervalTemporaryThresholds> thresholds) {
        super(id, type, time);
        this.resendTimeSyncWaitTime = resendTimeSyncWaitTime;
        this.defaultTimeLead = defaultTimeLead;
        if (timeouts != null && !timeouts.isEmpty()) {
            timeouts.forEach(d -> this.timeouts.put(d.getIntervalLength(), d));
        }
        if (thresholds != null && !thresholds.isEmpty()) {
            thresholds.forEach(d -> this.thresholds.put(d.getIntervalLength(), d));
        }
        if (timeLeads != null && !timeLeads.isEmpty()) {
            timeLeads.forEach(d -> this.timeLeads.put(d.getIntervalLength(), d));
        }
    }

    /**
     * Get the configured timeout in seconds for one interval length in seconds
     *
     * @param seconds Interval length in seconds
     * @return the timeout or an empty Optional
     */
    public Optional<Integer> getTimeoutForInterval(int seconds) {
        return IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds).equals(IntervalLengthValue.UNDEFINED)
                ? Optional.empty()
                : Optional.of(IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds)).map(timeouts::get)
                .map(IntervalTimout::getTimeout);
    }

    /**
     * Get the configured timeout in seconds for one interval length as enum value
     *
     * @param ilValue Interval length represented by type {@link IntervalLengthValue}
     * @return the timeout or an empty Optional
     */
    public Optional<Integer> getTimeoutForInterval(IntervalLengthValue ilValue) {
        return Optional.ofNullable(timeouts.get(ilValue)).map(IntervalTimout::getTimeout);
    }

    /**
     * Get the configured time lead in seconds for one interval length in seconds
     *
     * @param seconds Interval length in seconds
     * @return the time lead or an empty Optional
     */
    public Optional<Integer> getTimeLeadForInterval(int seconds) {
        return IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds).equals(IntervalLengthValue.UNDEFINED)
                ? Optional.empty()
                : Optional.of(IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds)).map(timeLeads::get)
                        .map(IntervalTimeLead::getTimeLead);
      }

    /**
     * Get the configured time lead in seconds for one interval length as enum value
     *
     * @param ilValue Interval length represented by type {@link IntervalLengthValue}
     * @return the time lead or an empty Optional
     */
    public Optional<Integer> getTimeLeadForInterval(IntervalLengthValue ilValue) {
        return Optional.ofNullable(timeLeads.get(ilValue)).map(IntervalTimeLead::getTimeLead);
    }

    /**
     * Get the configured lower temporary threshold in seconds for one interval length in seconds
     *
     * @param seconds Interval length in seconds
     * @return the lower temporary threshold or an empty Optional
     */
    public Optional<Integer> getLowerTemporayThresholdForInterval(int seconds) {
        return IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds).equals(IntervalLengthValue.UNDEFINED)
                ? Optional.empty()
                : Optional.of(IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds)).map(thresholds::get)
                .map(IntervalTemporaryThresholds::getLowerThreshold);
    }

    /**
     * Get the configured lower temporary threshold in seconds for one interval length as enum value
     * 
     * @param ilValue Interval length represented by type {@link IntervalLengthValue}
     * @return the lower temporary threshold or an empty Optional
     */
    public Optional<Integer> getLowerTemporayThresholdForInterval(IntervalLengthValue ilValue) {
        return Optional.ofNullable(thresholds.get(ilValue)).map(IntervalTemporaryThresholds::getLowerThreshold);
    }

    /**
     * Get the configured upper temporary threshold in seconds for one interval length in seconds
     *
     * @param seconds Interval length in seconds
     * @return the upper temporary threshold or an empty Optional
     */
    public Optional<Integer> getUpperTemporayThresholdForInterval(int seconds) {
        return IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds).equals(IntervalLengthValue.UNDEFINED)
                ? Optional.empty()
                : Optional.of(IntervalLengthValue.getIntervalLengthValueOfSeconds(seconds)).map(thresholds::get)
                .map(IntervalTemporaryThresholds::getUpperThreshold);
    }

    /**
     * Get the configured upper temporary threshold in seconds for one interval length as enum value
     *
     * @param ilValue Interval length represented by type {@link IntervalLengthValue}
     * @return the upper temporary threshold or an empty Optional
     */
    public Optional<Integer> getUpperTemporayThresholdForInterval(IntervalLengthValue ilValue) {
        return Optional.ofNullable(thresholds.get(ilValue)).map(IntervalTemporaryThresholds::getUpperThreshold);
    }

}
