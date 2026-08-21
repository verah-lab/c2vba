package de.heuboe.tls.receiver.rdr.getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.item.FloatItem;

public class FloatGetter extends AbstractNumberGetter {

    private static boolean haveInvalidReplacement = false;
    private static double  invalidReplacement     = -99999; // this should be used if everyone is informed and ready
    
    private static byte[] inv = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

    /**
     * Allow a value to be set which indicates that floating point values are invalid
     * 
     * @param invalidValue
     *            string representing the invalid value. Will be parsed by Double.parseDouble.
     */
    public static void setFloatInvalid( String invalidValue ) {
        if ( null == invalidValue || ( null != invalidValue && invalidValue.trim().length() == 0 ) ) { // NOSONAR stress non-null value of invalidValue
            return;
        }
        invalidReplacement = Double.parseDouble( invalidValue );
        haveInvalidReplacement = true;
    }

    public FloatGetter( String name, Expression expr, FunctionAbstract func, String targetType ) {
        super( name, expr, func, targetType );
    }

    @Override
    public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
        if ( ofs + 4 > data.length ) {
            throw new IllegalArgumentException( "DeBlock too short" );
        }
        double value = ByteBuffer.wrap( data, ofs, 4 ).order( ByteOrder.LITTLE_ENDIAN ).getFloat();
        if ( ( data[ ofs + 0] == inv[0]
               && data[ ofs + 1] == inv[1]
               && data[ ofs + 2] == inv[2]
               && data[ ofs + 3] == inv[3] )
             || Double.isNaN( value ) ) {
            if (haveInvalidReplacement) {
                value = invalidReplacement;
            } else {
                value = Float.MIN_VALUE;
            }
        }
        DataItem soFar = new FloatItem( name, value, 4 );
        return handleCalculations( soFar, etelVars );
    }

    @Override
    public void prepareType( String name, Map<String, DataItemType> typeMap ) {
        resType = DataItemType.FLOAT;
        basePrepareType( name, typeMap );
    }
}
