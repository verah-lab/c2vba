package de.heuboe.datex2.mst.builder;

import de.heuboe.datex2.exception.D2ExceptionBase;

/**
 * 
 * Exception class
 * 
 * @author peters
 *
 */
public class D2MSTPubException extends D2ExceptionBase
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8518651012371672669L;
	
	public static final int D2_ERR_READING_SUPPLY_TABLE 			= 1001;
	public static final int D2_ERR_NO_MST_FOR_READING_SUPPLY_TABLE 	= 1002;
	public static final int D2_ERR_NO_MST_FOR_KEY_FOUND 			= 1003;
	public static final int D2_ERR_MULTIPLE_MST_FOR_KEY_FOUND 		= 1004;
	public static final int D2_ERR_WRITE_TO_FILE					= 1005;
	public static final int D2_ERR_INVALID_ATTR_VALUE				= 1006;
	public static final int D2_ERR_NO_DATA_SOURCE_FOR_ITEM			= 1007;	
	public static final int D2_ERR_INVALID_PUBLICATION				= 1008;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param message	Error message
	 */
	public D2MSTPubException(String message) 
	{
		super( message );
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param message	Error message
	 * @param cause		Cause as exception
	 */
    public D2MSTPubException(String message, Throwable cause) 
    {
	        super(message, cause);
	}
    
	/**
	 * 
	 * Constructor
	 * 
	 * @param errCode	Error code
	 * @param message	Error message
	 */
	public D2MSTPubException( int errCode, String message) 
	{
		super(message);
		this.errCode = errCode;
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param errCode	Error code
	 * @param message	Error message
	 * @param cause		Cause as exception
	 */
    public D2MSTPubException( int errCode, String message, Throwable cause) 
    {
        super(message, cause);
		this.errCode = errCode;
    }
    
    @Override
    public String toString()
    {
    	return "D2ExceptionMST{ Fehler-Code: " + getErrCode() + " [" + getMessage() + "] }";
    }
}
