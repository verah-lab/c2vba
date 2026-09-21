package de.heuboe.asfinag.control.base.actors;

import eu.vmis_ehe.vmis2.paramservice.pojo.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * This class reduces the memory usage of a parameters object ({@link AbstractParameterActor.Parameters}). For this,
 * an internal cache is used to store data objects that should be reused. If a parameters object should be reduced
 * then a copy is created. All data objects contained in the parameters object that already exist in the cache
 * are replaced by their cached representations. If the cache does not contain a data object then it is added to the
 * cache. The following object types are cached: {@link String}, {@link PParametrizationTarget},
 * {@link PParameterValue}, {@link PNumberedParameterValues}, {@link PValueWrapper}.
 */
public class ParametersMemoryReducer implements UnaryOperator<AbstractParameterActor.Parameters<PParameterSetList>> {

    /**
     * Internal cache.
     */
    private Map<Class<?>, Map<Object, Object>> objectCache = getEmptyCache();

    /**
     * Reduces the given parameters object.
     *
     * @param parameters Parameters object to be reduced.
     * @return Reduced parameters object.
     */
    @Override
    public AbstractParameterActor.Parameters<PParameterSetList> apply(
            AbstractParameterActor.Parameters<PParameterSetList> parameters) {
        return new AbstractParameterActor.Parameters<>(
                cacheIfAbsent(parameters.key()),
                cacheIfAbsent(parameters.roadId()),
                cacheIfAbsent(parameters.algo()),
                cacheIfAbsent(parameters.instance()),
                cacheIfAbsent(parameters.system()),
                reduce(parameters.parameters())
        );
    }

    /**
     * Returns the number of cached objects per class.
     *
     * @return The number of cached objects per class.
     */
    public Map<Class<?>, Integer> numberOfCachedObjectsPerClass() {
        return Map.of(
                String.class, objectCache.get(String.class).size(),
                PParametrizationTarget.class, objectCache.get(PParametrizationTarget.class).size(),
                PParameterValue.class, objectCache.get(PParameterValue.class).size(),
                PNumberedParameterValues.class, objectCache.get(PNumberedParameterValues.class).size(),
                PValueWrapper.class, objectCache.get(PValueWrapper.class).size()
        );
    }

    /**
     * Clears the internally used cache.
     */
    public void clearInternalCache() {
        objectCache = getEmptyCache();
    }

    /**
     * Returns an empty cache.
     *
     * @return An empty cache.
     */
    private Map<Class<?>, Map<Object, Object>> getEmptyCache() {
        return Map.of(
                String.class, new ConcurrentHashMap<>(),
                PParametrizationTarget.class, new ConcurrentHashMap<>(),
                PParameterValue.class, new ConcurrentHashMap<>(),
                PNumberedParameterValues.class, new ConcurrentHashMap<>(),
                PValueWrapper.class, new ConcurrentHashMap<>()
        );
    }

    /**
     * Reduces the given parameter set list object.
     *
     * @param parameterSetList Parameter set list object to be reduced.
     * @return Reduced parameter set list object.
     */
    private PParameterSetList reduce(PParameterSetList parameterSetList) {
        return PParameterSetList.builder()
                .iid(cacheIfAbsent(parameterSetList.getIid()))
                .instanceId(cacheIfAbsent(parameterSetList.getInstanceId()))
                .roadId(cacheIfAbsent(parameterSetList.getRoadId()))
                .definitionSetId(cacheIfAbsent(parameterSetList.getDefinitionSetId()))
                .valuesList(reduce(parameterSetList.getValuesList()))
                .build();
    }

    /**
     * Reduces the given list of parameter set objects.
     *
     * @param parameterSets List of parameter set objects to be reduced.
     * @return Reduced list of parameter set objects.
     */
    private List<PParameterSet> reduce(List<PParameterSet> parameterSets) {
        return parameterSets.stream()
                .map(this::reduce)
                .toList();
    }

    /**
     * Reduces the given parameter set object.
     *
     * @param parameterSet Parameter set object to be reduced.
     * @return Reduced parameter set object.
     */
    private PParameterSet reduce(PParameterSet parameterSet) {
        return parameterSet.toBuilder()
                .previousIid(cacheIfAbsent(parameterSet.getPreviousIid()))
                .systemEnvironment(cacheIfAbsent(parameterSet.getSystemEnvironment()))
                .paramCaseId(cacheIfAbsent(parameterSet.getParamCaseId()))
                .userId(cacheIfAbsent(parameterSet.getUserId()))
                .comment(cacheIfAbsent(parameterSet.getComment()))
                .definitionSetId(cacheIfAbsent(parameterSet.getDefinitionSetId()))
                .iid(cacheIfAbsent(parameterSet.getIid()))
                .instanceId(cacheIfAbsent(parameterSet.getInstanceId()))
                .target(cacheIfAbsent(parameterSet.getTarget()))
                .numberedValuesList(
                        parameterSet.getNumberedValuesList().stream()
                                .map(this::cacheIfAbsent)
                                .toList()
                )
                .valuesList(
                        parameterSet.getValuesList().stream()
                                .map(this::cacheIfAbsent)
                                .toList()
                )
                .build();
    }

    /**
     * Reduces the given numbered parameter values object.
     *
     * @param numberedParameterValues Numbered parameter values to be reduced.
     * @return Reduced numbered parameter values object.
     */
    private PNumberedParameterValues reduce(PNumberedParameterValues numberedParameterValues) {
        return numberedParameterValues.toBuilder()
                .valuesList(
                        numberedParameterValues.getValuesList().stream()
                                .map(this::cacheIfAbsent)
                                .toList()
                )
                .build();
    }

    /**
     * Reduces the given parameter value object.
     *
     * @param parameterValue Parameter value object to be reduced.
     * @return Reduced parameter value object.
     */
    private PParameterValue reduce(PParameterValue parameterValue) {
        return PParameterValue.builder()
                .parameterId(cacheIfAbsent(parameterValue.getParameterId()))
                .value(cacheIfAbsent(parameterValue.getValue()))
                .build();
    }

    /**
     * If the specified object is not contained in the cache, enters it into the cache.
     *
     * @param stringObject Object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private String cacheIfAbsent(String stringObject) {
        return cacheIfAbsent(stringObject, String.class, UnaryOperator.identity());
    }

    /**
     * If the specified object is not contained in the cache, enters it into the cache.
     *
     * @param parametrizationTarget Object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private PParametrizationTarget cacheIfAbsent(PParametrizationTarget parametrizationTarget) {
        return cacheIfAbsent(parametrizationTarget, PParametrizationTarget.class, UnaryOperator.identity());
    }

    /**
     * If the specified object is not contained in the cache, attempts to reduce its value and enters it into the cache.
     *
     * @param parameterValue Object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private PParameterValue cacheIfAbsent(PParameterValue parameterValue) {
        return cacheIfAbsent(parameterValue, PParameterValue.class, this::reduce);
    }

    /**
     * If the specified object is not contained in the cache, attempts to reduce its value and enters it into the cache.
     *
     * @param numberedParameterValues Object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private PNumberedParameterValues cacheIfAbsent(PNumberedParameterValues numberedParameterValues) {
        return cacheIfAbsent(numberedParameterValues, PNumberedParameterValues.class, this::reduce);
    }

    /**
     * If the specified object is not contained in the cache, enters it into the cache.
     *
     * @param valueWrapper Object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private PValueWrapper cacheIfAbsent(PValueWrapper valueWrapper) {
        return cacheIfAbsent(valueWrapper, PValueWrapper.class, UnaryOperator.identity());
    }

    /**
     * If the specified object is not contained in the cache, attempts to reduce its value using the given
     * reducing function and enters it into the cache.
     *
     * @param object           Object to be cached.
     * @param objectClass      Class of the object.
     * @param reducingFunction Reducing function.
     * @param <T>              Type of the object to be cached.
     * @return The current (existing or reduced) cached representation of the given object.
     */
    private <T> T cacheIfAbsent(T object, Class<T> objectClass, UnaryOperator<T> reducingFunction) {
        if (Objects.isNull(object)) {
            return null;
        }

        return objectClass.cast(
                objectCache.get(objectClass).computeIfAbsent(object, o -> reducingFunction.apply(object)));
    }

}
