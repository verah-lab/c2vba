package de.heuboe.tls.grammar.sequencer.flops;

import de.heuboe.tls.grammar.base.ObjectResult;
import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.services.SequencerSendingService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * This class presents a flop object and its properties.
 */
@Slf4j
public class Flop implements Filler {

    @Getter
    @Setter
    private String key;
    @Getter
    private final FlopType type;
    @Getter
    private final boolean retriggerable;
    @Getter
    private final FlopParameter parameter;
    @Setter
    private SequencerSendingService sequencerSendingService;

    @Getter
    private List<Filler> actions;

    private Timer timer;

    @Getter
    private boolean destroyable = false;

    @Getter
    private boolean running = false;
    private long latestExecutionTime = -1L;

    private int ptr;
    private Object inputData;
    private Map<String, Variable> variableTable;

    /**
     * Constructs a flop object.
     *
     * @param type          The FlopType of the new flop.
     * @param retriggerable Flag that indicates if a flop can be retriggered before its task was executed.
     * @param parameter     The FlopParameter that holds the script configured timeouts.
     * @param actions       A list of actions that should be executed when the trigger task is executed.
     */
    public Flop(String type, boolean retriggerable, FlopParameter parameter,
            List<Filler> actions) {
        this.type = FlopType.findByKeyWord(type);
        this.retriggerable = retriggerable;
        this.parameter = parameter;
        this.actions = actions;
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        // the result variable will not be used in this context because every flop must have its one ObjectResult
        // update meta data when flop is executed
        this.ptr = ptr;
        this.inputData = inputData;
        // do a hard copy of the variableTable map to decouple from object reference
        this.variableTable = new HashMap<>(variableTable);

        // if the trigger already runs ...
        if (running && retriggerable) {
            retrigger();
        } else {
            trigger();
        }
        return ptr;
    }

    /**
     * This method will trigger the timer for this flop.
     */
    private void trigger() {
        // set meta data at timer start
        running = true;
        long startingTime = System.currentTimeMillis();
        latestExecutionTime = startingTime + (parameter.getMaxTimeout());

        // start timer with timeout parameter
        startTimer(parameter.getTimeout());

        log.debug("Flop for Id '{}' triggered.", key);
    }

    /**
     * This method will retrigger the timer of this flop. Therefor the old timer will be deleted and the new timer will
     * be set. The new timeout for this timer depends on the max timeout parameter. Retriggering will never time the
     * timer after the starting time plus the max timeout.
     */
    private void retrigger() {
        // cancel current timer
        timer.cancel();

        // calculate max timer delay depending on timer start and maxTimeout
        long currentTime = System.currentTimeMillis();
        long delay = parameter.getTimeout();

        // if the max timer is exceeded the new delay will be recalculated to respect the max timeout.
        if ((currentTime + delay) > latestExecutionTime) {
            delay = latestExecutionTime - currentTime;
        }

        // start timer with new delay
        startTimer(delay);
        log.debug("Flop for Id '{}' retriggered.", key);
    }

    /**
     * This method starts the timer with the defined delay in milliseconds.
     *
     * @param delay The timer delay in milliseconds.
     */
    private void startTimer(long delay) {
        log.debug("Starting timer with {} ms delay.", delay);
        timer = new Timer(key);
        timer.schedule(createTask(), delay);
    }

    /**
     * This method creates the task for the timer.
     *
     * @return A {@link TimerTask} that contains the execution logic.
     */
    private TimerTask createTask() {
        return new TimerTask() {
            @Override
            public void run() {
                executeFlop();
            }
        };
    }

    /**
     * This method holds the main logic for the execution task of the current {@link Flop}.
     */
    private synchronized void executeFlop() {
        log.debug("Executing task of flop '{}' ...", key);
        ObjectResult result = new ObjectResult();

        // run through all defined actions of the current flop
        for (Filler action : actions) {
            // execute assignment statement and collect new objects in result
            action.execute(result, ptr, inputData, variableTable);
        }

        if (result.getResult() != null) {
            // collect objects that should be sent from result
            Set<GenericProtoObject> antlrResult = (Set<GenericProtoObject>) (result).getResult();

            if (!antlrResult.isEmpty()) {
                // send objects to broker
                antlrResult.forEach(gpo -> sequencerSendingService.sendMessage(gpo));
            }
        }

        running = false;

        // mark flop for destruction to keep flop storage clean
        destroyable = true;

        log.debug("Task of flop for Id '{}' executed.", key);
    }

    /**
     * This method will update the execution block of the current {@link Flop}.
     *
     * @param flop The {@link Flop} that holds the new execution logic for the current {@link Flop}.
     */
    public void updateExecutionBlock(Flop flop) {
        this.actions = flop.getActions();
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getOperandName() {
        return "Flop";
    }
}
