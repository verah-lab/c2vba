package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This class represents a copy statement that directly returns the input object as result if the defined target object
 * matches the source object.
 */
@Slf4j
@AllArgsConstructor
public class CopyStatement implements Filler {

    private final String target;
    private final ObjectDirection objectDirection;
    private final String targetTopic;

    private final SequencerUtils utils = new SequencerUtils();

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        int adv = 0;
        GenericProtoObject object = (GenericProtoObject) inputData;

        // only copy content if defined target and object class name is equal
        if (object.getClassName().equals(target)) {
            Map<String, Object> metaData = ((GenericProtoObject) inputData).getMetaData();
            // add direction to meta data
            metaData.put(ObjectDirection.class.getSimpleName(), this.objectDirection);
            // add target topic to meta data
            metaData.put(SequencerUtils.TOPIC_TARGET_KEY, targetTopic);

            // if the tlsTime autofill property is set and the tlsTime of the source object is not set to a realistic value
            if (metaData.getOrDefault(ObjectProperty.AUTO_FILL_TLS_TIME.name(), "").equals("true")
                    && (object.getTimestampValue(SequencerUtils.TLS_TIME).getSeconds() == 0)
                    && (object.getTimestampValue(SequencerUtils.TLS_TIME).getNanos() == 0)) {
                // add the current time as tlsTime
                utils.setCurrentTime(object, SequencerUtils.TLS_TIME);
            }

            // write object to result
            utils.addOrUpdateResult(result, (GenericProtoObject) inputData);
        } else {
            log.warn("Object copy could not be completed. Defined target object '{}' does not match received " +
                    "source object '{}'!", target, object.getClassName());
        }
        return adv;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getOperandName() {
        return "copy";
    }
}
