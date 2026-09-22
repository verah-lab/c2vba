package de.heuboe.asfinag.vmis2.tls.resources.generator.proto;

import de.heuboe.tls.resources.generator.SendScriptGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is an extension of {@link de.heuboe.tls.resources.generator.ScriptGetter} with the custom send scripts of
 * this project.
 */
public class Vmis2SendScriptGetter extends SendScriptGetter {

    /**
     * Get an array of all available script names in the order they are described in the TLS-2012 documentation. The
     * script "send-fg-all.txt" contains all data types, that can be sent.
     * Custom types are in the "send-fg-custom.txt" script.
     *
     * @return the script names in the following order: all, custom
     */
    @Override
    public String[] getCatalog() {
        List<String> scriptFiles = new ArrayList<>(Arrays.asList(super.getCatalog()));
        scriptFiles.add("send-fg-custom.txt");
        return scriptFiles.toArray(new String[0]);
    }

}
