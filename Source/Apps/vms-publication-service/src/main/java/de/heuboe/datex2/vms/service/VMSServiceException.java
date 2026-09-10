package de.heuboe.datex2.vms.service;

public class VMSServiceException extends Exception
{
	public static final int VMS_SVC_ERR_CONN_WLS	 		= 1001;
	public static final int VMS_SVC_ERR_READ_INFRA 			= 1002;
	public static final int VMS_SVC_ERR_IMAGE 				= 1003;
	public static final int VMS_SVC_ERR_WLS 				= 1004;
	public static final int VMS_SVC_ERR_DATA_LISTENER		= 1005;
	public static final int VMS_SVC_ERR_UNKNOWN_TABLE_ID	= 1006;
	public static final int VMS_SVC_ERR_DB_INIT 			= 1007;
	public static final int VMS_SVC_ERR_DB_SERIALIZE_CFG	= 1008;
	public static final int VMS_SVC_ERR_PARAM				= 1009;
	public static final int VMS_SVC_ERR_IMAGE_SVC_INIT		= 1010;
	public static final int VMS_SVC_ERR_CFG					= 1011;
	
	private int errId = -1;
	
	private static final long serialVersionUID = 1L;
	
    public VMSServiceException() 
    {
        super();
    }
    
    public VMSServiceException(String message) 
    {
        super(message);
    }
    
    public VMSServiceException(String message, Throwable cause) 
    {
        super(message, cause);
    }
    
    public VMSServiceException( int errId, String message ) 
    {
        super(message);
        this.errId = errId;
    }
    
    public VMSServiceException( int errId, String message, Throwable cause) 
    {
        super( message, cause);
        this.errId = errId;
    }
    
    public VMSServiceException(Throwable cause) 
    {
        super(cause);
    }
    
    public int getErrId()
    {
    	return errId;
    }
}
