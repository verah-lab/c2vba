package de.heuboe.tls.grammar.sequencer.flops;

import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Map;

import static de.heuboe.tls.sequencer.utils.SequencerUtils.VARIABLE_CLUSTER_ID;
import static de.heuboe.tls.sequencer.utils.SequencerUtils.VARIABLE_NODE_ID;

/**
 * This class will handle the {@link Flop} management and execution for the sequencer.
 */
@Slf4j
public class FlopStatement implements Filler {

    private Flop flop;
    private SequencerBeanContainer sequencerBeanContainer;

    /**
     * The constructor that prepare the {@link Flop} execution for the passed {@link Flop}.
     *
     * @param flop                   The main flop definition for the current statement.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains necessary Beans for execution.
     */
    public FlopStatement(Flop flop, SequencerBeanContainer sequencerBeanContainer) {
        if (sequencerBeanContainer != null) {
            flop.setSequencerSendingService(sequencerBeanContainer.getSequencerSendingService());
        }
        this.flop = flop;
        this.sequencerBeanContainer = sequencerBeanContainer;
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        // set flop key depending on flop type
        GenericProtoObject gpo = (GenericProtoObject) inputData;
        switch (flop.getType()) {
            case MONO:
                // add flop type and trigger timer to the key
                StringBuilder key = new StringBuilder(
                        String.format("%s_%s", flop.getType().toString(), flop.getParameter().getTimeout()));

                // if the flop is retriggerable
                if (flop.isRetriggerable()) {
                    // add a buzz word and the max timeout
                    key.append(String.format("_RETRIGGERABLE_%s", flop.getParameter().getMaxTimeout()));
                }

                // add the class name
                key.append(String.format("_%s", gpo.getClassName()));

                // add the assembled key in upper case
                flop.setKey(key.toString().toUpperCase());
                break;
            case EA:
                flop.setKey(gpo.getStringValue("id"));
                break;
            case NODE:
                flop.setKey(variableTable.get(VARIABLE_NODE_ID).getValue().getStringValue());
                break;
            case CLUSTER:
                flop.setKey(variableTable.get(VARIABLE_CLUSTER_ID).getValue().getStringValue());
                break;
            default:
                log.error("Key generation for flop type '{}' not implemented! Skip flop handling.", flop.getType().name());
                return -1;
        }

        // check if key was set correctly
        if (ObjectUtils.isEmpty(flop.getKey())) {
            log.error("Setting key for flop type '{}' failed. Skip flop handling.", flop.getType().name());
        } else {

            // check if flop exists in storage
            if (!sequencerBeanContainer.getFlopStorage().exists(flop)) {
                // add flop to storage
                sequencerBeanContainer.getFlopStorage().addOrUpdateFlop(flop);
            } else if (flop.isRetriggerable() && flop.isRunning()) {
                // load flop from storage and update action and error message
                Flop storedFlop = sequencerBeanContainer.getFlopStorage().getFlop(flop.getKey());
                storedFlop.updateExecutionBlock(flop);
                flop = storedFlop;
            } else if (!flop.isRetriggerable() && flop.isRunning()) {
                log.error("Error updating flop '{}'. This flop is not retriggerable. Please check the sequencer " +
                                "script '{}' to avoid unreachable statements.",
                        flop.getKey(), variableTable.get("CURRENT_SCRIPT_NAME").getValue().getStringValue());
                return 0;
            }
            flop.execute(result, ptr, inputData, variableTable);
            return 0;
        }
        return -1;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getOperandName() {
        return "FlopStatement";
    }

}
