package de.heuboe.datex2.vms.service.table.data;

import java.util.Date;

import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayPosition;
import de.heuboe.util.JavaObject2JSON;

public class D2VMSDisplayPart
{
	public static enum DisplayPartType
	{
		ASign(1),
		BSign(2),
		CSign(3),
		DSign(4),
		ESign(5),
		SSign(6),
		Pikt(7),			// dWiSta-Piktogramm
		Text(8);			// dWiSta-Textzeile
		
		private int value; 
		private DisplayPartType( int value ) { this.value = value; }

		public String toString()
		{
			switch( value )
			{
				case 2:
					return "WZG-B";
				case 3:
					return "WZG-C";
				case 4:
					return "WZG-D";
				case 5:
					return "WZG-E";
				case 6:
					return "WZG-S";
				case 7:
					return "PIKT";
				case 8:
					return "TEXT";
				case 1:
				default:	
					return "WZG-A";
			}
		}
	}
	
	public static enum DisplayPartMode {
		IMAGE(1),
		TEXT(2);
		
		private int value; 
		private DisplayPartMode( int value ) { this.value = value; }

		public String toString()
		{
			switch( value )
			{
				case 2:
					return "IMAGE";
				case 1:
				default:	
					return "TEXT";
			}
		}
	}
	
	private Date time;
	private String internalId;
	private int de = -1;
	private int pictogramIndex;
	
	private DisplayPartType displayPartType;
	private DisplayPartMode displayPartMode;
	
	private DisplayPosition displayPosition;
	
	
	public String getInternalId()
	{
		return internalId;
	}
	public void setInternalId(String internalId)
	{
		this.internalId = internalId;
	}
	
	public ObjectKey getObjectKey()
	{
		return new ObjectKey( ObjectKey.TYPE_WZG, internalId); 
	}
	
	public DisplayPartType getDisplayPartType()
	{
		return displayPartType;
	}
	public void setDisplayPartType( DisplayPartType displayPartType)
	{
		this.displayPartType = displayPartType;
	}
	
	public Date getTime()
	{
		return time;
	}
	public void setTime(Date time)
	{
		this.time = time;
	}

	public int getPictogramIndex()
	{
		return pictogramIndex;
	}
	public void setPictogramIndex( int pictogramIndex )
	{
		this.pictogramIndex = pictogramIndex;
	}
	

	@Override
	public String toString()
	{
		return JavaObject2JSON.toString( this );
	}
	public int getDe()
	{
		return de;
	}
	public void setDe(int de)
	{
		this.de = de;
	}
	
	public DisplayPosition getDisplayPosition()
	{
		return displayPosition;
	}

	public void setDisplayPosition(DisplayPosition displayPosition)
	{
		this.displayPosition = displayPosition;
	}
	public DisplayPartMode getDisplayPartMode()
	{
		return displayPartMode;
	}
	public void setDisplayPartMode(DisplayPartMode displayPartMode)
	{
		this.displayPartMode = displayPartMode;
	}
}
