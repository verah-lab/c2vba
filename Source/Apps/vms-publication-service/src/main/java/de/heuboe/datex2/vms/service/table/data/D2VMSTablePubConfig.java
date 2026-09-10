package de.heuboe.datex2.vms.service.table.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.heuboe.util.JavaObject2JSON;

public class D2VMSTablePubConfig
{
	private Date time;
	private String description;
	
	private List<D2VMSTablePubConfigPart> details = new ArrayList<>();

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
	
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String configXML() 
	{
		String xml = "";
		for( D2VMSTablePubConfigPart detail : details )
		{
			xml += detail.getXmlPart();
		}
		
		return xml;
	}
}
