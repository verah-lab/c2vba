package de.heuboe.datex2.vms.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;


@WebService
public interface VMSDataListener 
{
	public class Identity
	{
		private String name;
		private String address;
		
		public Identity()
		{
			
		}
		
		public Identity( String name, String address )
		{
			this.name = name;
			this.address = address;
		}

		public String getAddress() { return address; }
		public String getName() { return name; }
		public void setAddress( String address ) { this.address = address; } 
		public void setName( String name ) { this.name = name; }
	}
	
	// ping expected each minute
	public static final long PING_INTERVAL = 120L;   
	public static final long PING_INTERVAL_MS = PING_INTERVAL * 1000L;   
	
	@WebMethod
	void ping( String clientId );
	
	@WebMethod
	void error( String reason );

	@WebMethod
	void notifyChange( List<ObjectKey> objIds );
}

