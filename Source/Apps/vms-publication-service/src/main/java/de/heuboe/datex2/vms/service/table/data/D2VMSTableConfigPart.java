package de.heuboe.datex2.vms.service.table.data;

import java.util.Date;

public class D2VMSTableConfigPart
{
	private Date time;
	private int partCount;
	
	private String xmlPart;

	public Date getTime()
	{
		return time;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}

	public int getPartCount()
	{
		return partCount;
	}

	public void setPartCount(int partCount)
	{
		this.partCount = partCount;
	}

	public String getXmlPart()
	{
		return xmlPart;
	}

	public void setXmlPart(String xmlPart)
	{
		this.xmlPart = xmlPart;
	} 

}
