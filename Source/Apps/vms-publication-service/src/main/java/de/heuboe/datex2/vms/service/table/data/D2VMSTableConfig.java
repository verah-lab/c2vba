package de.heuboe.datex2.vms.service.table.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class D2VMSTableConfig
{
	private Date time;
	private String description;

	private List<D2VMSTableConfigPart> parts = new ArrayList<>();

	public Date getTime()
	{
		return time;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<D2VMSTableConfigPart> getParts()
	{
		return parts;
	}

	public void setParts(List<D2VMSTableConfigPart> parts)
	{
		this.parts = parts;
	}
}
