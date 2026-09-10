package de.heuboe.datex2.mst.builder;

import java.io.File;
import java.util.Date;

import de.heuboe.datex2.base.data.D2TimeEnvironment;


public class D2MSTConf
{
	public static final String SCHEMA_CATEGORY_MDM = "mdm";

	public static final String PREFERRED_ENCODING_COORDINATE = "COORDINATE";
	public static final String PREFERRED_ENCODING_ALERTC = "ALERTC";
	
	private static D2TimeEnvironment timeEnvironment = new D2TimeEnvironment();
	
	private boolean checkValid		= false;
	private String 	pubErrFilePath = null;	
	private boolean overwriteOutput;
	
	private String d2SchemaPath;
	private String d2SchemaFile;
	private String d2SchemaModelBaseVersion;
	private String d2SchemaNameSpace;
	private String d2SchemaCategory;
	
	private String preferredLocEncoding = "";
	
	private boolean _useMVTemplateFactory;

	
	
	public D2MSTConf(   boolean checkValid,
						String pubErrFilePath,	
						String d2SchemaPath,
						String d2SchemaFile,
						String d2SchemaModelBaseVersion,
						String d2SchemaNameSpace,
						String d2SchemaCategory,
						String preferredLocEncoding,
						boolean _useMVTemplateFactory,
						boolean overwriteOutput )
	{
		this.checkValid					= checkValid;
		this.pubErrFilePath 			= pubErrFilePath;	
		this.d2SchemaPath				= d2SchemaPath;
		this.d2SchemaFile				= d2SchemaFile;
		this.d2SchemaModelBaseVersion	= d2SchemaModelBaseVersion;
		this.d2SchemaNameSpace			= d2SchemaNameSpace;
		this.d2SchemaCategory			= d2SchemaCategory;
		this.preferredLocEncoding		= preferredLocEncoding;
		this._useMVTemplateFactory		= _useMVTemplateFactory;
		this.overwriteOutput			= overwriteOutput;	
	}
	
	public static boolean isValidPreferredLocEncoding( String plc )
	{
		return ( 
				 plc.isEmpty() || 
				 plc.equals( PREFERRED_ENCODING_COORDINATE ) || 
				 plc.equals( PREFERRED_ENCODING_ALERTC ) 
			   );
	}
	
	public boolean checkValid()
	{
		return checkValid;
	}

	
	public String getErrPath()
	{
		return pubErrFilePath;
	}

	
	public String getD2SchemaPath()
	{
		return d2SchemaPath;
	}

	
	public boolean overwriteOutput() 
	{
		return overwriteOutput;
	}

	
	public String getD2SchemaFile()
	{
		return d2SchemaFile;
	}

	public void setD2SchemaFile(String schemaFile)
	{
		d2SchemaFile = schemaFile;
	}
	
	public String getD2SchemaPathFile()
	{
		return d2SchemaPath + File.separator + d2SchemaFile;
	}
	

	public String getD2SchemaModelBaseVersion()
	{
		return d2SchemaModelBaseVersion;
	}
	
	public int getModelBaseVersionNumber()
	{
		String mbv = getD2SchemaModelBaseVersion(); 
		if( ( mbv == null ) || mbv.length() == 0 )
			return 1;
		else
		{
			char c = mbv.charAt(0);
			if( (c >= '0') && (c <= '9') )
				return Integer.parseInt( Character.toString(c) );
			else
				return 1;
		}
	}
	

	public void setD2SchemaModelBaseVersion(String schemaModelBaseVersion)
	{
		d2SchemaModelBaseVersion = schemaModelBaseVersion;
	}

	public String getD2SchemaNameSpace()
	{
		return d2SchemaNameSpace;
	}

	public void setD2SchemaNameSpace(String schemaNameSpace)
	{
		this.d2SchemaNameSpace = schemaNameSpace;
	}
	
	public void setD2SchemaCategory(String schemaCategory)
	{
		this.d2SchemaCategory = schemaCategory;
	}
	
	public boolean useMVTemplateFactory()
	{
		return _useMVTemplateFactory;
	}

	public void useMVTemplateFactory(boolean _useMVTemplateFactory)
	{
		this._useMVTemplateFactory = _useMVTemplateFactory;
	}
	
	public String getD2SchemaCategory()
	{
		return d2SchemaCategory;
	}

	public void setPreferredLocEncoding( String preferredLocEncoding )
	{
		this.preferredLocEncoding = preferredLocEncoding;
	}
	
	public String getPreferredLocEncoding()
	{
		return preferredLocEncoding;
	}
	
	public static void setTimeEnvironment( D2TimeEnvironment te )
	{
		timeEnvironment = te;
	}
	
	public static Date now()
	{
		return timeEnvironment.now();
	}

}
