package de.heuboe.by.c2vba.datex2.mst.localisation.server;

/**
 * 
 * Internal Exception class  
 * 
 * 
 * @author peters
 *
 */
@SuppressWarnings("serial")
public class SvcException extends Exception 
{
	public static final int ERROR_ARG_PARSER 					= 1002;
	public static final int ERROR_INIT 							= 1003;
	public static final int ERROR_DB_WRITE 						= 1005;
	public static final int ERROR_DB_READ 						= 1006;
	public static final int ERROR_D2_INTERNAL 					= 1007;
	public static final int ERROR_INVALID_LOCATION_DEF			= 1010;
	public static final int ERROR_WLS_MFS 						= 1011;
	public static final int ERROR_KAFKA 						= 1012;

	
	private final int errorCode;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param errorCode error number
	 * @param errMsg error message
	 */
	public SvcException( int errorCode, String errMsg )
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
	public SvcException( int errorCode, String errMsg, Throwable cause )
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
	@Override
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
