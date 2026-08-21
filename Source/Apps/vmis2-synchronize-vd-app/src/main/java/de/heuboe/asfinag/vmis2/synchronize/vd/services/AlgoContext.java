package de.heuboe.asfinag.vmis2.synchronize.vd.services;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Context needed to run measure algorithm
 */
@Value
@Slf4j
public class AlgoContext {
    
    private static final String PATTERN_CENTRETOPIC = "{centreTopic}";
    private static final String PATTERN_SYSTEM_WIDE = "{systemWideShortcut}";
    
    private Marker marker = MarkerFactory.getMarker("AlgoContext");

    private SynchronizeVdProperties properties;
    
    private boolean listenSingleVehicleData;
    private String topicTlsData0;
    private String topicTlsData1;
    private String topicTlsData2;
    private String topicTlsData3;
    private String topicTlsData4;
    private String topicTlsData5;
    private String topicTlsData6;
    private String topicTlsSingleVehicleData;
    private String topicTlsSingleVehicleCollectedData;

    private String topicTlsError;
    private String topicTlsOperatingParam;
    private String topicTlsChannelControl;
    private String topicTlsSysError;

    private String topicCollectedDataLane;
    private String topicCollectedTrafficCategoriesLane;
    private String topicCollectedOpcUaDataLane;
    private String topicDiscardedDataLane;
    private String topicDataChange;
    private String topicControlSequence;
    private String topicTlsTrafficCategoriesParam;
    
    /**
     * Constructor with the working road and topic template
     * 
     * @param properties    SynchronizeVdProperties
     */
    
    public AlgoContext(SynchronizeVdProperties properties) {
        this.properties = properties;
        String ct = properties.getCentreTopic();
        String sw = properties.getSystemWideShortcut();
        log.info("Property centreTopic = {}", properties.getCentreTopic());
        log.info("Property systemWideShortcut = {}", properties.getSystemWideShortcut());
        
        if (ct.length() != ct.trim().length()) {
            log.error(marker, "Property centreTopic= '{}' contains spaces!!! Is that on purpose?",
                    ct);
        }
        if (sw.length() != sw.trim().length()) {
            log.error(marker, "Property systemWideShortcut= '{}' contains spaces!!! Is that on purpose?",
                    sw);
        }

        this.listenSingleVehicleData    = properties.isListenSingleVehicleData();
        this.topicTlsData0              = properties.getTlsData0TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData1              = properties.getTlsData1TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData2              = properties.getTlsData2TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData3              = properties.getTlsData3TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData4              = properties.getTlsData4TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData5              = properties.getTlsData5TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsData6              = properties.getTlsData6TopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsSingleVehicleData  = properties.getTlsSingleVehDataTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsSingleVehicleCollectedData     = properties.getTlsSingleVehCollectedDataTopicTemplate()
                                                                .replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsError              = properties.getTlsErrorTopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        
        this.topicTlsOperatingParam     = properties.getTlsOperatingParamTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsChannelControl     = properties.getTlsChannelControlTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicTlsSysError           = properties.getTlsSysErrorTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicCollectedDataLane     = properties.getCollectedDataLaneTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicDiscardedDataLane     = properties.getDiscardedDataLaneTopicTemplate()
                                                    .replace(PATTERN_CENTRETOPIC, ct);
        this.topicCollectedOpcUaDataLane            = properties.getColletedOpcUaDataLaneTopicTemplate()
                                                                .replace(PATTERN_CENTRETOPIC, ct);
        this.topicCollectedTrafficCategoriesLane    = properties.getCollectedTrafficCategoriesLaneTopicTemplate()
                                                                .replace(PATTERN_CENTRETOPIC, ct);

        this.topicDataChange = properties.getDataChangeTopicTemplate().replace(PATTERN_CENTRETOPIC, ct);
        
        this.topicControlSequence = properties.getTopicTmpltControlSequence().replace(PATTERN_SYSTEM_WIDE, sw);
        this.topicTlsTrafficCategoriesParam =
                properties.getTlsTrafficCategoriesParamTopicTemplate().replace(PATTERN_SYSTEM_WIDE, sw);
        
        log.info("AlgoContext = {}", this);
    }
}
