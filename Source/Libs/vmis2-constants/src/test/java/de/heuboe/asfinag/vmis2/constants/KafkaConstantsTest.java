package de.heuboe.asfinag.vmis2.constants;

import static de.heuboe.asfinag.vmis2.constants.KafkaConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


/**
 * {@link KafkaConstants} related unit tests.
 */
public class KafkaConstantsTest {

    @Test
    public void testConstantValues() {

        // 5 declared constants are expected
        assertEquals(9, KafkaConstants.class.getFields().length);

        // they should be defined with the following values
        assertEquals("X-RoadId", KAFKA_HEADER_ROAD_ID); // NOSONAR argument order is intended to be this way
        assertEquals("X-Algo", KAFKA_HEADER_ALGO);// NOSONAR
        assertEquals("X-Instance", KAFKA_HEADER_INSTANCE);// NOSONAR
        assertEquals("X-AggInterval", KAFKA_HEADER_AGG_INTERVAL);// NOSONAR
        assertEquals("X-SituationClass", KAFKA_HEADER_SITUATION_CLASS);// NOSONAR
        assertEquals("X-SituationSubClasses", KAFKA_HEADER_SITUATION_SUB_CLASSES);// NOSONAR
        assertEquals("X-PARAMETER_DEFINITION_SET_ID", KAFKA_HEADER_DEFINITION_SET_ID);// NOSONAR
        assertEquals("X-PARAMETER_SYSTEM", KAFKA_HEADER_SYSTEM);// NOSONAR
        assertEquals("X-Origin", KAFKA_HEADER_ORIGIN);// NOSONAR

    }

}
