package de.heuboe.tls.grammar.sequencer;

import com.google.protobuf.Descriptors;
import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * This class represents an object and its property.
 */
@Slf4j
public class ObjectVariable extends BasicVariable {

    @Getter @Setter
    private String objectName;

    @Getter @Setter
    private ObjectStateType objectState;

    private final KafkaOperatorService kafkaOperatorService;

    /**
     * Constructs an {@link ObjectVariable}.
     *
     * @param objectName           The name of the object as string.
     * @param objectState          The state of the object that will be matched to an {@link ObjectStateType}.
     * @param name                 The name of the object property.
     * @param kafkaOperatorService The KafkaOperatorService bean to access the history.
     */
    public ObjectVariable(String objectName, String objectState, String name, KafkaOperatorService kafkaOperatorService) {
        super(name, null);
        this.objectName = objectName;
        this.objectState = ObjectStateType.findByKeyWord(objectState);
        this.kafkaOperatorService = kafkaOperatorService;
    }

    @Override
    public Value eval(Object dataFromBroker, Map<String, Variable> variableTable) {
        GenericProtoObject object = (GenericProtoObject) dataFromBroker;
        String objectId = variableTable.get("id").getValue().getStringValue();

        Value result = null;

        // get basic object if history data was requested
        if (objectState == ObjectStateType.OLD) {
            // load history objects from KafkaOperatorService
            List<GenericProtoObject> objectList = kafkaOperatorService.getLastFromHistory(this.objectName, objectId, false);

            // handle different content of history object list
            if (objectList == null || objectList.isEmpty()) {
                log.warn("History status '{}' was requested for object '{}' with id '{}' but no object could be " +
                        "found that is older than the current!", objectState, objectId, this.getObjectName());
                return null;
            }

            //we can only handle exact one object so always return the last one
            object = objectList.getLast();
        }

        Descriptors.FieldDescriptor.JavaType dataType = object.getFieldDescriptor(getName()).getJavaType();
        switch (dataType) {
            case INT, LONG, BOOLEAN, ENUM:
                result = new ValueCollection.IntValue(object.getIntegerValue(getName()));
                break;
            case DOUBLE, FLOAT:
                result = new ValueCollection.DoubleValue(object.getDoubleValue(getName()));
                break;
            case STRING:
                result = new ValueCollection.StringValue(object.getStringValue(getName()));
                break;
            default:
                log.error("Requested data type '{}' is currently not supported!", dataType);
        }

        return result;
    }
}
