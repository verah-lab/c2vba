package de.heuboe.vmis2.jprotoc.api;

/**
 * Exception that is thrown, if an error occurred during the generation and the plugin should
 * respond with an error message instead of the files to generate.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class GenerationException extends RuntimeException {

    private static final long serialVersionUID = 3426287081299481079L;

    /**
     * Creates a new GenerationException with the given message.
     *
     * @param message The message explaining the circumstances of the exception.
     */
    public GenerationException(final String message) {
        super(message);
    }

    /**
     * Creates a new GenerationException with the given message and cause.
     *
     * @param message The message explaining the circumstances of the exception.
     * @param cause The actual cause that caused the exception.
     */
    public GenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
