package de.heuboe.tls.receiver.getter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import de.heuboe.log.Logger;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.GregorianItem;
import de.heuboe.tls.receiver.item.TimeItem;

public class TimeGetter extends AbstractGetter {

	private static final Logger LOGGER = Logger.getLogger(TimeGetter.class);
	private static final String DEBLOCK_TOO_SHORT = "DeBlock too short";
	private static final String STD_TIMEFMT_SHORT = "dd.MM.yyyy HH:mm:ss z";
	
	private List<String> spec = new ArrayList<>();
	private boolean dstCorrection = true;
	private boolean unixTime = false;
	
        public TimeGetter( String letters, String name ) {
                super( name );
                if ( letters.startsWith( "UTC>" ) ) {
                        dstCorrection = false;
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
                if (0 > b) {
                        res += 256;
                }
                return res;
        }
        
        private static int getUnsignedIntFromInt( final int i ) {
                int res = i;
                if (0 > i) {
                        res += 256;
                }
                return res;
        }
        
        private static class BoolRef {
                private boolean b;
                BoolRef(boolean init) {
                        this.b = init;
                }
                public boolean isB() {
                        return b;
                }
                public void setB( boolean b ) {
                        this.b = b;
                }
        }

        /*
         * (non-Javadoc)
         * @see de.heuboe.tls.receiver.interfaces.GetterRule#get(byte[], int, java.util.Map)
         * Analyse a sequence of bytes which are meant to be time
         * preset is done to RcvTimeBase (if present) the original time recorded of of data delivered at a later stage
         * if real time analysis (no RcvTimeBase present) is done, preset is made to the timestamp of the construction time of the telegram being analysed
         */
	@Override
        public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
                GregorianCalendar actualTime = etelVars.get( "#ActualTime" ).getAsGregorianCalendar();
                long millis = actualTime.getTimeInMillis();
                if ( null != etelVars.get( "#RcvTimeBase" ) ) {
                        millis = ( (GregorianCalendar) etelVars.get( "#RcvTimeBase" ).getAsGregorianCalendar() ).getTimeInMillis();
                }

                GregorianCalendar cal = new GregorianCalendar(); // wall time
                cal.setTimeInMillis( millis );
                cal.set( GregorianCalendar.MILLISECOND, 0 );
                TimeZone tz = TimeZone.getTimeZone( "UTC" );
                GregorianCalendar cal2 = new GregorianCalendar( tz ); // utc
                cal2.setTimeInMillis( millis );
                cal2.set( GregorianCalendar.MILLISECOND, 0 );
                int i = 0;
                //boolean tlsTimeIsDst = false;
                BoolRef tlsTimeIsDst = new BoolRef( false );
                GregorianCalendar calRes;
                BoolRef haveDay = new BoolRef( false );
                if ( unixTime ) {
                        calRes = new GregorianCalendar();
                        i += handleUnixTime( calRes, data, ofs, i );
                } else {
                        for ( String letter : spec ) {
                                if ( ofs + i >= data.length ) {
                                        throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
                                }
                                int value = data[ofs + i];
                                ++i;
                                i = handleTimeFormatCharacter( data, ofs, cal, cal2, i, tlsTimeIsDst, haveDay, letter, value );
                        }
                        
                        {
                                int d = -1;
                                int h = -1;
                                int m = -1;
                                int s = -1;

                                h = cal.get( Calendar.HOUR_OF_DAY );
                                m = cal.get( Calendar.MINUTE );
                                s = cal.get( Calendar.SECOND );

                                if ( haveDay.isB() ) {
                                        d = cal.get( Calendar.DAY_OF_MONTH );
                                        LOGGER.debug( "time[d:h:m:s] = " + d + ":" + h + ":" + m + ":" + s );
                                } else {
                                        LOGGER.debug( "time[h:m:s] = " + h + ":" + m + ":" + s );
                                }
                        }
                        
                        if ( dstCorrection ) {
                                boolean currentlyInDst = cal.get( Calendar.DST_OFFSET ) != 0;
                                if ( tlsTimeIsDst.isB() && !currentlyInDst ) {
                                        cal.add( Calendar.HOUR_OF_DAY, -1 );
                                }
                                if ( !(tlsTimeIsDst.isB()) && currentlyInDst ) {
                                        cal.add( Calendar.HOUR_OF_DAY, +1 );
                                }
                                SimpleDateFormat fmt = new SimpleDateFormat( STD_TIMEFMT_SHORT );
                                fmt.setTimeZone( cal.getTimeZone() );
                                LOGGER.debug( "Zeit: " + fmt.format( cal.getTime() ) );
                                calRes = cal;
                        } else {
                                SimpleDateFormat fmt = new SimpleDateFormat( STD_TIMEFMT_SHORT );
                                fmt.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
                                LOGGER.debug( "Zeit(UTC): " + fmt.format( cal2.getTime() ) );
                                calRes = cal2;
                        }
                }
                return new TimeItem( name, calRes.getTime(), i );
        }

        private int handleTimeFormatCharacter( byte[] data, int ofs, GregorianCalendar cal, GregorianCalendar cal2, int i, BoolRef tlsTimeIsDst,
                        BoolRef haveDay, String letter, int value ) {
                switch ( letter ) {
                case "z":
                        cal.set( Calendar.HOUR_OF_DAY, 0 );
                        cal.set( Calendar.MINUTE, 0 );
                        cal.set( Calendar.SECOND, 0 );

                        cal2.set( Calendar.HOUR_OF_DAY, 0 );
                        cal2.set( Calendar.MINUTE, 0 );
                        cal2.set( Calendar.SECOND, 0 );
                        i = i - 1;
                        break;
                case "c":
                        rangeCheck( value, 0, 99, "Century is out of range" );
                        value *= 100;
                        int year = cal.get( Calendar.YEAR ) % 100;
                        cal.set( Calendar.YEAR, value + year );
                        year = cal2.get( Calendar.YEAR ) % 100;
                        cal2.set( Calendar.YEAR, value + year );
                        break;
                case "y":
                        rangeCheck( value, 0, 99, "Year is out of range" );
                        int century = (cal.get( Calendar.YEAR ) / 100) * 100;
                        cal.set( Calendar.YEAR, value + century );
                        century = (cal2.get( Calendar.YEAR ) / 100) * 100;
                        cal2.set( Calendar.YEAR, value + century );
                        break;
                case "Y":
                        if ( ofs + i >= data.length ) {
                                throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
                        }
                        value = getUnsignedIntFromInt(value) + ( data[ofs + i] << 8 );
                        ++i;
                        rangeCheck( value, 0, 9999, "Year is out of range" );
                        cal.set( Calendar.YEAR, value );
                        cal2.set( Calendar.YEAR, value );
                        break;
                case "M":
                        rangeCheck( value, 1, 12, "Month is out of range" );
                        cal.set( Calendar.MONTH, value - 1 );
                        cal2.set( Calendar.MONTH, value - 1 );
                        break;
                case "d":
                        rangeCheck( value, 1, 31, "Day is out of range" );
                        cal.set( Calendar.DAY_OF_MONTH, value );
                        cal2.set( Calendar.DAY_OF_MONTH, value );
                        haveDay.setB( true );
                        break;
                case "h":
                        value = getUnsignedIntFromInt(value);
                        if ( value >= 128 ) {
                                tlsTimeIsDst.setB( true );
                                value -= 128;
                        }
                        rangeCheck( value, 0, 23, "Hour is out of range" );
                        cal.set( Calendar.HOUR_OF_DAY, value );
                        cal2.set( Calendar.HOUR_OF_DAY, value );
                        break;
                case "m":
                        rangeCheck( value, 0, 59, "Minute is out of range" );
                        cal.set( Calendar.MINUTE, value );
                        cal2.set( Calendar.MINUTE, value );
                        break;
                case "s":
                        rangeCheck( value, 0, 59, "Second is out of range" );
                        cal.set( Calendar.SECOND, value );
                        cal2.set( Calendar.SECOND, value );
                        break;
                default:
                        throw new IllegalArgumentException( "Illegal time format character: " + letter );
                }
                return i;
        }

        private void rangeCheck( final int value, int allowedLow, final int allowedHigh, String failureText ) {
                if ( value < allowedLow || value > allowedHigh ) {
                        throw new IllegalArgumentException( failureText );
                }
        }

        private int handleUnixTime( GregorianCalendar calRes, byte[] data, int ofs, int i ) {
                int value;
                long secs = 0;
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
                ++i;
                if ( ofs + i >= data.length ) {
                        throw new IllegalArgumentException( DEBLOCK_TOO_SHORT );
                }
                value = getUnsignedIntFromByte( data[ofs + i] );
                secs = 256 * secs + value;
                ++i;

                calRes.setTimeInMillis( secs * 1000 );

                SimpleDateFormat fmt = new SimpleDateFormat( STD_TIMEFMT_SHORT );
                fmt.setTimeZone( calRes.getTimeZone() );
                LOGGER.debug( "Zeit: " + fmt.format( calRes.getTime() ) );
                return 4;
        }
}
