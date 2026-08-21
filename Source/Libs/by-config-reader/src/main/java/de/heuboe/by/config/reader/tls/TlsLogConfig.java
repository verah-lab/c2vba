package de.heuboe.by.config.reader.tls;

import de.heuboe.config.base.Types;
import io.vavr.collection.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * A bunch of static methods used to handle the 'logical' TLS world
 */
@Slf4j
@Getter
public class TlsLogConfig {

    private TlsLogConfig() {
    }

    /**
     * creates and link the AQs under the given sst
     * @param sst ConfigObject
     */
    public static void linkAqs(ConfigObject sst) {
        log.info("start linkAqs for " + printInfos(sst));
        ConfigObject cl = null;
        ConfigObject aq = null;
        java.util.List<ConfigObject> toRemove = new ArrayList<>();
        for (ConfigObject de : List.ofAll(sst.getChildren()).filter(c -> c.getFg() == 4)) {
            if (de.isCl()) {
                cl = de;
                de.setTlsRef("");
                continue;
            }
            Types.ConfigItemType type = de.getType();
            if (type == Types.ConfigItemType.AQ) {
                if (cl == null) {
                    // passiert auch bei doppelten AQs der erste hat ggf. keine Cl
                    toRemove.add(de);
                    log.warn("Duplicate AQ/ AQ without CL [{}] at ", de.getId(), printInfos(sst));
                    continue;
                }
                aq = de;
                if (doOsiMatch(aq, cl)) {
                    aq.setTlsRef(cl.getId());
                } else {
                    throw new IllegalArgumentException("No OSI matching with AQ and CL [" + aq.getId()
                            + " | " + cl.getId() + "] at " + printInfos(sst));
                }
            } else if (type == Types.ConfigItemType.WZG) {
                if (cl == null) {
                    toRemove.add(de);
                    // passiert auch bei doppelten AQs der erste hat ggf. keine Cl
                    continue;
                }
                if(aq == null) {
                    throw new IllegalArgumentException("NO cluster an NO AQ for WZG '" + de.getId() + "' at " +
                            printInfos(sst));
                }
                de.setTlsRef(aq.getId());
            }
        }
        sst.getChildren().removeAll(toRemove);
    }

    private static boolean doOsiMatch(ConfigObject aq, ConfigObject cl) {
        return aq.getPort() == cl.getPort() && aq.getSlave() == cl.getSlave() && aq.getEak() == cl.getEak();
    }

    private static String printInfos(ConfigObject obj) {
        return obj.getType().name() + " '" + obj.getId()
                + "' (TLS_REF='" + obj.getTlsRef() + "', road='" + obj.getRoad() + "')";
    }
}
