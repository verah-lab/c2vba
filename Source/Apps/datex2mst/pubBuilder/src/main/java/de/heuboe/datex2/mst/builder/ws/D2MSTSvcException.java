package de.heuboe.datex2.mst.builder.ws;


public class D2MSTSvcException extends Exception
{
	private static final long serialVersionUID = 1L;

	public D2MSTSvcException(String message) 
	{
		super(message);
	}

    public D2MSTSvcException(String message, Throwable cause) 
    {
	    super(message, cause);
	}
}
