package de.heuboe.tls.iface.prot.tlsoip.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import de.heuboe.tls.iface.iface.TimeSyncGenerator;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericTimeSyncGenerator implements TimeSyncGenerator {

    private TimeZone defaultTimeZone = TimeZone.getTimeZone( "Europe/Berlin" ); // timezone where tls timestamps operate in
    // Europe/Berlin MEZ with daylight saving time
    // GMT+01:00 MEZ but always wintertime !needs to be exact!
    // UTC ...
    
    @Setter
    private boolean useDstBit = false;

    public void setTimeZone( String tzID ) { // need a valid timezone ID here. otherwise check may fails
        defaultTimeZone = TimeZone.getTimeZone( tzID );
        if ( !tzID.equals( defaultTimeZone.getID() ) ) {
            throw new IllegalArgumentException( tzID + " seems to be no valid timezone id" );
        }
        log.info( "Using timezone " + defaultTimeZone.getID() + " for TLS timestamps" );
    }

    @Override
    public byte[] makeTimeSyncTele() {
        // for time sync we need 'now time' perhaps in differing time zones
        GregorianCalendar cal = new GregorianCalendar( defaultTimeZone ); // 'wall time'


        byte dstBit = 0; // sommerzeit bit dst bit
        if ( useDstBit ) {
            int off = cal.get( Calendar.DST_OFFSET );
            if ( 0 != off ) {
                dstBit = (byte) 0x80;
            }
        }

        byte[] ts = new byte[7];
        ts[0] = (byte) ( ( cal.get( Calendar.HOUR_OF_DAY ) | dstBit ) & 0xff ); // sommerzeit bit dst bit
        ts[1] = (byte) ( cal.get( Calendar.MINUTE ) & 0xff );
        ts[2] = (byte) ( cal.get( Calendar.SECOND ) & 0xff );
        ts[3] = (byte) ( cal.get( Calendar.DAY_OF_MONTH ) & 0xff );
        ts[4] = (byte) ( cal.get( Calendar.MONTH ) & 0xff );
        ts[5] = (byte) ( (cal.get( Calendar.YEAR ) % 100 ) & 0xff );
        // dow ~ 1 = sun, 2 = mon ... tls: 1 = Mo, 2 = Di
        ts[6] = (byte) ((cal.get(Calendar.DAY_OF_WEEK)-1) & 0xff);
        if (ts[6] == 0) {
            ts[6] = 7;                   // special TLS value for sunday
        }
        
        TlsDeBlock deBlock = new TlsDeBlock( null, 255, 18 ); // KRI and IBs
        deBlock.setContent( ts );

        TlsETel etel = new TlsETel( null, 254, 2, 88 );
        etel.addDeBlock( deBlock );

        return etel.getBytes();
    }

}
