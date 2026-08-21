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
 * Changing interval length (same interval end for old and new interval)
 * "Prüffall 9: Ändern einer Intervalllänge"
 *
 * Objective: Change interval length for measurement points.<br>
 * Description: Data come in for one measurement point. Before they are published, the interval length of this 
 * point is changed from interval 15s to 30s. The received data should not be deleted, but be published anyway (for 
 * interval 15s). The interval ends for 15 and 30s interval are at the same point of time. Values for interval 30 
 * are missing.<br>
 * Precondition: Infrastructure is filled, algorithm and infrastructure parameter are set<br>
 * Requirements:
 */
@SuppressWarnings("unchecked")
public class TC9b_ChangeIntervalLengthMissingDataTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(TC9b_ChangeIntervalLengthMissingDataTest.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    private static MockClock mockClock      = MockClock.at(2019, 1, 17, 14, 13, 0, ZoneId.systemDefault());
    private static Instant starttime        = Instant.now(mockClock);
    private static int secondsI15           = IntervalLengthValue.SEC_15.getSeconds();
    private static int secondsI30           = IntervalLengthValue.SEC_30.getSeconds();
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
    
    private static final List<IntervalTimeLead>             timeleads       = Arrays.asList(
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
    
    public TC9b_ChangeIntervalLengthMissingDataTest() {
        initAlgo(algo, infraObjects, infraParams, timeleads, timeouts, thresholds);
    }
    
    /**
     * Make sure, test order is correct
     */
    @Test
    public void testChangeIntervalLength() {
        Instant time1300 = Instant.now(mockClock);      // 14:13:00
        LOG.debug("---------------------------- " + DTF.format(time1300) +" -----------------------------");
        
        mockClock.advanceBySeconds(15);
        Instant time1315 = Instant.now(mockClock);      // 14:13:15
        LOG.debug("---------------------------- " + DTF.format(time1315) + " -----------------------------");
        
        mockClock.advanceBySeconds(13);
        Instant time1328 = Instant.now(mockClock);      // 14:13:28
        LOG.debug("---------------------------- " + DTF.format(time1328) + " -----------------------------");
        testStep1(time1315, time1328); //Data for id71 come in, id73 not
        
        mockClock.advanceBySeconds(1);
        Instant time1329= Instant.now(mockClock);      // 14:13:29
        LOG.debug("---------------------------- " + DTF.format(time1329) + "  -----------------------------");
        testStep2(time1329); //id71 interval change to 30, id73 stays at i15
        
        mockClock.advanceBySeconds(1);
        Instant time1330 = Instant.now(mockClock);      // 14:13:30
        LOG.debug("---------------------------- " + DTF.format(time1330) +" -----------------------------");
        triggerIntervalEnd(time1330, Arrays.asList(secondsI15, secondsI30));

        mockClock.advanceBySeconds(12);
        Instant time1342 = Instant.now(mockClock);      // 14:13:42
        LOG.debug("---------------------------- " + DTF.format(time1342) + " -----------------------------");
        triggerTimeoutEnd(time1330, time1342, Arrays.asList(secondsI15, secondsI30));

//        //interval end (30) reached;
//        LOG.debug("---------------------------- 14:13:30 -----------------------------");
//        mockClock.advanceBySeconds(16);
//        Instant time1330 = Instant.now(mockClock);      // 14:13:30
//        testStep3(time1300, time1330); 
    }
    
    /**
     * Test step 1:  values come in
     */
    public void testStep1(Instant eventtime, Instant processingTime) {
        List<Object> builders = new ArrayList<>();
        Map<String, InfraState> infraStates = new HashMap<>();

        List<String> i15Ids = Arrays.asList(id_71);
        for(String id : i15Ids) {
            builders.add(getDataBuilder1(id, IntervalLengthValue.SEC_15.getValue()));
            infraStates.put(id, new InfraState(id, true, null, eventtime, false, false));
        }
        //Set infraState of missing id:
        infraStates.put(id_73, new InfraState(id_73, true, null, eventtime, false, false));
        
        //InfraStates
        algo.setInfraState(infraStates);
        
        //send data at second 0
        TCUtils.setIntervalData(algo, builders, eventtime, processingTime);
    }
    
    /**
     * Test step 2: change infrastructure parameter -> interval 15 => interval 30
     * 
     * @param intervalEnd
     */
    public void testStep2(Instant processingTime) {
        Map<String, InfraParameter> newInfraParams = new HashMap<>() {{
            put(id_71, new InfraParameter(id_71, Type.DIRECT_SET, processingTime, secondsI30, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
            put(id_73, new InfraParameter(id_73, Type.DIRECT_SET, processingTime, secondsI15, version, defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw));
        }};
        algo.setInfraParameter(newInfraParams);
    }
    
    /**
     * Test step 3: interval end for interval 30; publish values, which came in for interval 15 at second 13.
     * @param eventtime
     * @param intervalEnd
     */
    public void testStep3(Instant eventtime, Instant intervalEnd) {
        algo.intervalEndTrigger(intervalEnd, Arrays.asList(secondsI15, secondsI30));
        
        ArgumentCaptor<List<AbstractData>> argInput2 = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds2 = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);

        // No more publish's
        Mockito.verify(pubMock, Mockito.times(1)).publish(argInput2.capture(), argMissingIds2.capture(),
                infraPara.capture(), infraStates.capture(), slowestVehData.capture());
    }
    
    /**
     * Interval end trigger
     * 
     * @param eventtime
     * @param processingTime
     */
    public void triggerIntervalEnd(Instant intervalend, List<Integer> intervalLengths) {
        //trigger interval end for interval 15 at second 30
        algo.intervalEndTrigger(intervalend, intervalLengths);
        //no publish expected, because values for id73 are missing.
    }

    /**
     * Reach timeout of first interval
     * @param timeoutEnd
     * @param intervalEnd
     */
    public void triggerTimeoutEnd(Instant intervalEnd, Instant timeoutEnd, List<Integer> intervalLenghts) {
        algo.intervalTimoutTrigger(timeoutEnd, intervalEnd, intervalLenghts);
        
        ArgumentCaptor<List<AbstractData>> argInput = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Tuple3<String, Instant, Integer>>> argMissingIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, InfraParameter>> infraPara = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, InfraState>> infraStates = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, SingleVehicleData>> slowestVehData = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(pubMock).publish(argInput.capture(), argMissingIds.capture(), infraPara.capture(),
                infraStates.capture(), slowestVehData.capture());

        //interval 15 id71 is there
        assertTrue(argContainsIds(argInput, Arrays.asList(id_71)));
        
        assertTrue(argMissingIds.getValue().size() == 2);
        //interval 30 id71 is missing
        assertTrue(argMissingIds.getValue()
                .contains(new Tuple3<String, Instant, Integer>(id_71, intervalEnd, secondsI30)));

        //interval 15 id73 is missing
        assertTrue(argMissingIds.getValue()
                .contains(new Tuple3<String, Instant, Integer>(id_73, intervalEnd, secondsI15)));
    }
    //-----------------------------------------------------------------------------------

    private boolean argContainsIds(ArgumentCaptor<List<AbstractData>> argInput, List<String> ids) {
        if(argInput == null  || argInput.getValue() == null || ids == null) {
            return false;
        }
        List<String> argIds = argInput.getValue().stream().map(arg -> arg.getId()).collect(Collectors.toList());
        return argIds.containsAll(ids);
    }
    
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
