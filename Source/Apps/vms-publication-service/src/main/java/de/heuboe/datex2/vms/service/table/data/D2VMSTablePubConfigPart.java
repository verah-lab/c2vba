package de.heuboe.datex2.vms.service.table.data;

import java.util.Date;

import de.heuboe.util.JavaObject2JSON;

public class D2VMSTablePubConfigPart
{
	private Date time;
	private int partCount = 0;
	private String xmlPart;
	
	public String getXmlPart()
	{
		return xmlPart;
	}

	public void setXmlPart(String xmlPart)
	{
		this.xmlPart = xmlPart;
	}

	
	public Date getTime() 
	{
		return this.time;
	}
	
	public void setTime(Date time) 
	{
		this.time = time;
	}
	
	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}
	
	public int getPartCount()
	{
		return partCount;
	}

	public void setPartCount(int partCount)
	{
		this.partCount = partCount;
	}

	
}
