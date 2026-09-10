package de.heuboe.datex2.location.service;


public class SiteType implements Comparable<SiteType>
{
	private String objType = "";
	private String subItemType = "";
	
	private String externalType;
	private boolean hasSubSites = false;
	
	public SiteType() {
		
	}
	
	public SiteType( String objType, String subItemType )
	{
		this.objType = objType;
		this.subItemType = subItemType;
	}
	
	public SiteType( String objType )
	{
		this( objType, "" );
	}

	public String getObjType()
	{
		return objType;
	}

	public String getSubItemType()
	{
		return subItemType;
	}
	
	public void setObjType(String objType)
	{
		this.objType = objType;
	}

	public void setSubItemType(String subItemType)
	{
		this.subItemType = subItemType;
	}

	public String getExternalType()
	{
		return externalType;
	}

	public void setExternalType( String externalType )
	{
		this.externalType = externalType;
	}
	
	@Override
	public int compareTo( SiteType other )
	{
		int result = objType.compareTo( other.objType );
		
		if( result != 0 )
		{
			return result;
		}
		
		return subItemType.compareTo( other.subItemType );
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
		
		SiteType st = (SiteType) obj;
		
		return this.objType.equals(st.objType) && 
			   this.subItemType.equals(st.subItemType);
	}

	@Override
	public int hashCode()
	{
		return objType.hashCode() ^ subItemType.hashCode();
	}

	public boolean isHasSubSites()
	{
		return hasSubSites;
	}

	public void setHasSubSites(boolean hasSubSites)
	{
		this.hasSubSites = hasSubSites;
	}

	
}

