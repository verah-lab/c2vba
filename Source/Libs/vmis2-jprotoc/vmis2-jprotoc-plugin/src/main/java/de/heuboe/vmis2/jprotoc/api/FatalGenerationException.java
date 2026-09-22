package de.heuboe.vmis2.jprotoc.api;

/**
 * Exception that is thrown, if a fatal error occurred during the generation and the plugin is
 * unable to handle this (unable to respond with an error message).
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class FatalGenerationException extends Exception {

    private static final long serialVersionUID = -1977100945307192649L;

    /**
     * Creates a new FatalGenerationException with the given message.
     *
     * @param message The message explaining the circumstances of the error.
     */
    public FatalGenerationException(final String message) {
        super(message);
    }

    /**
     * Creates a new FatalGenerationException with the given message and cause.
     *
     * @param message The message explaining the circumstances of the error.
     * @param cause The actual cause that caused the failure.
     */
    public FatalGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
