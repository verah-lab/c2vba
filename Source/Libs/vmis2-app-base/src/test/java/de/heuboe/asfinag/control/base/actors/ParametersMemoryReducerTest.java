package de.heuboe.asfinag.control.base.actors;

import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;
import eu.vmis_ehe.vmis2.paramservice.pojo.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ParametersMemoryReducerTest {

    @Test
    void emptyCache_test() {
        ParametersMemoryReducer parametersMemoryReducer = new ParametersMemoryReducer();

        Map<Class<?>, Integer> expectedNumberOfCachedObjectsPerClass = Map.of(
                String.class, 0,
                PParametrizationTarget.class, 0,
                PParameterValue.class, 0,
                PNumberedParameterValues.class, 0,
                PValueWrapper.class, 0
        );

        assertEquals(expectedNumberOfCachedObjectsPerClass, parametersMemoryReducer.numberOfCachedObjectsPerClass());
    }

    @Test
    void clearCache_test() {
        ParametersMemoryReducer parametersMemoryReducer = new ParametersMemoryReducer();

        AbstractParameterActor.Parameters<PParameterSetList> parameters = new AbstractParameterActor.Parameters<>(
                "key",
                "roadId",
                "algo",
                "instance",
                "system",
                PParameterSetList.builder()
                        .build()

        );

        assertEquals(parameters, parametersMemoryReducer.apply(parameters));

        Map<Class<?>, Integer> expectedNumberOfCachedObjectsPerClass = Map.of(
                String.class, 0,
                PParametrizationTarget.class, 0,
                PParameterValue.class, 0,
                PNumberedParameterValues.class, 0,
                PValueWrapper.class, 0
        );

        assertNotEquals(expectedNumberOfCachedObjectsPerClass, parametersMemoryReducer.numberOfCachedObjectsPerClass());

        parametersMemoryReducer.clearInternalCache();

        assertEquals(expectedNumberOfCachedObjectsPerClass, parametersMemoryReducer.numberOfCachedObjectsPerClass());
    }

    @Test
    void cachedObjects_test() {
        ParametersMemoryReducer parametersMemoryReducer = new ParametersMemoryReducer();

        PParameterSetList parameterSetList1 = PParameterSetList.builder()
                .iid("iid1")
                .valuesList(
                        List.of(
                                PParameterSet.builder()
                                        .target(
                                                PParametrizationTarget.builder()
                                                        .itemType(PItemType.builder()
                                                                .type(PConfigItemType.AQ)
                                                                .build()
                                                        )
                                                        .build()
                                        )
                                        .numberedValuesList(
                                                List.of(
                                                        PNumberedParameterValues.builder()
                                                                .valuesList(
                                                                        List.of(
                                                                                PParameterValue.builder()
                                                                                        .value(
                                                                                                PValueWrapper.builder()
                                                                                                        .intVal(42)
                                                                                                        .build()
                                                                                        )
                                                                                        .build()
                                                                        )
                                                                )
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                )
                .build();

        AbstractParameterActor.Parameters<PParameterSetList> parameters1 = new AbstractParameterActor.Parameters<>(
                "key1",
                "roadId",
                "algo",
                "instance",
                "system",
                parameterSetList1

        );

        PParameterSetList parameterSetList2 = PParameterSetList.builder()
                .iid("iid2")
                .valuesList(
                        List.of(
                                PParameterSet.builder()
                                        .target(
                                                PParametrizationTarget.builder()
                                                        .itemType(PItemType.builder()
                                                                .type(PConfigItemType.AQ)
                                                                .build()
                                                        )
                                                        .build()
                                        )
                                        .numberedValuesList(
                                                List.of(
                                                        PNumberedParameterValues.builder()
                                                                .valuesList(
                                                                        List.of(
                                                                                PParameterValue.builder()
                                                                                        .value(
                                                                                                PValueWrapper.builder()
                                                                                                        .intVal(42)
                                                                                                        .build()
                                                                                        )
                                                                                        .build()
                                                                        )
                                                                )
                                                                .build()
                                                )
                                        )
                                        .valuesList(
                                                List.of(
                                                        PParameterValue.builder()
                                                                .value(
                                                                        PValueWrapper.builder()
                                                                                .intVal(4711)
                                                                                .build()
                                                                )
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                )
                .build();

        AbstractParameterActor.Parameters<PParameterSetList> parameters2 = new AbstractParameterActor.Parameters<>(
                "key2",
                "roadId",
                "algo",
                "instance",
                "system",
                parameterSetList2

        );

        assertEquals(parameters1, parametersMemoryReducer.apply(parameters1));
        assertEquals(parameters2, parametersMemoryReducer.apply(parameters2));

        Map<Class<?>, Integer> expectedNumberOfCachedObjectsPerClass = Map.of(
                String.class, 8,
                PParametrizationTarget.class, 1,
                PParameterValue.class, 2,
                PNumberedParameterValues.class, 1,
                PValueWrapper.class, 2
        );

        assertEquals(expectedNumberOfCachedObjectsPerClass, parametersMemoryReducer.numberOfCachedObjectsPerClass());
    }
}
