package de.heuboe.asfinag.vmis2.synchronize.vd.timesync;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Completeness of received data. Some values with communication problems. "Prüffall 5: Test auf
 * Vollständigkeit"
 *
 * Objective: Accepting input data received in several steps<br>
 * Description: The received values are not published, till all measurement point sent data for an
 * interval (or the timeout is reached).<br>
 * Precondition: Infrastructure is filled, algorithm and infrastructure parameter are set<br>
 * Requirements:
 */
@SuppressWarnings("unchecked")
public class TC5a_CompletenessCommProbTest {

    private static final Logger LOG = LoggerFactory.getLogger(TC5a_CompletenessCommProbTest.class);
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private static MockClock mockClock = MockClock.at(2019, 1, 28, 14, 12, 30, ZoneId.systemDefault());
    private static Instant starttime = Instant.now(mockClock);
    private static int secondsI15 = IntervalLengthValue.SEC_15.getSeconds();
    private static int secondsI30 = IntervalLengthValue.SEC_30.getSeconds();
    private static int version = PTlsDataVersion.VERSION_3.getNumber();

    private static final String id_uz = "UZ1";
    private static final String roadId = "S01_1";
    private static final int defaultTimelead = 14;
    private static final int defaultTimeout = 12;
    private static final int defaultSyncWait = 40;
    private static final int defaultLowThresh = -11;
    private static final int defaultUppThresh = 26;
    private static final Boolean defaultvArithmetical = true;
    private static final List<Integer> categoryBoudariesPkw = List.of(10, 20, 50, 100, 120, 160, 200);
    private static final List<Integer> categoryBoudariesLkw = List.of(5, 15, 45, 95, 115);

    private static final String id_71 = "2000071";
    private static final String id_72 = "2000072";
    private static final String id_73 = "2000073";
    private static final String id_75 = "2000075";
    private static final String id_76 = "2000076";
    private static final String id_77 = "2000077";
    private static final String id_79 = "2000079";
    private static final String id_80 = "2000080";
    private static final String id_81 = "2000081";
    private static final String id_83 = "2000083";
    private static final String id_84 = "2000084";
    private static final String id_85 = "2000085";
    private static final String id_87 = "2000087";
    private static final String id_89 = "2000089";
    private static final String id_91 = "2000091";
    private static final String id_92 = "2000092";
    private static final String id_93 = "2000093";

    private static final List<IntervalTimout> timeouts =
            Arrays.asList(new IntervalTimout(IntervalLengthValue.SEC_15, defaultTimeout),
                    new IntervalTimout(IntervalLengthValue.SEC_30, defaultTimeout));

    private static final List<IntervalTimeLead> timeleads =
            Arrays.asList(new IntervalTimeLead(IntervalLengthValue.SEC_15, defaultTimelead),
                    new IntervalTimeLead(IntervalLengthValue.SEC_30, defaultTimelead));

    private static final List<IntervalTemporaryThresholds> thresholds = Arrays.asList(
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_15, defaultLowThresh, defaultUppThresh),
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_30, defaultLowThresh, defaultUppThresh));

    private static final List<InfrastructureObject> infraObjects = Arrays.asList(
            new MockObjects.Lane(id_71, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 59, 60)),
            new MockObjects.Lane(id_72, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 60, 61)),
            new MockObjects.Lane(id_73, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 51, 62)),
            new MockObjects.Lane(id_75, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 62, 63)),
            new MockObjects.Lane(id_76, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 63, 64)),
            new MockObjects.Lane(id_77, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 64, 65)),
            new MockObjects.Lane(id_79, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 65, 66)),
            new MockObjects.Lane(id_80, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 66, 67)),
            new MockObjects.Lane(id_81, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 67, 68)),
            new MockObjects.Lane(id_83, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 68, 69)),
            new MockObjects.Lane(id_84, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 69, 70)),
            new MockObjects.Lane(id_85, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 70, 71)),
            new MockObjects.Lane(id_87, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 71, 72)),
            new MockObjects.Lane(id_89, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 72, 73)),
            new MockObjects.Lane(id_91, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 73, 74)),
            new MockObjects.Lane(id_92, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 74, 75)),
            new MockObjects.Lane(id_93, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 75, 76)));

    private static final Map<String, InfraParameter> infraParams = new HashMap<>() {
        {
            // Interval 15
            put(id_71,
                    new InfraParameter(id_71, Type.DIRECT_SET, starttime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_72,
                    new InfraParameter(id_72, Type.DIRECT_SET, starttime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_73,
                    new InfraParameter(id_73, Type.DIRECT_SET, starttime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            // Interval 30
            put(id_75,
                    new InfraParameter(id_75, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_76,
                    new InfraParameter(id_76, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_77,
                    new InfraParameter(id_77, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_79,
                    new InfraParameter(id_79, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_80,
                    new InfraParameter(id_80, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_81,
                    new InfraParameter(id_81, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_83,
                    new InfraParameter(id_83, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_84,
                    new InfraParameter(id_84, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_85,
                    new InfraParameter(id_85, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_87,
                    new InfraParameter(id_87, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_89,
                    new InfraParameter(id_89, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_91,
                    new InfraParameter(id_91, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_92,
                    new InfraParameter(id_92, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_93,
                    new InfraParameter(id_93, Type.DIRECT_SET, starttime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
        }
    };

    // Create and init algo object (to interval begin)
    private SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
    private SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, mockClock);

    public TC5a_CompletenessCommProbTest() {
        initAlgo(algo, infraObjects, infraParams, timeleads, timeouts, thresholds);
    }

    /**
     * Make sure, test order is correct
     */
    @Test
    public void testCompleteness() {
        Instant time1230 = Instant.now(mockClock); // 14:12:30
        LOG.debug("---------------------------- " + DTF.format(time1230) + " -----------------------------");
        mockClock.advanceBySeconds(15);
        Instant time1245 = Instant.now(mockClock); // 14:12:45
        LOG.debug("---------------------------- " + DTF.format(time1245) + " -----------------------------");
        mockClock.advanceBySeconds(15);
        Instant time1300 = Instant.now(mockClock); // 14:13:00
        LOG.debug("---------------------------- " + DTF.format(time1300) + " -----------------------------");
        // All values for interval 30 and some values for interval 15 sent
        testStep1(time1245, time1230, time1300);

        mockClock.advanceBySeconds(3);
        Instant time1303 = Instant.now(mockClock); // 14:13:03
        LOG.debug("---------------------------- " + DTF.format(time1303) + " -----------------------------");
        // errorcode for one 15sec value
        testStep1Error(time1303);

        // one missing value sent for interval 15
        mockClock.advanceBySeconds(2);
        Instant time1305 = Instant.now(mockClock); // 14:13:05
        LOG.debug("---------------------------- " + DTF.format(time1305) + " -----------------------------");
        testStep2(time1245, time1305);

        // Timeout reached for first interval
        mockClock.advanceBySeconds(7);
        Instant time1312 = Instant.now(mockClock); // 14:13:12
        LOG.debug("---------------------------- " + DTF.format(time1312) + " -----------------------------");
        testStep3(time1312, time1300);

        // intervalend reachead for seconds interval
        mockClock.advanceBySeconds(3);
        Instant time1315 = Instant.now(mockClock); // 14:13:15
        LOG.debug("---------------------------- " + DTF.format(time1315) + " -----------------------------");
        testStep4(time1315);

        mockClock.advanceBySeconds(2);
        Instant time1317 = Instant.now(mockClock); // 14:13:17
        LOG.debug("---------------------------- " + DTF.format(time1317) + " -----------------------------");
        // Values for next interval 15 sent
        testStep5(time1300, time1317);

        // interval timeout reachead
        mockClock.advanceBySeconds(8);
        Instant time1325 = Instant.now(mockClock); // 14:13:25
        LOG.debug("---------------------------- " + DTF.format(time1325) + " -----------------------------");
        testStep5Error(time1325);

        // interval timeout reachead
        mockClock.advanceBySeconds(2);
        Instant time1327 = Instant.now(mockClock); // 14:13:27
        LOG.debug("---------------------------- " + DTF.format(time1327) + " -----------------------------");
        testStep6(time1315, time1327);
        //
        // interval end (15, 30) reached; values come in
        mockClock.advanceBySeconds(3);
        Instant time1330 = Instant.now(mockClock); // 14:13:30
        LOG.debug("---------------------------- " + DTF.format(time1330) + " -----------------------------");
        testStep7(time1315, time1300, time1330);

        mockClock.advanceBySeconds(5);
        Instant time1335 = Instant.now(mockClock); // 14:13:35
        LOG.debug("---------------------------- " + DTF.format(time1335) + " -----------------------------");
        testStep8(time1300, time1335);

        mockClock.advanceBySeconds(7);
        Instant time1342 = Instant.now(mockClock); // 14:13:42
        LOG.debug("---------------------------- " + DTF.format(time1342) + " -----------------------------");
        testStep9(time1330, time1342);

        mockClock.advanceBySeconds(13);
        Instant time1355 = Instant.now(mockClock); // 14:13:55
        LOG.debug("---------------------------- " + DTF.format(time1355) + " -----------------------------");
        testStep9Error(time1355);
    }

    /**
     * Test step 1: second 0 not all data for interval 15 are send (no publish expected); all data for
     * interval 30 expected (publish expected);
     */
    public void testStep1(Instant eventtime15, Instant eventtime30, Instant processingTime) {
        List<Object> builders = new ArrayList<>();
        Map<String, InfraState> infraStates = new HashMap<>();

        List<String> i15Ids = Arrays.asList(id_71);
        List<String> i30Ids = Arrays.asList(id_75, id_76, id_77, id_79, id_80, id_81, id_83, id_84, id_85, id_87, id_89,
                id_91, id_92, id_93);
        for (String id : i15Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
            infraStates.put(id, new InfraState(id, true, null, eventtime15, false, false));
        }
        for (String id : i30Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_30.getValue()));
            infraStates.put(id, new InfraState(id, true, null, eventtime30, false, false));
        }
        infraStates.put(id_72, new InfraState(id_72, true, null, eventtime15, false, false));
        infraStates.put(id_73, new InfraState(id_73, true, null, eventtime15, false, false));

        // InfraStates
        algo.setInfraState(infraStates);

        // send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime15, processingTime);
        TCUtils.setIntervalData(algo, builders, eventtime30, processingTime);

        // trigger interval end for interval 15 and interval 30 at second 0
        algo.intervalEndTrigger(processingTime, Arrays.asList(secondsI15, secondsI30));

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStatesArg.capture(), slowestVehData.capture());

        // Check if interval 15 ids are not published
        assertTrue(argContainsNoneOfIds(argInput, i15Ids));
        // Check if interval 30 ids are published
        assertTrue(argContainsIds(argInput, i30Ids));
        // missingIds contains no id
        assertTrue(argMissingIds.getValue().size() == 0);
    }

    /**
     * Set errorcode for one interval 15 value.
     * 
     * @param eventtime
     * @param processingTime
     */
    public void testStep1Error(Instant processingTime) {
        Map<String, InfraState> infraStates = new HashMap<>();

        List<String> ids = Arrays.asList(id_71, id_73, id_75, id_76, id_77, id_79, id_80, id_81, id_83, id_84, id_85,
                id_87, id_89, id_91, id_92, id_93);
        for (String id : ids) {
            infraStates.put(id, new InfraState(id, true, null, processingTime, false, false));
        }
        infraStates.put(id_72, new InfraState(id_72, false, "Test Fehlercode 1 für id 2000072", processingTime, false, false));

        // InfraStates
        algo.setInfraState(infraStates);
    }

    /**
     * Test step 2: second 5 Missing value for last interval 15 ID is sent. instant publish of interval
     * 15 expected. Timeout at second 12 -> no publish excepted
     */
    public void testStep2(Instant eventtime, Instant processingTime) {
        List<Object> builders = new ArrayList<>();
        builders.add(getDataBuilder1(id_73, IntervalLengthValue.SEC_15.getValue()));

        // send data at second 5
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // Publish expected (2/3 values came in, 1/3 values are in error status)
        Mockito.verify(pubMock, Mockito.times(2)).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStatesArg.capture(), slowestVehData.capture());

        // Check if interval 30 ids are published
        assertTrue(argContainsIds(argInput, Arrays.asList(id_71, id_73)));
        // missingIds contains exactly 1 id
        assertTrue(argMissingIds.getValue().size() == 1);
        // missingIds contains id 72
        assertTrue(argMissingIds.getValue()
                .contains(new Tuple3<String, Instant, Integer>(id_72, eventtime.plusSeconds(secondsI15), secondsI15)));
    }

    /**
     * Test step 3: reach timeout of first interval
     * 
     * @param timeoutEnd
     * @param intervalEnd
     */
    public void testStep3(Instant timeoutEnd, Instant intervalEnd) {
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(secondsI15, secondsI30));
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock, Mockito.times(2)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());
    }

    /**
     * Test step 4: intervalend for interval 15 reached. All values can be published.
     * 
     * @param intervalEnd
     */
    public void testStep4(Instant intervalEnd) {
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(secondsI15));
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // no more publish's
        Mockito.verify(pubMock, Mockito.times(2)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());
    }

    /**
     * Test step 5: all 3 15-sec values come in at second 12. Should publish directly.
     * 
     * @param eventtime
     * @param processingTime
     */
    public void testStep5(Instant eventtime, Instant processingTime) {
        List<Object> builders = new ArrayList<>();

        List<String> i15Ids = Arrays.asList(id_71, id_73);
        for (String id : i15Ids) {
            if (id.equals(id_73)) {
                builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
            } else {
                builders.add(getDataBuilder2(id, IntervalLengthValue.SEC_15.getValue()));
            }
        }

        // send data at second 17
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // Publish, because interval end is over and missing id is in error state
        Mockito.verify(pubMock, Mockito.times(3)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());

        // Check if interval 15 ids are published
        assertTrue(argContainsIds(argInput, Arrays.asList(id_71, id_73)));
        // missingIds contains exactly 1 id
        assertTrue(argMissingIds.getValue().size() == 1);
        // missingIds contains id 72
        assertTrue(argMissingIds.getValue()
                .contains(new Tuple3<String, Instant, Integer>(id_72, eventtime.plusSeconds(secondsI15), secondsI15)));
    }

    public void testStep5Error(Instant processingTime) {
        List<String> ids = Arrays.asList(id_71, id_72, id_73, id_76, id_77, id_79, id_80, id_81, id_83, id_84, id_85,
                id_87, id_89, id_91, id_92, id_93);
        Map<String, InfraState> infraStates = new HashMap<>();
        for (String id : ids) {
            infraStates.put(id, new InfraState(id, true, null, processingTime, false, false));
        }
        infraStates.put(id_75, new InfraState(id_75, true, null, processingTime, true, false));

        // InfraStates
        algo.setInfraState(infraStates);
    }

    /**
     * Test step 6: timeout end for interval 15 reached. No more publish.
     * 
     * @param intervalEnd
     * @param timeoutEnd
     */
    public void testStep6(Instant intervalEnd, Instant timeoutEnd) {
        // trigger interval timeout for interval 15 at second 27
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(secondsI15));

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // No more new publish's
        Mockito.verify(pubMock, Mockito.times(3)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());
    }

    /**
     * Test step 7: all 3 15sec values come in, some 30sec values. interval 15 values should be
     * published; interval 30 values should not be published;
     * 
     * @param eventtime
     * @param processingTime
     */
    public void testStep7(Instant eventtime15, Instant eventtime30, Instant processingTime) {
        List<Object> builders = new ArrayList<>();

        List<String> i15Ids = Arrays.asList(id_71, id_72, id_73);
        List<String> i30Ids =
                Arrays.asList(id_77, id_79, id_80, id_81, id_83, id_84, id_85, id_87, id_89, id_91, id_92, id_93);
        for (String id : i15Ids) {
            if (id.equals(id_73)) {
                builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
            } else {
                builders.add(getDataBuilder2(id, IntervalLengthValue.SEC_15.getValue()));
            }
        }
        for (String id : i30Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_30.getValue()));
        }

        // send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime15, processingTime);
        TCUtils.setIntervalData(algo, builders, eventtime30, processingTime);

        // trigger interval end for interval 15 and interval 30 at second 30
        algo.intervalEndTrigger(processingTime, Arrays.asList(secondsI15, secondsI30));

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock, Mockito.times(4)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());

        // Check if interval 15 ids are not published
        assertTrue(argContainsNoneOfIds(argInput, i30Ids));
        // Check if interval 30 ids are published
        assertTrue(argContainsIds(argInput, i15Ids));
        // missingIds contains no id
        assertTrue(argMissingIds.getValue().size() == 0);
    }

    /**
     * Test step 8: missing 2 interval 30 values coming in at second 40. Publish interval 30.
     * 
     * @param eventtime30
     * @param processingTime
     */
    public void testStep8(Instant eventtime30, Instant processingTime) {
        List<Object> builders = new ArrayList<>();

        List<String> i30Ids = Arrays.asList(id_76);
        for (String id : i30Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_30.getValue()));
        }

        // send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime30, processingTime);

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock, Mockito.times(5)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());

        List<String> i15Ids = Arrays.asList(id_71, id_72, id_73);
        i30Ids = Arrays.asList(id_76, id_77, id_79, id_80, id_81, id_83, id_84, id_85, id_87, id_89, id_91, id_92,
                id_93);

        // Check if interval 15 ids are not published
        assertTrue(argContainsNoneOfIds(argInput, i15Ids));
        // Check if interval 30 ids are published
        assertTrue(argContainsIds(argInput, i30Ids));
        // missingIds contains 1 id
        assertTrue(argMissingIds.getValue().size() == 1);
        // missingIds contains id 75
        assertTrue(argMissingIds.getValue().contains(
                new Tuple3<String, Instant, Integer>(id_75, eventtime30.plusSeconds(secondsI30), secondsI30 )));
    }

    /**
     * Test step 9: timeout end for interval 15 an 30 reached. No more publish.
     * 
     * @param intervalEnd
     * @param timeoutEnd
     */
    public void testStep9(Instant intervalEnd, Instant timeoutEnd) {
        // trigger interval timeout for interval 15 at second 27
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(secondsI15, secondsI30));

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // No more new publish's
        Mockito.verify(pubMock, Mockito.times(5)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());
    }

    /**
     * Test step 10: reset passivation byte at second 55.
     * 
     * @param processingTime
     */
    public void testStep9Error(Instant processingTime) {
        List<String> ids = Arrays.asList(id_71, id_72, id_73, id_75, id_76, id_77, id_79, id_80, id_81, id_83, id_84,
                id_85, id_87, id_89, id_91, id_92, id_93);

        Map<String, InfraState> infraStates = new HashMap<>();
        for (String id : ids) {
            infraStates.put(id, new InfraState(id, true, null, processingTime, false, false));
        }
        // InfraStates
        algo.setInfraState(infraStates);

        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStatesArg = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        // No more new publish's
        Mockito.verify(pubMock, Mockito.times(5)).publish(argInput.capture(), argMissingIds.capture(),
                infraPara.capture(), infraStatesArg.capture(), slowestVehData.capture());
    }
    // -----------------------------------------------------------------------------------

    private boolean argContainsIds(ArgumentCaptor<List<AbstractData>> argInput, List<String> ids) {
        if (argInput == null || argInput.getValue() == null || ids == null) {
            return false;
        }
        List<String> argIds = argInput.getValue().stream().map(arg -> arg.getId()).collect(Collectors.toList());
        return argIds.containsAll(ids);
    }

    private boolean argContainsNoneOfIds(ArgumentCaptor<List<AbstractData>> argInput, List<String> ids) {
        if (argInput == null || argInput.getValue() == null || ids == null) {
            return true;
        }
        List<String> argIds = argInput.getValue().stream().map(arg -> arg.getId()).collect(Collectors.toList());
        for (String id : ids) {
            if (argIds.contains(id)) {
                return false;
            }
        }
        return true;
    }

    private static void initAlgo(SyncVdAlgo<AbstractData> algo, List<InfrastructureObject> infraObjects,
            Map<String, InfraParameter> infraParams, List<IntervalTimeLead> timeLeads, List<IntervalTimout> timeouts,
            List<IntervalTemporaryThresholds> thresholds) {

        // InfraObjects
        algo.setInfrastructure(infraObjects);

        // AlgoParameter
        AlgoParameter algoParam1 = new AlgoParameter(id_uz, Type.DIRECT_SET, starttime, defaultSyncWait,
                defaultTimelead, timeouts, timeLeads, thresholds);
        algo.setAlgoParameter(algoParam1);

        algo.setInfraParameter(infraParams);
    }

    private static final PLVEErgebnisVersion3Builder getDataBuilder1(String id, int intervalLength) {
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.qKfz(7);
        dataBuilder.qLkwAe(3);
        dataBuilder.vPkwAe(110);
        dataBuilder.vLkwAe(80);
        dataBuilder.nettozeitluecke(4);
        dataBuilder.belegung(50);
        dataBuilder.sKfz(20);
        dataBuilder.vKfz(60);
        dataBuilder.id(id);
        dataBuilder.intervalllaenge(intervalLength);
        return dataBuilder;
    }

    private static final PLVEErgebnisVersion3Builder getDataBuilder2(String id, int intervalLength) {
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.qKfz(20);
        dataBuilder.qLkwAe(5);
        dataBuilder.vPkwAe(70);
        dataBuilder.vLkwAe(60);
        dataBuilder.nettozeitluecke(3);
        dataBuilder.belegung(50);
        dataBuilder.vKfz(60);
        dataBuilder.id(id);
        dataBuilder.intervalllaenge(intervalLength);
        return dataBuilder;
    }
}
