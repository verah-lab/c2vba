package de.heuboe.tls.receiver.rdr.getter;

/**
 * New way to analyse TLS tim stamps
 * @author ronald
 */

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.rdr.item.TimeItem;
import de.heuboe.tls.tlstele.meta.Helper;
import org.apache.logging.log4j.LogManager;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

/**
 * These Getters have the purpose to extract TLS time out of a byte array
 * Objects of this class get TLS time
 */
public class TimeGetter extends AbstractGetter { // NOSONAR here is a comment

    private static final org.apache.logging.log4j.Logger
             LOGGER            = LogManager.getLogger( de.heuboe.tls.receiver.rdr.getter.TimeGetter.class );
    private static final String DEBLOCK_TOO_SHORT = "DeBlock too short";
    private static final String STD_TIMEFMT_SHORT = "dd.MM.yyyy HH:mm:ss z";

    private static SystemMessageManagement smm = null;

    private static int          timetolerance;                                               // number of seconds in the future acceptable for a timestamp

    private static TimeZone defaultTimeZone = TimeZone.getTimeZone( "Europe/Berlin" );
             // timezone where tls timestamps operate in
    // Europe/Berlin MEZ with daylight saving time
    // GMT+01:00 MEZ but always wintertime !needs to be exact!
    // UTC ...

    private static ZoneId zoneId;
    private final List< String > spec = new ArrayList<>();
    private boolean unixTime = false;
    private final String targetType;

    public static void setTimeZone( String tzID ) { // need a valid timezone ID here. otherwise check may fails
        defaultTimeZone = TimeZone.getTimeZone( tzID );
        if ( !tzID.equals( defaultTimeZone.getID() ) ) {
            throw new IllegalArgumentException( tzID + " seems to be no valid timezone id" );
        }
        zoneId = ZoneId.of( tzID );
        LOGGER.info( "Using timezone " + defaultTimeZone.getID() + " for TLS timestamps/ New " + zoneId );
    }

    public static void setTimetolerance( int tolerance ) {
        timetolerance = tolerance;
    }

    public static void setSystemMessageManagement( SystemMessageManagement smm ) {
        TimeGetter.smm = smm;
    }

    /**
     * Constructor
     * @param letters how to interpret the bytes or an instruction (z)
     * @param name Name of the resulting object
     * @param targetType For type evaluating after parsing
     */
    public TimeGetter( String letters, String name, String targetType ) {
        super( name );
        this.targetType = targetType;
        if ( letters.startsWith( "UTC>" ) ) {
//            dstCorrection = false;
            letters = letters.substring( "UTC>".length() );
        }
        if ( "U".equals( letters ) ) {
            unixTime = true;
        } else {
            for ( String letter : letters.split( "[ :]" ) ) {
                switch ( letter ) {
                    case "c":
                    case "y":
                    case "Y":
                    case "m":
                    case "d":
                    case "h":
                    case "M":
                    case "s":
                    case "z":
                        // these letters are ok
                        spec.add( letter );
                        break;
                    default:
                        // all other letters are invalid
                        throw new IllegalArgumentException( "Invalid time specification: " + letters );
                }
            }
        }
    }

    private static int getUnsignedIntFromByte( final byte b ) {
        int res = b;
        if ( 0 > b ) {
            res += 256;
        }
        return res;
    }

    private static int getUnsignedIntFromInt( final int i ) {
        int res = i;
        if ( 0 > i ) {
            res += 256;
        }
        return res;
    }

    private static class BoolRef {
        private boolean b;

        BoolRef( boolean init ) {
            this.b = init;
        }

        public boolean isTrue() {
            return b;
        }

        public void setValue( boolean b ) {
            this.b = b;
        }
    }

    private static class LocalTimeRef {
        private LocalTime lt;
        LocalTimeRef( LocalTime init ) {
            this.lt = init;
        }
        public LocalTime getVal() {
            return lt;
        }
        public void setVal( LocalTime b ) {
            this.lt = b;
        }
    }

    private enum CorrectionModules {
        INIT, DAY, MONTH, NOTREQUIRED
    }

    private static class CorrectionModuleslRef {
        private CorrectionModules b;

        CorrectionModuleslRef( CorrectionModules init ) {
            this.b = init;
        }

        public CorrectionModules getVal() {
            return b;
        }

        public void setB( CorrectionModules b ) {
            this.b = b;
        }
    }

    private static class RecvRawDate {
        public int year = 0;
        public int mon = 0;
        public int day = 0;

        @Override
        public String toString() {
            return year+"-"+mon+"-"+day;
        }
    }

    private static final TimeZone tzUTC = TimeZone.getTimeZone( "UTC" );
    private static final boolean doDebugTimes = false;

    /*
     * (non-Javadoc)
     * @see de.heuboe.tls.receiver.interfaces.GetterRule#get(byte[], int, java.util.Map)
     * Analyse a sequence of bytes which are meant to be time
     * preset is done to RcvTimeBase (if present) the original time recorded of of data delivered at a later stage
     * if real time analysis (no RcvTimeBase present) is done, preset is made to the timestamp of the construction time of the telegram being analysed
     */
    @Override
    public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
        GregorianCalendar actualTime = (GregorianCalendar) etelVars.get( "#ActualTime" ).getAsGregorianCalendar().clone();
        if( null != etelVars.get( "#RcvTimeBase" ) ) {
            actualTime = etelVars.get( "#RcvTimeBase" ).getAsGregorianCalendar();
        }

        long actualTimeInMillis = actualTime.getTimeInMillis();
        long actualTimeInSecs = actualTimeInMillis / 1000;
        Instant recvTime = Instant.ofEpochMilli( actualTimeInMillis );
        ZonedDateTime recvDt = ZonedDateTime.ofInstant( recvTime, zoneId );

        actualTime.add( Calendar.MILLISECOND, -actualTime.get( Calendar.MILLISECOND ) );
        long millis = actualTime.getTimeInMillis();
        if( doDebugTimes ) {
            LOGGER.debug( "Start 0 -> Zeit: " + printTime( actualTime ) + " ms " + millis );
        }

        LocalDate nowDate = recvDt.toLocalDate();//ldt.toLocalDate();

        LocalTime startOfTime = LocalTime.of( 0, 0, 0, 0 );
        LocalTimeRef timeRef = new LocalTimeRef( startOfTime );
        RecvRawDate rawDate = new RecvRawDate();

        int bytesConsumed = 0;
        BoolRef tlsTimeIsDst = new BoolRef( false );
        GregorianCalendar calRes;

        if( unixTime ) {
            calRes = new GregorianCalendar();
            bytesConsumed += handleUnixTime( calRes, data, ofs, bytesConsumed );

            return new TimeItem( name, calRes.getTime(), bytesConsumed );
        }

        CorrectionModuleslRef mod = new CorrectionModuleslRef( CorrectionModules.INIT );

        bytesConsumed = getTlsTimestampVector( data, ofs, timeRef, rawDate, bytesConsumed, tlsTimeIsDst, mod );

        int yearOfConcern = recvDt.getYear();

        LOGGER.debug( "tsVec: {}T{} DST {}", rawDate.toString(), timeRef.getVal().toString(), tlsTimeIsDst.isTrue() );
//            cal.set( Calendar.DST_OFFSET, dstoff );

        if( mod.getVal() == CorrectionModules.MONTH ) { // time specified up to day inclusively ~ Typ 31
            return getTimeItem31( actualTimeInSecs, yearOfConcern, nowDate, timeRef, rawDate, bytesConsumed, tlsTimeIsDst );
        }

        if( mod.getVal() == CorrectionModules.DAY ) { // time specified up to hour inclusively ~ Typ 30
            return getTimeItem30( actualTimeInSecs, nowDate, timeRef, bytesConsumed, tlsTimeIsDst, yearOfConcern );
        }

        if( mod.getVal() != CorrectionModules.NOTREQUIRED ) {
            throw new IllegalStateException( "type of timestamp not expected" );
        }

        // fully qualified timestamp here
        return getTimeItemFullyQualified( timeRef, rawDate, bytesConsumed, tlsTimeIsDst );
    }

    private TimeItem getTimeItemFullyQualified( LocalTimeRef timeRef, RecvRawDate rawDate, int bytesConsumed, BoolRef tlsTimeIsDst ) {
        // fully qualified date here
        LocalTime inDayVec = timeRef.getVal();
        LocalDate date = LocalDate.of( rawDate.year, rawDate.mon, rawDate.day  );

        LocalDateTime dt = LocalDateTime.of( date, inDayVec );

        ZonedDateTime dW = ZonedDateTime.of( rawDate.year, 1, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset winterOffset = dW.getOffset();

        ZonedDateTime dS = ZonedDateTime.of( rawDate.year, 7, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset summerOffset = dS.getOffset();

        ZonedDateTime zdtRes;
        if ( tlsTimeIsDst.isTrue()) {
            zdtRes = ZonedDateTime.ofLocal(  dt, zoneId, summerOffset );
        } else {
            zdtRes = ZonedDateTime.ofLocal(  dt, zoneId, winterOffset );
        }

        GregorianCalendar dateResOut = new GregorianCalendar( tzUTC );
        dateResOut.setTimeInMillis( zdtRes.toEpochSecond() * 1000 );

        return new TimeItem( name, dateResOut.getTime(), bytesConsumed );
    }

    private int getTlsTimestampVector( byte[] data, int ofs, LocalTimeRef timeRef, RecvRawDate rawDate, int bytesConsumed, BoolRef tlsTimeIsDst, CorrectionModuleslRef mod ) {
        LOGGER.debug( "Time in " + Helper.hexdump( data ) );
        for( String letter : spec ) {
            if( ofs + bytesConsumed >= data.length ) {
                throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
            }
            int value = data[ofs + bytesConsumed];
            ++bytesConsumed;
            bytesConsumed = handleTimeFormatCharacter( data, ofs, timeRef, bytesConsumed, tlsTimeIsDst, letter, value, mod, rawDate );
        }
        return bytesConsumed;
    }

    private TimeItem getTimeItem31( long actualTimeInSecs, int yearOfConcern, LocalDate nowDate, LocalTimeRef timeRef, RecvRawDate rawDate, int bytesConsumed, BoolRef tlsTimeIsDst ) {
        LocalDate monthStart = nowDate.withDayOfMonth( 1 );
        LocalDate prevMonthStart = monthStart.minusMonths( 1 );
        LocalDate nextMonthStart = monthStart.plusMonths( 1 );

        LocalTime inDayVec = timeRef.getVal();
        LocalDate dateRes = monthStart.plusDays( rawDate.day - 1 );
        LocalDate dateResPrev = prevMonthStart.plusDays( rawDate.day - 1 );
        LocalDate dateResNext = nextMonthStart.plusDays( rawDate.day - 1 );

        ZonedDateTime dW = ZonedDateTime.of( yearOfConcern, 1, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset winterOffset = dW.getOffset();

        ZonedDateTime dS = ZonedDateTime.of( yearOfConcern, 7, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset summerOffset = dS.getOffset();

//        LocalDateTime dt = LocalDateTime.of( dateRes, inDayVec );
        ZonedDateTime zdtCurMonth;
        ZonedDateTime zdtPrevMonth;
        ZonedDateTime zdtNextMonth;

        if ( tlsTimeIsDst.isTrue()) {
            zdtCurMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateRes, inDayVec ), zoneId, summerOffset );
            zdtPrevMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateResPrev, inDayVec ), zoneId, summerOffset );
            zdtNextMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateResNext, inDayVec ), zoneId, summerOffset );
            LOGGER.trace( "summer offset" );
        } else {
            zdtCurMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateRes, inDayVec ), zoneId, winterOffset );
            zdtPrevMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateResPrev, inDayVec ), zoneId, winterOffset );
            zdtNextMonth = ZonedDateTime.ofLocal( LocalDateTime.of( dateResNext, inDayVec ), zoneId, winterOffset );
            LOGGER.trace( "winter offset" );
        }

        long dateResSecs = zdtCurMonth.toEpochSecond();
        long dateResPrevSecs = zdtPrevMonth.toEpochSecond();
        long dateResNextSecs = zdtNextMonth.toEpochSecond();

        LOGGER.debug( "pms {} / ms {} / in {} nms {}", dateResPrevSecs, dateResSecs, actualTimeInSecs, dateResNextSecs );
        GregorianCalendar dateResOut = new GregorianCalendar( tzUTC );
        if( Math.abs( dateResSecs - actualTimeInSecs ) < Math.abs( dateResPrevSecs - actualTimeInSecs ) ) {
            if ( Math.abs( dateResSecs - actualTimeInSecs ) < Math.abs( dateResNextSecs - actualTimeInSecs ) ) {
                if( dateResSecs > actualTimeInSecs + timetolerance ) { // TODO make counter for that
                    dateResOut.setTimeInMillis( dateResPrevSecs * 1000 ); // move timestamp to yesterday
                } else {
                    dateResOut.setTimeInMillis( dateResSecs * 1000 );
                }
                LOGGER.debug( "current month" );
            } else { // date of next mont is nearest
                if( dateResNextSecs > actualTimeInSecs + timetolerance ) { // TODO make counter for that
                    dateResOut.setTimeInMillis( dateResSecs * 1000 ); // move timestamp to yesterday
                } else {
                    dateResOut.setTimeInMillis( dateResNextSecs * 1000 );
                }
                LOGGER.debug( "next month" );
            }
        } else {
            dateResOut.setTimeInMillis( dateResPrevSecs * 1000 );
            LOGGER.debug( "previous month" );
        }
        return new TimeItem( name, dateResOut.getTime(), bytesConsumed );
    }

//    private static void checkTimeCorrectionBack( GregorianCalendar cal, GregorianCalendar calCompare, CorrectionModuleslRef mod, List<String> spec, BoolRef haveDay, IntRef daytRef ) {
//            if ( cal.getTimeInMillis() > ( calCompare.getTimeInMillis() + ( timetolerance * 1000 ) ) ) {
//                // evaluated timestamp is in the future
//                // we need to correct it back in time
//            if ( CorrectionModules.INIT == mod.getVal() ) {
//                // should not happen
//                LOGGER.error( "Unexpected strange timeformat {}. Correction has state INIT!", spec );
//
//                String msg = String.format( "Unexpected strange timeformat %s. Correction has state INIT!", spec.toString() );
//
//                if ( null != smm ) {
//                    smm.sendMessage( msg );
//                }
//            }
//            if ( CorrectionModules.DAY == mod.getVal()) {
//                cal.add( Calendar.DAY_OF_MONTH, -1 );
//            }
//            if ( CorrectionModules.MONTH == mod.getVal()) {
//                // one 'month' back
//                LOGGER.debug( "Reduce month by one from: " + cal.get( Calendar.MONTH ) );
//                cal.add( Calendar.MONTH, -1 );
//                // another abnormal situation: 31.2.<Y> -> 3.3.<Y>. this means one month back is not enough
//                if ( cal.getTimeInMillis() > ( calCompare.getTimeInMillis() + ( timetolerance * 1000 ) ) ) {
//                    cal.add( Calendar.MONTH, -1 );
//                }
//            }
//
//            // there may be an abnormal situation: e.g. receive data at 01.04.<Y> 00:00:01 for 31.03.<Y> 23:59:00, i.e. including day(!)
//            // this will end up here as 01.05.<Y> since the mix in of 31., illegal for april!, into april leads to first may : 01.05.<Y> 23:59:00
//            // at this place it is assumed to have the day of data above as the same day in the grgorian calendar (well most of the time)
//            if ( haveDay.isTrue() && ( cal.get( Calendar.DAY_OF_MONTH ) != daytRef.getInt() ) ) {
//                // timestamps may not be (far) in the future
//                // since 31.04. -> 01.05. is >plus one day< one day is subtracted here to correct
//                // after correction of month above again apply the month
//                cal.set( Calendar.DAY_OF_MONTH, daytRef.getInt() );
//                int m1 = calCompare.get( Calendar.MONTH );
//                int m2 = cal.get( Calendar.MONTH );
//                // some last absurdities
//                if (m2 > m1) { // date may not be in the future
//                    cal.add( Calendar.MONTH, -1 );
//                }
//                LOGGER.debug( "Crrected abnormal: " + printTime( cal ) );
//            }
//        }
//    }

    // @formatter:off
    private TimeItem getTimeItem30(
             long actualTimeInSecs,
             LocalDate nowDate,
             LocalTimeRef timeRef,
             int bytesConsumed,
             BoolRef tlsTimeIsDst,
             int yearOfConcern
    ) { // @formatter:on
        LocalTime inDayVec = timeRef.getVal();
        LocalDate dateRes = nowDate.plusDays( 0 ); // make a copy
        LocalDate dateResPrev = nowDate.minusDays( 1 );
        LocalDate dateResNext = nowDate.plusDays( 1 );

        ZonedDateTime dW = ZonedDateTime.of( yearOfConcern, 1, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset winterOffset = dW.getOffset();

        ZonedDateTime dS = ZonedDateTime.of( yearOfConcern, 7, 1, 0, 0, 1, 0, zoneId );
        ZoneOffset summerOffset = dS.getOffset();

        ZonedDateTime zdtToday;
        ZonedDateTime zdtYesterday;
        ZonedDateTime zdtTomorrow;

        if ( tlsTimeIsDst.isTrue()) {
            zdtToday = ZonedDateTime.ofLocal( LocalDateTime.of( dateRes, inDayVec ), zoneId, summerOffset );
            zdtYesterday = ZonedDateTime.ofLocal( LocalDateTime.of( dateResPrev, inDayVec ), zoneId, summerOffset );
            zdtTomorrow = ZonedDateTime.ofLocal( LocalDateTime.of( dateResNext, inDayVec ), zoneId, summerOffset );
            LOGGER.trace( "summer offset" );
        } else {
            zdtToday = ZonedDateTime.ofLocal( LocalDateTime.of( dateRes, inDayVec ), zoneId, winterOffset );
            zdtYesterday = ZonedDateTime.ofLocal( LocalDateTime.of( dateResPrev, inDayVec ), zoneId, winterOffset );
            zdtTomorrow = ZonedDateTime.ofLocal( LocalDateTime.of( dateResNext, inDayVec ), zoneId, winterOffset );
            LOGGER.trace( "winter offset" );
        }

        long dateResSecs = zdtToday.toEpochSecond();
        long dateResPrevSecs = zdtYesterday.toEpochSecond();
        long dateResNextSecs = zdtTomorrow.toEpochSecond();

        LOGGER.trace( "pds {} / ms {} / in {} / nds {}", dateResPrevSecs, dateResSecs, actualTimeInSecs, dateResNextSecs );
        GregorianCalendar dateResOut = new GregorianCalendar( tzUTC );

        // lets get closer to the current moment
        if( Math.abs( dateResSecs - actualTimeInSecs ) < Math.abs( dateResPrevSecs - actualTimeInSecs ) ) {
            if ( Math.abs( dateResSecs - actualTimeInSecs ) < Math.abs( dateResNextSecs - actualTimeInSecs ) ) {
                if( dateResSecs > actualTimeInSecs + timetolerance ) { // TODO make counter for that
                    dateResOut.setTimeInMillis( dateResPrevSecs * 1000 ); // move timestamp to yesterday
                    LOGGER.trace( "t30 far future" );
                } else {
                    dateResOut.setTimeInMillis( dateResSecs * 1000 );
                    LOGGER.trace( "t30 today" );
                }
            } else { // date of next mont is nearest
                if( dateResNextSecs > actualTimeInSecs + timetolerance ) { // TODO make counter for that
                    dateResOut.setTimeInMillis( dateResSecs * 1000 ); // move timestamp to yesterday
                    LOGGER.trace( "t30 today/next is future" );
                } else {
                    dateResOut.setTimeInMillis( dateResNextSecs * 1000 );
                    LOGGER.trace( "t30 next day" );
                }
            }
        } else {
            dateResOut.setTimeInMillis( dateResPrevSecs * 1000 );
            LOGGER.trace( "t30 previous day" );
        }

        return new TimeItem( name, dateResOut.getTime(), bytesConsumed );
    }

    private int handleTimeFormatCharacter( byte[] data, int ofs, LocalTimeRef tsVecRef, int i, BoolRef tlsTimeIsDst, // NOSONAR parameters are needed
                                           String letter, int value, CorrectionModuleslRef mod, RecvRawDate rawDate ) {
        switch ( letter ) {
            case "z":
                // avoid 'set bug' in gregorian calendar
                // use add -value instead of setting to zero
                tsVecRef.setVal( LocalTime.of(  0, 0, 0, 0  ) );
                i = i - 1;
                break;
            case "c":
                rangeCheck( value, 0, 99, "Century is out of range" );
                value *= 100;
                rawDate.year = ( rawDate.year % 100 ) + value;
                break;
            case "y":
                rangeCheck( value, 0, 99, "Year (y) is out of range" );
                int century = ( rawDate.year / 100 ) * 100;
                if ( 0 == century ) {
                    century = 2000;
                }
                rawDate.year = value + century;
                mod.setB( CorrectionModules.NOTREQUIRED ); // need no check, max value
                break;
            case "Y":
                if ( ofs + i >= data.length ) {
                    throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
                }
                value = getUnsignedIntFromInt( value ) + ( data[ofs + i] << 8 );
                ++i;
                rangeCheck( value, 1970, 2200, "Year (Y) is out of range" );
                rawDate.year = value;
                mod.setB( CorrectionModules.NOTREQUIRED ); // need no check, max value
                break;
            case "M":
                rangeCheck( value, 1, 12, "Month is out of range" );
                rawDate.mon = value;
                break;
            case "d":
                rangeCheck( value, 1, 31, "Day is out of range" );
                rawDate.day = value;
                if ( mod.getVal().ordinal() < CorrectionModules.MONTH.ordinal() ) {
                    mod.setB( CorrectionModules.MONTH );
                }
                break;
            case "h":
                value = getUnsignedIntFromInt( value );
                if ( value >= 128 ) {
                    tlsTimeIsDst.setValue( true );
                    value -= 128;
                }
                rangeCheck( value, 0, 23, "Hour is out of range" );
                tsVecRef.setVal( tsVecRef.getVal().withHour( value ) );
                if ( mod.getVal().ordinal() < CorrectionModules.DAY.ordinal() ) {
                    mod.setB( CorrectionModules.DAY );
                }
                break;
            case "m":
                rangeCheck( value, 0, 59, "Minute is out of range" );
                tsVecRef.setVal( tsVecRef.getVal().withMinute( value ) );
                break;
            case "s":
                rangeCheck( value, 0, 59, "Second is out of range" );
                tsVecRef.setVal( tsVecRef.getVal().withSecond( value ) );
                break;
            default:
                throw new IllegalArgumentException( "Illegal time format character: " + letter );
        }
        return i;
    }

    private static String printTime( GregorianCalendar cal ) {
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

    private void rangeCheck( final int value, int allowedLow, final int allowedHigh, String failureText ) {
        if ( value < allowedLow || value > allowedHigh ) {
            throw new IllegalArgumentException( failureText );
        }
    }

    private int handleUnixTime( GregorianCalendar calRes, byte[] data, int ofs, int i ) {
        int value;
        if ( ofs + i >= data.length ) {
            throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
        }
        value = getUnsignedIntFromByte( data[ofs + i] );
        long secs = value;
        ++i;
        if ( ofs + i >= data.length ) {
            throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
        }
        value = getUnsignedIntFromByte( data[ofs + i] );
        secs = 256 * secs + value;
        ++i;
        if ( ofs + i >= data.length ) {
            throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
        }
        value = getUnsignedIntFromByte( data[ofs + i] );
        secs = 256 * secs + value;
        ++i;
        if ( ofs + i >= data.length ) {
            throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
        }
        value = getUnsignedIntFromByte( data[ofs + i] );
        secs = 256 * secs + value;

        calRes.setTimeInMillis( secs * 1000 );

        SimpleDateFormat fmt = new SimpleDateFormat( STD_TIMEFMT_SHORT );
        fmt.setTimeZone( calRes.getTimeZone() );
        LOGGER.debug( "Zeit: " + fmt.format( calRes.getTime() ) );
        return 4;
    }

    @Override
    public void prepareType( String name, Map<String, DataItem.DataItemType > typeMap ) {
        // NOSONAR nothing to do here
        resType = DataItem.DataItemType.GREGORIAN;
    }

    @Override
    public String getTargetType() {
        return targetType;
    }
}
