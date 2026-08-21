package de.heuboe.asfinag.tls.replay.testlve;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

import de.heuboe.tls.cfglib.Osi7Cfg;
import eu.vmis_ehe.vmis2.tls.received.WZGNichtDarstellbareWVZ;

public class Tester2 {
    @Test
    void checkMe() {
        WZGNichtDarstellbareWVZ ndz =null;
        WZGNichtDarstellbareWVZ.Builder bld = WZGNichtDarstellbareWVZ.newBuilder();

        ByteString bs0;
        
        byte[] codes = { 26, 28, (byte) 213 };
        ByteString bs = ByteString.copyFrom( codes );
        
        bld.setWvzCodes( bs );
        
        ndz = bld.build();
        
        ndz.getWvzCodes().forEach( bbb -> doSomethingWith( bbb ) );
        
        int i=0; i=i=1;
    }
    
    void doSomethingWith( byte b ) {
        System.out.println( "Code " + (b&0xFF) );
        System.out.println( " or potentially wrong Code " + b );
    }
    
    void checkMe2() {
        Osi7Cfg cfg = null;
        String myId = "";
        String cl = cfg.getClusterEaPermId( myId );
    }
}
