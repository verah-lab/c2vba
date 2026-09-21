package de.heuboe.asfinag.control.base.services;

import eu.vmis_ehe.vmis2.paramservice.pojo.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterUtilsTest {
    private Marker logMarker = MarkerFactory.getMarker("NYI");

    @Test
    void testIntegerValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", 1);
        valuesList.put("second", 2);
        valuesList.put("third", 3);
        Optional<Integer> threeValue =
                ParameterUtils.getIntParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(3, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getIntParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testIntegerNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1);
        numberedValuesList.put("second", 2);
        numberedValuesList.put("third", 3);
        Optional<Integer> threeValue = ParameterUtils
                .getIntParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(3, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getIntParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testBooleanValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", true);
        valuesList.put("second", false);
        valuesList.put("third", true);
        Optional<Boolean> threeValue =
                ParameterUtils.getBooleanParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(true, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getBooleanParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testBooleanNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", true);
        numberedValuesList.put("second", false);
        numberedValuesList.put("third", true);
        Optional<Boolean> threeValue = ParameterUtils
                .getBooleanParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(true, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getBooleanParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testDoubleValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", 1.0);
        valuesList.put("second", 2.0);
        valuesList.put("third", 3.0);
        Optional<Double> threeValue =
                ParameterUtils.getDoubleParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(3.0, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getDoubleParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testDoubleNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1.0);
        numberedValuesList.put("second", 2.0);
        numberedValuesList.put("third", 3.0);
        Optional<Double> threeValue = ParameterUtils
                .getDoubleParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(3.0, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getDoubleParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testFloatValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", 1f);
        valuesList.put("second", 2f);
        valuesList.put("third", 3f);
        Optional<Float> threeValue =
                ParameterUtils.getFloatParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(3f, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getFloatParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getFloatParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testFloatNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1f);
        numberedValuesList.put("second", 2f);
        numberedValuesList.put("third", 3f);
        Optional<Float> threeValue = ParameterUtils
                .getFloatParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(3f, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getFloatParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker),
                "Expected getFloatParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }


    @Test
    void testLongValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", 1L);
        valuesList.put("second", 2L);
        valuesList.put("third", 3L);
        Optional<Long> threeValue =
                ParameterUtils.getLongParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(3L, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getLongParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getLongParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testLongNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1L);
        numberedValuesList.put("second", 2L);
        numberedValuesList.put("third", 3L);
        Optional<Long> threeValue = ParameterUtils
                .getLongParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(3L, threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getLongParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getLongParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testStringArrayValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", PStringsWrapper.builder().valuesList(List.of("a1", "a2", "a3")).build());
        valuesList.put("second", PStringsWrapper.builder().valuesList(List.of("b4", "b5", "b6")).build());
        valuesList.put("third", PStringsWrapper.builder().valuesList(List.of("c7", "c8", "c9")).build());
        Optional<PStringsWrapper> threeValue =
                ParameterUtils.getStringArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(PStringsWrapper.builder().valuesList(List.of("c7", "c8", "c9")).build(), threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getStringArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getStringArrayParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testStringArrayNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", PStringsWrapper.builder().valuesList(List.of("a1", "a2", "a3")).build());
        numberedValuesList.put("second", PStringsWrapper.builder().valuesList(List.of("b4", "b5", "b6")).build());
        numberedValuesList.put("third", PStringsWrapper.builder().valuesList(List.of("c7", "c8", "c9")).build());
        Optional<PStringsWrapper> threeValue = ParameterUtils
                .getStringArrayParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(PStringsWrapper.builder().valuesList(List.of("c7", "c8", "c9")).build(), threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getStringArrayParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getStringArrayParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testIntArrayValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", PIntsWrapper.builder().valuesList(List.of(1, 2, 3)).build());
        valuesList.put("second", PIntsWrapper.builder().valuesList(List.of(4, 5, 6)).build());
        valuesList.put("third", PIntsWrapper.builder().valuesList(List.of(7, 8, 9)).build());
        Optional<PIntsWrapper> threeValue =
                ParameterUtils.getIntArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(PIntsWrapper.builder().valuesList(List.of(7, 8, 9)).build(), threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getIntArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testDoubleArrayValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", PDoublesWrapper.builder().valuesList(List.of(1.0, 2.0, 3.0)).build());
        valuesList.put("second", PDoublesWrapper.builder().valuesList(List.of(4.0, 5.0, 6.0)).build());
        valuesList.put("third", PDoublesWrapper.builder().valuesList(List.of(7.0, 8.0, 9.0)).build());
        Optional<PDoublesWrapper> threeValue =
                ParameterUtils.getDoubleArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals(PDoublesWrapper.builder().valuesList(List.of(7.0, 8.0, 9.0)).build(), threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getIntArrayParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));


        threeValue =
                ParameterUtils.getDoubleArrayParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "third", logMarker);
        assertTrue(threeValue.isEmpty());
    }

    @Test
    void testIntArrayNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", PIntsWrapper.builder().valuesList(List.of(1, 2, 3)).build());
        numberedValuesList.put("second", PIntsWrapper.builder().valuesList(List.of(4, 5, 6)).build());
        numberedValuesList.put("third", PIntsWrapper.builder().valuesList(List.of(7, 8, 9)).build());
        Optional<PIntsWrapper> threeValue = ParameterUtils
                .getIntArrayParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals(PIntsWrapper.builder().valuesList(List.of(7, 8, 9)).build(), threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getIntArrayParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testStringValuesList() {
        Map<String, Object> valuesList = new HashMap<>();
        valuesList.put("first", "one");
        valuesList.put("second", "two");
        valuesList.put("third", "three");
        Optional<String> threeValue =
                ParameterUtils.getStringParameter(getPParameterSet(valuesList, new HashMap<>()), "third", logMarker);
        assertEquals("three", threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                        .getStringParameter(getPParameterSet(valuesList, new HashMap<>()), "fourth", logMarker),
                "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"\""));
    }

    @Test
    void testStringNumberedValuesList() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", "one");
        numberedValuesList.put("second", "two");
        numberedValuesList.put("third", "three");
        Optional<String> threeValue = ParameterUtils
                .getStringParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "third", 1, logMarker);
        assertEquals("three", threeValue.get());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ParameterUtils
                .getStringParameter(getPParameterSet(new HashMap<>(), numberedValuesList), "fourth", 1, logMarker), "Expected getIntParameter() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains(
                "Requested parameter with name \"fourth\" is missing for detection site or sensor id \"DS1\". \"Number of lanes is 1\""));
    }

    @Test
    void testIntegerParameterForLaneNrIsEmptyAndParameterMissing() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1);
        numberedValuesList.put("second", 2);
        numberedValuesList.put("third", 3);
        // IMPORTANT: numberedValuesList is now switched to a normal ValueList, number of lanes is 2
        // "fourth" is also not found in the lists
        Optional<Integer> fourthValue = ParameterUtils
                .getIntParameter(getPParameterSet(numberedValuesList, new HashMap<>()), "fourth", 2, logMarker);
        // trigger case that parameterForLaneNr is Empty and the program should not throw an exception, but log and return an empty optional
        assertTrue(fourthValue.isEmpty());
    }

    @Test
    void testIntegerParameterForLaneNrIsEmptyAndParameterFound() {
        Map<String, Object> numberedValuesList = new HashMap<>();
        numberedValuesList.put("first", 1);
        numberedValuesList.put("second", 2);
        numberedValuesList.put("third", 3);
        // IMPORTANT: numberedValuesList is now switched to a normal ValueList, number of lanes is 2
        // "third" is found in the lists
        Optional<Integer> thirdValue = ParameterUtils
                .getIntParameter(getPParameterSet(numberedValuesList, new HashMap<>()), "third", 2, logMarker);
        // trigger case that parameterForLaneNr is Empty and return the found value of 3
        assertEquals(3, thirdValue.get());
    }

    @Test
    void testEmptyLists() {
        Optional<Integer> emptyInteger = ParameterUtils
                .getIntParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyInteger.isEmpty());
        emptyInteger = ParameterUtils
                .getIntParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyInteger.isEmpty());

        Optional<Boolean> emptyBoolean = ParameterUtils
                .getBooleanParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyBoolean.isEmpty());
        emptyBoolean = ParameterUtils
                .getBooleanParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyBoolean.isEmpty());

        Optional<String> emptyString = ParameterUtils
                .getStringParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyString.isEmpty());
        emptyString = ParameterUtils
                .getStringParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyString.isEmpty());

        Optional<Float> emptyFloat = ParameterUtils
                .getFloatParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyFloat.isEmpty());
        emptyFloat = ParameterUtils
                .getFloatParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyFloat.isEmpty());

        Optional<Long> emptyLong = ParameterUtils
                .getLongParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyLong.isEmpty());
        emptyLong = ParameterUtils
                .getLongParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyLong.isEmpty());

        Optional<Double> emptyDouble = ParameterUtils
                .getDoubleParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyDouble.isEmpty());
        emptyDouble = ParameterUtils
                .getDoubleParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyDouble.isEmpty());

        Optional<PIntsWrapper> emptyPIntsWrapper = ParameterUtils
                .getIntArrayParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", 1, logMarker);
        assertTrue(emptyPIntsWrapper.isEmpty());
        emptyPIntsWrapper = ParameterUtils
                .getIntArrayParameter(getPParameterSet(new HashMap<>(), new HashMap<>()), "emptyLists", logMarker);
        assertTrue(emptyPIntsWrapper.isEmpty());
    }

    /*
    String in map is parameter id and Integer is the corresponding value in the values list
     */

    /**
     * Get PParameterSet with values and numbered values list filled. Numbered values list has lane number 0.
     *
     * @param valuesList         PParameterSet initial valuesList
     * @param numberedValuesList PParameterSet initial parameter numbered values list for lane number 0
     * @return PParameterSet for numbered and values list
     */
    private PParameterSet getPParameterSet(Map<String, Object> valuesList, Map<String, Object> numberedValuesList) {
        List<PParameterValue> paramValues = getListPParameterValue(valuesList);
        List<PParameterValue> paramNumberedValues = getListPParameterValue(numberedValuesList);
        return PParameterSet.builder().target(PParametrizationTarget.builder().itemId("DS1").build())
                .numberedValuesList(
                        List.of(PNumberedParameterValues.builder().number(0).valuesList(paramNumberedValues).build()))
                .valuesList(paramValues).build();
    }

    private List<PParameterValue> getListPParameterValue(Map<String, Object> list) {
        List<PParameterValue> paramValues = new LinkedList<>();
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            String parameterId = entry.getKey();
            Object parameterValue = entry.getValue();
            PParameterValue p =
                    PParameterValue.builder().parameterId(parameterId).value(getObjectValue(parameterValue)).build();
            paramValues.add(p);
        }
        return paramValues;
    }

    private PValueWrapper getObjectValue(Object parameterValue) {
        if (parameterValue instanceof Integer) {
            return PValueWrapper.builder().intVal((Integer) parameterValue).build();
        } else if (parameterValue instanceof Boolean) {
            return PValueWrapper.builder().booleanVal((Boolean) parameterValue).build();
        } else if (parameterValue instanceof Double) {
            return PValueWrapper.builder().doubleVal((Double) parameterValue).build();
        } else if (parameterValue instanceof Float) {
            return PValueWrapper.builder().doubleVal(((Float) parameterValue).doubleValue()).build();
        } else if (parameterValue instanceof Long) {
            return PValueWrapper.builder().longVal(((Long) parameterValue).longValue()).build();
        } else if (parameterValue instanceof String) {
            return PValueWrapper.builder().stringVal((String) parameterValue).build();
        } else if (parameterValue instanceof PIntsWrapper) {
            return PValueWrapper.builder().intVals((PIntsWrapper) parameterValue).build();
        } else if (parameterValue instanceof PDoublesWrapper) {
            return PValueWrapper.builder().doubleVals((PDoublesWrapper) parameterValue).build();
        } else if (parameterValue instanceof PStringsWrapper) {
            return PValueWrapper.builder().stringVals((PStringsWrapper) parameterValue).build();
        }
        return null;
    }
}
