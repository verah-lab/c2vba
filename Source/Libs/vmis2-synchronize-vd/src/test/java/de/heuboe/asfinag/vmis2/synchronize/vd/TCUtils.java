package de.heuboe.asfinag.vmis2.synchronize.vd;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SyncVdAlgo;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0.PLVEErgebnisVersion0Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1.PLVEErgebnisVersion1Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2.PLVEErgebnisVersion2Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3.PLVEErgebnisVersion3Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4.PLVEErgebnisVersion4Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion5.PLVEErgebnisVersion5Builder;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion6.PLVEErgebnisVersion6Builder;

public class TCUtils {
    public enum ClassNameEnum {
        PLVEErgebnisVersion0Builder, PLVEErgebnisVersion1Builder, PLVEErgebnisVersion2Builder, PLVEErgebnisVersion3Builder, 
        PLVEErgebnisVersion4Builder, PLVEErgebnisVersion5Builder, PLVEErgebnisVersion6Builder
    }
    public static TestTlsInputData switchByClassType(Object obj, Instant eventTime, Instant processingTime) {

        ClassNameEnum className = ClassNameEnum.valueOf(obj.getClass().getSimpleName());
        TestTlsInputData tlsInputData = null;

        switch (className) {
            case PLVEErgebnisVersion0Builder:
                PLVEErgebnisVersion0Builder input = ((PLVEErgebnisVersion0Builder) obj);
                tlsInputData = new TestTlsInputData(input.tlsTime(eventTime).processTime(processingTime).build());
                break;
            case PLVEErgebnisVersion1Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion1Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
               break;
            case PLVEErgebnisVersion2Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion2Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
                break;
            case PLVEErgebnisVersion3Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion3Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
                break;
            case PLVEErgebnisVersion4Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion4Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
               break;
            case PLVEErgebnisVersion5Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion5Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
                break;
            case PLVEErgebnisVersion6Builder:
                tlsInputData = new TestTlsInputData(((PLVEErgebnisVersion6Builder) obj).tlsTime(eventTime).processTime(processingTime).build());
                break;
            default:
                break;
        }
        return tlsInputData;
    }

    public static void setIntervalData(SyncVdAlgo<AbstractData> algo,
            List<Object> dataBuilders,
            Instant eventtime,
            Instant processingTime) {

        //InputData
        List<AbstractData> inputData = new ArrayList<>();
        dataBuilders.forEach(b -> {
            inputData.add(switchByClassType(b, eventtime, processingTime));
        });
        //Data
        algo.setData(inputData);
    }
    
    public static void setInfraStates(SyncVdAlgo<AbstractData> algo,
            List<String> ids,
            Instant eventtime,
            Instant processingTime) {
        
        //InfraStates
        Map<String, InfraState> infraStates2 = new HashMap<>();
        for(String id : ids) {
            infraStates2.put(id, new InfraState(id, true, null, eventtime, false, false));
        }
        algo.setInfraState(infraStates2);
        
    }  
    public static void setIntervalData(SyncVdAlgo<AbstractData> algo,
            List<Object> dataBuilders,
            List<String> ids,
            Instant eventtime,
            Instant processingTime) {
        
        //InputData
        List<AbstractData> inputData = new ArrayList<>();
        dataBuilders.forEach(b -> {
            inputData.add(switchByClassType(b, eventtime, processingTime));
        });
        algo.setData(inputData);
    }
    
    public static void setSingleVehicleData(SyncVdAlgo<AbstractData> algo,
            List<Object> dataBuilders,
            List<String> ids,
            Instant eventtime,
            Instant processingTime) {
        
        //InputData
        List<AbstractData> inputData = new ArrayList<>();
        dataBuilders.forEach(b -> {
            inputData.add(switchByClassType(b, eventtime, processingTime));
        });
        algo.setData(inputData);
    }
    
}
