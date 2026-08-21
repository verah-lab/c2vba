package de.heuboe.now.tls.scripts.tests;

import de.heuboe.now.tls.resources.generator.proto.NowRcvScriptGetter;
import de.heuboe.tls.resources.scripts.tests.TestTransformer;

import java.io.IOException;

/**
 * This class is an extension of {@link TestTransformer} that will use the project specific receive scripts for creating
 * a {@link de.heuboe.tls.sender.interfaces.Transformer}.
 */
public class NowRcvTestTransformer extends TestTransformer {

    /**
     * The constructor that create the project specific transformer.
     *
     * @throws IOException if something went wrong while parsing the input scripts.
     */
    public NowRcvTestTransformer() throws IOException {
        super();
        createTransformer(new NowRcvScriptGetter().concatAllInputStreams());
    }

}
