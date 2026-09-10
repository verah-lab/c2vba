package de.heuboe.datex2.vms.status.pub.check;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Current data data from Kafka
 * 
 * @author peters
 *
 */
public class DbData
{
	public static class DbDataSet
	{
		private Map<String,DbData> dataSet = new HashMap<>();
		
		public DbDataSet() {
		}
		
		public DbDataSet( Map<String,DbData> dataSet ) {
			this.dataSet = dataSet;
		}
		
		public void add( DbData data )
		{
			dataSet.put( data.getId(), data);
		}
		
		public DbData getData( String id )
		{
			return dataSet.get( id );
		}
	}
	
	private String id;
	private Date time;
	private Date systemTime;
	private int code = -1;
	private boolean error = false;

	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public int getCode()
	{
		return code;
	}
	public void setCode( int code)
	{
		this.code = code;
	}
	public boolean isError()
	{
		return error;
	}
	public void setError(boolean error)
	{
		this.error = error;
	}
	
	public Date getTime()
	{
		return time;
	}
	public void setTime(Date time)
	{
		this.time = time;
	}
	
	public Date getSystemTime()
	{
		return systemTime;
	}
	public void setSystemTime(Date systemTime)
	{
		this.systemTime = systemTime;
	}
}
