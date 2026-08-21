package de.heuboe.asfinag.vmis2.synchronize.vd.timesync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.statemachinesystems.mockclock.MockClock;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.synchronize.vd.MockObjects;
import de.heuboe.asfinag.vmis2.synchronize.vd.TCUtils;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractInfraParameter.Type;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTemporaryThresholds;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimeLead;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimout;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.IntervalLengthValue;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SingleVehicleData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SyncVdAlgo;
import de.heuboe.asfinag.vmis2.synchronize.vd.publish.SyncVdPublisher;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3.PLVEErgebnisVersion3Builder;
import io.vavr.Tuple3;

/**
 * Data acceptance (valid time slot).
 * "Prüffall 1: Datenakzeptanz Zeitfenster"
 *
 * Objective: Accepting input data received within the allowed time window <br>
 * Description: The received value is checked for the parameterized interval
 * length. It must arrive within the allowed time window. This time window is
 * defined by leadtime until "end timeout value". If a value arrives within the
 * time window, it is processed further.<br>
 * Precondition: Infrastructure is filled, algorithm and infrastructure
 * parameter are set<br>
 * Requirements:
 */
@SuppressWarnings("unchecked")
public class TC1_DataAcceptanceTest {

    private static final String              id_uz          = "UZ1";
    private static final String              id             = "2000071";
    private static final double              kmFrom         = 59;
    private static final double              kmTo           = 60;
    private static final String              roadId         = "S01_1";
    private static final IntervalLengthValue intervalLength = IntervalLengthValue.SEC_15;
    private static final PTlsDataVersion     version        = PTlsDataVersion.VERSION_3;
    private static final int defaultTimeout                 = 12;
    private static final int defaultTimelead                = 5;
    private static final int defaultSyncWait                = 40;
    private static final int defaultUpperThreshold          = 26;
    private static final int defaultLowerThreshold          = -11;
    private static final Boolean defaultvArithmetical = true;
    private static final List<Integer> categoryBoudariesPkw = List.of(10, 20, 50, 100, 120, 160, 200);
    private static final List<Integer> categoryBoudariesLkw = List.of(5, 15, 45, 95, 115);
    
    
    private static final List<InfrastructureObject> infraObjects = Arrays.asList(
            new MockObjects.Lane(id, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, kmFrom, kmTo)));
    
    /**
     * Test content 1: interval = 15s; data arrival at second 0; timeout = n.a.
     * timelead = n.a.
     */
    @Test
    public void acceptingInputData() {
        MockClock eventtimeClock = MockClock.at(2018, 12, 24, 7, 59, 45, ZoneId.systemDefault());
        MockClock processtimeClock = MockClock.at(2018, 12, 24, 7, 59, 45, ZoneId.systemDefault());
        
        int timeout = defaultTimeout;
        int timelead = defaultTimelead;
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.id(id);
        dataBuilder.qKfz(8);
        dataBuilder.qLkwAe(3);
        dataBuilder.vPkwAe(70);
        dataBuilder.vLkwAe(60);
        dataBuilder.nettozeitluecke(3.);
        dataBuilder.belegung(50);
        dataBuilder.sKfz(20);
        dataBuilder.vKfz(60);
        dataBuilder.intervalllaenge(intervalLength.getValue());
               
        // Initial start at interval begin
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, processtimeClock);
        
        Instant processingTime = Instant.now(processtimeClock);        
        initAlgo(algo, processingTime, infraObjects, Arrays.asList(id), version.getNumber(),
                intervalLength.getSeconds(), timelead, timeout, defaultLowerThreshold, defaultUpperThreshold);      
        
        processtimeClock.advanceBySeconds(15);
        Instant eventtime = Instant.now(eventtimeClock);
        Instant intervalEnd = eventtime.plusSeconds(intervalLength.getSeconds());
        Instant timeoutEnd = intervalEnd.plusSeconds(timeout);
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id), eventtime,
                processtimeClock.instant());
        
        // Trigger interval end
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(intervalLength.getSeconds()));
        
        // Set time to current sending time
        processtimeClock.advanceBySeconds(1);
        // Set input data
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder), Arrays.asList(id), eventtime,
                processtimeClock.instant());
        // Trigger timeout 
        processtimeClock.advanceBySeconds(timeout-1);
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(intervalLength.getSeconds()));

        // Test criteria / acceptance
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());
        assertTrue(argMissingIds.getValue().size()==0);
    }
    
    /**
     * Test content 1: interval = 15s; data arrival at second 0; timeout = n.a.
     * timelead = n.a.
     */
    @Test
    public void acceptingInputDataWithSingleVehData() {
        MockClock eventtimeClock = MockClock.at(2018, 12, 24, 7, 59, 45, ZoneId.systemDefault());
        MockClock processtimeClock = MockClock.at(2018, 12, 24, 7, 59, 45, ZoneId.systemDefault());
        
        int timeout = defaultTimeout;
        int timelead = defaultTimelead;
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.id(id);
        dataBuilder.qKfz(8);
        dataBuilder.qLkwAe(3);
        dataBuilder.vPkwAe(70);
        dataBuilder.vLkwAe(60);
        dataBuilder.nettozeitluecke(3.);
        dataBuilder.belegung(50);
        dataBuilder.sKfz(20);
        dataBuilder.vKfz(60);
        dataBuilder.intervalllaenge(intervalLength.getValue());
               
        // Initial start at interval begin
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, processtimeClock);
        
        Instant processingTime = Instant.now(processtimeClock);        
        initAlgo(algo, processingTime, infraObjects, Arrays.asList(id), version.getNumber(),
                intervalLength.getSeconds(), timelead, timeout, defaultLowerThreshold, defaultUpperThreshold);
        
        Instant eventtime = Instant.now(eventtimeClock);
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id), eventtime,
                processtimeClock.instant());
       
        // Set time for the single vehicle data
        processtimeClock.advanceBySeconds(1);      
        // Set single vehicle data
        algo.setSingleVehicleData(SingleVehicleData.builder()
                .id(id)
                .passageTime(Instant.now(processtimeClock))
                .quality(0)
                .state(0)
                .vehicleCategory(3)
                .vFZ(50)
                .build());
        
        // Trigger interval end
        processtimeClock.advanceBySeconds(15);
        eventtime = Instant.now(eventtimeClock);
        Instant intervalEnd = eventtime.plusSeconds(intervalLength.getSeconds());
        Instant timeoutEnd = intervalEnd.plusSeconds(timeout);
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(intervalLength.getSeconds()));
        
        // Set time to current sending time
        processtimeClock.advanceBySeconds(5);
        // Set input data
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder), Arrays.asList(id), eventtime,
                processtimeClock.instant());
        
        // Trigger timeout 
        processtimeClock.advanceBySeconds(timeout-1);
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(intervalLength.getSeconds()));

        // Test criteria / acceptance
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());
        assertTrue(argMissingIds.getValue().size()==0);
        
        // Was the slowest vehicle assigned to the same interval (slowest vehicle data was passed to the
        // publisher with the short term data)?
        assertTrue(slowestVehData.getValue().size()==1);
        assertTrue(slowestVehData.getValue().containsKey(id));        
    }

    /**
     * Test content 2: interval = 15s; data arrival at second 0; timeout = n.a.
     * timelead = 5s;
     */
    @Test
    public void acceptingInputDataWithTimelead() {
        MockClock eventtimeClock = MockClock.at(2018, 12, 24, 8, 0, 0, ZoneId.systemDefault());
        MockClock processtimeClock = MockClock.at(2018, 12, 24, 8, 0, 0, ZoneId.systemDefault());
        //MockClock processtimeClock = MockClock.at(2018, 12, 24, 8, 0, 11, ZoneId.systemDefault());
        
        Integer timeout = defaultTimeout;
        Integer timelead = 5;

        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.id(id);
        dataBuilder.qKfz(9);
        dataBuilder.qLkwAe(2);
        dataBuilder.vPkwAe(110);
        dataBuilder.vLkwAe(60);
        dataBuilder.nettozeitluecke(3.);
        dataBuilder.belegung(50);
        dataBuilder.sKfz(20);
        dataBuilder.vKfz(60);
        dataBuilder.intervalllaenge(intervalLength.getValue());        
        
        // Initial start at interval begin
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, processtimeClock);
        
        Instant processingTime = Instant.now(processtimeClock);        
        initAlgo(algo, processingTime, infraObjects, Arrays.asList(id), version.getNumber(),
                intervalLength.getSeconds(), timelead, timeout, defaultLowerThreshold, defaultUpperThreshold);

        // Set time to current sending time
        processtimeClock.advanceBySeconds(11);
        eventtimeClock.set(processingTime);
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id), eventtimeClock.instant(),
                processtimeClock.instant());

        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder), Arrays.asList(id), eventtimeClock.instant(),
                processtimeClock.instant());
      
        // Set single vehicle data
        algo.setSingleVehicleData(SingleVehicleData.builder()
                .id(id)
                .passageTime(Instant.now(processtimeClock))
                .quality(0)
                .state(0)
                .vehicleCategory(3)
                .vFZ(50)
                .build());
        
        // Trigger interval end
        processtimeClock.advanceBySeconds(4);
        Instant intervalEnd = eventtimeClock.instant().plusSeconds(intervalLength.getSeconds());
        Instant timeoutEnd = intervalEnd.plusSeconds(timeout);
        // Trigger timeout
        processtimeClock.advanceBySeconds(timeout);
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(intervalLength.getSeconds()));
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(intervalLength.getSeconds()));

        // Test criteria / acceptance
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());
        assertTrue(argMissingIds.getValue().size()==0);
        // Was the slowest vehicle assigned to the same interval (slowest vehicle data was passed to the
        // publisher with the short term data)?
        assertTrue(slowestVehData.getValue().size()==1);
        assertTrue(slowestVehData.getValue().containsKey(id));
    }

    /**
     * Test content 3: interval = 15s; data arrival at second 10; timeout = 12s;
     * timelead = n.a.;
     */
    @Test
    public void acceptingInputDataWithTimeout() {
        MockClock eventtimeClock = MockClock.at(2018, 12, 24, 8, 0, 15, ZoneId.systemDefault());
        MockClock processtimeClock = MockClock.at(2018, 12, 24, 8, 0, 16, ZoneId.systemDefault());
        
        Integer timeout = 12;
        Integer timelead = defaultTimelead;
        
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.id(id);
        dataBuilder.qKfz(4);
        dataBuilder.qLkwAe(3);
        dataBuilder.vPkwAe(121);
        dataBuilder.vLkwAe(60);
        dataBuilder.nettozeitluecke(4.);
        dataBuilder.belegung(50);
        dataBuilder.sKfz(20);
        dataBuilder.vKfz(60);
        dataBuilder.intervalllaenge(intervalLength.getValue());

        // Initial start at interval begin
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, processtimeClock);
        
        initAlgo(algo, processtimeClock.instant(), infraObjects, Arrays.asList(id), version.getNumber(),
                intervalLength.getSeconds(), timelead, timeout, defaultLowerThreshold, defaultUpperThreshold);

        // Reach interval end (30)
        processtimeClock.advanceBySeconds(14);
        Instant eventtime = Instant.now(eventtimeClock);
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id), eventtimeClock.instant(),
                processtimeClock.instant());
        Instant intervalEnd = eventtime.plusSeconds(15);
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(intervalLength.getSeconds()));
        
        // Set single vehicle data
        algo.setSingleVehicleData(SingleVehicleData.builder()
                .id(id)
                .passageTime(Instant.now(processtimeClock))
                .quality(0)
                .state(0)
                .vehicleCategory(3)
                .vFZ(50)
                .build());
        
        // Set time to current sending time (39):
        processtimeClock.advanceBySeconds(9);
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder), Arrays.asList(id), eventtime,
                processtimeClock.instant());
               
        // Trigger interval timeout (42)
        processtimeClock.advanceBySeconds(3);
        Instant timeoutEnd = intervalEnd.plusSeconds(timeout);
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(intervalLength.getSeconds()));

        // Test criteria / acceptance
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);      
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        assertTrue(argMissingIds.getValue().size()==0);
        
        // Was the slowest vehicle NOT assigned to the same interval (slowest vehicle data was NOT passed to the
        // publisher with the short term data)?
        assertTrue(slowestVehData.getValue().size()==0);
        
        // Next interval
        eventtimeClock.advanceBySeconds(15);
        processtimeClock.advanceBySeconds(1);
        
        eventtime = Instant.now(eventtimeClock);
 
        // Reach interval end (45)
        processtimeClock.advanceBySeconds(2);
        intervalEnd = eventtime.plusSeconds(15);
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(intervalLength.getSeconds()));

        // Set time to current sending time (54):
        processtimeClock.advanceBySeconds(9);
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder), Arrays.asList(id), eventtime, 
                processtimeClock.instant());
        
        // Trigger interval timeout (57)
        processtimeClock.advanceBySeconds(3);
        timeoutEnd = intervalEnd.plusSeconds(timeout);
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(intervalLength.getSeconds()));
        
        Mockito.verify(pubMock, Mockito.atLeast(2)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStates.capture(), slowestVehData.capture());
        assertTrue(argMissingIds.getValue().size()==0);
        
        // Was the slowest vehicle assigned to the same interval (slowest vehicle data was passed to the
        // publisher with the short term data)?
        assertTrue(slowestVehData.getValue().size()==1);
        assertTrue(slowestVehData.getValue().containsKey(id));
        
        
        // Set single vehicle data with old passage time
        algo.setSingleVehicleData(SingleVehicleData.builder()
                .id(id)
                .passageTime(Instant.now(eventtimeClock).minusSeconds(120))
                .quality(0)
                .state(0)
                .vehicleCategory(3)
                .vFZ(50)
                .build());
    }
    
    private static void initAlgo(SyncVdAlgo<AbstractData> algo,
            Instant initTime,
            List<InfrastructureObject> infraObjects,
            List<String> ids,
            int version,
            int intervalSeconds,
            int timelead,
            int timeout,
            int lowerThreshold,
            int upperThreshold) {
        
        //InfraObjects 
        algo.setInfrastructure(infraObjects);

        //AlgoParameter
        AlgoParameter algoParam1 = new AlgoParameter(id_uz, Type.DIRECT_SET, initTime,
                defaultSyncWait, defaultTimelead,
                Arrays.asList(new IntervalTimout(intervalLength,timeout)),
                Arrays.asList(new IntervalTimeLead(intervalLength,timelead)),
                Arrays.asList(new IntervalTemporaryThresholds(intervalLength,lowerThreshold, upperThreshold)));
        algo.setAlgoParameter(algoParam1);

        Map<String, InfraParameter> infraParams = new HashMap<>();
        for(String id : ids) {
            infraParams.put(id, new InfraParameter(id, Type.DIRECT_SET, initTime, intervalSeconds, version,
                    defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
        }
        //InfraParameter
        algo.setInfraParameter(infraParams);
    }

}
