package de.heuboe.asfinag.control.base.actors;

import eu.vmis_ehe.vmis2.paramservice.pojo.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemoryReducedParametersInstanceHandlerTest {

    @Mock
    private AbstractParameterActor.InstanceHandler<PParameterSetList> baseInstanceHandler;

    @Test
    void matchesInstance_test() {
        when(baseInstanceHandler.matchesInstance(any())).thenReturn(true);

        MemoryReducedParametersInstanceHandler memoryReducedParametersInstanceHandler =
                new MemoryReducedParametersInstanceHandler(baseInstanceHandler, new ParametersMemoryReducer());

        ConsumerRecord<String, byte[]> consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", new byte[0]);

        assertTrue(memoryReducedParametersInstanceHandler.matchesInstance(consumerRecord));
        verify(baseInstanceHandler).matchesInstance(consumerRecord);
    }

    @Test
    void toParameters_empty_test() {
        when(baseInstanceHandler.toParameters(any())).thenReturn(Optional.empty());

        MemoryReducedParametersInstanceHandler memoryReducedParametersInstanceHandler =
                new MemoryReducedParametersInstanceHandler(baseInstanceHandler, new ParametersMemoryReducer());

        ConsumerRecord<String, byte[]> consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", new byte[0]);

        assertTrue(memoryReducedParametersInstanceHandler.toParameters(consumerRecord).isEmpty());
        verify(baseInstanceHandler).toParameters(consumerRecord);
    }

    @Test
    void toParameters_nonEmpty_test() {

        AbstractParameterActor.Parameters<PParameterSetList> parameters = new AbstractParameterActor.Parameters<>(
                "key",
                "roadId",
                "algo",
                "instance",
                "system",
                PParameterSetList.builder()
                        .build()

        );

        ParametersMemoryReducer parametersMemoryReducer = new ParametersMemoryReducer();

        when(baseInstanceHandler.toParameters(any())).thenReturn(Optional.of(parameters));

        MemoryReducedParametersInstanceHandler memoryReducedParametersInstanceHandler =
                new MemoryReducedParametersInstanceHandler(baseInstanceHandler, parametersMemoryReducer);

        ConsumerRecord<String, byte[]> consumerRecord = new ConsumerRecord<>("topic", 0, 0, "key", new byte[0]);

        Map<Class<?>, Integer> expectedNumberOfCachedObjectsPerClass = Map.of(
                String.class, 5,
                PParametrizationTarget.class, 0,
                PParameterValue.class, 0,
                PNumberedParameterValues.class, 0,
                PValueWrapper.class, 0
        );

        Optional<AbstractParameterActor.Parameters<PParameterSetList>> optionalParameters =
                memoryReducedParametersInstanceHandler.toParameters(consumerRecord);

        assertFalse(optionalParameters.isEmpty());
        verify(baseInstanceHandler).toParameters(consumerRecord);
        assertEquals(expectedNumberOfCachedObjectsPerClass, parametersMemoryReducer.numberOfCachedObjectsPerClass());

    }
}
