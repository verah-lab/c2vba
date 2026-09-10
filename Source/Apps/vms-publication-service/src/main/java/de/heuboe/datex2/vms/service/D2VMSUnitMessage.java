package de.heuboe.datex2.vms.service;

import java.util.ArrayList;
import java.util.List;

public class D2VMSUnitMessage
{
	private String					id;
	private List<D2VMSMessage>      vmsMessages = new ArrayList<D2VMSMessage>();
	private D2VMSErrorType			error = null;
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public List<D2VMSMessage> getVmsMessages()
	{
		return vmsMessages;
	}
	public void setVmsMessages(List<D2VMSMessage> vmsMessages)
	{
		this.vmsMessages = vmsMessages;
	}
	public D2VMSErrorType getError()
	{
		return error;
	}
	public void setError(D2VMSErrorType error)
	{
		this.error = error;
	}
}
