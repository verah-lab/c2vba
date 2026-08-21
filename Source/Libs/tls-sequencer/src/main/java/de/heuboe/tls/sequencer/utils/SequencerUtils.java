package de.heuboe.tls.sequencer.utils;

import com.google.protobuf.Timestamp;
import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.base.ObjectResult;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.grammar.sequencer.ObjectDirection;
import de.heuboe.tls.grammar.sequencer.ObjectProperty;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.parser.proto.model.DataField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.*;

/**
 * This class provides several utility methods for the sequencer.
 */
@Configuration
@Slf4j
public class SequencerUtils {

    public static final String TOPIC_TARGET_KEY = "targetTopic";
    public static final String SCRIPT_BLOCK_NAME = "scriptBlockName";
    public static final String VARIABLE_DE_NUMBER = "DeNummer";
    public static final String VARIABLE_EAID = "Eaid";
    public static final String VARIABLE_NODE_ID = "NodeId";
    public static final String VARIABLE_CLUSTER_ID = "ClusterId";

    public static final String PROCESS_TIME = "processTime";
    public static final String TLS_TIME = "tlsTime";

    /**
     * Extracts the value of a {@link Headers} object that fits to the passed name string.
     *
     * @param headers The {@link Headers} from the received kafka message.
     * @param name    The name of the header as string the value is requested for.
     * @return the value of the header as string or an empty string if nothing was found.
     */
    public String extractHeader(Headers headers, String name) {
        try {
            return new String(headers.headers(name).iterator().next().value());
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * This method will set the field of an object with a value.
     * Integer values are currently of most interest.
     * Arrays of these types are currently needed.
     * If arrays of other types will become necessary, this method will be extended.
     *
     * @param object The object that contains the field.
     * @param field  The field that should be manipulated.
     * @param value  The value that should be set.
     * @return true if a value was updated else false.
     */
    public boolean setValue(GenericProtoObject object, DataField field, BasicVariable value) {
        if (value.getValue() == null) {
            log.warn("Data field '{}' could not be updated because the new value is null! The value '{}' will be " +
                    "left unchanged.", field.name(), object.get(field.name()));
            return false;
        }
        
        switch (field.dataType()) {
            case STRING:
                object.updateValue(field.name(), value.getValue().getStringValue());
                break;
            case INTEGER:
                handleInteger( object, field, value );
                break;
            case DOUBLE:
                object.updateValue(field.name(), value.getValue().getDoubleValue());
                break;
            case PROTO_OBJECT:
                if (value.getValue().getObjectValue() instanceof List) {
                    // List objects must be added value by value
                    ((Collection)value.getValue().getObjectValue())
                            .forEach(v -> object.addListValue(field.name(), v));
                } else {
                    object.updateValue(field.name(), value.getValue().getObjectValue());
                }
                break;
            default:
                log.warn("The data type '{}' of the field '{}' is currently not supported. Field will be " +
                        "ignored!", field.dataType(), field.name());
                return false;
        }
        return true;
    }
    
    /**
     * Handles the processing of integer values within a {@link GenericProtoObject}.
     * This method updates fields of the object based on whether the field is
     * marked as repeated or not. If there is a type mismatch between the field
     * and the value provided, a warning is logged and the value is ignored.
     *
     * @param object The {@link GenericProtoObject} whose field should be updated.
     * @param field  The {@link DataField} to be processed, which may be single or repeated.
     * @param value  The {@link BasicVariable} containing the value to be set in the object.
     */
    private void handleInteger( GenericProtoObject object, DataField field, BasicVariable value ) {
        Object objVal = value.getValue().getObjectValue();
        boolean mismatch = false;
        if( !field.repeated() ) {
            if( !(objVal instanceof List< ? >) ) {
                object.updateValue( field.name(), value.getValue().getIntValue() );
            } else {
                mismatch = true;
            }
        } else {
            if( objVal instanceof List< ? > ) {
                object.updateValue( field.name(), objVal );
            } else {
                mismatch = true;
            }
        }
        if( mismatch ) {
            log.warn( "Mismatch between simple field and array field! '{} = {}' will be ignored!",
                     field.name(), value.getName() );
        }
    }
    
    /**
     * This method will add or update {@link GenericProtoObject}s in the {@link Result} object used in the sequencer.
     *
     * @param result The {@link Result} object that holds created or modified result objects.
     * @param input  The {@link GenericProtoObject} that should be added or updated in the result object.
     */
    public void addOrUpdateResult(Result result, GenericProtoObject input) {

        // put every object into a set
        Set<GenericProtoObject> container = new HashSet<>();
        container.add(input);

        // write object to result
        if (((ObjectResult) result).getResult() == null) {
            result.setResult(container);
        } else {
            result.addToResult(container);
        }

        // finally update the process time because we modified an object
        updateProcessTime(input);
    }

    /**
     * This method create or update objects for each target id. The data will be used from the passed
     * {@link BasicVariable}.
     *
     * @param objName       The name of the current object.
     * @param varName       The name of the objects property as string.
     * @param objDirection  The direction of the object should be sent.
     * @param targetTopic   The kafka topic the object should be sent to. Can be null.
     * @param targetIds     A list of ids the object should exist for.
     * @param gpo           The source {@link GenericProtoObject} that will be used for creating new objects.
     * @param value         The {@link BasicVariable} that holds the value that should be added
     * @param result        The object for saving the final result.
     * @param variableTable A definition of variables.
     */
    public void handleObjectsForTargetIds(String objName, String varName, ObjectDirection objDirection, // NOSONAR it is as it is
            String targetTopic, Set<String> targetIds, GenericProtoObject gpo, BasicVariable value, Result result,
            Map<String, Variable> variableTable) {
        // run through every target id
        for (String targetId : targetIds) {
            if (StringUtils.isEmpty(targetId)) {
                log.warn("Target id could not be retrieved for  object '{}' with id '{}'. Object '{}' will " +
                                "not be send!",
                        gpo.getClassName(),
                        gpo.getStringValue("id"),
                        objName);
                continue;
            }

            // check if current object exists with current target id in result object
            if (((ObjectResult) result).getResult() == null) {
                // create a new object for this id and add it to the result
                addObject(objName, varName, objDirection, targetTopic, gpo, targetId, result, value, variableTable);
            } else {
                // check elements in result if one with the current target id and same object class is present
                Set<GenericProtoObject> resultSet = ((HashSet) ((ObjectResult) result).getResult());
                Optional<GenericProtoObject> resultObject = resultSet.stream()
                        .filter(obj -> obj.getStringValue("id").equals(targetId)
                                && obj.getClassName().equals(objName))
                        .findFirst();
                if (resultObject.isPresent()) {
                    // if an object with the same id is already present in the list update the new value
                    updateObject(resultObject.get(), value);
                } else {
                    // else create a new object for this id and add it to the result
                    addObject(objName, varName, objDirection, targetTopic, gpo, targetId, result, value, variableTable);
                }
            }
        }
    }

    /**
     * Create a new {@link GenericProtoObject} with the data from the input object and data set in the
     * {@link BasicVariable}.
     *
     * @param objName       The name of the current object.
     * @param varName       The name of the objects property as string.
     * @param objDirection  The direction of the object should be sent.
     * @param targetTopic   The kafka topic the object should be sent to. Can be null.
     * @param gpo           The {@link GenericProtoObject} that serves as source for the new object.
     * @param targetId      The id of the new object.
     * @param result        The {@link ObjectResult} the new created object will be written at.
     * @param value         The {@link BasicVariable} that contains data that should be set in the new object.
     * @param variableTable A definition of variables.
     */
    public void addObject(String objName, String varName, ObjectDirection objDirection, String targetTopic, // NOSONAR it is as it is
            GenericProtoObject gpo, String targetId, Result result, BasicVariable value,
            Map<String, Variable> variableTable) {
        // create a new object
        GenericProtoObject newObject = createNewObject(objName, objDirection, targetTopic, gpo);

        // fills the new created object with values
        fillObject(targetId, varName, value, newObject, variableTable);

        // write object to result
        addOrUpdateResult(result, newObject);
    }

    /**
     * Update the data field of the {@link GenericProtoObject} object with the data in the {@link BasicVariable}.
     *
     * @param gpo   The {@link GenericProtoObject} that should be updated.
     * @param value The {@link BasicVariable} that should be updated in the {@link GenericProtoObject}.
     */
    public void updateObject(GenericProtoObject gpo, BasicVariable value) {
        Optional<DataField> f = gpo.getFields().stream().filter(field -> field.name().equals(value.getName())).findFirst();
        f.ifPresent(dataField -> setValue(gpo, dataField, value));
    }

    /**
     * Create a new object on the base of the input object depending on the object definition of the assignment.
     *
     * @param objName      The name of the current object.
     * @param objDirection The direction of the object should be sent.
     * @param targetTopic  The kafka topic the object should be sent to. Can be null.
     * @param gpo          The object the new values should be set.
     * @return the new created {@link GenericProtoObject}.
     */
    public GenericProtoObject createNewObject(String objName, ObjectDirection objDirection, String targetTopic,
            GenericProtoObject gpo) {

        // create a copy of the meta data object to avoid ugly side effects
        Map<String, Object> metaData = new HashMap<>(gpo.getMetaData());

        // add direction to meta data
        metaData.put(ObjectDirection.class.getSimpleName(), objDirection);
        // add target topic to meta data
        metaData.put(SequencerUtils.TOPIC_TARGET_KEY, targetTopic);

        // create a new object and risk a ClassCastException for an uncaught exception
        return new GenericProtoObject(gpo.getClassPath() + "." + objName, metaData);
    }

    /**
     * This method will fill values into the necessary fields of the passed new object. The filled fields are always the
     * id field of the object and the varName of the passed {@link BasicVariable}.
     *
     * @param targetId      The target id of the object.
     * @param varName       The name of the objects property as string.
     * @param value         The value that contains the name and value of the field that should be set.
     * @param newObject     The object the new values should be set.
     * @param variableTable A definition of variables.
     */
    public void fillObject(String targetId, String varName, BasicVariable value, GenericProtoObject newObject,
            Map<String, Variable> variableTable) {
        // check fields of new created object and set new values
        for (DataField field : newObject.getFields()) {
            // id fields must be handled separately because they cannot be set directly in the script
            if (field.name().equalsIgnoreCase("id")) {
                newObject.updateValue(field.name(), targetId);
            } else if (field.name().equalsIgnoreCase("eaId")) {
                // update eaid (could be a relict and not be present any more in future proto files)
                newObject.updateValue(field.name(), variableTable.get("id").getValue().getStringValue());
            } else if (field.name().equalsIgnoreCase(PROCESS_TIME)) {
                // add processTime with current timestamp
                updateProcessTime(newObject);
            } else if (field.name().equalsIgnoreCase(TLS_TIME)) {
                // update tls time
                updateTlsTime(newObject, variableTable, value, varName);
            } else if (field.name().equals(varName)) {
                // fill object parameter with info from variableTable depending on field type
                setValue(newObject, field, value);
            }
        }
    }

    /**
     * This method will update the process time of the current object to the current time.
     *
     * @param gpo The {@link GenericProtoObject} the process time should be updated in.
     */
    private void updateProcessTime(GenericProtoObject gpo) {
        // add processTime with current timestamp
        setCurrentTime(gpo, PROCESS_TIME);
    }

    /**
     * This method will update the tls time of the current object to the current time.
     *
     * @param gpo           The {@link GenericProtoObject} the tls time should be updated in.
     * @param variableTable A definition of variables.
     * @param value         The value that contains the name and value of the field that should be set.
     * @param varName       The name of the objects property as string.
     */
    private void updateTlsTime(GenericProtoObject gpo, Map<String, Variable> variableTable, BasicVariable value,
                               String varName) {
        // add tlsTime with variable definition if present but only if we currently update the tlsTime variable via script
        if (varName != null
                && varName.equals(TLS_TIME)
                && value.getValue() != null
                && value.getValue().getType().equals(Value.ValueType.TIMESTAMP)) {
            gpo.updateValue(TLS_TIME, value.getValue().getTimestampValue());

        } else if (variableTable != null
                && variableTable.get(TLS_TIME).getValue().getTimestampValue().getSeconds() > 0) {
            // add the tlsTime from the source object if a realistic time exists
            gpo.updateValue(TLS_TIME, variableTable.get(TLS_TIME).getValue().getTimestampValue());

        } else if (gpo.getMetaData().getOrDefault(ObjectProperty.AUTO_FILL_TLS_TIME.name(), "").equals("true")) {
            // if configured via object property set the tls time to the current time
            log.info("For '{}' the 'tlsTime' was set to the current time because it is configured in the definition " +
                    "block property!", gpo.getClassName());
            setCurrentTime(gpo, TLS_TIME);
        } else {
            log.warn("No tlsTime will be set for the object '{}'!", gpo.getClassName());
        }
    }

    /**
     * Set the current time for the timeField in the {@link GenericProtoObject}.
     *
     * @param gpo       The {@link GenericProtoObject} the time field should be updated in.
     * @param timeField The name of the time field that should be updated.
     */
    public void setCurrentTime(GenericProtoObject gpo, String timeField) {
        Instant now = Instant.now();
        gpo.updateValue(timeField,
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build());
    }
}
