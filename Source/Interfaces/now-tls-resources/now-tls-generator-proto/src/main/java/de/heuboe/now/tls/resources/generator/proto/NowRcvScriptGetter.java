package de.heuboe.now.tls.resources.generator.proto;

import de.heuboe.tls.resources.generator.HeaderFieldInfo;
import de.heuboe.tls.resources.generator.RcvScriptGetter;

import java.io.InputStream;
import java.util.*;

/**
 * This class can be used to get the rcv-scripts as {@link InputStream}s. It also has methods to get static information
 * about specific datatypes7fields of the scripts.
 */
public class NowRcvScriptGetter extends RcvScriptGetter {

    @Override
    protected Set<String> createIgnoredDeBlockDefinitions() {
        Set<String> result = super.createIgnoredDeBlockDefinitions();
        result.remove("FVEUnbekannterTyp");
        return result;
    }

    /**
     * Get an array of all available script names in the order they are described in the TLS-2012 dokumentation. The script
     * "rcv-fg-header.txt" contains all HEADER datatypes, otherwise each funktionsgruppe has one script.
     * Custom types are in the "rcv-fg-custom.txt" script.
     *
     * @return the script names in the following order: header, 254, 1, 2, 3, 4, 6, 9, custom
     */
    @Override
    public String[] getCatalog() {
        List<String> scriptFiles = new ArrayList<>(Arrays.asList(super.getCatalog()));
        scriptFiles.add("rcv-fg-custom.txt");
        scriptFiles.add("now-rcv-fg4.txt");
        // now-rcv-fg4.txt should replace this script, so we exclude it here
        scriptFiles.remove("rcv-fg4.txt");
        return scriptFiles.toArray(new String[0]);
    }

    @Override
    protected Map<String, Collection<HeaderFieldInfo>> createAdditionalHeaderFields() {
        Map<String, Collection<HeaderFieldInfo>> result = super.createAdditionalHeaderFields();
        HashMap<String, List<HeaderFieldInfo>> headerFieldInfos = this.createHeaderFieldInfos();
        result.put("WVZGrundeinstellung32", headerFieldInfos.get("header_4_31"));
        result.put("WZGGrundeinstellung33", headerFieldInfos.get("header_4_31"));
        result.put("WVZStellzustand48", headerFieldInfos.get("header_4_31"));
        result.put("WZGStellzustand55", headerFieldInfos.get("header_4_31"));
        return result;
    }
}
