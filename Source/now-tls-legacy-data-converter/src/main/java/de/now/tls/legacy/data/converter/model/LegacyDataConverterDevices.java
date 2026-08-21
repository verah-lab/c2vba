package de.now.tls.legacy.data.converter.model;

import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgEa;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgFg;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgMoreInfo;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class handle the legacy devices from the config service.
 *
 * @author alexandero
 */
@Slf4j
@Component
public class LegacyDataConverterDevices {

    @Autowired
    private LegacyDataConverterProperties properties;

    @Setter
    private TlsCfgGetter tlsCfgGetter;

    @Getter
    private final Set<String> legacyDeviceIds = new HashSet<>();

    /**
     * Collect all ids of legacy devices from the current config service. A legacy device will be determined by checking
     * the {@link TlsCfgMoreInfo} field of a {@link TlsCfgEa} device. If it contains the enumeration TYP_48 the id of
     * the device will be stored in an internal set.
     */
    public void collectLegacyDevices() {
        List<TlsCfgDevice> devices = tlsCfgGetter.getDevices4Uz(properties.getUzId());
        Set<String> ids = new HashSet<>();

        for (TlsCfgDevice device : devices) {
            for (TlsCfgFg fg : device.getFgsList()) {
                for (TlsCfgEa ea : fg.getEasList()) {
                    if (!ea.getMoreList().isEmpty() && ea.getMoreList().contains(TlsCfgMoreInfo.TYP_48)) {
                        ids.add(ea.getEaid());
                    }
                }
            }
        }

        // clear the set before adding the new ids, this will be necessary if a config change was detected
        legacyDeviceIds.clear();
        legacyDeviceIds.addAll(Collections.unmodifiableSet(ids));

        log.info("Found {} legacy device{}", ids.size(), ids.size() == 1 ? "" : "s");
        log.debug("{}", legacyDeviceIds);
    }
}
