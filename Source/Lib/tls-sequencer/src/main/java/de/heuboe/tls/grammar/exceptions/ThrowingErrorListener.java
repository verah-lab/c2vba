package de.heuboe.tls.grammar.exceptions;

import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * This class will handle script parse exceptions.
 */
public class ThrowingErrorListener extends BaseErrorListener {

    private final SequencerMessageManagement sequencerMessageManagement;

    /**
     * The constructor that initialize the {@link SequencerMessageManagement} for usage in exception methods.
     *
     * @param sequencerMessageManagement The {@link SequencerMessageManagement} errors should be sent to.
     */
    public ThrowingErrorListener(SequencerMessageManagement sequencerMessageManagement) {
        this.sequencerMessageManagement = sequencerMessageManagement;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        String errMsg = "line " + line + ": " + charPositionInLine + " " + msg;
        sequencerMessageManagement.sendMessage(errMsg);
        throw new ParseCancellationException(errMsg);
    }
}
