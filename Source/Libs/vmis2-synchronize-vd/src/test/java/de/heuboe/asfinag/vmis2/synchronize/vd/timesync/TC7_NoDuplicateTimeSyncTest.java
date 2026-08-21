package de.heuboe.asfinag.vmis2.synchronize.vd.timesync;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
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
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SyncVdAlgo;
import de.heuboe.asfinag.vmis2.synchronize.vd.publish.SyncVdPublisher;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3.PLVEErgebnisVersion3Builder;

/**
 * Prevent duplicate time synchronisation
 * "Prüffall 7:  Verhindern einer doppelten Zeitsynchronisation"
 *
 * Objective: Prevent duplicate time synchronisation<br>
 * Description: Time synchronisation should be triggered if received data are out of defined timeslot, but only if
 * synchronisation wait time is over - no duplicate time synchronisations.<br>
 * Precondition: Infrastructure is filled, algorithm and infrastructure parameter are set<br>
 * Requirements:
 */
@SuppressWarnings("unchecked")
public class TC7_NoDuplicateTimeSyncTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(TC7_NoDuplicateTimeSyncTest.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    private static MockClock mockClock      = MockClock.at(2019, 1, 17, 14, 12, 45, ZoneId.systemDefault());
    private static Instant starttime        = Instant.now(mockClock);
    private static int secondsI15           = IntervalLengthValue.SEC_15.getSeconds();
    private static int version              = PTlsDataVersion.VERSION_3.getNumber();
    
    private static final String id_uz           = "UZ1";
    private static final String roadId          = "S01_1";
    private static final int defaultTimelead    = 14;
    private static final int defaultTimeout     = 12;
    private static final int defaultSyncWait    = 40;
    private static final int defaultLowThresh   = -11;
    private static final int defaultUppThresh   = 26;
    private static final Boolean defaultvArithmetical = true;
    private static final List<Integer> categoryBoudariesPkw = List.of(10, 20, 50, 100, 120, 160, 200);
    private static final List<Integer> categoryBoudariesLkw = List.of(5, 15, 45, 95, 115);
    
    private static final String id_71       = "2000071";
    private static final String id_73       = "2000073";
    
    private static final List<IntervalTimout>               timeouts        = Arrays.asList(
            new IntervalTimout(IntervalLengthValue.SEC_15, defaultTimeout),
            new IntervalTimout(IntervalLengthValue.SEC_30, defaultTimeout));
    
    private static final List<IntervalTimeLead>                     timeleads       = Arrays.asList(
            new IntervalTimeLead(IntervalLengthValue.SEC_15, defaultTimelead),
            new IntervalTimeLead(IntervalLengthValue.SEC_30, defaultTimelead));
    
    private static final List<IntervalTemporaryThresholds>  thresholds      = Arrays.asList(
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_15, defaultLowThresh, defaultUppThresh),
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_30, defaultLowThresh, defaultUppThresh));
    
    private static final List<InfrastructureObject>         infraObjects  = Arrays.asList(
            new MockObjects.Lane(id_71, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 59, 60)),
            new MockObjects.Lane(id_73, "Autobahn test", "A test", "V1", new MockObjects.LogKm(roadId, 51, 62)));
    
    private static final Map<String, InfraParameter>               infraParams     = new HashMap<>() {{
            put(id_71, new InfraParameter(id_71, Type.DIRECT_SET, starttime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_73, new InfraParameter(id_73, Type.DIRECT_SET, starttime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
    }};

    //Create and init algo object (to interval begin)
    private SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
    private SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, mockClock);
    
    public TC7_NoDuplicateTimeSyncTest() {
        initAlgo(algo, infraObjects, infraParams, timeleads, timeouts, thresholds);
    }
    
    /**
     * Make sure, test order is correct
     */
    @Test
    public void testCompleteness() {
        Instant time1245 = Instant.now(mockClock);      // 14:12:45
        LOG.debug("---------------------------- " + DTF.format(time1245) +" -----------------------------");
        mockClock.advanceBySeconds(15);
        Instant time1300 = Instant.now(mockClock);      // 14:13:00
        LOG.debug("---------------------------- " + DTF.format(time1300) +" -----------------------------");
        testStep1(time1245, time1300);
        
        mockClock.advanceBySeconds(11);
        Instant time1311 = Instant.now(mockClock);      // 14:13:11
        LOG.debug("---------------------------- " + DTF.format(time1311) +" -----------------------------");
        
        mockClock.advanceBySeconds(15);
        Instant time1326 = Instant.now(mockClock);      // 14:13:26
        LOG.debug("---------------------------- " + DTF.format(time1326) +" -----------------------------");
        testStep2(time1326);

        //interval end (15, 30) reached; values come in
        mockClock.advanceBySeconds(4);
        Instant time1330 = Instant.now(mockClock);      // 14:13:30
        LOG.debug("---------------------------- " + DTF.format(time1330) +" -----------------------------");
        
        mockClock.advanceBySeconds(8);
        Instant time1338 = Instant.now(mockClock);      // 14:13:38
        LOG.debug("---------------------------- " + DTF.format(time1338) +" -----------------------------");
        testStep3(time1311, time1326, time1338);
        
        mockClock.advanceBySeconds(7);
        Instant time1345 = Instant.now(mockClock);      // 14:13:45
        LOG.debug("---------------------------- " + DTF.format(time1345) +" -----------------------------");
        testStep4(time1345);
        
        mockClock.advanceBySeconds(12);
        Instant time1357 = Instant.now(mockClock);      // 14:13:57
        LOG.debug("---------------------------- " + DTF.format(time1357) +" -----------------------------");
        testStep5(time1345, time1357);
        
        mockClock.advanceBySeconds(3);
        Instant time1400 = Instant.now(mockClock);      // 14:14:00
        LOG.debug("---------------------------- " + DTF.format(time1400) +" -----------------------------");
        testStep6(time1400);
        
        mockClock.advanceBySeconds(12);
        Instant time1412 = Instant.now(mockClock);      // 14:14:12
        LOG.debug("---------------------------- " + DTF.format(time1412) +" -----------------------------");
        testStep7(time1400, time1412);
        
        mockClock.advanceBySeconds(18);
        Instant time1415 = Instant.now(mockClock);      // 14:14:15
        LOG.debug("---------------------------- " + DTF.format(time1415) +" -----------------------------");
        testStep8(time1330, time1415);
        
        mockClock.advanceBySeconds(15);
        Instant time1430 = Instant.now(mockClock);      // 14:14:30
        LOG.debug("---------------------------- " + DTF.format(time1430) +" -----------------------------");
        testStep9(time1345, time1430);
    }
    
    /**
     * Test step 1.
     */
    public void testStep1(Instant eventtime15, Instant processingTime) {
        List<Object> builders = new ArrayList<>();
        Map<String, InfraState> infraStates = new HashMap<>();

        List<String> i15Ids = Arrays.asList(id_71, id_73);
        for(String id : i15Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
            infraStates.put(id, new InfraState(id, true, null, eventtime15, false, false));
        }
        
        //InfraStates
        algo.setInfraState(infraStates);
        
        //send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime15, processingTime);
        
        //trigger interval end for interval 15 and interval 30 at second 0
        algo.intervalEndTrigger(processingTime, Arrays.asList(secondsI15));
    }
    
    /**
     * Test step 2: intervalend for interval 15 reached.
     * 
     * @param intervalEnd
     */
    public void testStep2(Instant intervalEnd) {
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(secondsI15));
    }
    
    /**
     * Test step 3: send values.
     * @param eventtime30
     * @param processingTime
     */
    public void testStep3(Instant eventtime, Instant intervalend, Instant processingTime) {
        List<Object> builders = new ArrayList<>();

        List<String> ids = Arrays.asList(id_71, id_73);
        for(String id : ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
        }

        //send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);
        
        algo.intervalTimoutTrigger(processingTime, intervalend, Arrays.asList(secondsI15));
    }
    
    /**
     * Test step 4: interval end trigger
     * 
     * @param eventtime
     * @param processingTime
     */
    public void testStep4(Instant intervalend) {
        //trigger interval end for interval 15 at second 30
        algo.intervalEndTrigger(intervalend, Arrays.asList(secondsI15));
    }

    /**
     * Test step 5: reach timeout of first interval
     * @param timeoutEnd
     * @param intervalEnd
     */
    public void testStep5(Instant intervalEnd, Instant timeoutEnd) {
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(secondsI15));
    }
    
    /**
     * Test step 6: intervalend for interval 15 reached.
     * 
     * @param intervalEnd
     */
    public void testStep6(Instant intervalEnd) {
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(secondsI15));
    }

    /**
     * Test step 7: reach timeout of first interval
     * @param timeoutEnd
     * @param intervalEnd
     */
    public void testStep7(Instant intervalEnd, Instant timeoutEnd) {
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, Arrays.asList(secondsI15));
    }
    
    /**
     * Test step 8: send values too late. time synchronisation should be published.
     * 
     * @param intervalEnd
     * @param timeoutEnd
     */
    public void testStep8(Instant eventtime, Instant processingTime) {
        List<Object> builders = new ArrayList<>();

        List<String> ids = Arrays.asList(id_71, id_73);
        for(String id : ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
        }
        //send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);
        
        //Time synchronisation should be published
        Mockito.verify(pubMock).publishTimeSynchronization();
    }
    
    /**
     * Test step 9: values too late. time synchronisation should not be published because of waittime.
     * 
     * @param intervalEnd
     * @param timeoutEnd
     */
    public void testStep9(Instant eventtime, Instant processingTime) {
        List<Object> builders = new ArrayList<>();
        
        List<String> ids = Arrays.asList(id_71, id_73);
        for(String id : ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
        }
        //send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);
        
        //Time synchronisation should not be published again (because of waittime 40 sec.)
        Mockito.verify(pubMock, Mockito.times(1)).publishTimeSynchronization();
    }
    //-----------------------------------------------------------------------------------

    private static void initAlgo(SyncVdAlgo<AbstractData> algo,
            List<InfrastructureObject> infraObjects,
            Map<String, InfraParameter> infraParams,
            List<IntervalTimeLead> timeLeads,
            List<IntervalTimout> timeouts,
            List<IntervalTemporaryThresholds> thresholds) {
        
        //InfraObjects 
        algo.setInfrastructure(infraObjects);

        //AlgoParameter
        AlgoParameter algoParam1 = new AlgoParameter(id_uz, Type.DIRECT_SET, starttime,
                defaultSyncWait, defaultTimelead, timeouts, timeLeads, thresholds);
        algo.setAlgoParameter(algoParam1);
        
        algo.setInfraParameter(infraParams);
    }
    
    private static final PLVEErgebnisVersion3Builder getDataBuilder1(String id, int intervalLength) {
        PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
        dataBuilder.qKfz(0);
        dataBuilder.qLkwAe(0);
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
