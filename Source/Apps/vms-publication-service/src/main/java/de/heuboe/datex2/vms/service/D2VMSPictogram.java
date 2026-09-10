package de.heuboe.datex2.vms.service;

import de.heuboe.datex2.vms.service.uz.Util;

public class D2VMSPictogram implements Equality
{
	private String imageFilePath;
	private String code;
	private String description;
	
	private Integer distanceAttribute;				// metres
	private Double heightAttribute;					// metres
	private Double lengthAttribute;					// metres
	private Double speedAttribute;					// km/h
	private Double weightAttribute;					// tonnes
	private Double widthAttributes;					// metres
	
	public String getImageFilePath()
	{
		return imageFilePath;
	}
	public void setImageFilePath(String imageFilePath)
	{
		this.imageFilePath = imageFilePath;
	}
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public Integer getDistanceAttribute()
	{
		return distanceAttribute;
	}
	public void setDistanceAttribute(Integer distanceAttribute)
	{
		this.distanceAttribute = distanceAttribute;
	}
	public Double getHeightAttribute()
	{
		return heightAttribute;
	}
	public void setHeightAttribute(Double heightAttribute)
	{
		this.heightAttribute = heightAttribute;
	}
	public Double getLengthAttribute()
	{
		return lengthAttribute;
	}
	public void setLengthAttribute(Double lengthAttribute)
	{
		this.lengthAttribute = lengthAttribute;
	}
	public Double getSpeedAttribute()
	{
		return speedAttribute;
	}
	public void setSpeedAttribute(Double speedAttribute)
	{
		this.speedAttribute = speedAttribute;
	}
	public Double getWeightAttribute()
	{
		return weightAttribute;
	}
	public void setWeightAttribute(Double weightAttribute)
	{
		this.weightAttribute = weightAttribute;
	}
	public Double getWidthAttribute()
	{
		return widthAttributes;
	}
	public void setWidthAttribute(Double widthAttributes)
	{
		this.widthAttributes = widthAttributes;
	}
	
	public boolean isEqual( Object obj )
	{
		if( obj == null )
			return false;
		
		if( !(obj instanceof D2VMSPictogram) )
			return false;
		
		D2VMSPictogram p = (D2VMSPictogram)obj;

		return 
				Util.isEqual( imageFilePath, p.imageFilePath ) &&
				Util.isEqual( code, p.code ) &&
				Util.isEqual( description, p.description ) &&
				Util.isEqual( distanceAttribute, p.distanceAttribute ) &&
				Util.isEqual( heightAttribute, p.heightAttribute ) &&
				Util.isEqual( lengthAttribute, p.lengthAttribute ) &&
				Util.isEqual( speedAttribute, p.speedAttribute ) &&
				Util.isEqual( weightAttribute, p.weightAttribute ) &&
				Util.isEqual( widthAttributes, p.widthAttributes );
	}
}
