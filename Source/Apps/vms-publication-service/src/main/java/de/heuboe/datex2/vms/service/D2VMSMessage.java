package de.heuboe.datex2.vms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.heuboe.datex2.vms.service.uz.Util;

public class D2VMSMessage implements Equality
{
	private String					objType;
	private String					objId;
	private OperationCodeType		operationCodeType = OperationCodeType.StandardTLS;
	private String					operationCode = null;
	private String					text = null;
	private String 					reasonForSetting;
	private D2VMSOperationReason	operationReason = null;
	private Date					timeLastSet = null;
	private D2VMSError				error = null;
	private List<D2VMSPictogram> 	pictograms = new ArrayList<>(); 
	
	
	public String getReasonForSetting()
	{
		return reasonForSetting;
	}
	public void setReasonForSetting( String reasonForSetting )
	{
		this.reasonForSetting = reasonForSetting;
	}
	
	public String getObjType()
	{
		return objType;
	}
	public void setObjType( String objType )
	{
		this.objType = objType;
	}
	public String getObjId()
	{
		return objId;
	}
	public void setObjId( String objId )
	{
		this.objId = objId;
	}
	
	public ObjectKey getObjectKey()
	{
		return new ObjectKey( objType, objId );
	}
	
	
	public Date getTimeLastSet()
	{
		return timeLastSet;
	}
	public void setTimeLastSet(Date timeLastSet)
	{
		this.timeLastSet = timeLastSet;
	}
	
	public List<D2VMSPictogram> getPictograms()
	{
		return pictograms;
	}
	public void setPictograms(List<D2VMSPictogram> pictograms)
	{
		this.pictograms = pictograms;
	}
	public void addPictograms( D2VMSPictogram...  pictograms )
	{
		this.pictograms.addAll( Arrays.asList( pictograms ) );
	}
	
	public D2VMSError getError()
	{
		return error;
	}
	public void setError(D2VMSError error)
	{
		this.error = error;
	}

	public String getOperationCode()
	{
		return operationCode;
	}
	public void setOperationCode( String operationCode )
	{
		this.operationCode = operationCode;
	}
	
	public D2VMSOperationReason getOperationReason()
	{
		return operationReason;
	}
	public void setOperationReason( D2VMSOperationReason operationReason )
	{
		this.operationReason = operationReason;
	}
	
	public OperationCodeType getOperationCodeType()
	{
		return operationCodeType;
	}
	public void setOperationCodeType( OperationCodeType operationCodeType )
	{
		this.operationCodeType = operationCodeType;
	}
	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	
	public boolean isEqual( Object obj )
	{
		if( obj == null )
			return false;
		
		if( !(obj instanceof D2VMSMessage) )
			return false;
		
		D2VMSMessage msg = (D2VMSMessage)obj;

		return
				Util.isEqual( objType, msg.objType ) &&
				Util.isEqual( objId, msg.objId ) &&
				Util.isEqual( operationCodeType, msg.operationCodeType ) &&
				Util.isEqual( operationCode, msg.operationCode ) &&
				Util.isEqual( text, msg.text ) &&
				Util.isEqual( reasonForSetting, msg.reasonForSetting ) &&
				Util.isEqual( operationReason, msg.operationReason ) &&
				Util.isEqual( timeLastSet, msg.timeLastSet ) &&
				Util.isEqual( error, msg.error ) /*&&
				Util.isEqual( pictograms, msg.pictograms )*/;
	}
	
	public void update( D2VMSMessage message )
	{
		operationCodeType = message.operationCodeType;
		operationCode = message.operationCode;
		text = message.text;
		operationReason= message.operationReason;
		reasonForSetting= message.reasonForSetting;
		timeLastSet = message.getTimeLastSet();
		error = message.getError();
		pictograms = message.getPictograms();
	}
}
