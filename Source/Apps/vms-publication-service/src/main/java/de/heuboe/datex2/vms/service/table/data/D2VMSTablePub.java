package de.heuboe.datex2.vms.service.table.data;

import java.util.Date;


import de.heuboe.util.JavaObject2JSON;

public class D2VMSTablePub
{
	private Date time;
	private String d2Id;
	private String d2Version;
	private boolean active;
	
	public Date getTime() 
	{
		return this.time;
	}
	
	public void setTime(Date time) 
	{
		this.time = time;
	}
	
	
	public String getD2Id()
	{
		return d2Id;
	}

	public void setD2Id(String d2Id)
	{
		this.d2Id = d2Id;
	}

	public String getD2Version()
	{
		return d2Version;
	}

	public void setD2Version(String d2Version)
	{
		this.d2Version = d2Version;
	}

	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}
	
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	
}
