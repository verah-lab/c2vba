package de.heuboe.tls.receiver.test;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgCable;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgServiceVersion;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgUZInfo;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import io.grpc.Status;

public class MockedCfgGetterChg implements TlsCfgGetter {
    private TlsCfgServiceVersion cfgServiceVersion;
    private List<TlsCfgUZInfo> cfgUZInfos;
    private List<TlsCfgDevice> cfgDevices;
    private List<TlsCfgCable> cfgCables;
    
    private String dir = "src/test/resources/cfgData/WIE-STAGING-210125/";
    
    private int switchConfigCounter = 0;

    public MockedCfgGetterChg() throws Exception {
        cfgServiceVersion = readVersionFile( dir + "tlsb-vers.json" );

        // @formatter:off
        cfgUZInfos = readListFile( dir + "tlsb-uzen.json", TlsCfgUZInfo.class, new TypeReference<List<TlsCfgUZInfo>>() {} );
        cfgDevices = readListFile( dir + "tlsb-devs.json", TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {} );
        cfgCables  = readListFile( dir + "tlsb-cabs.json", TlsCfgCable.class,  new TypeReference<List<TlsCfgCable>>()  {} );
        // @formatter:on

        //            cfgUZInfos = readUzFile( "src/test/resources/cfgData/kaernten/uzList.json" );
//        cfgDevices = readDeviceFile( "src/test/resources/cfgData/kaernten/devList.json" );
//        cfgCables = readCableFile( "src/test/resources/cfgData/kaernten/cabList.json" );
    }
    
    public void setDir( final String dir ) {
        this.dir = dir;
        String lastChar = this.dir.substring(dir.length() - 1);
        String sep = File.separator;
        if (!lastChar.endsWith( sep )) {
            this.dir += sep;
        }
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
//        if ( ! "UZ_Kaernten".equals( uzId ) ) {
//            throw new IllegalArgumentException( "The configuration saved is for UZ_Kaernten" );
//        }
        return cfgDevices;
    }

    @Override
    public List<TlsCfgCable> getCables4Uz( String uzId ) {
//        if ( ! "UZ_Kaernten".equals( uzId ) ) {
//            throw new IllegalArgumentException( "The configuration saved is for UZ_Kaernten" );
//        }
//        if ( 1 == switchConfigCounter ) {
//            throw new io.grpc.StatusRuntimeException( Status.NOT_FOUND.withDescription( "[FAKE] Invalid UZ id " + uzId + " for TlsDevice id " ) );
//        }
        return cfgCables;
    }
    
    // ===========================================================================================
    
    public void switchConfig( String verFileName, String uzListFileName, String devListFileName, String cabListFileName ) throws Exception {
        if ( null != verFileName ) {
            cfgServiceVersion = readVersionFile( dir + verFileName );
        }

        if ( null != uzListFileName ) {
            cfgUZInfos = readListFile( dir + uzListFileName, TlsCfgUZInfo.class, new TypeReference<List<TlsCfgUZInfo>>() {
            } );
        }
        if ( null != devListFileName ) {
            cfgDevices = readListFile( dir + devListFileName, TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {
            } );
        }
        if ( null != cabListFileName ) {
            cfgCables = readListFile( dir + cabListFileName, TlsCfgCable.class, new TypeReference<List<TlsCfgCable>>() {
            } );
        }
        switchConfigCounter++;
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
