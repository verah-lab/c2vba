package de.heuboe.asfinag.vmis2.constants;

/**
 * In this class, constants are defined that specify values which should be used uniformly when
 * using Kafka.
 */
public final class KafkaConstants {

    private KafkaConstants() {
        // Do not instantiate
    }

    public static final String KAFKA_HEADER_ROAD_ID = "X-RoadId";
    public static final String KAFKA_HEADER_ALGO = "X-Algo";
    public static final String KAFKA_HEADER_INSTANCE = "X-Instance";
    public static final String KAFKA_HEADER_AGG_INTERVAL = "X-AggInterval";
    public static final String KAFKA_HEADER_SITUATION_CLASS = "X-SituationClass";
    public static final String KAFKA_HEADER_SITUATION_SUB_CLASSES = "X-SituationSubClasses";
    public static final String KAFKA_HEADER_DEFINITION_SET_ID = "X-PARAMETER_DEFINITION_SET_ID";
    public static final String KAFKA_HEADER_SYSTEM = "X-PARAMETER_SYSTEM";
	public static final String KAFKA_HEADER_ORIGIN = "X-Origin";
}
