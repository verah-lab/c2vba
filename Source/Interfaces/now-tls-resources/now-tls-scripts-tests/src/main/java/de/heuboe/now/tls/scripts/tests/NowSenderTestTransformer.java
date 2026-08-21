package de.heuboe.now.tls.scripts.tests;

import de.heuboe.now.tls.resources.generator.proto.NowSendScriptGetter;
import de.heuboe.tls.resources.scripts.tests.SenderTestTransformer;

import java.io.IOException;

/**
 * This class is an extension of {@link SenderTestTransformer} that will use the project specific send scripts for
 * creating a {@link de.heuboe.tls.sender.interfaces.Transformer}.
 */
public class NowSenderTestTransformer extends SenderTestTransformer {

    /**
     * The constructor that create the project specific transformer.
     *
     * @throws IOException if something went wrong while parsing the input scripts.
     */
    public NowSenderTestTransformer() throws IOException {
        super();
        createTransformer(new NowSendScriptGetter().concatAllInputStreams());
    }
}
