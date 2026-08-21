package de.heuboe.asfinag.vmis2.synchronize.vd.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;

import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Class for the properties needed to publish on kafka, to read from kafka.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.asfinag.vmis2.synchronize.vd")
@Data
public class SynchronizeVdProperties {

    // -------------------------- common properties -------------------------------

    /**
     * UZ/rVMZ centre/system id to query the ConfigService. If the centreId is equal to the property
     * centreIdAllUZ, then work should be carried out for all UZten.
     */
    @NotNull
    private String centreId;
    
    /**
     * If the centreId contains this string (default: "-ALL-"), then all UZten contained in the
     * configuration should be read(configService).
     */
    private String centreIdAllUZ = "-ALL-";
    
    /**
     * UZ/rVMZ centre/system short name for topic names. Attention, if the centreId is equal to
     * centreIdAllUZ, then centreTopic property should be equal to systemWideShortcut property,
     * because centreTopc is used for the result topic names, for example.
     */
    @NotNull
    private String centreTopic;
    
    /**
     * Identification(shortcut) string for system-wide(default: "VRZ"). systemWideShortcut is used
     * for parameter topic name, for example.
     */
    private String systemWideShortcut = "VRZ"; 
    
    /**
     * Street pattern to work for.
     */
    @NotNull
    private String streets = ".*";
    
    /**
     * If MQs have no roadId, the app could not handle them. Take this defaultRoadId for those MQs.
     * This field is nullable. If no defaultRoadId is set, MQs with empty roadId will be ignored.
     */
    private String defaultRoadId;

    private int defaultErrorValue = -1;
    private float defaultErrorValueFloat = -99999.0f;

    /**
     * test data logging.
     */
    private boolean writeFileLog = false;
    
    /**
     * Indicate, if discarded data should be written or not.
     */
    private boolean writeDiscarded = true;

    /**
     * Indicate, if application should listen to single vehicle data.
     * If true, app listens to tlsSingleVehDataTopicTemplate & tlsSingleVehCollectedDataTopicTemplate.
     */
    private boolean listenSingleVehicleData = true;
    
    /**
     * test data logging.
     */
    private String fileLogPath = "data/";

    /**
     * fake/set infrastructure parameter(LVEBetriebsParam) for all lane ids. So that not a large part of
     * the input values is discarded.
     */
    private boolean fakeInfraParams;

    // --------------------------- algo properties ------------------------------
    @NotNull
    private String algoName;       //TODO remove, not used
    @NotNull
    private String algoShortName; // for kafka topic        //TODO remove, not used
    @NotNull
    private String instanceName;

    // -------------------------- KAFKA properties -------------------------------
    /**
     * tls input data topic for option 0.
     */
    @NotNull
    private String tlsData0TopicTemplate;

    /**
     * tls input data topic for option 1.
     */
    @NotNull
    private String tlsData1TopicTemplate;

    /**
     * tls input data topic for option 2.
     */
    @NotNull
    private String tlsData2TopicTemplate;

    /**
     * tls input data topic for option 3.
     */
    @NotNull
    private String tlsData3TopicTemplate;

    /**
     * tls input data topic for option 4.
     */
    @NotNull
    private String tlsData4TopicTemplate;

    /**
     * tls input data topic for option 5.
     */
    @NotNull
    private String tlsData5TopicTemplate;

    /**
     * tls input data topic for option 6.
     */
    @NotNull
    private String tlsData6TopicTemplate;
    
    /**
     * Tls input data topic for single vehicle data
     */
    @NotNull
    private String tlsSingleVehDataTopicTemplate;
    
    /**
     * Tls input data topic for single vehicle data (collected).
     */
    @NotNull
    private String tlsSingleVehCollectedDataTopicTemplate;

    /**
     * data change topic
     */
    @NotNull
    private String dataChangeTopicTemplate;

    @NotNull
    private String tlsErrorTopicTemplate;

    @NotNull
    private String tlsOperatingParamTopicTemplate;

    @NotNull
    private String tlsChannelControlTopicTemplate;

    @NotNull
    private String tlsSysErrorTopicTemplate;
    
    @NotNull
    private String tlsTrafficCategoriesParamTopicTemplate;
    
    @NotEmpty
    private List<String> parameterRoadTopics;
    
    @NotEmpty
    private List<String> parameterSystemTopics;
    
    /**
     * publish all collected lane data to kafka.
     */
    @NotNull
    private String collectedDataLaneTopicTemplate;

    /**
     * publish all collected lane traffic categories to kafka.
     */
    @NotNull
    private String collectedTrafficCategoriesLaneTopicTemplate;

    /**
     * publish all collected opc-ua lane data to kafka.
     */
    @NotNull
    private String colletedOpcUaDataLaneTopicTemplate;

    /**
     * publish discarded lane data.
     */
    @NotNull
    private String discardedDataLaneTopicTemplate;
    
    /**
     * Control sequence topic to request a global time synchronization
     */
    @NotNull
    private String topicTmpltControlSequence;
  
    /**
     * Data change types that do an infrastructure reinitialization. for possible enum names, see:
     * {@link PConfigItemType}
     */
    @NotEmpty
    private Set<String> dataChangeType;
    
    /**
     *  Maximum numbers of retries to restart an actor before system.exit
     */
    private int maxNrOfRestartRetries = 4;
    
    /**
     * Time range (in minutes) for actor restarts before system.exit
     * 0=deactivate of system.exit cause of actor restarts
     */
    private int restartsWithinTimeRange = 5;
    
    /**
     * Tls control sequence action to request a global time synchronization
     */
    private Integer actionNrRequestGlobalTimeSync = 2002018;
    
    private Integer categoryPkw = 32;
    private Integer categoryLkw = 33;
    
    /**
     * Default quality of the slowest vehicle data built on the basis of single vehicle data
     */
    private Integer maxQualitySVDataInput= 100;
    
    /**
     * Set default quality of the slowest vehicle data built on the basis of traffic categories (TLS version
     * 4)
     */
    private Integer maxQualityTCDataInput = 80;
    
    /**
     * Set default quality, if speed of slowest vehicle could not be determined
     */
    private Integer minQualityInputSlowV = 0;
    
}
