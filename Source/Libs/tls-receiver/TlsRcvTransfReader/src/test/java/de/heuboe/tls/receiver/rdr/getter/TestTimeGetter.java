package de.heuboe.tls.receiver.rdr.getter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import de.heuboe.log.Logger;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.item.GregorianItem;
import de.heuboe.tls.tlstele.meta.Helper;

public class TestTimeGetter {
    private static final Logger LOGGER      = Logger.getLogger( TestTimeGetter.class );
    private static final String ACTUAL_TIME = "#ActualTime";

    String timezoneIdUTC = "UTC";
    TimeZone timezoneUTC = TimeZone.getTimeZone( timezoneIdUTC );
    
    String timezoneIdGMTp1 = "GMT+01:00";
    TimeZone timezoneGMTp1 = TimeZone.getTimeZone( timezoneIdGMTp1 );
    
    String timezoneIdEuropeBerlin = "Europe/Berlin";
    TimeZone timezoneEuropeBerlin = TimeZone.getTimeZone( timezoneIdEuropeBerlin );
    
    static int allCnt = 0;

    @Test
    public void testGetter0() {
        int cnt = 0;
        LocalDate d = LocalDate.of( 2020, 10, 25 );
        LocalTime t = LocalTime.of( 2, 1, 0 );
        LocalDateTime dt = LocalDateTime.of( d, t );
        ZoneId zi = ZoneId.of( "Europe/Berlin" );
        ZonedDateTime zdt0 = ZonedDateTime.ofLocal(  dt, zi, ZoneOffset.of( "+00:00" ) );
        ZonedDateTime zdt = ZonedDateTime.ofLocal(  dt, zi, ZoneOffset.of( "+01:00" ) );
        ZonedDateTime zdta2 = ZonedDateTime.ofLocal(  dt, zi, ZoneOffset.of( "+02:00" ) );
        ZonedDateTime zdta3 = ZonedDateTime.ofLocal(  dt, zi, ZoneOffset.of( "+03:00" ) );
        ZonedDateTime zdtErr = ZonedDateTime.of(  d, t, zi );
        long secs = zdt.toEpochSecond();
        long secsErr = zdtErr.toEpochSecond();
        long secs3 = zdta3.toEpochSecond();

        ZoneOffset zo = zi.getRules().getOffset( dt );
        Instant inst = dt.toInstant( zo );
        long ll = inst.getEpochSecond();

        boolean dstOn = zi   // Represent a specific time zone, the history of past, present, and future changes to the offset-from-UTC used by the people of a certain region.
                             .getRules()                // Obtain the list of those changes in offset.
                             .isDaylightSavings(        // See if the people of this region are observing Daylight Saving Time at a specific moment.
                                dt.toInstant( zo )//Instant.now()          // Specify the moment. Here we capture the current moment at runtime.
                         );
        {
            LocalDate d2 = LocalDate.of( 2020, 1, 2 );
            LocalTime t2 = LocalTime.of( 2, 1, 0 );
            LocalDateTime dt2 = LocalDateTime.of( d2, t2 );

            ZonedDateTime zdt2 = ZonedDateTime.of( dt2, zi );
            ZoneId zi2 = zdt2.getZone();
            ZoneOffset winterOffset = zdt2.getOffset();

            d2 = LocalDate.of( 2020, 7, 2 );
            t2 = LocalTime.of( 2, 1, 0 );
            dt2 = LocalDateTime.of( d2, t2 );

            zdt2 = ZonedDateTime.of( dt2, zi );
            ZoneId zi3 = zdt2.getZone();
            ZoneOffset summerOffset = zdt2.getOffset();

            int i = 0; i = i + 1;
        }

        int i = 0; i = i + 1;

        assertTrue( i != 0 );
    }
    
    @Test
    public void testGetter1() {
        int cnt = 0;

        DataItem res1 = null;
        try {
            String letters = "h m s d";
            TimeGetter tg = new TimeGetter( letters, "TimeGetter", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            // @formatter:off
            GregorianCalendar check = makeTime( timezoneUTC,  2020, 5, 29,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "\n\n" + printTime( check ) );
            
            GregorianCalendar cal = new GregorianCalendar( timezoneGMTp1 ); // now
            
            byte[] data = new byte[4];
            data[0] = (byte) cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) cal.get( Calendar.SECOND );
            data[3] = (byte) cal.get( Calendar.DAY_OF_MONTH );
            
            Map<String, DataItem> etelVars = new HashMap<>();
            GregorianCalendar actualTime = new GregorianCalendar( timezoneGMTp1 ); // timezone does not really matter. only (milli)seconds will be taken
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!
            
            res1 = tg.get( data, 0, etelVars );
            
            LOGGER.info( res1.getName() );
            DataItemType t = res1.getType();
            
            assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterSummertime() {
        
        int cnt = 0;
        
        GregorianCalendar actualTime = makeTime( timezoneEuropeBerlin,  2020, 10, 25,   1, 55, 0,  423 ); // summer time?

//        for ( int i = 0; i < 120; ++i ) {
//            LOGGER.info(" T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
//            actualTime.add( Calendar.MINUTE, 1 );
//        }

        DataItem res1 = null;
        try {
            String letters = "h m s";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );
            
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 5, 29,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "Pretend now:" + printTime( actualTime ) );
            Map<String, DataItem> etelVars = new HashMap<>();
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            boolean currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);

            byte[] data = new byte[3]; // 23:59:01
            data[0] = (byte) ( 23 + ( currentlyInDst ? 128 : 0 ) ); // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 01; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            DataItemType t = res1.getType();

            assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            GregorianCalendar fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) ); 
            
            // beginning of dst in MEZ
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 3, 29,   3, 0, 5,  423 ); // dst turned on 5 seconds before. still receive mst
            // @formatter:on
            LOGGER.info( "Pretend now:" + printTime( actualTime ) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);

            data = new byte[3]; // 01:59:00 // next minute will be dst
            data[0] = (byte) 1; // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 00; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            calRes = res1.getAsGregorianCalendar();
            
            fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) ); 
            
            
            // \\\\\\\\\\\\\\\\\\\\\\

            // beginning of dst in MEZ
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 3, 29,   1, 29, 5,  123 ); // dst turned on 5 seconds before. still receive mst
            // @formatter:on
            LOGGER.info( "\n\n\nInit: " + printTime( actualTime ) );

            for ( int m = 0; m < 180; ++m ) {
                actualTime.add( Calendar.MINUTE, 1 );
                etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//                LOGGER.info( "Now: " + printTime( actualTime ) + " ms " + actualTime.getTimeInMillis() );

                GregorianCalendar getLastMinute = (GregorianCalendar) actualTime.clone();
                getLastMinute.add( Calendar.MINUTE, -1 );
                int dstBit = ( getLastMinute.get( Calendar.DST_OFFSET ) != 0 ) ? 128 : 0;
                data[0] = (byte) ( getLastMinute.get( Calendar.HOUR_OF_DAY ) + dstBit );
                data[1] = (byte) ( getLastMinute.get( Calendar.MINUTE )  );

                if ( ( 1 == (getLastMinute.get( Calendar.HOUR_OF_DAY ) + dstBit) ) && ( 59 == getLastMinute.get( Calendar.MINUTE ) ) ) {
                    breakMe();
                }

                res1 = tg.get( data, 0, etelVars );

                calRes = res1.getAsGregorianCalendar();
//                LOGGER.info( "Result: " + printTime( calRes ) );

                fin = new GregorianCalendar( timezoneUTC );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

                assertEquals( 65, actualTime.getTimeInMillis() / 1000 - fin.getTimeInMillis() / 1000 );
                cnt++;
            }
            
            // \/\/\/\/\/\/\/\/\/\/\/\/
            
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 10, 25,   1, 29, 2,  0 ); // summer time
            // @formatter:on
            LOGGER.info( "\n\n\nInit: " + printTime( actualTime ) );

            for ( int m = 0; m < 180; ++m ) {
                actualTime.add( Calendar.MINUTE, 1 );
                etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//                LOGGER.info( "Now: " + printTime( actualTime ) + " ms " + actualTime.getTimeInMillis() );

                GregorianCalendar getLastMinute = (GregorianCalendar) actualTime.clone();
                getLastMinute.add( Calendar.MINUTE, -1 );
                int dstBit = ( getLastMinute.get( Calendar.DST_OFFSET ) != 0 ) ? 128 : 0;
                data[0] = (byte) ( getLastMinute.get( Calendar.HOUR_OF_DAY ) + dstBit );
                data[1] = (byte) ( getLastMinute.get( Calendar.MINUTE )  );

                if ( ( 130 == getLastMinute.get( Calendar.HOUR_OF_DAY ) + dstBit ) && ( 0 == getLastMinute.get( Calendar.MINUTE ) ) ) {
                    breakMe();
                }

                res1 = tg.get( data, 0, etelVars );

                calRes = res1.getAsGregorianCalendar();
//                LOGGER.info( "Result: " + printTime( calRes ) );

                fin = new GregorianCalendar( timezoneUTC );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

                assertEquals( 62, ( actualTime.getTimeInMillis() - fin.getTimeInMillis() ) / 1000 );
                cnt++;
            }

            // \\\\\\\\\\\\\\\\\\\\\\
            
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 10, 25,   1, 55, 2,  0 ); // summer time
            // @formatter:on

            for ( int i = 0; i < 5; ++i ) {
                LOGGER.info("--- T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
                actualTime.add( Calendar.MINUTE, 1 );
            }
            
            // end of dst in MEZ before first three o'clock. i.e. still summertime
            // @formatter:off
         //actualTime = makeTime( tz,  2020, 10, 25,   2, 0, 5,  423 ); // summer or winter time?
            // @formatter:on
         //actualTime.set( Calendar.DST_OFFSET , 3600000 );
            LOGGER.info( "Pretend now:" + printTime( actualTime ) + " dst: " + (actualTime.get( Calendar.DST_OFFSET ) != 0) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            data[0] = (byte) (1 + 128); // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 00; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            calRes = res1.getAsGregorianCalendar();
            LOGGER.info( "Result: " + printTime( calRes ) );
            
            fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) ); 
            
            assertEquals( "UTC 24.10.2020 23:59:00.000 DST OFF", printTime( fin ) );
            LOGGER.info( "delta t1 " + (actualTime.getTimeInMillis() - fin.getTimeInMillis()) / 1000 );
            cnt++;

            for ( int i = 0; i < 60; ++i ) {
                //LOGGER.info("--- T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
                actualTime.add( Calendar.MINUTE, 1 );
            }
            
            // end of dst in MEZ vefore first three o'clock. i.e. no more summertime
            // @formatter:off
         //actualTime = makeTime( tz,  2020, 10, 25,   2, 0, 5,  423 ); // summer or winter time?
            // @formatter:on
         //actualTime.set( Calendar.DST_OFFSET , 0 ); // be sure to be in winter time
            LOGGER.info( "Pretend now:" + printTime( actualTime ) + " dst: " + (actualTime.get( Calendar.DST_OFFSET ) != 0) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            data[0] = (byte) (2 + 128); // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 00; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            calRes = res1.getAsGregorianCalendar();
            LOGGER.info( "Result: " + printTime( calRes ) );
            
            fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) ); 
            
            assertEquals( "UTC 25.10.2020 00:59:00.000 DST OFF", printTime( fin ) );
            LOGGER.info( "delta t2 " + (actualTime.getTimeInMillis() - fin.getTimeInMillis()) / 1000 );
            cnt++;
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterSummertimeAdv() {

        int cnt = 0;

        GregorianCalendar actualTime;

//        for ( int i = 0; i < 120; ++i ) {
//            LOGGER.info(" T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
//            actualTime.add( Calendar.MINUTE, 1 );
//        }

        DataItem res1 = null;
        try {
            String letters = "h m s";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );

            Map<String, DataItem> etelVars = new HashMap<>();
            byte[] data = new byte[3]; // 23:59:01
            GregorianCalendar calRes;// = res1.getAsGregorianCalendar();
            GregorianCalendar fin;// = new GregorianCalendar( timezoneUTC );

            //            // @formatter:off
//            actualTime = makeTime( timezoneEuropeBerlin,  2020, 5, 28,   23, 0, 5,  432 ); // summer time?
//            // @formatter:on
//            LOGGER.info( "Pretend now:" + printTime( actualTime ) );
//            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//
//            boolean currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);
//
//            data[0] = (byte) ( 23 + ( currentlyInDst ? 128 : 0 ) ); // cal.get( Calendar.HOUR_OF_DAY );
//            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
//            data[2] = (byte) 01; // cal.get( Calendar.SECOND );
//
//            res1 = tg.get( data, 0, etelVars );
//
//            DataItemType t = res1.getType();
//
//            assertEquals( DataItemType.DATE, t );
//            cnt++;
//
//            fin.setTimeInMillis( calRes.getTimeInMillis() );
//            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
//
//            // beginning of dst in MEZ
//            // @formatter:off
//            actualTime = makeTime( timezoneEuropeBerlin,  2020, 3, 29,   3, 0, 5,  423 ); // dst turned on 5 seconds before. still receive mst
//            // @formatter:on
//            LOGGER.info( "Pretend now:" + printTime( actualTime ) );
//            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//
//            currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);
//
//            data = new byte[3]; // 01:59:00 // next minute will be dst
//            data[0] = (byte) 1; // cal.get( Calendar.HOUR_OF_DAY );
//            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
//            data[2] = (byte) 00; // cal.get( Calendar.SECOND );
//
//            res1 = tg.get( data, 0, etelVars );
//
//            calRes = res1.getAsGregorianCalendar();
//
//            fin = new GregorianCalendar( timezoneUTC );
//            fin.setTimeInMillis( calRes.getTimeInMillis() );
//            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );


            // \\\\\\\\\\\\\\\\\\\\\\

            // beginning of dst in MEZ
            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 3, 28,   23, 0, 40,  111 );
            // @formatter:on
            LOGGER.info( "\n\n\nInit: " + printTime( actualTime ) );

            for ( int m = 0; m < 200; ++m ) {
                actualTime.add( Calendar.MINUTE, 1 );
                etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//                LOGGER.info( "Now: " + printTime( actualTime ) + " ms " + actualTime.getTimeInMillis() );

                GregorianCalendar getNextMinute = (GregorianCalendar) actualTime.clone();
                getNextMinute.add( Calendar.SECOND, 31 );
                int dstOff = getNextMinute.get( Calendar.DST_OFFSET );
                int dstBit = ( dstOff != 0 ) ? 128 : 0;
                data[0] = (byte) ( getNextMinute.get( Calendar.HOUR_OF_DAY ) + dstBit );
                data[1] = (byte) ( getNextMinute.get( Calendar.MINUTE )  );
                data[2] = (byte) ( getNextMinute.get( Calendar.SECOND )  );

                res1 = tg.get( data, 0, etelVars );

                calRes = res1.getAsGregorianCalendar();
//                LOGGER.info( "Result: " + printTime( calRes ) );

                fin = new GregorianCalendar(timezoneEuropeBerlin);//( timezoneUTC );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

                assertEquals( -31, actualTime.getTimeInMillis() / 1000 - fin.getTimeInMillis() / 1000 );
                cnt++;
            }

            // \/\/\/\/\/\/\/\/\/\/\/\/

            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 10, 24,   23, 29, 40,  0 ); // summer time
            // @formatter:on
            LOGGER.info( "\n\n\nInit: " + printTime( actualTime ) );

            for ( int m = 0; m < 320; ++m ) {
                actualTime.add( Calendar.MINUTE, 1 );
                etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
//                LOGGER.info( "Now: " + printTime( actualTime ) + " ms " + actualTime.getTimeInMillis() );

                GregorianCalendar getNextMinute = (GregorianCalendar) actualTime.clone();
                getNextMinute.add( Calendar.SECOND, 31 );
                int dstOff = getNextMinute.get( Calendar.DST_OFFSET );
                int dstBit = ( dstOff != 0 ) ? 128 : 0;
                data[0] = (byte) ( getNextMinute.get( Calendar.HOUR_OF_DAY ) + dstBit );
                data[1] = (byte) ( getNextMinute.get( Calendar.MINUTE )  );
                data[2] = (byte) ( getNextMinute.get( Calendar.SECOND )  );

                res1 = tg.get( data, 0, etelVars );

                calRes = res1.getAsGregorianCalendar();
//                LOGGER.info( "Result: " + printTime( calRes ) );

                fin = new GregorianCalendar( timezoneEuropeBerlin );//( timezoneUTC );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

                assertEquals( -31, ( actualTime.getTimeInMillis() - fin.getTimeInMillis() ) / 1000 );
                cnt++;
            }

            // \\\\\\\\\\\\\\\\\\\\\\

            // @formatter:off
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 10, 25,   1, 55, 2,  0 ); // summer time
            // @formatter:on

            for ( int i = 0; i < 5; ++i ) {
                LOGGER.info("--- T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
                actualTime.add( Calendar.MINUTE, 1 );
            }

            // end of dst in MEZ before first three o'clock. i.e. still summertime
            // @formatter:off
            //actualTime = makeTime( tz,  2020, 10, 25,   2, 0, 5,  423 ); // summer or winter time?
            // @formatter:on
            //actualTime.set( Calendar.DST_OFFSET , 3600000 );
            LOGGER.info( "Pretend now:" + printTime( actualTime ) + " dst: " + (actualTime.get( Calendar.DST_OFFSET ) != 0) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));

            data[0] = (byte) (1 + 128); // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 00; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            calRes = res1.getAsGregorianCalendar();
            LOGGER.info( "Result: " + printTime( calRes ) );

            fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

            assertEquals( "UTC 24.10.2020 23:59:00.000 DST OFF", printTime( fin ) );
            LOGGER.info( "delta t1 " + (actualTime.getTimeInMillis() - fin.getTimeInMillis()) / 1000 );
            cnt++;

            for ( int i = 0; i < 60; ++i ) {
                //LOGGER.info("--- T:" + printTime( actualTime ) + " dst: " + ( actualTime.get( Calendar.DST_OFFSET ) != 0 ) );
                actualTime.add( Calendar.MINUTE, 1 );
            }

            // end of dst in MEZ vefore first three o'clock. i.e. no more summertime
            // @formatter:off
            //actualTime = makeTime( tz,  2020, 10, 25,   2, 0, 5,  423 ); // summer or winter time?
            // @formatter:on
            //actualTime.set( Calendar.DST_OFFSET , 0 ); // be sure to be in winter time
            LOGGER.info( "Pretend now:" + printTime( actualTime ) + " dst: " + (actualTime.get( Calendar.DST_OFFSET ) != 0) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));

            data[0] = (byte) (2 + 128); // cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; // (cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 00; // cal.get( Calendar.SECOND );

            res1 = tg.get( data, 0, etelVars );

            calRes = res1.getAsGregorianCalendar();
            LOGGER.info( "Result: " + printTime( calRes ) );

            fin = new GregorianCalendar( timezoneUTC );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );

            assertEquals( "UTC 25.10.2020 00:59:00.000 DST OFF", printTime( fin ) );
            LOGGER.info( "delta t2 " + (actualTime.getTimeInMillis() - fin.getTimeInMillis()) / 1000 );
            cnt++;

            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterReplayMitternachtsproblem() {
        int cnt = 0;

        DataItem res1 = null;
        try {
            String letters = "h m s";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            // @formatter:off
            GregorianCalendar actualTime = makeTime( timezoneGMTp1,  2020, 5, 29,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
            Map<String, DataItem> etelVars = new HashMap<>();
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            byte[] data = new byte[3]; // 23:59:01
            data[0] = (byte) 23; //cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; //(cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 01; //cal.get( Calendar.SECOND );
            
            res1 = tg.get( data, 0, etelVars );
            
            DataItemType t = res1.getType();
            
            /* x */assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            
            /* x */assertEquals( "GMT+01:00 28.05.2020 23:59:01.000 DST OFF", printTime( fin ) );
            cnt++;
            
            // @formatter:off
            actualTime = makeTime( timezoneGMTp1,  2020, 1, 1,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!
            
            res1 = tg.get( data, 0, etelVars );
            
            t = res1.getType();
            
            /* x */assertEquals( DataItemType.DATE, t );
            calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            fin = new GregorianCalendar( timezoneGMTp1 );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            
            /* x */assertEquals( "GMT+01:00 31.12.2019 23:59:01.000 DST OFF", printTime( fin ) );
            cnt++;
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterReplayMitternachtsproblemTyp31NoDstChange() {
        int cnt = 0;

        GregorianCalendar check0 = makeTime( timezoneGMTp1,  2020, 4, 31,   0, 0, 12,  423 );
        LOGGER.info( "der >31.< April 2020: " + printTime( check0 ) );

        DataItem res1 = null;
        try {
            String letters = "h m s d";
            TimeGetter tg = new TimeGetter( letters, "TimeGetter", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            // @formatter:off
            GregorianCalendar actualTime = makeTime( timezoneGMTp1,  2020, 5, 29,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
            Map<String, DataItem> etelVars = new HashMap<>();
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            byte[] data = new byte[4]; // 23:59:01
            data[0] = (byte) 23; //cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; //(cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 01; //cal.get( Calendar.SECOND );
            data[3] = (byte) 28; //cal.get( Calendar.DAY_OF_MONTH );
            
            res1 = tg.get( data, 0, etelVars );
            
            DataItemType t = res1.getType();
            
            assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            
            assertEquals( "GMT+01:00 28.05.2020 23:59:01.000 DST OFF", printTime( fin ) );
            cnt++;
            
            actualTime = makeTime( timezoneGMTp1,  2020, 2, 1,   0, 0, 12,  423 );
            YearMonth ym;
            int md;
            int md2 = actualTime.getActualMaximum( Calendar.DAY_OF_MONTH );
//            ym = YearMonth.of( 2020, 0 ); md =  ym.lengthOfMonth(); month = [1 .. 12]
            ym = YearMonth.of( 2020, 1 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 2 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 3 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 4 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 5 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 6 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 7 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 8 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 9 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 10 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 11 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 12 ); md =  ym.lengthOfMonth();
            breakMe();
            
            for ( int y = 1999; y < 2030; ++y) {
                for ( int m = 1; m < 13; ++m ) {
                    ym = YearMonth.of( y, m );
                    md =  ym.lengthOfMonth();
                    for ( int d = 1; d <= md; ++d ) {
                        // @formatter:off
                        actualTime = makeTime( timezoneGMTp1,  y, m, d,   0, 0, 12,  423 );
                        // @formatter:on
                        GregorianCalendar getLastDay = (GregorianCalendar) actualTime.clone();
                        getLastDay.add( Calendar.DAY_OF_MONTH, -1 );
                        data[3] = (byte)  getLastDay.get( Calendar.DAY_OF_MONTH ); //cal.get( Calendar.DAY_OF_MONTH );
                        LOGGER.info( "Pretend now:" + printTime( actualTime ) );
                        etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!
                        
                        res1 = tg.get( data, 0, etelVars );
                        
                        t = res1.getType();
                        
                        assertEquals( DataItemType.DATE, t );
                        calRes = res1.getAsGregorianCalendar();
                        
                        fin = new GregorianCalendar( timezoneGMTp1 );
                        fin.setTimeInMillis( calRes.getTimeInMillis() );
                        LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
                        LOGGER.trace( "delta sec=" + ((actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 )) );
                        assertEquals( 71, (actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 ) );
                        cnt++;
                    }
                }
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterReplayMitternachtsproblemTyp31NoDstChangeSpcl() {
        int cnt = 0;

        GregorianCalendar check0 = makeTime( timezoneGMTp1,  2020, 4, 31,   0, 0, 12,  423 );
        LOGGER.info( "der >31.< April 2020: " + printTime( check0 ) );

        DataItem res1 = null;
        try {
            String letters = "h m s d";
            TimeGetter tg = new TimeGetter( letters, "TimeGetter", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            GregorianCalendar calRes = null;
            GregorianCalendar actualTime = null;
            GregorianCalendar actualTime0 = null;

            {
                // @formatter:off
                actualTime0 = makeTime( timezoneUTC,  2022, 3, 31,   23, 9, 47,  0 ); // 22-03-31 23:09:47.0
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime0 ) );
                actualTime0.setTimeZone( timezoneGMTp1 );
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime0 ) );
                actualTime0.setTimeZone( timezoneEuropeBerlin );
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime0 ) );
                actualTime0.setTimeZone( timezoneGMTp1 );
            }

            {
                // @formatter:off
                actualTime = actualTime0;//makeTime( timezoneGMTp1,  2022, 3, 31,   23, 9, 47,  0 ); // 22 03 31 23:09:47.0
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                
                byte[] data = new byte[4]; // 00:09:48 1.
                data[0] = (byte) 0; //cal.get( Calendar.HOUR_OF_DAY );
                data[1] = (byte) 9; //(cal.get( Calendar.MINUTE ) + 1);
                data[2] = (byte) 48; //cal.get( Calendar.SECOND );
                data[3] = (byte) 1; //cal.get( Calendar.DAY_OF_MONTH );
                
                res1 = tg.get( data, 0, etelVars );
                
                DataItemType t = res1.getType();
                
                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;
                
                GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
                
//                assertTrue( calR );
            }

            {
                // @formatter:off
                actualTime = actualTime0;//makeTime( timezoneGMTp1,  2022, 3, 31,   23, 9, 47,  0 ); // 22-03-31 23:09:47.0 UTC !!! -> 1.4.22 0:09:47 GMT+01:00
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                
                byte[] data = new byte[4]; // 00:09:48 1.
                data[0] = (byte) 0; //cal.get( Calendar.HOUR_OF_DAY );
                data[1] = (byte) 9; //(cal.get( Calendar.MINUTE ) + 1);
                data[2] = (byte) 48; //cal.get( Calendar.SECOND );
                data[3] = (byte) 31; //cal.get( Calendar.DAY_OF_MONTH ); // ! 31.3.(2022) wg. Replay
                
                res1 = tg.get( data, 0, etelVars );
                
                DataItemType t = res1.getType();
                
                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;
                
                GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            }

            for ( int h = 0; h < 24; ++h ) {
                // @formatter:off
                actualTime = (GregorianCalendar) actualTime0.clone();//makeTime( timezoneGMTp1,  2022, 3, 31,   23, 9, 47,  0 ); // 22-03-31 23:09:47.0 UTC !!! -> 1.4.22 0:09:47 GMT+01:00
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                
                byte[] data = new byte[4]; // 00:09:48 1.
                data[0] = (byte)  h; //cal.get( Calendar.HOUR_OF_DAY );
                data[1] = (byte) 9; //(cal.get( Calendar.MINUTE ) + 1);
                data[2] = (byte) 48; //cal.get( Calendar.SECOND );
                data[3] = (byte) 31; //cal.get( Calendar.DAY_OF_MONTH ); // ! 31.3.(2022) wg. Replay
                
                res1 = tg.get( data, 0, etelVars );
                
                DataItemType t = res1.getType();
                
                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;
                
                GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( "\n\n>h>h> " + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            }

            {
                // @formatter:off
                actualTime = actualTime0;//makeTime( timezoneGMTp1,  2022, 3, 31,   23, 9, 47,  0 ); // 22-03-31 23:09:47.0 UTC !!! -> 1.4.22 0:09:47 GMT+01:00
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                
                byte[] data = new byte[4]; // 00:09:48 1.
                data[0] = (byte) 1; //cal.get( Calendar.HOUR_OF_DAY );
                data[1] = (byte) 9; //(cal.get( Calendar.MINUTE ) + 1);
                data[2] = (byte) 48; //cal.get( Calendar.SECOND );
                data[3] = (byte) 1; //cal.get( Calendar.DAY_OF_MONTH ); // ! 31.3.(2022) wg. Replay
                
                res1 = tg.get( data, 0, etelVars );
                
                DataItemType t = res1.getType();
                
                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;
                
                GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            }

            {
                // @formatter:off
                actualTime = (GregorianCalendar) actualTime0.clone();//makeTime( timezoneGMTp1,  2022, 3, 31,   23, 9, 47,  0 ); // 22-03-31 23:09:47.0 UTC !!! -> 1.4.22 0:09:47 GMT+01:00
                actualTime.add( Calendar.SECOND, -86400 ); // one day back
                // @formatter:on
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                
                byte[] data = new byte[4]; // 00:09:48 1.
                data[0] = (byte) 0; //cal.get( Calendar.HOUR_OF_DAY );
                data[1] = (byte) 9; //(cal.get( Calendar.MINUTE ) + 1);
                data[2] = (byte) 48; //cal.get( Calendar.SECOND );
                data[3] = (byte) 1; //cal.get( Calendar.DAY_OF_MONTH ); // ! 31.3.(2022) wg. Replay
                
                res1 = tg.get( data, 0, etelVars );
                
                DataItemType t = res1.getType();
                
                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;
                
                GregorianCalendar fin = new GregorianCalendar( timezoneGMTp1 );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( "\n\n" + printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) + "\n" ); 
            }
            
//            assertEquals( "GMT+01:00 28.05.2020 23:59:01.000 DST OFF", printTime( fin ) );
//            cnt++;
//            
            actualTime = makeTime( timezoneGMTp1,  2020, 2, 1,   0, 0, 12,  423 );
//            YearMonth ym;
//            int md;
//            int md2 = actualTime.getActualMaximum( Calendar.DAY_OF_MONTH );
////            ym = YearMonth.of( 2020, 0 ); md =  ym.lengthOfMonth(); month = [1 .. 12]
//            ym = YearMonth.of( 2020, 1 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 2 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 3 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 4 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 5 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 6 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 7 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 8 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 9 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 10 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 11 ); md =  ym.lengthOfMonth();
//            ym = YearMonth.of( 2020, 12 ); md =  ym.lengthOfMonth();
//            breakMe();
//            
//            for ( int y = 1999; y < 2030; ++y) {
//                for ( int m = 1; m < 13; ++m ) {
//                    ym = YearMonth.of( y, m );
//                    md =  ym.lengthOfMonth();
//                    for ( int d = 1; d <= md; ++d ) {
//                        // @formatter:off
//                        actualTime = makeTime( timezoneGMTp1,  y, m, d,   0, 0, 12,  423 );
//                        // @formatter:on
//                        GregorianCalendar getLastDay = (GregorianCalendar) actualTime.clone();
//                        getLastDay.add( Calendar.DAY_OF_MONTH, -1 );
//                        data[3] = (byte)  getLastDay.get( Calendar.DAY_OF_MONTH ); //cal.get( Calendar.DAY_OF_MONTH );
//                        LOGGER.info( "Pretend now:" + printTime( actualTime ) );
//                        etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!
//                        
//                        res1 = tg.get( data, 0, etelVars );
//                        
//                        t = res1.getType();
//                        
//                        assertEquals( DataItemType.DATE, t );
//                        calRes = res1.getAsGregorianCalendar();
//                        
//                        fin = new GregorianCalendar( timezoneGMTp1 );
//                        fin.setTimeInMillis( calRes.getTimeInMillis() );
//                        LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
//                        LOGGER.trace( "delta sec=" + ((actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 )) );
//                        assertEquals( 71, (actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 ) );
//                        cnt++;
//                    }
//                }
//            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterReplayMitternachtsproblemTyp31DstChange() {
        int cnt = 0;

        GregorianCalendar check0 = makeTime( timezoneEuropeBerlin,  2020, 4, 31,   0, 0, 12,  423 );
        LOGGER.info( "der >31.< April 2020: " + printTime( check0 ) );

        DataItem res1 = null;
        try {
            String letters = "h m s d";
            TimeGetter tg = new TimeGetter( letters, "TimeGetter", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );
            
            // @formatter:off
            GregorianCalendar actualTime = makeTime( timezoneEuropeBerlin,  2020, 5, 29,   0, 0, 12,  423 );
            // @formatter:on
            LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
            Map<String, DataItem> etelVars = new HashMap<>();
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
            
            byte[] data = new byte[4]; // 23:59:01
            data[0] = (byte) (23+128); //cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 59; //(cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 01; //cal.get( Calendar.SECOND );
            data[3] = (byte) 28; //cal.get( Calendar.DAY_OF_MONTH );
            
            res1 = tg.get( data, 0, etelVars );
            
            DataItemType t = res1.getType();
            
            assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;
            
            GregorianCalendar fin = new GregorianCalendar( timezoneEuropeBerlin );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) ); 
            
            assertEquals( "Europe/Berlin 28.05.2020 23:59:01.000 DST ON", printTime( fin ) );
            cnt++;
            
            actualTime = makeTime( timezoneEuropeBerlin,  2020, 2, 1,   0, 0, 12,  423 );
            YearMonth ym;
            int md;
            int md2 = actualTime.getActualMaximum( Calendar.DAY_OF_MONTH );
//            ym = YearMonth.of( 2020, 0 ); md =  ym.lengthOfMonth(); month = [1 .. 12]
            ym = YearMonth.of( 2020, 1 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 2 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 3 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 4 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 5 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 6 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 7 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 8 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 9 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 10 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 11 ); md =  ym.lengthOfMonth();
            ym = YearMonth.of( 2020, 12 ); md =  ym.lengthOfMonth();
            breakMe();
            
            for ( int y = 1999; y < 2030; ++y) {
                for ( int m = 1; m < 13; ++m ) {
                    ym = YearMonth.of( y, m );
                    md =  ym.lengthOfMonth();
                    for ( int d = 1; d <= md; ++d ) {
                        // @formatter:off
                        actualTime = makeTime( timezoneEuropeBerlin,  y, m, d,   0, 0, 12,  423 );
                        // @formatter:on
                        GregorianCalendar getLastDay = (GregorianCalendar) actualTime.clone();
                        getLastDay.add( Calendar.DAY_OF_MONTH, -1 );
                        
                        boolean currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);

                        data[0] = (byte) ( 23 + ( currentlyInDst ? 128 : 0 ) );
                        data[3] = (byte)  getLastDay.get( Calendar.DAY_OF_MONTH );
                        LOGGER.info( "Pretend now:" + printTime( actualTime ) );
                        etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!
                        
                        res1 = tg.get( data, 0, etelVars );
                        
                        t = res1.getType();
                        
                        assertEquals( DataItemType.DATE, t );
                        calRes = res1.getAsGregorianCalendar();
                        
                        fin = new GregorianCalendar( timezoneEuropeBerlin );
                        fin.setTimeInMillis( calRes.getTimeInMillis() );
                        LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
                        LOGGER.trace( "delta sec=" + ((actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 )) );
                        assertEquals( 71, (actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 ) );
                        cnt++;
                    }
                }
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterReplayMitternachtsproblemTyp31DstChangeAdv() {
        int cnt = 0;

        DataItem res1 = null;
        try {
            String letters = "h m s d";
            TimeGetter tg = new TimeGetter( letters, "TimeGetter", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );

            GregorianCalendar actualTime = makeTime( timezoneEuropeBerlin,  2020, 5, 28,   23, 59, 59,  321 );
            LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
            Map<String, DataItem> etelVars = new HashMap<>();
            etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));

            byte[] data = new byte[4]; // 23:59:01
            data[0] = (byte) (0+128); //cal.get( Calendar.HOUR_OF_DAY );
            data[1] = (byte) 0; //(cal.get( Calendar.MINUTE ) + 1);
            data[2] = (byte) 5; //cal.get( Calendar.SECOND );
            data[3] = (byte) 28; //cal.get( Calendar.DAY_OF_MONTH );

            res1 = tg.get( data, 0, etelVars );

            DataItemType t = res1.getType(); // expect 2020-05-28 0:01:00 CEST

            assertEquals( DataItemType.DATE, t );
            GregorianCalendar calRes = res1.getAsGregorianCalendar();
            cnt++;

            GregorianCalendar fin = new GregorianCalendar( timezoneEuropeBerlin );
            fin.setTimeInMillis( calRes.getTimeInMillis() );
            LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
            assertEquals( "Europe/Berlin 28.05.2020 00:00:05.000 DST ON", printTime( fin ) );
            cnt++;

            {
                LOGGER.info( "\n\nPretend now:" + printTime( actualTime ) );
                etelVars = new HashMap<>();
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));

                byte[] data2 = new byte[4]; // 23:59:01
                data2[0] = (byte) (0+128); //cal.get( Calendar.HOUR_OF_DAY );
                data2[1] = (byte) 1; //(cal.get( Calendar.MINUTE ) + 1);
                data2[2] = (byte) 30; //cal.get( Calendar.SECOND );
                data2[3] = (byte) 29; //cal.get( Calendar.DAY_OF_MONTH );

                res1 = tg.get( data2, 0, etelVars );

                t = res1.getType(); // expect 2020-05-28 0:01:00 CEST

                assertEquals( DataItemType.DATE, t );
                calRes = res1.getAsGregorianCalendar();
                cnt++;

                fin = new GregorianCalendar( timezoneEuropeBerlin );
                fin.setTimeInMillis( calRes.getTimeInMillis() );
                LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
                assertEquals( "Europe/Berlin 29.04.2020 00:01:30.000 DST ON", printTime( fin ) );
                cnt++;
            }

            for ( int y = 1999; y < 2030; ++y) {
                for ( int m = 1; m < 13; ++m ) {
                    YearMonth ym = YearMonth.of( y, m );
                    int md =  ym.lengthOfMonth();
                    for ( int d = 1; d <= md; ++d ) {
                        // @formatter:off
                        actualTime = makeTime( timezoneEuropeBerlin,  y, m, d,   23, 59, 59,  321 );
                        // @formatter:on
                        GregorianCalendar getNextDay = (GregorianCalendar) actualTime.clone();
                        getNextDay.add( Calendar.DAY_OF_MONTH, 1 );

                        boolean currentlyInDst = (actualTime.get( Calendar.DST_OFFSET ) != 0);
                        if (currentlyInDst) {
                            breakMe();
                        }

                        data[0] = (byte) ( 0 + ( currentlyInDst ? 128 : 0 ) );
                        data[3] = (byte)  getNextDay.get( Calendar.DAY_OF_MONTH );
                        LOGGER.info( "Pretend now:" + printTime( actualTime ) );
                        etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!

                        res1 = tg.get( data, 0, etelVars );

                        t = res1.getType();

                        assertEquals( DataItemType.DATE, t );
                        calRes = res1.getAsGregorianCalendar();

                        fin = new GregorianCalendar( timezoneEuropeBerlin );
                        fin.setTimeInMillis( calRes.getTimeInMillis() );
                        LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
                        LOGGER.trace( "delta sec=" + ((actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 )) );
                        assertEquals( -6, (actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 ) );
                        cnt++;
                    }
                }
            }

            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterLongTimenoDstChange() {
        int cnt = 0;
        
        GregorianCalendar actualTime = makeTime( timezoneGMTp1,  2020, 10, 25,   1, 55, 0,  423 ); // summer time?

        try {
            String letters = "z y M d h";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            Map<String, DataItem> etelVars = new HashMap<>();

            byte[] data = new byte[4]; // 23:59:01
            
            List<GregorianCalendar> ts = new ArrayList<>();
            ts.add( makeTime( timezoneGMTp1,  2020, 2,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 2,  5,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 3,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 4,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 5, 29,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 10, 5,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 11, 1,   0, 0, 12,  423 ) );
            
            for (GregorianCalendar t : ts ) {
                GregorianCalendar hourBack = (GregorianCalendar) t.clone();
                hourBack.add( Calendar.HOUR_OF_DAY, -1 );
                data[0] = (byte) ( hourBack.get( Calendar.YEAR ) % 100 );
                data[1] = (byte) ( hourBack.get( Calendar.MONTH  ) + 1 );
                data[2] = (byte) ( hourBack.get( Calendar.DAY_OF_MONTH  ) );
                data[3] = (byte) ( hourBack.get( Calendar.HOUR_OF_DAY  ) );
                
                actualTime = t;
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // !!!

                analyseAndCheckLTDST( timezoneGMTp1, actualTime, letters, tg, etelVars, data, 3612 );
                cnt++;
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterLongTimeDstChange() {
        int cnt = 0;
        GregorianCalendar actualTime = null;

        try {
            String letters = "z y M d h";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );
            
            Map<String, DataItem> etelVars = new HashMap<>();

            byte[] data = new byte[4];
            GregorianCalendar t0;
            
            // @formatter:off
            List<GregorianCalendar> ts = new ArrayList<>();
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 1,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 2,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 2,  5,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 3,  1,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneEuropeBerlin,  2020, 3, 29,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 5 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            //ts.add( makeTime( tz,  2020, 3, 29,   1, 0, 12,  423 ) );
            //ts.add( makeTime( tz,  2020, 3, 29,   2, 0, 12,  423 ) ); // gibt es nicht. hier wurde die Uhr auf drei vorgestellt
            //ts.add( makeTime( tz,  2020, 3, 29,   3, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 4,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 4,  2,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 5, 29,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 10, 5,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneEuropeBerlin,  2020, 10,25,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst 0n
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst off
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 11, 1,   0, 0, 12,  423 ) );
            // @formatter:on
            
            for (GregorianCalendar t : ts ) {
                GregorianCalendar hourBack = (GregorianCalendar) t.clone();
                int dstBit = 0;
                hourBack.add( Calendar.HOUR_OF_DAY, -1 );
                if (0 < hourBack.get( Calendar.DST_OFFSET )) {
                    dstBit = 128;
                }
                data[0] = (byte) ( hourBack.get( Calendar.YEAR ) % 100 );
                data[1] = (byte) ( hourBack.get( Calendar.MONTH  ) + 1 );
                data[2] = (byte) ( hourBack.get( Calendar.DAY_OF_MONTH  ) );
                data[3] = (byte) ( hourBack.get( Calendar.HOUR_OF_DAY  ) + dstBit );
                
                actualTime = t;
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                String nowTimeString = printTime( actualTime );
                LOGGER.info( "Pretend now: " + nowTimeString + " hourBack dstBit: " + dstBit );
                if ("Europe/Berlin 25.10.2020 02:00:12.423 DST OFF".equals( nowTimeString )) {
                    breakMe();
                }

                analyseAndCheckLTDST( timezoneEuropeBerlin, actualTime, letters, tg, etelVars, data, 3612 );
                cnt++;
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterAxlExactDstChange() {
        int cnt = 0;
        GregorianCalendar actualTime= null;

        try {
            String letters = "Y M d h m s";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdEuropeBerlin );
            
            Map<String, DataItem> etelVars = new HashMap<>();

            byte[] data = new byte[8];
            GregorianCalendar t0;
            
            // @formatter:off
            List<GregorianCalendar> ts = new ArrayList<>();
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 1,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 2,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 2,  5,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 3,  1,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneEuropeBerlin,  2020, 3, 29,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 5 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            //ts.add( makeTime( tz,  2020, 3, 29,   1, 0, 12,  423 ) );
            //ts.add( makeTime( tz,  2020, 3, 29,   2, 0, 12,  423 ) ); // gibt es nicht. hier wurde die Uhr auf drei vorgestellt
            //ts.add( makeTime( tz,  2020, 3, 29,   3, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 4,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 4,  2,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 5, 29,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 10, 5,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneEuropeBerlin,  2020, 10,25,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst 0n
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst off
            ts.add( makeTime( timezoneEuropeBerlin,  2020, 11, 1,   0, 0, 12,  423 ) );
            // @formatter:on
            
            for (GregorianCalendar t : ts ) {
                GregorianCalendar hourBack = (GregorianCalendar) t.clone();
                int dstBit = 0;
                hourBack.add( Calendar.HOUR_OF_DAY, -1 );
                if (0 < hourBack.get( Calendar.DST_OFFSET )) {
                    dstBit = 128;
                }
                data[0] = (byte) ( hourBack.get( Calendar.YEAR ) % 256 );
                data[1] = (byte) ( hourBack.get( Calendar.YEAR ) / 256 );
                data[2] = (byte) ( hourBack.get( Calendar.MONTH  ) + 1 );
                data[3] = (byte) ( hourBack.get( Calendar.DAY_OF_MONTH  ) );
                data[4] = (byte) ( hourBack.get( Calendar.HOUR_OF_DAY  ) + dstBit );
                data[5] = (byte) ( hourBack.get( Calendar.MINUTE  ) );
                data[6] = (byte) ( hourBack.get( Calendar.SECOND  ) );
                data[7] = (byte) ( hourBack.get( Calendar.MILLISECOND  ) ); // currently unused in timestamp
                
                actualTime = t;
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                String nowTimeString = printTime( actualTime );
                LOGGER.info( "Pretend now: " + nowTimeString + " hourBack dstBit: " + dstBit );
                if ("Europe/Berlin 25.10.2020 02:00:12.423 DST OFF".equals( nowTimeString )) {
                    breakMe();
                }

                analyseAndCheckLTDST( timezoneEuropeBerlin, actualTime, letters, tg, etelVars, data, 3600 );
                cnt++;
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetterAxlExactNoDstChange() {
        int cnt = 0;
        GregorianCalendar actualTime= null;

        try {
            String letters = "Y M d h m s";
            TimeGetter tg = new TimeGetter( letters, "Typ30", "targetType?" );
            TimeGetter.setTimetolerance( 45 );
            TimeGetter.setTimeZone( timezoneIdGMTp1 );
            
            Map<String, DataItem> etelVars = new HashMap<>();

            byte[] data = new byte[8];
            GregorianCalendar t0;
            
            // @formatter:off
            List<GregorianCalendar> ts = new ArrayList<>();
            ts.add( makeTime( timezoneGMTp1,  2020, 1,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 2,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 2,  5,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 3,  1,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneGMTp1,  2020, 3, 29,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 5 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            //ts.add( makeTime( tz,  2020, 3, 29,   1, 0, 12,  423 ) );
            //ts.add( makeTime( tz,  2020, 3, 29,   2, 0, 12,  423 ) ); // gibt es nicht. hier wurde die Uhr auf drei vorgestellt
            //ts.add( makeTime( tz,  2020, 3, 29,   3, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 4,  1,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 4,  2,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 5, 29,   0, 0, 12,  423 ) );
            ts.add( makeTime( timezoneGMTp1,  2020, 10, 5,   0, 0, 12,  423 ) );
            t0 = makeTime( timezoneGMTp1,  2020, 10,25,   0, 0, 12,  423 );
            ts.add( (GregorianCalendar) t0.clone() ); // 0 dst 0n
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 1 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst on
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 2 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 3 dst off
            t0.add( Calendar.HOUR_OF_DAY, 1 );
            ts.add( (GregorianCalendar) t0.clone() ); // 4 dst off
            ts.add( makeTime( timezoneGMTp1,  2020, 11, 1,   0, 0, 12,  423 ) );
            // @formatter:on
            
            for (GregorianCalendar t : ts ) {
                GregorianCalendar hourBack = (GregorianCalendar) t.clone();
                int dstBit = 0;
                hourBack.add( Calendar.HOUR_OF_DAY, -1 );
                if (0 < hourBack.get( Calendar.DST_OFFSET )) {
                    dstBit = 128;
                }
                data[0] = (byte) ( hourBack.get( Calendar.YEAR ) % 256 );
                data[1] = (byte) ( hourBack.get( Calendar.YEAR ) / 256 );
                data[2] = (byte) ( hourBack.get( Calendar.MONTH  ) + 1 );
                data[3] = (byte) ( hourBack.get( Calendar.DAY_OF_MONTH  ) );
                data[4] = (byte) ( hourBack.get( Calendar.HOUR_OF_DAY  ) + dstBit );
                data[5] = (byte) ( hourBack.get( Calendar.MINUTE  ) );
                data[6] = (byte) ( hourBack.get( Calendar.SECOND  ) );
                data[7] = (byte) ( hourBack.get( Calendar.MILLISECOND  ) ); // currently unused in timestamp
                
                actualTime = t;
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0));
                String nowTimeString = printTime( actualTime );
                LOGGER.info( "Pretend now: " + nowTimeString + " hourBack dstBit: " + dstBit );
                if ("Europe/Berlin 25.10.2020 02:00:12.423 DST OFF".equals( nowTimeString )) {
                    breakMe();
                }

                analyseAndCheckLTDST( timezoneGMTp1, actualTime, letters, tg, etelVars, data, 3600 );
                ++cnt;
            }
            
            allCnt += cnt;
            LOGGER.info( "#Tests = " + cnt + "/" + allCnt );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    private void analyseAndCheckLTDST( TimeZone tz, GregorianCalendar actualTime, String letters, TimeGetter tg, Map<String, DataItem> etelVars, byte[] data, int deltaT ) {
        DataItem res1;
        GregorianCalendar calRes;
        GregorianCalendar fin;
        if ( "Europe/Berlin 25.10.2020 03:00:12.423 DST OFF".equals( printTime( actualTime ) )) {
            int i = 0; i = 1 + 1;
        }
        res1 = tg.get( data, 0, etelVars );

        calRes = res1.getAsGregorianCalendar();
        
        fin = new GregorianCalendar( tz );
        fin.setTimeInMillis( calRes.getTimeInMillis() );
        LOGGER.info( printTime( actualTime ) + " + (" + Helper.hexdump( data ) + "/" + letters + ") -> " + printTime( fin ) );
        LOGGER.info( "delta sec=" + ((actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 )) );
        // delta t = 3612?
        assertEquals( deltaT, (actualTime.getTimeInMillis() / 1000) - ( fin.getTimeInMillis() / 1000 ) );
    }
    
    String printTime( GregorianCalendar cal ) {
        return String.format(
                // @formatter:off
                "%s %02d.%02d.%04d %02d:%02d:%02d.%03d %s", 
                cal.getTimeZone().getID(),
                
                cal.get( Calendar.DAY_OF_MONTH ),
                cal.get( Calendar.MONTH ) + 1,
                cal.get( Calendar.YEAR ),
                
                cal.get( Calendar.HOUR_OF_DAY ),
                cal.get( Calendar.MINUTE ),
                cal.get( Calendar.SECOND ),
                
                cal.get( Calendar.MILLISECOND ),
                
                cal.get( Calendar.DST_OFFSET ) != 0 ? "DST ON" : "DST OFF"
                ); // @formatter:on
    }
    
    // parameter mon is the real month. I.e. [1 .. 12]
    GregorianCalendar makeTime( TimeZone tz, int year, int mon_1to12, int day, int hour, int minute, int second, int millisecond) {
        GregorianCalendar res = new GregorianCalendar( tz );
        res.setTimeInMillis( 0 );
        
        res.set( Calendar.DAY_OF_MONTH, day );
        res.set( Calendar.MONTH, mon_1to12 - 1 );
        res.set( Calendar.YEAR, year );
            
        res.set( Calendar.HOUR_OF_DAY, hour );
        res.set( Calendar.MINUTE, minute );
        res.set( Calendar.SECOND, second);
            
        res.set( Calendar.MILLISECOND, millisecond );
        
        res.add( Calendar.SECOND, 0 );
        
        return res;
    }
    
    private void breakMe() {
        ; // NOSONAR
    }

}
