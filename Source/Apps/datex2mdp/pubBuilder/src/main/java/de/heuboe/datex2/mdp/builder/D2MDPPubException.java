package de.heuboe.datex2.mdp.builder;

import de.heuboe.datex2.exception.D2ExceptionBase;

/**
 * Exception class for all mdp builder errors
 */
public class D2MDPPubException extends D2ExceptionBase
{
	private static final long serialVersionUID = 1L;
	
	public static final int D2_ERR_READING_SUPPLY_TABLE 			= 3001;
	public static final int D2_ERR_NO_MST_FOR_READING_SUPPLY_TABLE 	= 3002;
	public static final int D2_ERR_NO_MST_FOR_KEY_FOUND 			= 3003;
	public static final int D2_ERR_MULTIPLE_MST_FOR_KEY_FOUND 		= 3004;
	public static final int D2_ERR_WRITE_TO_FILE					= 3005;
	public static final int D2_ERR_INVALID_ATTR_VALUE				= 3006;
	public static final int D2_ERR_NO_DATA_SOURCE_FOR_ITEM			= 3007;
	public static final int D2_ERR_DDP_EXCEPTION					= 3008;
	public static final int D2_ERR_UNKNOWN_DATAKIND					= 3009;
	public static final int D2_ERR_UNKNOWN_MV_PUB_TEMPLATE			= 3010;
	public static final int D2_ERR_UNKNOWN_VALUE_CALCULATOR			= 3011;
	public static final int D2_ERR_UNKNOWN_EX						= 4001;
	public static final int D2_ERR_READING_PROP_FILE				= 5001;	
	public static final int D2_ERR_INVALID_FUNCTION_DEF				= 6001;

	/**
	 * Constructor
	 *
	 * @param message error message
	 */
	public D2MDPPubException( String message )
	{
		super( message );
	}

	/**
	 * Constructor
	 *
	 * @param message error message
	 * @param cause Throwable
	 */
    public D2MDPPubException( String message, Throwable cause )
    {
	        super(message, cause);
	}

	/**
	 * Constructor
	 *
	 * @param message error message
	 * @param errCode error code, there are fix values defined
	 */
	public D2MDPPubException( int errCode, String message) 
	{
		super(message);
		this.errCode = errCode;
	}

	/**
	 * Constructor
	 *
	 * @param message error message
	 * @param errCode error code, there are fix values defined
	 * @param cause Throwable
	 */
    public D2MDPPubException( int errCode, String message, Throwable cause) 
    {
        super(message, cause);
		this.errCode = errCode;
    }

	@Override
    public String toString()
    {
    	return "D2MDPPubException{ Fehler-Code: " + getErrCode() + " [" + getMessage() + "] }";
    }
}
