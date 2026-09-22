package de.heuboe.asfinag.vmis2.tls.scripts.tests;

import de.heuboe.asfinag.vmis2.tls.resources.generator.proto.Vmis2RcvScriptGetter;
import de.heuboe.tls.resources.scripts.tests.TestTransformer;

import java.io.IOException;

/**
 * This class is an extension of {@link TestTransformer} that will use the project specific receive scripts for creating
 * a {@link de.heuboe.tls.sender.interfaces.Transformer}.
 */
public class Vmis2RcvTestTransformer extends TestTransformer {

    /**
     * The constructor that create the project specific transformer.
     *
     * @throws IOException if something went wrong while parsing the input scripts.
     */
    public Vmis2RcvTestTransformer() throws IOException {
        super();
        createTransformer(new Vmis2RcvScriptGetter().concatAllInputStreams());
    }

}
