package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class presents a message that can be sent with flop execution.
 */
@Slf4j
public class Message implements Filler {

    @Getter
    private MessageType type;

    @Getter @Setter
    private String formatString;

    @Getter @Setter
    private List<Variable> parameter = new ArrayList<>();

    private final SequencerBeanContainer sequencerBeanContainer;

    /**
     * Constructs a {@link Message} with a {@link SequencerBeanContainer}.
     *
     * @param type                   The message type that should be a string represented by {@link MessageType} enum.
     * @param formatString           The message string that can contain placeholders in form of curly brackets.
     * @param parameter              A list of {@link Variable }parameters that will be used for the placeholders in the
     *                               formatString.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains necessary Beans for execution.
     */
    public Message(String type, String formatString, List<Variable> parameter,
            SequencerBeanContainer sequencerBeanContainer) {
        this.type = MessageType.findByKeyWord(type);
        this.formatString = formatString;
        this.parameter = parameter;
        this.sequencerBeanContainer = sequencerBeanContainer;

    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {

        // prepare format parameter
        Object[] args = new Object[parameter.size()];
        AtomicInteger counter = new AtomicInteger(0);
        try {
            parameter.forEach(param -> {
                Value val = param.eval(inputData, variableTable);
                if (val != null) {
                    args[counter.getAndIncrement()] = val.getStringValue();
                } else {
                    log.warn("Printing message '{}' failed because the requested parameter {} does not contain a " +
                            "value!", formatString, param.getName());
                }
            });

            int placeHolderCount = StringUtils.countMatches(formatString, "{}");
            if (placeHolderCount != args.length) {
                log.error(
                        "In message '{}' the count of placeholder ({}) does not match the count of variables ({})! Message will not be " +
                                "send!", formatString, placeHolderCount, args.length);
            } else {

                // replace script placeholder with java format string placeholder
                formatString = StringUtils.replace(formatString, "{}", "%s");

                // format message string with parameter
                String res = String.format(formatString, args);
                if (type == MessageType.ERROR) {
                    sequencerBeanContainer.getSequencerMessageManagement()
                            .sendMessage(res, ((GenericProtoObject) inputData).getStringValue("id"));
                    log.error(res);
                } else if (type == MessageType.SYSTEM) {
                    log.info(res);
                }

            }
        } catch (IllegalStateException e) {
            log.error("Formatting the message '{}' fails with the following error: '{}'! Message will not be send!"
                    , formatString, e.getMessage());
        }
        return 0;
    }

    public void setType(String type) {
        this.type = MessageType.findByKeyWord(type);
    }

    /**
     * Add a new Variable as parameter to the list.
     *
     * @param parameter The Variable that should be added as parameter to the list.
     */
    public void addParameter(Variable parameter) {
        this.parameter.add(parameter);
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getOperandName() {
        return "message";
    }

}
