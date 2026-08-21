package de.heuboe.now.tls.resources.generator.proto;

import de.heuboe.tls.resources.generator.SendScriptGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is an extension of {@link de.heuboe.tls.resources.generator.ScriptGetter} with the custom send scripts of
 * this project.
 */
public class NowSendScriptGetter extends SendScriptGetter {

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
        // the order of the scripts is important because the legal rule must be in the first script!
        scriptFiles.add("now-send-fg-all.txt");
        scriptFiles.add("send-fg-custom.txt");
        // now-send-fg-all.txt should replace this script, so we exclude it here
        scriptFiles.remove("send-fg-all.txt");
        return scriptFiles.toArray(new String[0]);
    }

}
