package de.heuboe.asfinag.vmis2.synchronize.vd.data;

import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.IntervalLengthValue;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion5;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion6;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Input traffic data (short term recording lane data) for the collecting and time synchronization
 * algorithm
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class TlsInputData extends AbstractData {

    private Object inputData;

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 0
     */
    public TlsInputData(PLVEErgebnisVersion0 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds(), 0, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 1
     */
    public TlsInputData(PLVEErgebnisVersion1 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                1, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 2
     */
    public TlsInputData(PLVEErgebnisVersion2 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                2, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 3
     */
    public TlsInputData(PLVEErgebnisVersion3 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                3, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 4
     */
    public TlsInputData(PLVEErgebnisVersion4 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                4, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 5
     */
    public TlsInputData(PLVEErgebnisVersion5 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                5, true);
        this.inputData = inputData;
    }

    /**
     * Constructor
     * 
     * @param inputData Traffic short term recording lane input data for option/version 6
     */
    public TlsInputData(PLVEErgebnisVersion6 inputData) {
        super(inputData.getId(), inputData.getTlsTime(), inputData.getProcessTime(),
                IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()) != null
                        ? IntervalLengthValue.getIntervalLengthValue(inputData.getIntervalllaenge()).getSeconds()
                        : null,
                6, true);
        this.inputData = inputData;
    }
}
