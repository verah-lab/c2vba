package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgCable;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgServiceVersion;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgUZInfo;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;

public class MockedCfgGetter implements TlsCfgGetter {
    private TlsCfgServiceVersion cfgServiceVersion;
    private List<TlsCfgUZInfo> cfgUZInfos;
    private List<TlsCfgDevice> cfgDevices;
    private List<TlsCfgCable> cfgCables;
    
    public MockedCfgGetter( String uzId ) throws Exception {
        String dir = null;
        if ("UZ_A2".equals( uzId )) {
            dir = "src/test/resources/cfgData/UZ_A2-4.0.1.1/";
        }
        if ("UZ_Kaernten".equals( uzId )) {
            dir = "src/test/resources/cfgData/UZ_Kaernten-4.0.1.1/";
        }
        System.out.println( "Directory for config data: "  + dir );
        
        if (null == dir) {
            throw new IllegalStateException("Bad uz id for config");
        }
        
//        dir = "src/test/resources/cfgData/kaernten/";
        cfgServiceVersion = readVersionFile( dir + "tlsb-vers.json" );

        // @formatter:off
        cfgUZInfos = readListFile( dir + "tlsb-uzen.json",  TlsCfgUZInfo.class, new TypeReference<List<TlsCfgUZInfo>>() {} );
        cfgDevices = readListFile( dir + "tlsb-devs.json", TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {} );
        cfgCables  = readListFile( dir + "tlsb-cabs.json", TlsCfgCable.class,  new TypeReference<List<TlsCfgCable>>()  {} );
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
    public List<TlsCfgDevice> getDevices4Uz( String uzId ) {
        if (! cfgDevices.get( 0 ).getId().contentEquals( uzId ) ) { // !!! Faulenzer
            throw new IllegalArgumentException( "The configuration saved is not the requested one" );
        }
        return cfgDevices;
    }

    @Override
    public List<TlsCfgCable> getCables4Uz( String uzId ) {
        if (! cfgDevices.get( 0 ).getId().contentEquals( uzId ) ) { // !!! Faulenzer
            throw new IllegalArgumentException( "The configuration saved is not the requested one" );
        }
        return cfgCables;
    }
    
    // ===========================================================================================
    
    private TlsCfgServiceVersion readVersionFile( String filename ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        URL url = new URL( "file:" + filename );
        
        TlsCfgServiceVersion res = objectMapper.readValue(url, TlsCfgServiceVersion.class );
        return res;
    }
    
//    private <T> List<T> readListFile( String filename,  TypeReference t ) throws Exception {
    private <T> List<T> readListFile( String filename,  Class<T> c, TypeReference<List<T>> t ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        URL url = new URL( "file:" + filename );
        
//        List<T> res = objectMapper.readValue(url, new TypeReference<List<T>>(){});
        List<T> res = objectMapper.readValue(url, t );
        return res;
    }
    
}
