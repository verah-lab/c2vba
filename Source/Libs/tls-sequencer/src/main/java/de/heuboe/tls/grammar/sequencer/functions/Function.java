package de.heuboe.tls.grammar.sequencer.functions;

import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The abstract class for functions.
 */
@Slf4j
public abstract class Function implements Expression {

    /* A list of parameters for the function. */
    @Getter
    private final List<Variable> parameters = new ArrayList<>();

    /* The initial count of parameter the function requires. */
    @Getter
    private final Set<Integer> parameterCount;

    /* The SequencerBeanContainer that hold several beans for usage inside of functions. */
    @Getter
    private final SequencerBeanContainer sequencerBeanContainer;

    /**
     * The constructor with the parameter count representation as string. The parameter can be a simple integer value if
     * the function will support exact the amount of parameter. If a range of parameter is necessary the different
     * amounts must be delimited by a pipe symbol. For example if a function should support 0, 2 and 4 parameter, the
     * parameterCount must be 0|2|4.
     *
     * @param parameterCount         The amount of parameters the function should support as string.
     * @param sequencerBeanContainer The SequencerBeanContainer that hold several beans for usage inside of functions.
     */
    protected Function(final String parameterCount, SequencerBeanContainer sequencerBeanContainer) {
        this.parameterCount = convertParameterCount(parameterCount);
        this.sequencerBeanContainer = sequencerBeanContainer;
    }

    /**
     * Converts the input string that represent an amount of parameter into a set of {@link Integer} values.
     *
     * @param parameterCount The amount of parameters as string that should be converted into a set of {@link Integer}.
     * @throws IllegalArgumentException if the parameter string could not be parsed to an {@link Integer}.
     */
    private Set<Integer> convertParameterCount(String parameterCount) {

        Set<Integer> result = new HashSet<>();

        try {
            if (parameterCount.isEmpty()) {
                result.add(0);
            } else if (parameterCount.contains("|")) {
                result.addAll(
                        Arrays.stream(parameterCount.split("\\|"))
                                .map(Integer::parseInt)
                                .collect(Collectors.toSet())
                );
            } else {
                result.add(Integer.parseInt(parameterCount));
            }
        } catch (NumberFormatException e) {
            log.error("This is an implementation error! The ANTLR grammar contains the illegal parameter definition " +
                    "'{}' for the function '{}'", parameterCount,
                    this.getClassName().getClass().getSimpleName());
            throw new IllegalArgumentException("Unable to parse parameter count: " + parameterCount);
        }
        return result;
    }

    /**
     * Compares the count of the parameter necessary for the function and passed by the script and return if it fits or
     * not.
     *
     * @param parameterCount The set of {@link Integer} that determine the possible parameter count for the function.
     * @param parameters     A list of {@link Variable}s that were passed as parameter to the function call.
     * @return true if the count of passed parameter fits to the count of necessary parameter, else false.
     */
    private boolean checkParameterCount(Set<Integer> parameterCount, List<Variable> parameters) {
        return parameterCount.contains(parameters.size());
    }

    @Override
    public Value eval(Object dataFromBroker, Map<String, Variable> variableTable) {
        // first check if the parameter count is correct
        if (!checkParameterCount(parameterCount, parameters)) {
            log.warn("The function '{}' was not executed. A wrong parameter count was detected. Received {} parameter" +
                            " but only {} are allowed!",
                    this.getClassName(), parameters.size(), parameterCount);
            return null;
        }

        // execute the main logic of the extending function
        return execute(dataFromBroker, variableTable);
    }

    /**
     * This is the main logic of the extending function.
     *
     * @param dataFromBroker The data from the broker.
     * @param variableTable  The map of variables in the current context.
     * @return the {@link Value} of the function. This can also be null!
     */
    public abstract Value execute(Object dataFromBroker, Map<String, Variable> variableTable);

    /**
     * Add a list of {@link Variable} as parameter to the function.
     *
     * @param parameters A list of {@link Variable} that represent the parameter of the function.
     */
    public void addParameters(List<Variable> parameters) {
        this.parameters.addAll(parameters);
    }

    @Override
    public ExprType getExprType() {
        return ExprType.FUNCTION;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

}
