package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.synchronize.vd.publish.SyncVdPublisher;
import io.vavr.Tuple3;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


/**
 * Collecting and time synchronization algorithm
 *
 * @param <D> Type of input data
 * 
 */
@Slf4j
public class SyncVdAlgo<D extends AbstractData> {

    private Marker logMarker1 = MarkerFactory.getMarker("sync");   

    private SyncVdPublisher<D> publisher;
    private Instant lastWriteTimeout;
    private Instant lastIntervalEndTimeout;
    private Instant lastTimeSync;
    private Map<String, InfrastructureObject> infraObjects = new HashMap<>();
    private AlgoParameter algoParameter = null;
    private Map<String, InfraParameter> infraParameter = new HashMap<>();
    private Map<String, InfraState> infraStates = new HashMap<>();
    private Map<ILengthIntervalEndInfraId, Optional<D>> intervalInfraId2Data = new HashMap<>();
    private Map<ILengthIntervalEndInfraId, SingleVehicleData> intervalInfraId2SVehData = new HashMap<>();
    private Set<ILength2IntervalEnd> publishBeforeTimeout = new HashSet<>();
    private Set<ILength2IntervalEnd> acceptedEarly = new HashSet<>();
    private Clock clock;
    private boolean waitingForInitialInfo = true;
    
    @Value
    protected static class ILength2IntervalEnd {
        int intervalLength;
        Instant intervalEnd;
    }

    @Data
    @AllArgsConstructor
    protected static class ILengthIntervalEndInfraId {
        String id;
        int intervalLength;
        Instant intervalEnd;
    }

    /**
     * Constructor
     * 
     * @param publisher Receives the collected data, the infrastructure object ids without data and the
     *        order for time synchronization
     * @param clock The injected clock. Can be use to run a fake clock in tests.
     */
    public SyncVdAlgo(SyncVdPublisher<D> publisher, Clock clock) {
        log.info(logMarker1, "Constructor call of SyncVdAlgo");
        this.publisher = publisher;
        this.clock = clock;
        this.lastWriteTimeout = Instant.now(clock);
        this.lastIntervalEndTimeout = Instant.now(clock);
        this.lastTimeSync = Instant.MIN;
        this.lastTimeSync = Instant.now(clock);
    }

    /**
     * Set complete infrastructure for algorithm
     * 
     * @param infraObjects All infrastructure objects for which the algorithm works
     */
    public void setInfrastructure(List<InfrastructureObject> infraObjects) {
        log.info (logMarker1, "Set complete infrastructure for algo");
        if (infraObjects == null || infraObjects.isEmpty()) {
            log.error(logMarker1, "Infrastructure objects are set to null !");
            throw new IllegalArgumentException("Infrastructure objects are set to null or empty");
        }
        this.infraObjects =
                infraObjects.stream().collect(Collectors.toMap(InfrastructureObject::getId, Function.identity()));
        publisher.initInfrastructure();
        initialInfoComplete(waitingForInitialInfo);
    }

    /**
     * Set the algorithm parameter, such as timeouts etc.
     * 
     * @param algoParameter Algorithm parameter
     */
    public void setAlgoParameter(AlgoParameter algoParameter) {
        log.info (logMarker1, "Set algo parameter");
        if (algoParameter == null) {
            log.error(logMarker1, "Algorithm parameter are set to null !");
            throw new IllegalArgumentException("Algorithm parameter are set to null");
        }
        this.algoParameter = algoParameter;
        initialInfoComplete(waitingForInitialInfo);
    }

    /**
     * Set complete infrastructure parameter for algorithm
     * 
     * @param infraParameter Infrastructure parameters per infrastructure object
     */
    public void setInfraParameter(Map<String, InfraParameter> infraParameter) {
        log.debug (logMarker1, "Set new infra parameter");
        if (infraParameter == null || infraParameter.isEmpty()) {
            log.error(logMarker1, "Infra parameter are set to null !");
            throw new IllegalArgumentException("Infra parameter are set to null or empty");
        }
        this.infraParameter = infraParameter;
        
        initialInfoComplete(waitingForInitialInfo);
    }

    /**
     * Set complete infrastructure states for algorithm
     * 
     * @param infraStates infrastructure states per infrastructure object
     */
    public void setInfraState(Map<String, InfraState> infraStates) {
        log.debug (logMarker1, "Set new infra states");
        
        if (infraStates == null || infraStates.isEmpty()) {
            log.error(logMarker1, "Infra states are set to null !");
            throw new IllegalArgumentException("Infra states are set to null or empty!");
        }
        this.infraStates = infraStates;
        
        // For new infra states, it should always be checked whether considered intervals are complete.
        initialInfoComplete(true);
    }
    
    /**
     * Check if all parameters, all states and the infrastructure are present.
     * @param control If true, check if all parameters, all states and the infrastructure are present.
     */
    private void initialInfoComplete (boolean control) {
        if (!control) {
            return;
        }
        if (infraObjects.isEmpty()) {
            log.info(logMarker1, "Waiting for infrastructure to be set!");
            return;
        }
        if (algoParameter == null) {
            log.info(logMarker1, "Waiting for the algo parameters to be set!");
            return;
        }
        if (infraStates.isEmpty()) {
            log.info(logMarker1, "Waiting for infra states to be set!");
            return;
        }
         if (infraParameter.isEmpty()) {
            log.info(logMarker1, "Waiting for infra parameters to be set!");
            return;
        }
        log.debug("Initial info is complete(parameters, states, infrastructure).");
        
        // If all parameters, all states and the infrastructure are present check if there already values
        // for the considered intervals.
        consideredIntervalsCompleted();

        waitingForInitialInfo = false;
    }
    
    /**
     * Check if there already data for the considered intervals. If yes, check if they are complete
     * and if data should be publish.
     */
    private void consideredIntervalsCompleted() {
        // If all parameters, all states and the infrastructure are present check if there already values
        // for the considered intervals.
        Instant now = Instant.now(this.clock);
        Set<ILength2IntervalEnd> acceptedIntervals = getProcessedIntervalsWithData().stream()
                .filter(iv -> !iv.getIntervalEnd().isAfter(this.lastIntervalEndTimeout)).collect(Collectors.toSet());
        if (!acceptedIntervals.isEmpty()) {
            log.info(logMarker1, "Check if considered intervalls are completed.");
            // Considered intervals complete?
            completed(acceptedIntervals, now);
        }
    }
    
    /**
     * Create an empty entry for interval length/interval end and id combination
     * 
     * @param infraId Infrastructure identification
     * @param intervalLength Interval length in seconds
     * @param intervalEnd End of interval
     * @return false if "new" entry exist
     */
    private boolean registerData(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        // Exist entry ?
        if (dataExpected(infraId, intervalLength, intervalEnd)) {
            return false;
        }
        intervalInfraId2Data.put(key, Optional.empty());
        return true;
    }

    
    /**
     * Insert data for interval length/interval end and id combination (key)
     * 
     * @param infraId Infrastructure identification
     * @param intervalLength Interval length in seconds
     * @param intervalEnd End of interval
     * @return false, if entry NOT exist for interval length/interval end and id combination (key) or if
     *         entry exist WITH data
     */
    private boolean insertData(String infraId, int intervalLength, Instant intervalEnd, D data) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        // Exist entry with data?
        if (dataReceived(infraId, intervalLength, intervalEnd)) {
            return false;
        }
        intervalInfraId2Data.put(key, Optional.of(data));
        return true;

    }

    private boolean dataExpected(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        // Exist entry for id/interval length/interval end key ?
        return intervalInfraId2Data.containsKey(key);
    }

    private boolean dataReceived(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        // Exist entry with data (D) for id/interval length/interval end key ?
        return intervalInfraId2Data.containsKey(key) && intervalInfraId2Data.get(key).isPresent();
    }

    private Optional<D> getData(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        if (dataReceived(infraId, intervalLength, intervalEnd)) {
            return intervalInfraId2Data.get(key);
        }
        return Optional.empty();
    }
    
    private Optional<SingleVehicleData> getSingleVehDataData(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);

        if (intervalInfraId2SVehData.containsKey(key)) {
            return Optional.of(intervalInfraId2SVehData.get(key));
        }
        return Optional.empty();
    }
    
    private void deleteData(String infraId, int intervalLength, Instant intervalEnd) {
        ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(infraId, intervalLength, intervalEnd);
        // Remove entries for infraId/intervalLength/intervalEnd, also Null entries
        intervalInfraId2Data.remove(key);
        // Also for the single vehicle data
        intervalInfraId2SVehData.remove(key);
    }

    /**
     * Check for which key consisting of id, interval length and interval end data are available.
     * 
     * @return Set with entries consisting of interval length and interval end for which data are
     *         available.
     */
    private Set<ILength2IntervalEnd> getProcessedIntervalsWithData() {
        // Collect entries with data (D) for id/interval length/interval end key
        List<ILengthIntervalEndInfraId> idIntervalWithData = intervalInfraId2Data.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent()).map(Entry::getKey).collect(Collectors.toList());

        // Mapping of entries with data to intervals (interval length/interval end) with data
        return idIntervalWithData.stream().map(v -> new ILength2IntervalEnd(v.getIntervalLength(), v.getIntervalEnd()))
                .collect(Collectors.toSet());
    }

    /**
     * Check if the currently considered intervals (interval length/interval end time stamp) are
     * complete(data received or in error state or channel passivated)
     * 
     * @param acceptedIntervals
     * @param timeToCompare
     */
    private void completed(Set<ILength2IntervalEnd> acceptedIntervals, Instant timeToCompare) {
        Instant now = Instant.now(clock);
        Set<ILength2IntervalEnd> publishIntervals = new HashSet<>();
        for (ILength2IntervalEnd interval : acceptedIntervals) {
            log.info(logMarker1,
                    "{}: Check if interval with length {} und interval end {} is completed, with comparison time {}",
                    now, interval.getIntervalLength(), interval.getIntervalEnd(), timeToCompare);
            Instant timeoutEnd = interval.getIntervalEnd()
                    .plusSeconds(algoParameter.getTimeoutForInterval(interval.getIntervalLength()).get());
            int numErrors = 0;
            int numLogicalPassivated = 0;
            boolean completed = true;
            for (String infraId : infraObjects.keySet()) {
                if (infraStates.get(infraId).isLogicalPassivated()) {
                    log.debug(getLaneMarker(infraId), "Is logical passivated");
                    numLogicalPassivated++;
                }
                if (dataExpected(infraId, interval.getIntervalLength(), interval.getIntervalEnd())) {
                    if(!dataReceived(infraId, interval.getIntervalLength(), interval.getIntervalEnd())) {
                        log.debug(getLaneMarker(infraId), "Data expected, but not received");
                        if (!infraStates.get(infraId).isOk() || infraStates.get(infraId).isPhysicalPassivated()) {
                            log.debug(getLaneMarker(infraId), "Is in error state or channel passivated");
                            numErrors++;
                        } else {
                            // Expected, no data received, no errors and no physical passivations => not completed
                            log.info(logMarker1, " => interval with length {} und interval end {} is NOT completed!",
                                    interval.getIntervalLength(), interval.getIntervalEnd());
                            completed = false;
                            break;
                        }
                    } else {
                        log.debug(getLaneMarker(infraId), "Data expected and received");
                    }
                }
            }
            if (completed) {
                log.info(logMarker1,
                        "Interval length {} is completed for interval end {} and timeout end {}: infra objects with errors/channel passivated {}, infra objects logical passivated {}.",
                        interval.getIntervalLength(), interval.getIntervalEnd(), timeoutEnd, numErrors,
                        numLogicalPassivated);

                if (timeToCompare.isBefore(timeoutEnd)) {
                    log.info(logMarker1, "Interval completed before the timeout end {}", timeoutEnd);
                    this.publishBeforeTimeout.add(interval);
                }
                publishIntervals.add(interval);

            } else {
                log.info(logMarker1, "Interval NOT completed for interval end {}, interval length {} and timeout end {}.",
                        interval.getIntervalEnd(), interval.getIntervalLength(), timeoutEnd);
            }
        }
        if (!publishIntervals.isEmpty()) {
            prepareForPublishing(publishIntervals);
        }
    }

    private void prepareForPublishing(Set<ILength2IntervalEnd> publishIntervals) {
        log.info(logMarker1, "{} intervals have to prepare for publishing ", publishIntervals.size());
        List<D> outPutData = new ArrayList<>();
        // Id, interval end, interval length
        List<Tuple3<String, Instant, Integer>> missingIds = new ArrayList<>();
        Map<String, SingleVehicleData> infraId2SlowestVehData = new HashMap<>();

        for (ILength2IntervalEnd publishInterval : publishIntervals) {
            log.info(logMarker1, "Prepare data for publishing the interval with length {} und interval end {}",
                    publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
            for (String infraId : infraObjects.keySet()) {
                if (dataExpected(infraId, publishInterval.getIntervalLength(), publishInterval.getIntervalEnd())) {                       
                    if (dataReceived(infraId, publishInterval.getIntervalLength(), publishInterval.getIntervalEnd())) {
                        Optional<D> data =
                                getData(infraId, publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
                        if (data.isPresent()) {
                            log.debug(getLaneMarker(infraId),
                                    "Publish and delete data for interval length {}, interval end {} in internal storage",
                                    publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
                            outPutData.add(data.get());
                            Optional<SingleVehicleData> sVehData = getSingleVehDataData(infraId,
                                    publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
                            if (sVehData.isPresent()) {
                                infraId2SlowestVehData.put(infraId, sVehData.get());
                            }
                        } else {
                            missingIds.add(new Tuple3<String, Instant, Integer>(infraId,
                                    publishInterval.getIntervalEnd(), publishInterval.getIntervalLength()));
                            log.error(getLaneMarker(infraId),
                                    "No data collected for interval length {}, interval end {} although data should be available => may not happen !",
                                    publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
                        }
                    } else {
                        missingIds.add(new Tuple3<String, Instant, Integer>(infraId, publishInterval.getIntervalEnd(),
                                publishInterval.getIntervalLength()));
                    }
                    deleteData(infraId, publishInterval.getIntervalLength(), publishInterval.getIntervalEnd());
                }
            }
        }
        log.info(logMarker1, "Publish {} data records, missing data for {} infrastructure objects ", outPutData.size(),
                missingIds.size());

        publisher.publish(outPutData, missingIds, infraParameter, infraStates, infraId2SlowestVehData);
    }

    /**
     * Set input data for infrastructure objects for algorithm
     * 
     * @param inputData List of input data
     */
    public void setData(List<D> inputData) {
        Instant now = Instant.now(clock);
        log.debug(logMarker1, "{}: {} input records received", now, inputData.size());

        int numDataAcceptedEarly = 0;
        int numDataAccepted = 0;
        int numData = 0;
        Set<ILength2IntervalEnd> acceptedIntervals = new HashSet<>();
        if (waitingForInitialInfo) {
            log.info(logMarker1,
                    "No data will be accepted until all parameters, all states and the infrastructure are present.");
            return;
        }
        for (D data : inputData) {
            numData++;
            if (data != null && continueProcessingData(data)
                    && algoParameter.getTimeoutForInterval(data.getIntervalLength()).isPresent()) {
                if (algoParameter.getTimeoutForInterval(data.getIntervalLength()).isPresent()) {
                    InfrastructureObject infraObj = infraObjects.get(data.getId());
                    InfraParameter infraPara = infraParameter.get(data.getId());

                    Instant intervalBegin = data.getEventTime();
                    Instant intervalEnd = intervalBegin.plusSeconds(infraPara.getIntervalLength());
                    Instant timeoutEnd = intervalEnd
                            .plusSeconds(algoParameter.getTimeoutForInterval(data.getIntervalLength()).orElse(0));

                    // Checking, if input data are temporal suitable
                    if (dataTemporalSuitable(data, now, intervalEnd, timeoutEnd, infraObj)) {
                        if (intervalEnd.isAfter(this.lastIntervalEndTimeout)) {
                            // This data are not yet registered as expected => register as expected
                            registerData(data.getId(), data.getIntervalLength(), intervalEnd);
                            // Now register data
                            insertData(data.getId(), data.getIntervalLength(), intervalEnd, data);
                            this.acceptedEarly.add(new ILength2IntervalEnd(data.getIntervalLength(), intervalEnd));
                            log.debug(getLaneMarker(infraObj.getId()),
                                    "Data accepted, but too early for interval end {}, interval length {}, timeout end {}",
                                    intervalEnd, data.getIntervalLength(), timeoutEnd);

                            numDataAcceptedEarly++;
                            numDataAccepted++;
                        } else if (insertData(data.getId(), data.getIntervalLength(), intervalEnd, data)) {
                            // Register data
                            numDataAccepted++;
                            log.debug(getLaneMarker(infraObj.getId()),
                                    "Data accepted for interval end {}, interval length {}, timeout end {}",
                                    intervalEnd, data.getIntervalLength(), timeoutEnd);
                            // Register interval end and interval length for accepted data
                            acceptedIntervals.add(new ILength2IntervalEnd(data.getIntervalLength(), intervalEnd));
                        } else {
                            log.debug(getLaneMarker(infraObj.getId()),
                                    "This data has already been received for interval end {}, interval length {}, timeout end {}",
                                    intervalEnd, data.getIntervalLength(), timeoutEnd);
                            // Publish data that has already been received
                            publisher.publishDiscardedData(Arrays.asList(data), infraParameter, infraStates);
                        }
                    }
                    // Test, if a time synchronization needs to be triggered
                    checkForTimeSync(data, now, intervalEnd, infraObj);
                } else {
                    log.error(logMarker1,
                            "No timeout in seconds for interval length {} found. Do not happen at this point !",
                            data.getIntervalLength());
                }
            }
        }
        log.debug(logMarker1, "{} data records were accepted, {} of them too early, {} data records discarded ", numDataAccepted,
                numDataAcceptedEarly, numData - numDataAccepted);

        if (!acceptedIntervals.isEmpty()) {
            completed(acceptedIntervals, now);
        }
    }

    private void checkForTimeSync(D data, Instant now, Instant intervalEnd, InfrastructureObject infraObj) {
        log.debug(getLaneMarker(infraObj.getId()), "At {} data received for interval end {} => check for time synchronization.", now,
                intervalEnd);

        // Test, if a time synchronization needs to be triggered
        Optional<Integer> lowerThreshold = algoParameter.getLowerTemporayThresholdForInterval(data.getIntervalLength());
        Optional<Integer> upperThreshold = algoParameter.getUpperTemporayThresholdForInterval(data.getIntervalLength());
        if (lowerThreshold.isPresent() && upperThreshold.isPresent()) {
            log.debug(logMarker1, "Lower threshold in seconds: {}", lowerThreshold.get());
            log.debug(logMarker1, "Upper threshold in seconds: {}", upperThreshold.get());
            if (now.isBefore(intervalEnd.plusSeconds(lowerThreshold.get()))
                    || now.isAfter(intervalEnd.plusSeconds(upperThreshold.get()))) {
                log.debug(logMarker1, "Now {} is outside the time synchronization range of {} to {}", now,
                        intervalEnd.plusSeconds(lowerThreshold.get()), intervalEnd.plusSeconds(upperThreshold.get()));
                if (this.lastTimeSync.equals(Instant.MIN)
                        || now.isAfter(this.lastTimeSync.plusSeconds(algoParameter.getResendTimeSyncWaitTime()))) {
                    log.debug(logMarker1, "Publish a time synchronization");
                    publisher.publishTimeSynchronization();
                    this.lastTimeSync = Instant.now(clock);
                }
            }
        } else {
            log.debug(getLaneMarker(infraObj.getId()), "Interval length {} invalid no tempory thresholds configured",
                    data.getIntervalLength());
        }
    }

    private Boolean continueProcessingData(D data) {
        Optional<Integer> timeoutSec = algoParameter.getTimeoutForInterval(data.getIntervalLength());
        String id = data.getId();
        if (!checkInfrastructure(id) || !checkInfraParameters(id) || !checkInfraStates(id)) {
            return false;
        }
        if (data.getIntervalLength() != infraParameter.get(id).getIntervalLength()) {
            log.debug(getLaneMarker(id), "Interval length {} expected, but {} received.",
                    infraObjects.get(id).getName(), infraParameter.get(id).getIntervalLength(),
                    data.getIntervalLength());
            // Publish data with not expected interval length
            publisher.publishDiscardedData(Arrays.asList(data), infraParameter, infraStates);
            return false;
        }

        if (!timeoutSec.isPresent()) {
            // TODO: Remove this message if this check has been implemented in setInfraParameter
            log.debug(logMarker1, "Interval length {} invalid no timeout configured", data.getIntervalLength());
            // Publish data with invalid interval length
            publisher.publishDiscardedData(Arrays.asList(data), infraParameter, infraStates);
            return false;
        }

        if (data.getVersion().isPresent() && infraParameter.get(id).getVersion().isPresent()
                && !data.getVersion().equals(infraParameter.get(id).getVersion())) {
            log.debug(getLaneMarker(id), "Version {} expected, but {} received.",
                    infraParameter.get(id).getVersion(), data.getVersion());
        }
        return true;
    }

    private Boolean dataTemporalSuitable(D data, Instant now, Instant intervalEnd, Instant timeoutEnd,
            InfrastructureObject infraObj) {

        // Checking, if input data are temporal suitable
        int timeLead = algoParameter.getDefaultTimeLead();
        Optional<Integer> timeLeadForInterval = algoParameter.getTimeLeadForInterval(data.getIntervalLength());
        if (timeLeadForInterval.isPresent()) {
            timeLead = timeLeadForInterval.get();
        }
        // Not to early and not to late and timeout not processed
        if (!now.isBefore(intervalEnd.minusSeconds(timeLead)) && !now.isAfter(timeoutEnd)
                && !now.isBefore(this.lastWriteTimeout)) {
            Optional<ILength2IntervalEnd> matchingInterval = this.publishBeforeTimeout.stream().filter(
                    tp -> tp.getIntervalEnd().equals(intervalEnd) && tp.getIntervalLength() == data.getIntervalLength())
                    .filter(tp -> tp.getIntervalEnd()
                            .plusSeconds(algoParameter.getTimeoutForInterval(tp.getIntervalLength()).get())
                            .equals(timeoutEnd))
                    .findFirst();
            // Was the interval already been completed early
            if (matchingInterval.isPresent()) {
                log.debug(getLaneMarker(infraObj.getId()),
                        "Interval is already completed for interval end {}, interval length {}, timeout end {}",
                        intervalEnd, data.getIntervalLength(), timeoutEnd);
                // Publish data with already completed interval
                publisher.publishDiscardedData(Arrays.asList(data), infraParameter, infraStates);
                return false;
            }
        } else {
            log.debug(infraObj.getId(),
                    "Input data outside the time window for interval end {}, interval length {}, timeout end {}",
                    intervalEnd, data.getIntervalLength(), timeoutEnd);
            // Publish data outside the time window
            publisher.publishDiscardedData(Arrays.asList(data), infraParameter, infraStates);
            return false;
        }
        return true;
    }

    /**
     * Method to call, when the end of one ore more intervals (intervalLength) has been reached.
     * 
     * @param intervalEnd Interval end, which was reached
     * @param intervalLengths Interval lengths for which the end of the interval has been reached.
     */
    public void intervalEndTrigger(Instant intervalEnd, List<Integer> intervalLengths) {
        if (waitingForInitialInfo) {
            log.info(logMarker1,
                    "No interval end trigger will be accepted until all parameters, all states and the infrastructure are present.");
            return;
        }
        Instant now = Instant.now(clock);
        int numNew = 0;
        int numEarlyIds = 0;
        this.lastIntervalEndTimeout = intervalEnd;

        log.info(logMarker1, "{}: Interval end {} received for {} interval lengths. intervalInfraId2Data.size={},"
                + " publishBeforeTimeout.size={}, acceptedEarly.size={}",
                now, intervalEnd, intervalLengths.size(),
                intervalInfraId2Data.size(), publishBeforeTimeout.size(), acceptedEarly.size());

        for (String infraId : infraObjects.keySet()) {
            if (checkInfraParameters(infraId)) {
                int intervalLength = infraParameter.get(infraId).getIntervalLength();
                Optional<Integer> timeoutSec = algoParameter.getTimeoutForInterval(intervalLength);

                if (!timeoutSec.isPresent()) {
                    // TODO: Remove this message, if this check has been implemented in setInfraParameter
                    log.warn(getLaneMarker(infraId), "Interval length {} invalid no timeout configured", intervalLength);
                    continue;
                }
                // Interval end received for interval length
                if (intervalLengths.contains(intervalLength)) {
                    numNew++;
                    Instant intervalBegin = intervalEnd.minusSeconds(intervalLength);
                    Instant timoutEnd = intervalEnd.plusSeconds(timeoutSec.get());
                    // Interval will be registered, if id does not exist
                    // If id is present, then the interval is registered "too early"
                    if (registerData(infraId, intervalLength, intervalEnd)) {
                        log.debug(getLaneMarker(infraId),
                                "Register IL {}, interval end {} and interval begin {} for timeout end {}",
                                intervalLength, intervalEnd, intervalBegin, timoutEnd);
                    } else {
                        numEarlyIds++;
                        log.debug(getLaneMarker(infraId),
                                "Interval length {}, interval end {} and interval begin {} is registered early for timeout end {}",
                                intervalLength, intervalEnd, intervalBegin, timoutEnd);
                    }
                }
            }
        }
        log.info(logMarker1, "Interval end registered for {} ids, records accepted too early for {} ids ", numNew, numEarlyIds);

        // Intervals accepted early ?
        if (!this.acceptedEarly.isEmpty()) {
            // Just check those where the interval end has just been reached (acceptedEarly.intervalEnd <=
            // intervalEnd)
            Set<ILength2IntervalEnd> checkList = this.acceptedEarly.stream()
                    .filter(tp -> !(tp.getIntervalEnd().isAfter(intervalEnd))).collect(Collectors.toSet());
            this.acceptedEarly.removeIf(tp -> !(tp.getIntervalEnd().isAfter(intervalEnd)));
            if (!checkList.isEmpty()) {
                completed(checkList, intervalEnd);
            }
        }
        // Clear up container for objects publish before timeout end <= intervalEnd(now)
        this.publishBeforeTimeout.removeIf(tp -> !(tp.getIntervalEnd()
                .plusSeconds(algoParameter.getTimeoutForInterval(tp.getIntervalLength()).get()).isAfter(intervalEnd)));
    }

    /**
     * Method to call, when the timeout has expired of one ore more intervals (interval length/interval
     * end).
     * 
     * @param timeoutEnd Timeout end (time point), which was reached.
     * @param intervalEnd Interval end for which the timeout has expired.
     * @param intervalLengths Interval lengths for which the timeout has expired.
     */
    public void intervalTimoutTrigger(Instant timeoutEnd, // NOSONAR: complexity just right
            Instant intervalEnd, List<Integer> intervalLengths) {
        if (waitingForInitialInfo) {
            log.info(logMarker1,
                    "No interval timeout trigger will be accepted until all parameters, all states and the infrastructure are present.");
            return;
        }

        Instant now = Instant.now(clock);
        this.lastIntervalEndTimeout = timeoutEnd;
        this.lastWriteTimeout = now;
        List<D> outPutData = new ArrayList<>();
        Map<String, SingleVehicleData> infraId2SlowestVehData = new HashMap<>();

        // Id, interval end, interval length
        List<Tuple3<String, Instant, Integer>> missingIds = new ArrayList<>();

        log.info(logMarker1, "{}: Timeout end {} received for interval end {} and {} interval lengths."
                + " intervalInfraId2Data.size={}, publishBeforeTimeout.size={}, acceptedEarly.size={}",
                now, timeoutEnd, intervalEnd, intervalLengths.size(), intervalInfraId2Data.size(), 
                publishBeforeTimeout.size(), acceptedEarly.size());

        for (Integer intervalLength : intervalLengths) {
            log.info(logMarker1, "Check interval length {} ", intervalLength);
            int numExpected = 0;
            int numReceived = 0;
            int numErrors = 0;
            int numLogicalPassivated = 0;
            int numMissingWithoutError = 0;

            for (String infraId : infraObjects.keySet()) {
                if (!checkInfraParameters(infraId) || !dataExpected(infraId, intervalLength, intervalEnd)) {
                    continue;
                }
                numExpected++;
                if (dataReceived(infraId, intervalLength, intervalEnd)) {
                    log.debug(getLaneMarker(infraId),
                            "Data received for interval length {} interval end {} and timeout end {}", intervalLength,
                            intervalEnd, timeoutEnd);
                    Optional<D> data = getData(infraId, intervalLength, intervalEnd);
                    if (checkInfraStates(infraId) && infraStates.get(infraId).isLogicalPassivated()) {
                        numLogicalPassivated++;
                        log.debug(getLaneMarker(infraId),
                                "Infrastructure is logical passivated for interval length {} interval end {} and timeout end {}",
                                intervalLength, intervalEnd, timeoutEnd);
                    }
                    if (data.isPresent()) {
                        numReceived++;
                        outPutData.add(data.get());
                        Optional<SingleVehicleData> sVehData = getSingleVehDataData(infraId, intervalLength, intervalEnd);
                        if (sVehData.isPresent()) {
                            infraId2SlowestVehData.put(infraId, sVehData.get());
                        }
                        deleteData(infraId, intervalLength, intervalEnd);
                    } else {
                        numMissingWithoutError++;
                        missingIds.add(new Tuple3<String, Instant, Integer>(infraId, intervalEnd, intervalLength));
                        log.error(getLaneMarker(infraId),
                                "No data collected for interval length {} and interval end {} although data should be available => may not happen!",
                                 intervalLength, intervalEnd);
                        deleteData(infraId, intervalLength, intervalEnd);
                    }
                } else {
                    boolean eFound = false;
                    missingIds.add(new Tuple3<String, Instant, Integer>(infraId, intervalEnd, intervalLength));
                    deleteData(infraId, intervalLength, intervalEnd);
                    if (checkInfraStates(infraId)) {
                        if (!infraStates.get(infraId).isOk() || infraStates.get(infraId).isPhysicalPassivated()) {
                            numErrors++;
                            eFound = true;
                            log.debug(getLaneMarker(infraId),
                                    "Infrastructure error or channel passivated received for interval length {} interval end {} and timeout end {}",
                                     intervalLength, intervalEnd, timeoutEnd);
                        } else if (infraStates.get(infraId).isLogicalPassivated()) {
                            numLogicalPassivated++;
                            log.debug(getLaneMarker(infraId),
                                    "Infrastructure is logical passivated for interval length {} interval end {} and timeout end {}",
                                     intervalLength, intervalEnd, timeoutEnd);
                        }
                    }
                    if (!eFound) {
                        log.debug(getLaneMarker(infraId),
                                "NO data and NO error/physical passivation received for interval length {} interval end {} and timeout end {}",
                                 intervalLength, intervalEnd, timeoutEnd);
                    }
                }
            }
            log.info(logMarker1, 
                    "Timeout end {} processed for interval length {} interval end {} for {} infra objects data expected:",
                    timeoutEnd, intervalLength, intervalEnd, numExpected);
            log.info(logMarker1, 
                    "Infra objects received without errors {}, infra objects with errors/channel passivated {}, infra objects locical passivated {}, infra objects missing {}",
                    numReceived, numErrors, numLogicalPassivated, missingIds.size());
        }

        // Is there anything to publish ?
        if (!outPutData.isEmpty() || !missingIds.isEmpty()) {
            log.info(logMarker1,
                    "Timeout: Publish {} data records, missing data for {} and slowest vehicle data for {} infrastructure objects ",
                    outPutData.size(), missingIds.size(), infraId2SlowestVehData.size());
            publisher.publish(outPutData, missingIds, infraParameter, infraStates, infraId2SlowestVehData);
        }
    }
    
    /**
     * Set single vehicle data for infrastructure objects for algorithm
     * 
     * @param singleVehicleData Parts of the single vehicle data needed to determine the speed of the
     *        slowest vehicle at the detector.
     */
    public void setSingleVehicleData (SingleVehicleData singleVehicleData) {
        if (waitingForInitialInfo) {
            log.info(logMarker1,
                    "No single vehicle data will be accepted until all parameters, all states and the infrastructure are present.");
            return;
        }

        Instant now = Instant.now(clock);
        log.debug(getLaneMarker(singleVehicleData.getId()),
                "{}: Single vehicle data received with speed {}, state {} and passage time {}", now, singleVehicleData.getVFZ(),
                singleVehicleData.getState(), singleVehicleData.getPassageTime());
       
        if (checkInfraParameters(singleVehicleData.getId())) {
            InfraParameter infraPara = infraParameter.get(singleVehicleData.getId());
            long rest = singleVehicleData.getPassageTime().getEpochSecond()%infraPara.getIntervalLength();
            Instant intervalBegin = singleVehicleData.getPassageTime().minusSeconds(rest).truncatedTo(ChronoUnit.SECONDS);
            Instant intervalEnd = intervalBegin.plusSeconds(infraPara.getIntervalLength());
            ILengthIntervalEndInfraId key = new ILengthIntervalEndInfraId(singleVehicleData.getId(),
                    infraPara.getIntervalLength(), intervalEnd);
            log.debug(getLaneMarker(singleVehicleData.getId()), "Constructed key {}", key);
            int compV = now.compareTo(intervalEnd);
            Instant timeoutIntervalEnd =
                    intervalEnd.plusSeconds(algoParameter.getTimeoutForInterval(infraPara.getIntervalLength()).orElse(0));
            if (compV < 0
                    || (compV >= 0 && now.isBefore(timeoutIntervalEnd) && intervalInfraId2Data.containsKey(key))) {
                 if (intervalInfraId2SVehData.containsKey(key)) {
                    // Is the current vehicle driving slower and is the status ok?
                    if (intervalInfraId2SVehData.get(key).getVFZ() > singleVehicleData.getVFZ()
                            && singleVehicleData.getState() == 0) {
                        log.debug(getLaneMarker(singleVehicleData.getId()), "Current speed {} < {}",
                                singleVehicleData.getVFZ(), intervalInfraId2SVehData.get(key).getVFZ());
                        intervalInfraId2SVehData.put(key, singleVehicleData);
                    }
                } else {
                    intervalInfraId2SVehData.put(key, singleVehicleData);
                }
            } else {
                log.debug(getLaneMarker(singleVehicleData.getId()),
                        "Passing time {} too old. The associated interval is already completed",
                        singleVehicleData.getPassageTime());
            }
        }      
    }
    
    /**
     * Checks whether infrastructure parameters for given infrastructure object id were found.
     * 
     * @param id id of infrastructure object
     * @return true if infrastructure parameters for infrastructure object with given id were found
     */
    private boolean checkInfraParameters(String id) {
        if (infraParameter == null || infraParameter.isEmpty()) {
            log.debug(logMarker1, "NO infrastructure parameters received.");
            return false;
        }

        if (infraParameter.get(id) == null) {
            log.warn(logMarker1, "No infrastructure parameter received for infrastructure object with id {}.", id);
            return false;
        }

        return true;
    }

    /**
     * Checks whether infrastructure states for given infrastructure object id were found.
     * 
     * @param id id of infrastructure object
     * @return true if infrastructure states for infrastructure object with given id were found
     */
    private boolean checkInfraStates(String id) {
        if (infraStates == null || infraStates.isEmpty()) {
            log.debug(logMarker1, "NO infrastructure states received.");
            return false;
        }

        if (infraStates.get(id) == null) {
            log.warn(getLaneMarker(id), "No infrastructure states received!");
            return false;
        }
        return true;
    }

    /**
     * Checks whether infrastructure for given infrastructure object id is found.
     * 
     * @param id id of infrastructure object
     * @return true if infrastructure for infrastructure object with given id is found
     */
    private boolean checkInfrastructure(String id) {
        if (infraObjects == null || infraObjects.isEmpty()) {
            log.debug("NO infrastructure received.");
            return false;
        }

        if (infraObjects.get(id) == null) {
            log.warn(getLaneMarker(id), "No infrastructure received!");
            return false;
        }
        return true;
    }
    
    private Marker getLaneMarker(String lane) {
        return (MarkerFactory.getMarker(lane));
    }
}
