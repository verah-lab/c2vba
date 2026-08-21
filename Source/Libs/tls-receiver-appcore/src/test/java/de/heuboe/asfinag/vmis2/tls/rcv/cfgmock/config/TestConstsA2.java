package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config;

import java.nio.charset.Charset;

public class TestConstsA2 {
    public static final String DIR_NAME = "src/test/resources/data/a2/";
    public static final String FILE_NAME_DEVS = DIR_NAME + "regDevs.json";
    public static final String FILE_NAME_CABS = DIR_NAME + "regCabs.json";
    public static final String FILE_NAME_UZ   = DIR_NAME + "regUZ.json";
    public static final String FILE_NAME_VERS = DIR_NAME + "regVers.json";
    
    public static final String uzId = "UZ_A2";
    
    public static final Charset utf8 = Charset.forName( "UTF-8" );
}
