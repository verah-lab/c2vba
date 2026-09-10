package de.heuboe.datex2.vms.service;

import java.util.Date;

public class D2VMSError
{
	private D2VMSErrorType type = D2VMSErrorType.Unknown;
	private String errorCode;
	private String description;
	private Date creationTime;
	private Date lastUpdateTime = new Date();
	
	
	public D2VMSErrorType getType()
	{
		return type;
	}
	public void setType(D2VMSErrorType type)
	{
		this.type = type;
	}
	public String getErrorCode()
	{
		return errorCode;
	}
	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public Date getCreationTime()
	{
		return creationTime;
	}
	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}
	public Date getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	public void setLastUpdateTime(Date lastUpdateTime)
	{
		this.lastUpdateTime = lastUpdateTime;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		D2VMSError other = (D2VMSError) obj;
		if (creationTime == null)
		{
			if (other.creationTime != null)
				return false;
		}
		else if (!creationTime.equals(other.creationTime))
			return false;
		if (description == null)
		{
			if (other.description != null)
				return false;
		}
		else if (!description.equals(other.description))
			return false;
		if (errorCode == null)
		{
			if (other.errorCode != null)
				return false;
		}
		else if (!errorCode.equals(other.errorCode))
			return false;
		if (lastUpdateTime == null)
		{
			if (other.lastUpdateTime != null)
				return false;
		}
		else if (!lastUpdateTime.equals(other.lastUpdateTime))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
