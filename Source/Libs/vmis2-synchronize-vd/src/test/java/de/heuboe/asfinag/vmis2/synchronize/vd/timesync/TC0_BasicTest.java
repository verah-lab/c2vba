package de.heuboe.asfinag.vmis2.synchronize.vd.timesync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.statemachinesystems.mockclock.MockClock;

import de.heuboe.asfinag.vmis2.synchronize.vd.MockObjects.TestAbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.TestTlsInputData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractInfraParameter.Type;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTemporaryThresholds;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimout;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimeLead;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.IntervalLengthValue;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SyncVdAlgo;
import de.heuboe.asfinag.vmis2.synchronize.vd.publish.SyncVdPublisher;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3.PLVEErgebnisVersion3Builder;
import org.mockito.Mockito;

/**
 * Basic module tests.
 *
 * Objective: Creating and handling with objects.<br>
 * Description: Check creation and handling of objects.<br>
 */
@SuppressWarnings("unchecked")
public class TC0_BasicTest {
    
    private static final MockClock clock        = MockClock.at(2018, 12, 24, 8, 0, 15, ZoneId.systemDefault());
    private static final Instant starttime      = Instant.now(clock); 
    private static final String id_uz           = "UZ1";
    private static final String id_71           = "2000071";
    private static final String id_73           = "2000073";
    private static final int codeI15            = IntervalLengthValue.SEC_15.getValue();
    private static final int codeI30            = IntervalLengthValue.SEC_30.getValue();
    private static final int secI15             = IntervalLengthValue.getIntervalLengthValue(codeI15).getSeconds();
    private static final int secI30             = IntervalLengthValue.getIntervalLengthValue(codeI30).getSeconds();
    private static final Integer version        = PTlsDataVersion.VERSION_3.getNumber();
    private static final int defaultTimeout     = 12;
    private static final int defaultTimelead    = 5;
    private static final int defaultSyncWait    = 40;
    private static final int defaultUppThresh   = 26;
    private static final int defaultLowThresh   = -11;
    private static final Boolean defaultvArithmetical = true;
    private static final List<Integer> categoryBoudariesPkw = List.of(10, 20, 50, 100, 120, 160, 200);
    private static final List<Integer> categoryBoudariesLkw = List.of(5, 15, 45, 95, 115);

    
    private static final List<IntervalTimout>               timeouts        = Arrays.asList(
            new IntervalTimout(IntervalLengthValue.SEC_15, defaultTimeout),
            new IntervalTimout(IntervalLengthValue.SEC_30, defaultTimeout));
    
    private static final List<IntervalTimeLead>             timeleads       = Arrays.asList(
            new IntervalTimeLead(IntervalLengthValue.SEC_15, defaultTimelead),
            new IntervalTimeLead(IntervalLengthValue.SEC_30, defaultTimelead));
    
    private static final List<IntervalTemporaryThresholds>  thresholds      = Arrays.asList(
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_15, defaultLowThresh, defaultUppThresh),
            new IntervalTemporaryThresholds(IntervalLengthValue.SEC_30, defaultLowThresh, defaultUppThresh));
    
    /**
     * Test with missing algoParameter.
     */
    @Test
    public void testNullAlgoParameter() {
        try {
            SyncVdPublisher<TestTlsInputData> pubMock = Mockito.mock(SyncVdPublisher.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        SyncVdPublisher<TestTlsInputData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<TestTlsInputData> algo = new SyncVdAlgo<>(pubMock, clock);
        try {
            algo.setAlgoParameter(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            algo.setInfraParameter(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            algo.setInfraState(null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            PLVEErgebnisVersion3Builder dataBuilder = PLVEErgebnisVersion3.builder();
            dataBuilder.id(id_71);
            dataBuilder.qKfz(7);
            dataBuilder.qLkwAe(3);
            dataBuilder.vPkwAe(110);
            dataBuilder.vLkwAe(80);
            dataBuilder.nettozeitluecke(4.);
            dataBuilder.belegung(50);
            dataBuilder.sKfz(20);
            dataBuilder.vKfz(60);
            dataBuilder.intervalllaenge(codeI15);
            algo.setData(Arrays.asList(new TestTlsInputData(dataBuilder.tlsTime(starttime).processTime(starttime).build())));
                       
 
        } catch(Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
        
    }
    
    /**
     * Test with missing parameters (if algo parameter is set)
     */
    @Test
    public void testNullParameter() {
        MockClock eventtime = MockClock.at(2018, 12, 24, 8, 0, 15, ZoneId.systemDefault());
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, eventtime);

        algo.setAlgoParameter(new AlgoParameter(id_uz, Type.DIRECT_SET, Instant.now(eventtime),
                defaultSyncWait, defaultTimelead, timeouts, timeleads, thresholds));

        try {
            algo.setInfraParameter(null);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            algo.setInfraState(null);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            algo.setInfrastructure(null);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    /**
     * Test AlgoParameter.
     */
    @Test
    public void testAlgoParameter() {
        AlgoParameter a = new AlgoParameter(id_uz, Type.DIRECT_SET, starttime,
                defaultSyncWait, defaultTimelead, timeouts, timeleads, thresholds);
        AlgoParameter a2 = new AlgoParameter(id_uz, Type.DIRECT_SET, starttime,
                defaultSyncWait, defaultTimelead, null, null, null);
        new AlgoParameter(id_uz, Type.DIRECT_SET, starttime,
                defaultSyncWait, defaultTimelead, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        
        Optional<Integer> lowT = a.getLowerTemporayThresholdForInterval(secI15);
        Optional<Integer> lowT2 = a.getLowerTemporayThresholdForInterval(IntervalLengthValue.SEC_15);
        Optional<Integer> upT = a.getUpperTemporayThresholdForInterval(secI30);
        Optional<Integer> upT2 = a.getUpperTemporayThresholdForInterval(IntervalLengthValue.SEC_30);
        Optional<Integer> tl = a.getTimeLeadForInterval(secI15);
        Optional<Integer> tl2 = a.getTimeLeadForInterval(IntervalLengthValue.SEC_15);
        Optional<Integer> to = a.getTimeoutForInterval(secI30);
        Optional<Integer> to2 = a.getTimeoutForInterval(IntervalLengthValue.SEC_30);
        
        assertTrue(lowT.isPresent() && lowT.get().intValue() == defaultLowThresh);
        assertTrue(lowT2.isPresent() && lowT2.get().intValue() == defaultLowThresh);
        assertTrue(upT.isPresent() && upT.get().intValue() == defaultUppThresh);
        assertTrue(upT2.isPresent() && upT2.get().intValue() == defaultUppThresh);
        
        assertTrue(tl.isPresent() && tl.get().intValue() == defaultTimelead);
        assertTrue(tl2.isPresent() && tl2.get().intValue() == defaultTimelead);
        assertTrue(to.isPresent() && to.get().intValue() == defaultTimeout);
        assertTrue(to2.isPresent() && to2.get().intValue() == defaultTimeout);
        
        assertTrue(id_uz.equals(a.getId()));
        assertTrue(starttime.equals(a.getTime()));
        assertTrue(defaultSyncWait == a.getResendTimeSyncWaitTime());
        assertTrue(defaultTimelead == a.getDefaultTimeLead());
        assertTrue(a.getThresholds() != null && a.getThresholds().size() == 2);
        assertTrue(a.getTimeLeads() != null && a.getTimeLeads().size() == 2);
        assertTrue(a.getTimeouts() != null && a.getTimeouts().size() == 2);
        assertTrue(Type.DIRECT_SET.equals(a.getType()));
        
        assertTrue(a.hashCode() != 0);
        assertTrue(!a.equals(a2));
        String toString = a.toString();
        assertNotNull(toString);
        assertTrue(!"".equals(toString));
    }
    
    /**
     * Test InfraStates.
     */
    @Test
    public void testInfraStates() {
        String cause = "This is a cause!";
        InfraState state = new InfraState(id_73, false, cause, starttime, true, false);
        InfraState state2 = new InfraState(id_73, false, cause, starttime, true, false);
        assertTrue(cause.equals(state.getCause()));
        assertTrue(id_73.equals(state.getId()));
        assertTrue(Boolean.FALSE.equals(state.isOk()));
        assertTrue(starttime.equals(state.getTime()));
        assertTrue(Boolean.TRUE.equals(state.isPhysicalPassivated()));
        assertTrue(Boolean.FALSE.equals(state.isLogicalPassivated()));
        assertTrue(state.hashCode() != 0);
        assertTrue(state.equals(state2));
        String toString = state.toString();
        assertNotNull(toString);
        assertTrue(!"".equals(toString));
    }
    
    /**
     * Test InfraParameter.
     */
    @Test
    public void testInfraParameter() {
        InfraParameter infra = new InfraParameter(id_71, Type.DIRECT_SET, starttime, secI15, version,
                defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw);
        InfraParameter infra2 = new InfraParameter(id_71, Type.DIRECT_SET, starttime, secI15, version,
                defaultvArithmetical, categoryBoudariesPkw, categoryBoudariesLkw);
        
        assertTrue(id_71.equals(infra.getId()));
        assertTrue(Type.DIRECT_SET.equals(infra.getType()));
        assertTrue(starttime.equals(infra.getTime()));
        assertTrue(infra.getVersion().isPresent() && infra.getVersion().get() == version);
        assertTrue(infra.getIntervalLength() == secI15);
        assertTrue(infra.hashCode() != 0);
        assertTrue(infra.equals(infra2));
        String toString = infra.toString();
        assertNotNull(toString);
        assertTrue(!"".equals(toString));
    }
    
    /**
     * Test TlsInputData / AbstractData.
     */
    @Test
    public void testTlsInputData() {
        PLVEErgebnisVersion3 lane = new PLVEErgebnisVersion3(id_71, 0, starttime, starttime, 1, codeI15, 7, 3, 110, 80, 3., 50, 20, 60);
        TestTlsInputData d = new TestTlsInputData(lane);
        TestTlsInputData d2 = new TestTlsInputData(lane);
        
        assertTrue(id_71.equals(d.getId()));
        assertTrue(starttime.equals(d.getEventTime()));
        assertTrue(starttime.equals(d.getProcessingTime()));
        assertTrue(lane.equals(d.getInputData()));
        assertTrue(d.getIntervalLength() == secI15);
        assertTrue(d.getVersion().isPresent() && d.getVersion().get().intValue() == PTlsDataVersion.VERSION_3.getNumber());
        assertTrue(d.hashCode() != 0);
        assertTrue(d.equals(d2));
        String toString = d.toString();
        assertNotNull(toString);
        assertTrue(!"".equals(toString));
        
        clock.advanceBySeconds(10);
        Instant eventtime = Instant.now(clock);
        d.setEventTime(eventtime);
        d.setProcessingTime(eventtime);
        d.setVersion(PTlsDataVersion.VERSION_2.getNumber());
        d.setIntervalLength(secI30);
        d.setId(id_73);
        d.setCreateTimeSynchronization(true);
        
        assertTrue(id_73.equals(d.getId()));
        assertTrue(eventtime.equals(d.getEventTime()));
        assertTrue(eventtime.equals(d.getProcessingTime()));
        assertTrue(d.getIntervalLength() == secI30);
        assertTrue(d.getVersion().isPresent() && d.getVersion().get().intValue() == PTlsDataVersion.VERSION_2.getNumber());
    }
    
    /**
     * Test IntervalLengthValue.
     */
    @Test
    public void testIntervalLengthValue() {
        //Get some available values:
        assertTrue(IntervalLengthValue.getIntervalLengthValue(1).getSeconds() == 15);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(2).getSeconds() == 30);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(4).getSeconds() == 60);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(20).getSeconds() == 300);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(60).getSeconds() == 900);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(120).getSeconds() == 1800);
        assertTrue(IntervalLengthValue.getIntervalLengthValue(240).getSeconds() == 3600);
        
        //Get not available value
        assertEquals(IntervalLengthValue.UNDEFINED, IntervalLengthValue.getIntervalLengthValue(7));
    }
    
    /**
     * Test SyncVdPublisher.
     */
    // Commented out first, because SyncVdPublisher is now an interface (MK)
    /*@Test
    public void testSyncVdPublisher() {
       
        PLVEErgebnisVersion3 lane = new PLVEErgebnisVersion3(id_71, starttime, starttime, 1, codeI15, 7, 3, 110, 80, 3., 50, 20, 60);
        TlsInputData d = new TlsInputData(lane);
        TestAbstractData testAD = new TestAbstractData();
        
        SyncVdPublisher<AbstractData> pub = new SyncVdPublisher<>();
        SyncVdPublisher<AbstractData> pub2 = new SyncVdPublisher<>();
        
        pub.publish(null, null);
        pub.publish(new ArrayList<>(), new ArrayList<>());
        pub.publish(Arrays.asList(d), Arrays.asList("id1", "id2"));
        pub.publish(Arrays.asList(testAD), new ArrayList<>());
        
        pub.publishDiscardedData(null);
        pub.publishDiscardedData(new ArrayList<>());
        pub.publishDiscardedData(Arrays.asList(d));
        pub.publishDiscardedData(Arrays.asList(testAD));
        
        pub.publishTimeSynchronization();
        
        assertTrue(pub.equals(pub2));
        assertTrue(pub.hashCode() != 0);
        String toString = pub.toString();
        assertNotNull(toString);
        assertTrue(!"".equals(toString));
    }*/
    
    /**
     * Test SyncVdAlgo.
     */
    @Test
    public void testSyncVdAlgo() {
        MockClock eventtime = MockClock.at(2018, 12, 24, 8, 0, 15, ZoneId.systemDefault());
        SyncVdPublisher<AbstractData> pubMock = Mockito.mock(SyncVdPublisher.class);
        SyncVdAlgo<AbstractData> algo = new SyncVdAlgo<>(pubMock, eventtime);
        
        algo.setAlgoParameter(new AlgoParameter(id_uz, Type.DIRECT_SET, Instant.now(eventtime),
                defaultSyncWait, defaultTimelead, timeouts, timeleads, thresholds));
        Map<String, InfraState> infraStates = new HashMap<>();
        infraStates.put(id_71, new InfraState(id_71, true, null, starttime, false, false));
        algo.setInfraState(infraStates);
       
    }
}
