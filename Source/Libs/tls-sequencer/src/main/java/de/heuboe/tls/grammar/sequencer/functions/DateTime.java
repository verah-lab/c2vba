package de.heuboe.tls.grammar.sequencer.functions;

import com.google.protobuf.Timestamp;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * This function provide a date time object that can be used for assignment to a field of an object. It will create a
 * {@link de.heuboe.tls.grammar.base.ValueCollection.TimestampValue} object. Depending on the parameter usage the date
 * time object will differ. If no parameter is passed to the function, the current date time will be created. If a
 * parameter is passed it will be parsed to an {@link Instant}. Therefor the parameter must match the following pattern:
 * <i>YYYY-MM-DDTHH:mm:ssZ</i>
 */
@Slf4j
public class DateTime extends Function {

    /**
     * This function provide a date time object that can be used for assignment to a field of an object. It will create
     * a {@link de.heuboe.tls.grammar.base.ValueCollection.TimestampValue} object. Depending on the parameter usage the
     * date time object will differ. If no parameter is passed to the function, the current date time will be created.
     * If a parameter is passed it will be parsed to an {@link Instant}. Therefor the parameter must match the following
     * pattern: <i>YYYY-MM-DDTHH:mm:ssZ</i>
     *
     * @param parameterCount         The amount of possible parameters for this function as {@link String}.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains the config service.
     */
    public DateTime(String parameterCount, SequencerBeanContainer sequencerBeanContainer) {
        super(parameterCount, sequencerBeanContainer);
    }

    /**
     * This is the main logic of the dateTime function. It will
     *
     * @param dataFromBroker The data from the broker.
     * @param variableTable  The map of variables in the current context.
     *
     * @return the {@link de.heuboe.tls.grammar.base.ValueCollection.TimestampValue} with the generated timestamp or
     * null if the timestamp or parsing process fails.
     */
    @Override
    public Value execute(Object dataFromBroker, Map<String, Variable> variableTable) {
        Value result = null;
        Instant time = Instant.now();
        String errMsg = "";

        // differ between parameter count
        if (getParameters().size() == 1) {
            try {
                time = Instant.parse(getParameters().getFirst().getValue().getStringValue());
            } catch (DateTimeParseException e) {
                errMsg = "The input string '" + getParameters().getFirst().getValue().getStringValue() +
                        "' could not be parsed to an Instant. Please use the format 'YYYY-MM-DDTHH:MM:SSZ'!";
            }
        }

        if (errMsg.isEmpty()) {
            result = new ValueCollection.TimestampValue(
                    Timestamp.newBuilder().setSeconds(time.getEpochSecond()).setNanos(time.getNano()).build());
        } else {
            getSequencerBeanContainer().getSequencerMessageManagement().sendMessage(errMsg);
            log.error(errMsg);
        }

        return result;
    }
}
