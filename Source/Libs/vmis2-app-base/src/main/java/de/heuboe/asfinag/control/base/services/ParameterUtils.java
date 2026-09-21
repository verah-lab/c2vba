package de.heuboe.asfinag.control.base.services;

import eu.vmis_ehe.vmis2.paramservice.pojo.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Utils to read parameter values from ParameterSet-structure.
 */
@Slf4j
public class ParameterUtils {

    private static final String LOG_NUMBER_OF_LANES = "Number of lanes is ";

    /**
     * Let the ParameterUtils.java produce an IllegalArgumentException.
     * If true, create an exception if a parameter is missing.
     * If false, return an Optional.empty() but throw no exception.
     */
    public static boolean allowExceptionThrow = true;

    private ParameterUtils() {
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the boolean value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Boolean> getBooleanParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getBooleanVal());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the boolean value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Boolean> getBooleanParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getBooleanVal());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the integer value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Integer> getIntParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getIntVal());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the long value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Long> getLongParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getLongVal());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the long value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Long> getLongParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getLongVal());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the int value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Integer> getIntParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getIntVal());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the string value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<String> getStringParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getStringVal());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the string value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<String> getStringParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getStringVal());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the string array value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<PStringsWrapper> getStringArrayParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getStringVals());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the string array value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<PStringsWrapper> getStringArrayParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getStringVals());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the double value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Double> getDoubleParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getDoubleVal());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the double value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Double> getDoubleParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getDoubleVal());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the float value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Float> getFloatParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getDoubleVal().floatValue());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the float value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<Float> getFloatParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getDoubleVal().floatValue());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the double array value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<PDoublesWrapper> getDoubleArrayParameter(PParameterSet currentParamValues, String paramName,
                                                              Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getDoubleVals());
    }

    /**
     * Search the parameter with name in the parameter set and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param logMarker log marker
     * @return the int array value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<PIntsWrapper> getIntArrayParameter(PParameterSet currentParamValues, String paramName,
            Marker logMarker) {
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), currentParamValues.getValuesList(), paramName,
                        false, "", logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getIntVals());
    }

    /**
     * Search the lane parameter with name in the parameter values list and return it.
     *
     * @param currentParamValues PParameterSet in which the list of parameter is
     * @param paramName name of the parameter to search in currentParamValues
     * @param numberOfLanes the number of the lane for numbered values parameter
     * @param logMarker log marker
     * @return the int array value of the PParameterSet with the specified parameter name
     * @throws IllegalArgumentException if parameter name isn't in the parameter set
     */
    public static Optional<PIntsWrapper> getIntArrayParameter(PParameterSet currentParamValues, String paramName,
            int numberOfLanes, Marker logMarker) {
        Tuple2<Boolean, List<PParameterValue>> parameterTup =
                getNumberedAndSystemwideValues(currentParamValues, numberOfLanes);
        Optional<PParameterValue> p =
                getParameter(currentParamValues.getTarget().getItemId(), parameterTup._2(), paramName,
                        parameterTup._1(), LOG_NUMBER_OF_LANES + numberOfLanes, logMarker);
        return p.map(pParameterValue -> pParameterValue.getValue().getIntVals());
    }

    // also works now although there are no numbered values in list
    private static Tuple2<Boolean, List<PParameterValue>> getNumberedAndSystemwideValues(
            PParameterSet currentParamValues, int numberOfLanes) {
        List<PParameterValue> parameterForLaneNr =
                getParameterForLaneNr(currentParamValues.getNumberedValuesList(), numberOfLanes);

        // add for the active parameter and every system wide parameter
        List<PParameterValue> systemwideAndLaneNrParameter = new LinkedList<>(currentParamValues.getValuesList());
        systemwideAndLaneNrParameter.addAll(parameterForLaneNr);

        return Tuple.of(parameterForLaneNr.isEmpty(), systemwideAndLaneNrParameter);
    }

    /**
     * Get one specific parameter out of the given parameter.
     *
     * @param id detection site id
     * @param currentParamValues the complete parameter list (system-wide parameter, active parameter, numbered values parameter)
     * @param paramName specific parameter name to search in the complete parameter list
     * @param missingParameterWithoutException if true only warning is logged, else exception is thrown when parameter not found
     * @param logInfo additional log information string
     * @param logMarker logging marker
     * @return PParameter specific value of searched parameter if it is found else empty optional or exception
     */
    public static Optional<PParameterValue> getParameter(String id, List<PParameterValue> currentParamValues,
            String paramName, boolean missingParameterWithoutException, String logInfo, Marker logMarker) {
        if (currentParamValues.isEmpty()) {
            log.warn(logMarker, "No '{}' parameter for this detection site or sensor with id '{}' found. {}", paramName,
                    id, logInfo);
            // no parameter for this MQ
            return Optional.empty();
        }

        for (PParameterValue p : currentParamValues) {
            if (p.getParameterId().equals(paramName)) {
                return Optional.of(p);
            }
        }

        if (missingParameterWithoutException) {
            // this case triggers only if e.g. getBooleanParameter(PParameterSet currentParamValues, String paramName, int numberOfLanes) is called
            // but this case triggers not if e.g. getBooleanParameter(PParameterSet currentParamValues, String paramName) is called
            // parameterForLaneNr is Empty
            log.warn(logMarker,
                    "No numbered parameter list exists for this number of lanes. {}. Missing '{}' parameter for detection site or sensor with id {}.",
                    logInfo, paramName, id);
            // no numbered parameter exists
            // case that lane number does not match any numbered parameter lane number
            return Optional.empty();
        }
        if (allowExceptionThrow) {
            // missing parameter
            log.error(logMarker, "Missing '{}' parameter for detection site or sensor with id '{}'. {}", paramName, id,
                    logInfo);
            throw new IllegalArgumentException(logMarker.getName() + " Requested parameter with name \"" + paramName
                    + "\" is missing for detection site or sensor id \"" + id + "\". \"" + logInfo + "\"");
        }
        return Optional.empty();
    }

    private static List<PParameterValue> getParameterForLaneNr(List<PNumberedParameterValues> numberedParameterValues,
            int nrOfLanes) {
        for (PNumberedParameterValues p : numberedParameterValues) {
            if ((p.getNumber() + 1) == nrOfLanes) {
                // number=0: 1 FS
                // number=1: 2 FS...
                return p.getValuesList();
            }
        }
        return Collections.emptyList();
    }
}
