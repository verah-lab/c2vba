package de.heuboe.asfinag.vmis2.synchronize.vd.config;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Parameter id strings for the access to the parameter list
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names")
@Data
public class AlgoParameterIdProperties {
    
    /**
     * Definition set id for time sync.
     */
    @NotNull
    private String timeSyncDefSetId;
    
    /**
     * Parameter id for waiting time in seconds to resend time synchronization
     */
    @NotNull
    private String syncWaitSec;
    
    /**
     * Parameter id for timelead in seconds.
     */
    @NotNull
    private String timelead;
    
    /*
     * Parameter id for 15 seconds timeout.
     */
    @NotNull
    private String  timeout15Secs;
    
    /*
     * Parameter id for 30 seconds timeout.
     */
    @NotNull
    private String  timeout30Secs;
    
    /*
     * Parameter id for 60 seconds timeout.
     */
    @NotNull
    private String  timeout60Secs;
    
    /*
     * Parameter id for 2 minutes timeout.
     */
    @NotNull
    private String  timeout2Min;

    /*
     * Parameter id for 3 minutes timeout.
     */
    @NotNull
    private String  timeout3Min;
    
    /*
     * Parameter id for 4 minutes timeout.
     */
    @NotNull
    private String  timeout4Min;
    
    /*
     * Parameter id for 5 minutes timeout.
     */
    @NotNull
    private String  timeout5Min;  
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 15 seconds interval.
     */
    @NotNull
    private String thresholdUpper15Sec;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 15 seconds interval.
     */
    @NotNull
    private String thresholdLower15Sec;
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 30 seconds interval.
     */
    @NotNull
    private String thresholdUpper30Sec;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 30 seconds interval.
     */
    @NotNull
    private String thresholdLower30Sec;
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 60 seconds interval.
     */
    @NotNull
    private String thresholdUpper60Sec;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 60 seconds interval.
     */
    @NotNull
    private String thresholdLower60Sec;
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 2 minutes interval.
     */
    @NotNull
    private String thresholdUpper2Min;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 2 minutes interval.
     */
    @NotNull
    private String thresholdLower2Min;
 
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 3 minutes interval.
     */
    @NotNull
    private String thresholdUpper3Min;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 3 minutes interval.
     */
    @NotNull
    private String thresholdLower3Min;
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 4 minutes interval.
     */
    @NotNull
    private String thresholdUpper4Min;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 4 minutes interval.
     */
    @NotNull
    private String thresholdLower4Min;
    
    /**
     * Parameter id for upper threshold in seconds to trigger synchronization for 5 minutes interval.
     */
    @NotNull
    private String thresholdUpper5Min;

    /**
     * Parameter id for lower threshold in seconds to trigger synchronization for 5 minutes interval.
     */
    @NotNull
    private String thresholdLower5Min;
    
    @NotNull
    private String logPassiveDefSetId;
    
    @NotNull
    private String logPassive;
}
