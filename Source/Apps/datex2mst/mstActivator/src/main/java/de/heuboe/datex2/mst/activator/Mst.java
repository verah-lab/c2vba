package de.heuboe.datex2.mst.activator;

import java.util.Date;

public class Mst
{
	private Date creationTime;
	private String mstId;
	private String mstVersion;
	private String defVersion;
	private boolean active;
	
	public Date getCreationTime()
	{
		return creationTime;
	}
	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}
	public String getMstId()
	{
		return mstId;
	}
	public void setMstId(String mstId)
	{
		this.mstId = mstId;
	}
	public String getMstVersion()
	{
		return mstVersion;
	}
	public void setMstVersion(String mstVersion)
	{
		this.mstVersion = mstVersion;
	}
	public String getDefVersion()
	{
		return defVersion;
	}
	public void setDefVersion(String defVersion)
	{
		this.defVersion = defVersion;
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
