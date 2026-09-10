package de.heuboe.srb.datex2.srp;

/**
 * 
 * Exception class  
 * 
 * 
 * @author peters
 *
 */
@SuppressWarnings("serial")
public class SRPException extends Exception 
{
	public static final int ERROR_ARG_PARSER 					= 1002;
    public static final int ERROR_DB                            = 1005;
    public static final int ERROR_XML                           = 1007;
    public static final int ERROR_LOC							= 1008;
    public static final int ERROR_IO							= 1009;
    public static final int ERROR_SVC							= 1010;
	
	private final int errorCode;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param errorCode error number
	 * @param errMsg error message
	 */
	public SRPException( int errorCode, String errMsg )
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
	public SRPException( int errorCode, String errMsg, Throwable cause )
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
