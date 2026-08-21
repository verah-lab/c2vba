package de.heuboe.asfinag.vmis2.synchronize.vd.publish;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SingleVehicleData;
import io.vavr.Tuple3;

/**
 * Publisher interface: A concrete publisher receives collected and synchronized data and passes it
 * on (broker, database etc.)
 *
 * @param <D> Data
 */
public interface SyncVdPublisher<D extends AbstractData> {

 
    /**
     * Transfer of correct data and ids that did not deliver data
     * 
     * @param data List of collected and synchronized data
     * @param missingIds Infrastructure ids that did not deliver data (Tuple 3 of id,
     *        interval end and interval length)
     * @param infraParameter Infrastructure parameters per infrastructure object
     * @param infraStates Infrastructure states per infrastructure object
     * @param infraId2SlowestVehData If available, data of the slowest vehicle per infrastructure object.
     */
    public void publish(List<D> data, List<Tuple3<String, Instant, Integer>> missingIds,
            Map<String, InfraParameter> infraParameter, Map<String, InfraState> infraStates, 
            Map<String, SingleVehicleData> infraId2SlowestVehData);

    /**
     * Transfer of discarded data
     * 
     * @param data List of data discarded
     * @param infraParameter Infrastructure parameters per infrastructure object
     * @param infraStates Infrastructure states per infrastructure object
     */
    void publishDiscardedData(List<AbstractData> data, Map<String, InfraParameter> infraParameter,
            Map<String, InfraState> infraStates);

    /**
     * Request of time synchronization
     */
    public void publishTimeSynchronization();
    
    /** 
     * Initialization of new infrastructure
     */
    public void initInfrastructure ();
 }
