package de.heuboe.datex2.location.service.server;

/**
 * 
 * Exception class  
 * 
 * 
 * @author peters
 *
 */
@SuppressWarnings("serial")
public class LSBException extends Exception 
{
	public static final int ERROR_ARG_PARSER 					= 1002;
	public static final int ERROR_DB_WRITE 						= 1005;
	public static final int ERROR_DB_READ 						= 1006;
	public static final int ERROR_D2_INTERNAL 					= 1007;
	public static final int ERROR_LOC_SERVICE_ERROR				= 1009;
	public static final int ERROR_NO_LOCATION					= 1011;
	public static final int ERROR_MST_CONFIG					= 1012;
	public static final int ERROR_MST_DEFINITION				= 1013;
	public static final int ERROR_XML							= 1015;

	
	private int errorCode;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param errorCode error number
	 * @param errMsg error message
	 */
	public LSBException( int errorCode, String errMsg )
	{
		super( errMsg );
		this.errorCode = errorCode;
	}
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param errorCode error number
	 * @param errMsg error message
	 * @param cause causing exception
	 */
	public LSBException( int errorCode, String errMsg, Throwable cause )
	{
		super( errMsg, cause );
		this.errorCode = errorCode;
	}

	/**
	 * 
	 * Return textual representation 
	 * 
	 * @return textual representation
	 */
	public String toString()
	{
		return "Error " + errorCode + ": {" + getMessage() + "}";
	}
	
	/**
	 * 
	 * Returns error number
	 * 
	 * @return error number
	 */
	public int getErrorCode()
	{
		return this.errorCode;
	}
}
