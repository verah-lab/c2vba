package de.heuboe.asfinag.vmis2.infrastructure.types;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObject;
import eu.vmis_ehe.vmis2.geomanager.features.pojo.PPointRoadReference;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Basic class for InfrastructureObjects.
 *
 * @author davidh
 */
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class InfrastructureObjectBase implements InfrastructureObject {
    // depth of recursive iteration cycles to print references,
    // -1 for printing every reference no matter which depth
    // 0 for printing no references
    // 1 for printing only references on first iteration step and so on
    // prevents from endless loop
    @Setter
    private static int maxIterationDepthWithReferences = -1;
    /**
     * Use DefaultPrettyPrinter to generate json of attached data value. Otherwise .toString() is used.
     */
    @Setter
    private static boolean usePrettyPrinter = true;

    private String id;
    private String name;
    private String shortName;
    private String version;
    private GeoReference geoReference;
    private String type;
    private Map<String, List<InfrastructureObject>> references = new HashMap<>();
    @JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
    private Map<String, Object> attachedData = new HashMap<>();


    /**
     * Get the id.
     *
     * @return the id.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Get the name.
     *
     * @return the name.
     */
    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Get the short name.
     *
     * @return the short name as Optional.
     */
    @Override
    public Optional<String> getShortName() {
        return Optional.ofNullable(shortName);
    }

    /**
     * Get the version.
     *
     * @return the version as Optional.
     */
    @Override
    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    /**
     * Get the geo reference.
     *
     * @return the geo reference as Optional.
     */
    @Override
    public Optional<GeoReference> getGeoReference() {
        return Optional.ofNullable(geoReference);
    }


    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Get reference list by given type.
     *
     * @param refType infrastructure reference type
     * @return the list of related InfrastructureObjects.
     */
    @Override
    public List<InfrastructureObject> getReferences(String refType) {
        return references == null || references.get(refType) == null ?
                Collections.emptyList() :
                references.get(refType);
    }

    @Override
    public Map<String, List<InfrastructureObject>> getReferences() {
        return references == null ? Collections.emptyMap() : references;
    }

    @Override
    public <T> Optional<T> getAttachedData(Class<T> clazz) {
        if (attachedData != null && attachedData.containsKey(clazz.getName())) {
            Object obj = attachedData.get(clazz.getName());
            if (obj.getClass().equals(clazz)) {
                return Optional.ofNullable(clazz.cast(obj));
            } else {
                Class<? extends Object> classOne = obj.getClass();
                Class<? extends Object> classTwo = clazz;

                log.warn("different classes: {} vs. {}.", classOne.getName(), classTwo.getName());
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateReferences(String refType, List<InfrastructureObject> referencesToSet) {
        if (refType != null) {
            if (references == null) {
                references = new HashMap<>();
            }
            references.put(refType, referencesToSet);
        }
    }

    @Override
    public Map<String, Object> getAttachedData() {
        return attachedData;
    }

    @Override
    public void updateAttachedData(Object obj) {
        if (attachedData != null) {
            attachedData.put(obj.getClass().getName(), obj);
        }

    }

    @Override
    public void removeAttachedData(Object obj) {
        String className = obj.getClass().getName();
        if (attachedData != null && attachedData.containsKey(className)) {
            attachedData.remove(obj.getClass().getName());
        }
    }

    @Override
    public void setAttachedDataMap(Map<String, Object> attachedDataMap) {
        attachedData = attachedDataMap;
    }

    @Override
    public PInfrastructureObject getInfrastructureObject() {
        return getInfrastructureObject(this);
    }


    /**
     * Get the Pojo of the infrastructure object.
     *
     * @param infraObj infrastructur object to convert to pojo
     * @return the pojo of an InfrastructureObject
     */
    public PInfrastructureObject getInfrastructureObject(InfrastructureObject infraObj) {
        return getInfrastructureObject(infraObj, 0);
    }


    /**
     * Get the Pojo of the infrastructure object.
     *
     * @param infraObj infrastructur object to convert to pojo
     * @param depth counter for reference depth to prevent endless loop
     * @return the pojo of an InfrastructureObject
     */
    public PInfrastructureObject getInfrastructureObject(InfrastructureObject infraObj, int depth) {
        List<PInfrastructureObject.AttachedData> attachedDataList = new ArrayList<>();
        Iterator it = infraObj.getAttachedData().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry item = (Map.Entry) it.next();

            if (usePrettyPrinter) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);

                mapper.configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), false);
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                try {
                    // use getValue().toString() to handle Protos like PointRoadReference
                    attachedDataList.add(PInfrastructureObject.AttachedData.builder().className(item.getKey().toString()).value(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item.getValue())).build());
                } catch (JsonProcessingException e) {
                    log.info(item.getKey().toString() + item.getValue().toString());
                    throw new IllegalArgumentException(e);
                }
            } else {
                // replace "=" for more readability in kafka (prevent encoding chars)
                attachedDataList.add(PInfrastructureObject.AttachedData.builder().className(item.getKey().toString()).value(item.getValue().toString().replace("=", ": ")).build());
            }
        }
        PInfrastructureObject.LineReference lineRef = PInfrastructureObject.LineReference.builder().build();

        if (infraObj.getGeoReference().isPresent()) {
            GeoReference geoRef= infraObj.getGeoReference().get();//NOSONAR
            lineRef = PInfrastructureObject.LineReference.builder()
                    .roadId(geoRef.getRoadId())
                    .kmFrom(geoRef.getKmFrom())
                    .kmTo(geoRef.getKmTo())
                    .mapVersion(geoRef.getMapVersion())
                    .pointRoadReference(geoRef.getPointRoadReference().isPresent() ? PPointRoadReference.from(geoRef.getPointRoadReference().get()) : null)//NOSONAR
                    .validitySection(geoRef.getValiditySection())
                    .build();
        }

        // References selbst aufrufend.
        List<PInfrastructureObject> referencesPInfraObject = new ArrayList<>();
        if (maxIterationDepthWithReferences==-1 || depth < maxIterationDepthWithReferences) {
            infraObj.getReferences().values().forEach(referencesList ->
                    referencesList.forEach(reference -> referencesPInfraObject.add(getInfrastructureObject(reference, depth+1))));
        }

        return PInfrastructureObject.builder()
                .id(infraObj.getId())
                .type(infraObj.getType())
                .attachedDataList(attachedDataList)
                .name(infraObj.getName().orElse(""))
                .shortName(infraObj.getShortName().orElse(""))
                .version(infraObj.getVersion().orElse(""))
                .referencesList(referencesPInfraObject)
                .lineReference(lineRef).build();
    }
}
