package de.heuboe.tls.sequencer.test.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgCable;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgServiceVersion;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgUZInfo;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;

import java.net.URL;
import java.util.List;

import static de.heuboe.tls.sequencer.test.helper.TestConsts.*;

public class MockedCfgGetter implements TlsCfgGetter {

    private TlsCfgServiceVersion cfgServiceVersion;
    private List<TlsCfgUZInfo> cfgUZInfos;
    private List<TlsCfgDevice> cfgDevices;
    private List<TlsCfgCable> cfgCables;

    public MockedCfgGetter() throws Exception {
        cfgServiceVersion = readVersionFile(FILE_NAME_VERS);

        cfgUZInfos = readListFile(FILE_NAME_UZ, TlsCfgUZInfo.class, new TypeReference<List<TlsCfgUZInfo>>() {
        });
        cfgDevices = readListFile(FILE_NAME_DEVS, TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {
        });
        cfgCables = readListFile(FILE_NAME_CABS, TlsCfgCable.class, new TypeReference<List<TlsCfgCable>>() {
        });
    }

    @Override
    public TlsCfgServiceVersion getCfgServiceVersion() {
        return cfgServiceVersion;
    }

    @Override
    public List<TlsCfgUZInfo> getUZInfos() {
        return cfgUZInfos;
    }

    @Override
    public List<TlsCfgDevice> getDevices4Uz(String uzId) {
        return cfgDevices;
    }

    @Override
    public List<TlsCfgCable> getCables4Uz(String uzId) {
        return cfgCables;
    }

    // ===========================================================================================

    private TlsCfgServiceVersion readVersionFile(String filename) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        URL url = new URL("file:" + filename);

        TlsCfgServiceVersion res = objectMapper.readValue(url, TlsCfgServiceVersion.class);
        return res;
    }

    private <T> List<T> readListFile(String filename, Class<T> c, TypeReference<List<T>> t) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        URL url = new URL("file:" + filename);

        List<T> res = objectMapper.readValue(url, t);
        return res;
    }

    // ===========================================================================================

    public void switchConfig() throws Exception {
        cfgDevices = readListFile(CFG_CHG_DEVS, TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {
        });
        cfgCables = readListFile(CFG_CHG_CABS, TlsCfgCable.class, new TypeReference<List<TlsCfgCable>>() {
        });
    }
}
