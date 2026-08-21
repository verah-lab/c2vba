package de.heuboe.tls.receiver.rdr.core;

/**
 * The class {@code TlsReceiverException} indicates conditions that are specific to the TlsReceiver code. It relies heavily on the base Exception class.
 *
 * @author Ronald Nikel
 * @see java.lang.Exception
 */
public class TlsReceiverException extends Exception {
        public TlsReceiverException() {
                super();
        }

        public TlsReceiverException( String err ) {
                super( err );
        }

        public TlsReceiverException( String message, Throwable cause ) {
                super( message, cause );
        }

        public TlsReceiverException( Throwable cause ) {
                super( cause );
        }
}
