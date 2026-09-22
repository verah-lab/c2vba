package de.heuboe.asfinag.vmis2.tls.scripts.tests;

import de.heuboe.asfinag.vmis2.tls.resources.generator.proto.Vmis2SendScriptGetter;
import de.heuboe.tls.resources.scripts.tests.SenderTestTransformer;

import java.io.IOException;

/**
 * This class is an extension of {@link SenderTestTransformer} that will use the project specific send scripts for
 * creating a {@link de.heuboe.tls.sender.interfaces.Transformer}.
 */
public class Vmis2SenderTestTransformer extends SenderTestTransformer {

    /**
     * The constructor that create the project specific transformer.
     *
     * @throws IOException if something went wrong while parsing the input scripts.
     */
    public Vmis2SenderTestTransformer() throws IOException {
        super();
        createTransformer(new Vmis2SendScriptGetter().concatAllInputStreams());
    }
}
