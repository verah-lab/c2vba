package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum defines possible interval lengths of measurement values (input)
 *
 * @author Marion Keune
 *
 */
public enum IntervalLengthValue {

    SEC_15(1), SEC_30(2), SEC_60(4), MIN_2(8), MIN_3(12), MIN_4(16), MIN_5(20), MIN_15(60), MIN_30(120), MIN_60(240), UNDEFINED(-1);

    private int value = 0;
    
    private static final Logger LOG = LoggerFactory.getLogger(IntervalLengthValue.class);

    private IntervalLengthValue(final int value) {
        this.value = value;
    }

    /**
     * Get the interval length in seconds.
     *
     * @return the interval length in seconds.
     */
    public int getSeconds() {
        if (!getIntervalLengthValue(this.value).equals(UNDEFINED)) {
            return this.value*15;
        }
        return (this.value);
    }

    /**
     * Get the enum type of interval length (15 =&gt; SEC_15)
     *
     * @param value The numeric coded value (1 =&gt; SEC_15 =&gt; 15 seconds) of the input interval
     * @return the enum type
     */
    public static IntervalLengthValue getIntervalLengthValue(final int value) {
        for (IntervalLengthValue ilv : IntervalLengthValue.values()) {
            if (value == ilv.value) {
                return ilv;
            }
        }
        LOG.error ("The numeric coded value {} of the input interval is invalid", value);
        return UNDEFINED;
    }
    
    /**
     * Get the enum type of interval length (15 =&gt; SEC_15)
     *
     * @param seconds The input interval in seconds (15 seconds =&gt; SEC_15)
     * @return the enum type
     */
    public static IntervalLengthValue getIntervalLengthValueOfSeconds(final int seconds) {
        int value = seconds / 15;
        for (IntervalLengthValue ilv : IntervalLengthValue.values()) {
            if (value == ilv.value) {
                return ilv;
            }
        }
        LOG.error ("The second value {} of the input interval is invalid", seconds);
        return UNDEFINED;
    }
    
    /** 
     * Get the coded interval length
     * 
     * @return the coded value
     */
    public int getValue() {
        return value;
    }
    
}
