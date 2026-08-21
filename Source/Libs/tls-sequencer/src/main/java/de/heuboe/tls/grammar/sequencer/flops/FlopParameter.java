package de.heuboe.tls.grammar.sequencer.flops;

import lombok.Getter;
import lombok.Setter;

/**
 * This class holds all parameter of a flop that can be defined in a script.
 */
@Getter
@Setter
public class FlopParameter {

    /**
     * Defines the time in milliseconds when the flop will be executed.
     */
    private long timeout;

    /**
     * Defines the max time in milliseconds when the flop will be executed. This time can be reached by retriggering a
     * flop.
     */
    private long maxTimeout;

    /**
     * Constructor for flops that can be retriggered. Therefor a maxTimeout is necessary.
     *
     * @param timeout    The time in milliseconds when the flop will be executed.
     * @param maxTimeout The max time in milliseconds when the flop is executed.
     */
    public FlopParameter(String timeout, String maxTimeout) {
        this.timeout = Long.parseLong(timeout);
        this.maxTimeout = Long.parseLong(maxTimeout);
    }

    /**
     * Constructor for flops that can not be retriggered. Therefor no maxTimeout is necessary.
     *
     * @param timeout The time in milliseconds when the flop will be executed.
     */
    public FlopParameter(String timeout) {
        this.timeout = Long.parseLong(timeout);
        this.maxTimeout = -1;
    }
}
