package de.heuboe.asfinag.vmis2.synchronize.vd.timesync;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PLongTermBufferRecoverRequest;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3.PLVEErgebnisVersion3Builder;
import io.vavr.Tuple3;

/**
 * Missing values for ID. "Prüffall 2: Test auf Initialwert" => "Test auf fehlenden Wert"
 *
 * Objective: Mark missing ID, if no data for ID were sent<br>
 * Description: The published values has to mark missing IDs, if no measured values are sent for the
 * id.<br>
 * Precondition: Infrastructure is filled, algorithm and infrastructure parameter are set<br>
 * Requirements:
 */
@SuppressWarnings("unchecked")
public class TC2_InitialValueTest {

    private static final String id_uz = "UZ1"; // ID sending sometimes NO data
    private static final String id_incomplete = "2000071"; // ID sending sometimes NO data
    private static final double kmFrom_incomplete = 59;
    private static final double kmTo_incomplete = 60;
    private static final String id_complete = "2000072"; // ID sending always complete data
    private static final double kmFrom_complete = 60;
    private static final double kmTo_complete = 61;
    private static final String roadId = "S01_1";
    private static final IntervalLengthValue intervalLength = IntervalLengthValue.SEC_15;
    private static final PTlsDataVersion version = PTlsDataVersion.VERSION_3;
    private static final int defaultTimeout = 12;
    private static final int defaultTimelead = 5;
    private static final int defaultSyncWait = 40;
    private static final int defaultUppThresh = 26;
    private static final int defaultLowThresh = -11;
    private static final Boolean defaultvArithmetical = true;
    private static final List<Integer> categoryBoudariesPkw = List.of(10, 20, 50, 100, 120, 160, 200);
    private static final List<Integer> categoryBoudariesLkw = List.of(5, 15, 45, 95, 115);
    
    // Initial start at interval begin
    private MockClock mockClock = MockClock.at(2018, 12, 24, 7, 59, 45, ZoneId.systemDefault());

    // Create and init algo object (to interval begin)
    private SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
    private SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, mockClock);

    public TC2_InitialValueTest() {
        Instant eventtime = Instant.now(mockClock);
        initAlgo(algo, eventtime, version.getNumber(), intervalLength.getSeconds());
    }

    /**
     * Interval 1: Test with 2 measurement points and 2 sending intervals. Test: Both IDs send values:
     * interval = 15s; data arrival at second 0;
     */
    @Test
    public void testI1CompleteValues() {
        // Change time to interval end / processing time / send values time
        mockClock.setHour(7);
        mockClock.setMinute(59);
        mockClock.setSecond(45);
        Instant eventtime = Instant.now(mockClock);
        mockClock.advanceBySeconds(intervalLength.getSeconds());
        Instant processingTime = Instant.now(mockClock);

        // PShortTermRecordingLaneDataBuilder dataBuilder1 = PShortTermRecordingLaneData.builder();
        PLVEErgebnisVersion3Builder dataBuilder1 = PLVEErgebnisVersion3.builder();

        dataBuilder1.id(id_incomplete);
        dataBuilder1.qKfz(8);
        dataBuilder1.qLkwAe(3);
        dataBuilder1.vPkwAe(90);
        dataBuilder1.vLkwAe(60);
        dataBuilder1.nettozeitluecke(3);
        dataBuilder1.belegung(50);
        dataBuilder1.sKfz(20);
        dataBuilder1.vKfz(60);
        dataBuilder1.intervalllaenge(intervalLength.getValue());

        PLVEErgebnisVersion3Builder dataBuilder2 = PLVEErgebnisVersion3.builder();
        dataBuilder2.id(id_complete);
        dataBuilder2.qKfz(9);
        dataBuilder2.qLkwAe(4);
        dataBuilder2.vPkwAe(100);
        dataBuilder2.vLkwAe(70);
        dataBuilder2.nettozeitluecke(4);
        dataBuilder2.belegung(50);
        dataBuilder2.sKfz(20);
        dataBuilder2.vKfz(60);
        dataBuilder2.intervalllaenge(intervalLength.getValue());
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        // Send values
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder1, dataBuilder2),
                Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        // trigger interval end
        algo.intervalEndTrigger(processingTime, Arrays.asList(intervalLength.getSeconds()));
        algo.intervalTimoutTrigger(processingTime, processingTime, Arrays.asList(intervalLength.getSeconds()));

        // Check if no ID is 'missing'
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);      
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);  
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        // missingIds contains no id
        assertTrue(argMissingIds.getValue().size() == 0);
    }

    /**
     * Interval 2: Test with 2 measurement points and 2 sending intervals. Test: One ID send NO values:
     * interval = 15s; data arrival at second 15;
     */
    @Test
    public void testI2IncompleteValues() {
        // Change time to interval end / processing time / send values time
        mockClock.setHour(8);
        mockClock.setMinute(0);
        mockClock.setSecond(0);
        Instant eventtime = Instant.now(mockClock);
        mockClock.advanceBySeconds(intervalLength.getSeconds());
        Instant processingTime = Instant.now(mockClock);

        PLVEErgebnisVersion3Builder dataBuilder2 = PLVEErgebnisVersion3.builder();
        dataBuilder2.id(id_complete);
        dataBuilder2.qKfz(9);
        dataBuilder2.qLkwAe(4);
        dataBuilder2.vPkwAe(100);
        dataBuilder2.vLkwAe(70);
        dataBuilder2.nettozeitluecke(4);
        dataBuilder2.belegung(50);
        dataBuilder2.sKfz(20);
        dataBuilder2.vKfz(60);
        dataBuilder2.intervalllaenge(intervalLength.getValue());
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder2), Arrays.asList(id_incomplete, id_complete), eventtime,
                processingTime);

        // trigger interval end
        algo.intervalEndTrigger(processingTime, Arrays.asList(intervalLength.getSeconds()));
        algo.intervalTimoutTrigger(processingTime, processingTime, Arrays.asList(intervalLength.getSeconds()));

        // Check if first ID is 'missing'
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        // missingIds contains ID of incomplete measurement point
        assertTrue(argMissingIds.getValue().contains(new Tuple3<String, Instant, Integer>(id_incomplete,
                eventtime.plusSeconds(intervalLength.getSeconds()), intervalLength.getSeconds())));

        // missingIds contains exact 1 id
        assertTrue(argMissingIds.getValue().size() == 1);
    }

    /**
     * Interval 3: Test with 2 measurement points and 2 sending intervals. Test: Both IDs send values:
     * interval = 15s; intevalBegin=15 data arrival at second 30;
     */
    @Test
    public void testI3CompleteValues() {
        mockClock.setHour(8);
        mockClock.setMinute(0);
        mockClock.setSecond(15);
        Instant eventtime = Instant.now(mockClock);
        // Change time to interval end / processing time / send values time
        mockClock.advanceBySeconds(intervalLength.getSeconds());
        Instant processingTime = Instant.now(mockClock);

        PLVEErgebnisVersion3Builder dataBuilder1 = PLVEErgebnisVersion3.builder();
        dataBuilder1.id(id_incomplete);
        dataBuilder1.qKfz(8);
        dataBuilder1.qLkwAe(3);
        dataBuilder1.vPkwAe(90);
        dataBuilder1.vLkwAe(60);
        dataBuilder1.nettozeitluecke(3);
        dataBuilder1.belegung(50);
        dataBuilder1.sKfz(20);
        dataBuilder1.vKfz(60);
        dataBuilder1.intervalllaenge(intervalLength.getValue());

        PLVEErgebnisVersion3Builder dataBuilder2 = PLVEErgebnisVersion3.builder();
        dataBuilder2.id(id_complete);
        dataBuilder2.qKfz(9);
        dataBuilder2.qLkwAe(4);
        dataBuilder2.vPkwAe(100);
        dataBuilder2.vLkwAe(70);
        dataBuilder2.nettozeitluecke(3);
        dataBuilder2.belegung(50);
        dataBuilder2.sKfz(20);
        dataBuilder2.vKfz(60);
        dataBuilder2.intervalllaenge(intervalLength.getValue());
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        // Send values
        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder1, dataBuilder2),
                Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        // trigger interval end
        algo.intervalEndTrigger(processingTime, Arrays.asList(intervalLength.getSeconds()));
        algo.intervalTimoutTrigger(processingTime, processingTime, Arrays.asList(intervalLength.getSeconds()));

        // Check if no ID is 'missing'
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);        
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        // missingIds contains no id
        assertTrue(argMissingIds.getValue().size() == 0);
    }

    /**
     * Interval 4: Test with 2 measurement points and 2 sending intervals. Test: One ID send NO values:
     * interval = 15s; intervalbegin=30; data arrival at second 45;
     */
    @Test
    public void testI4IncompleteValues() {
        // Change time to interval end / processing time / send values time
        mockClock.setHour(8);
        mockClock.setMinute(0);
        mockClock.setSecond(30);
        Instant eventtime = Instant.now(mockClock);
        mockClock.advanceBySeconds(intervalLength.getSeconds());
        Instant processingTime = Instant.now(mockClock);

        PLVEErgebnisVersion3Builder dataBuilder2 = PLVEErgebnisVersion3.builder();
        dataBuilder2.id(id_complete);
        dataBuilder2.qKfz(9);
        dataBuilder2.qLkwAe(4);
        dataBuilder2.vPkwAe(100);
        dataBuilder2.vLkwAe(70);
        dataBuilder2.nettozeitluecke(3);
        dataBuilder2.belegung(50);
        dataBuilder2.sKfz(20);
        dataBuilder2.vKfz(60);
        dataBuilder2.intervalllaenge(intervalLength.getValue());
        
        // Set infrastructure states
        TCUtils.setInfraStates(algo, Arrays.asList(id_incomplete, id_complete), eventtime, processingTime);

        TCUtils.setIntervalData(algo, Arrays.asList(dataBuilder2), Arrays.asList(id_incomplete, id_complete), eventtime,
                processingTime);

        // trigger interval end
        algo.intervalEndTrigger(processingTime, Arrays.asList(intervalLength.getSeconds()));
        algo.intervalTimoutTrigger(processingTime, processingTime, Arrays.asList(intervalLength.getSeconds()));

        // Check if first ID is 'missing'
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        // missingIds contains ID of incomplete measurement point
        assertTrue(argMissingIds.getValue().contains(new Tuple3<String, Instant, Integer>(id_incomplete,
                eventtime.plusSeconds(intervalLength.getSeconds()), intervalLength.getSeconds())));
        // missingIds contains exact 1 id
        assertTrue(argMissingIds.getValue().size() == 1);
    }

    private static void initAlgo(SyncVdAlgo<AbstractData> algo, Instant time, int version, int intervalSeconds) {
        // InfraObjects
        List<InfrastructureObject> infraObjects = new ArrayList<>();
        infraObjects.add(new MockObjects.Lane(id_incomplete, "Autobahn test", "A test", "V1",
                new MockObjects.LogKm(roadId, kmFrom_incomplete, kmTo_incomplete)));
        infraObjects.add(new MockObjects.Lane(id_complete, "Autobahn test", "A test", "V1",
                new MockObjects.LogKm(roadId, kmFrom_complete, kmTo_complete)));
        algo.setInfrastructure(infraObjects);

        // AlgoParameter
        AlgoParameter algoParam1 = new AlgoParameter(id_uz, Type.DIRECT_SET, time, defaultSyncWait, defaultTimelead,
                Arrays.asList(new IntervalTimout(intervalLength, defaultTimeout)),
                Arrays.asList(new IntervalTimeLead(intervalLength, defaultTimelead)),
                Arrays.asList(new IntervalTemporaryThresholds(intervalLength, defaultLowThresh, defaultUppThresh)));
        algo.setAlgoParameter(algoParam1);

        // InfraParameter
        Map<String, InfraParameter> infraParams = new HashMap<>();
        infraParams.put(id_incomplete, new InfraParameter(id_incomplete, Type.DIRECT_SET, time, intervalSeconds,
                version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
        infraParams.put(id_complete, new InfraParameter(id_complete, Type.DIRECT_SET, time, intervalSeconds, version,
                defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
        algo.setInfraParameter(infraParams);
    }

}
