package de.heuboe.c2vba.datex2.kafka;

/**
 * 
 * Exception class  
 * 
 * 
 * @author peters
 *
 */
@SuppressWarnings("serial")
public class C2VbaException extends Exception 
{
	public static final int ERROR_REFLECTION 					= 1002;
	public static final int ERROR_CONVERSION 					= 1003;
	public static final int ERROR_SENDING 					    = 1004;
	
	private final int errorCode;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param errorCode error number
	 * @param errMsg error message
	 */
	public C2VbaException( int errorCode, String errMsg )
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
	public C2VbaException( int errorCode, String errMsg, Throwable cause )
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
