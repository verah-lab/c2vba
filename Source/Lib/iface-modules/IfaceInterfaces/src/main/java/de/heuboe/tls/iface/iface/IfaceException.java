package de.heuboe.tls.iface.iface;

/**
 * Exceptions of this type may be thrown by methods from the iface classes
 */
public class IfaceException extends Exception {

    private static final long serialVersionUID = 1L;

    private final boolean     potentialStreamProblem;
    
    /**
     * Construct an exception with a string message
     * @param message The message to pass along with the exception
     */
    public IfaceException( String message ) {
        this( message, false );
    }
    
    /**
     * Construct an exception with a string message and an indicator
     * @param message The message to pass along with the exception
     * @param potentialStreamProblem should be set, if the underlying reason can be a structural problem in the input stream of telegrams
     */
    public IfaceException( String message, boolean potentialStreamProblem ) {
        super( message );
        this.potentialStreamProblem = potentialStreamProblem;
    }
    
    // ---

    public IfaceException( String message, Throwable e ) {
        this( message, e, false );
    }
    
    
    /**
     * Construct an exception with a string message, an indicator and an underlying cause
     * @param message The message to pass along with the exception
     * @param e an underlying cause
     * @param potentialStreamProblem should be set, if the underlying reason can be a structural problem in the input stream of telegrams
     */
    public IfaceException( String message, Throwable e, boolean potentialStreamProblem ) {
        super( message, e );
        this.potentialStreamProblem = potentialStreamProblem;
    }
    
    // ---

    public IfaceException( Throwable e ) {
        super( e );
        this.potentialStreamProblem = false;
    }
    
    // ---

    public boolean isPotentialStreamProblem() {
        return potentialStreamProblem;
    }

}
