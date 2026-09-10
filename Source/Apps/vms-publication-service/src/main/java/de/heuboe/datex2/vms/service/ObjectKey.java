package de.heuboe.datex2.vms.service;


public class ObjectKey implements Comparable<ObjectKey>
{
	public static final String TYPE_AQ = "AQ";
	public static final String TYPE_WZG = "WZG";
	
	private String type;
	private String id;
	
	public ObjectKey()
	{
	}
	
	public ObjectKey( String type, String id )
	{
		this.type = type;
		this.id = id;
	}
	
	public String getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}
	
	public void setType( String type )
	{
		this.type = type;
	}

	public void setId( String id )
	{
		this.id = id;
	}
	
	@Override
	public int compareTo( ObjectKey other )
	{
		int result = type.compareTo( other.type );
		
		if( result != 0 )
		{
			return result;
		}
		
		return id.compareTo( other.id );
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		if (obj.getClass() != this.getClass() )
		{ 
			return false;
		}
		
		ObjectKey key = (ObjectKey)obj;
		
		return this.getType().equals(key.getType()) && 
			   this.getId().equals(key.getId());
	}

	
	@Override
	public int hashCode()
	{
		return type.hashCode() ^ id.hashCode();
	}

	@Override
	public String toString()
	{
		return "ObjectKey(" + type + ", " + id + ")";
	}
	
}
