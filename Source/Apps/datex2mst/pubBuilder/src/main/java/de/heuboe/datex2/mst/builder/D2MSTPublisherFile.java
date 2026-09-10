package de.heuboe.datex2.mst.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.w3c.tidy.Tidy;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.push.D2Publisher;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;

public class D2MSTPublisherFile extends D2Publisher
{
	private static final Logger LOGGER = Logger.getLogger( "de.heuboe.datex2.mst.builder" );
	
	private D2MSTConf conf;
	
	private D2MSTPubPostProcessor postProcessor = null;
	
	private String pubFilePath;
	private String mstName;
	private String mstIdExt;
	private boolean overwriteOutput;
	
	public D2MSTPublisherFile( D2MSTConf conf,
								 String filePath, 
			                     String mstName, 
			                     String mstIdExt,
			                     boolean overwrite) 
	{
		super();
		
		this.conf = conf;	
		this.pubFilePath = filePath;
		this.mstName = mstName;
		this.mstIdExt = mstIdExt;
		this.overwriteOutput = overwrite;
	}
	
	public void setPostProcessor( D2MSTPubPostProcessor postProcessor ) {
		this.postProcessor = postProcessor;
	}
	
	@Override
	public String getName()
	{
		String name = "File-Writer[" + pubFilePath + "]";
		if( overwriteOutput )
			name += ", overwrite mode";
		return name;
	}

	@Override
	public String publish( String content ) 
			throws D2ExceptionBase 
	{
		return publish( content, pubFilePath, "" );
	}
	
	@Override
	public String publish( String content, String path, String version) 
			throws D2ExceptionBase 
	{
		return publish( content, path, mstName, version);		
	}
	
	@Override
	public String publish( String content, String path, String pubId, String version) 
			throws D2ExceptionBase
	{
		String fileName = null;
		
		try 
		{
			if( content != null ) 
			{
				fileName = writePublication( content, path, pubId, mstIdExt, version);
			}
		} 
		catch (D2ExceptionBase e) 
		{
			e.printStackTrace();
			throw e;
		}
		
		return fileName;
	}


	private String writePublication( String co, 
			                         String pubPath, 
			                         String pubId,
			                         String mstIdExt, 
			                         String version) 
			throws D2ExceptionBase  
	{
		String content = co;
		
		// Ausgabe in Datei

		LOGGER.debug("Start von D2MSTPublisherVSInfo::writePublication()");
		
		if( postProcessor != null ) {
			content = postProcessor.process( content );
		}

		String fileName;
		if( overwriteOutput ) 
		{
			fileName = "content.xml";
		}
		else 
		{
			fileName = "D2MSTPub";

			if( (pubId != null) && !pubId.equals("") )
			{
				fileName += "_" + pubId;
				if( (mstIdExt != null) && !mstIdExt.equals("") )
				{
					fileName += "[" + mstIdExt + "]";
				}
				
			}
			
			if( (version != null) && !version.equals("") )
				fileName += "_" + version;
				
			fileName += ".xml";
		}

		try
		{
			File p = new File(pubPath);
			if( !p.exists() )
				p.mkdirs();
			
			if( !p.exists() )
			{
				throw new D2MSTPubException( D2MSTPubException.D2_ERR_WRITE_TO_FILE, 
						 					 "Verzeichnis '" + pubPath + "' kann nicht angelegt werden !" );
			}
		}
		catch( Exception ex )
		{
			throw new D2MSTPubException( D2MSTPubException.D2_ERR_WRITE_TO_FILE, 
					 ex.getMessage());
		}
		
		String path = pubPath + System.getProperty("file.separator") + fileName;

		LOGGER.debug( "Datei: " + path );
		
		
		//String writeContent = xmlFileHeader + content;
		String writeContent = content;
		
    	Tidy tidy = new Tidy();
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
        tidy.setWraplen(Integer.MAX_VALUE);
        tidy.setXmlTags( true );
        tidy.setXmlOut(true);
        tidy.setSpaces( 4 );
        tidy.setSmartIndent(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(writeContent.getBytes( StandardCharsets.UTF_8 ));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        
        if( tidy.getParseErrors() == 0 )
        	writeContent = outputStream.toString( StandardCharsets.UTF_8 );
		
		
		boolean valid = true;
    	if( conf.checkValid() )
    	{
    		try {
	    		JAXBUtil jaxbUtil = new JAXBUtil( "schema/mdm/MeasurementSiteTable_2017_01-00-00.xsd",
	    				                          // conf.getD2SchemaPath() + "/" + conf.getD2SchemaPathFile(), 
	                    						  SchemaUtil.DATEX2_SCHEMA_PACK, 
	                    						  conf.getD2SchemaNameSpace(),
	                    						  new HashMap<>(),
	                    						  true );
	    		jaxbUtil.getObject( writeContent );
    		} catch( Throwable ex ) {
    			LOGGER.error( "Erzeugte Publikation ist nicht valide !" );
    			LOGGER.error( "Error: " + ex.toString() );
    			valid = false;
    		}
    	}
    	
    	if( !valid )
    	{
    		String errPath = conf.getErrPath();
    		if( errPath != null )
    		{
    			path = errPath + File.separator + fileName;
    		}
    	}
    	
		try
		{
			FileOutputStream F = new FileOutputStream(path);  // NOSONAR
			F.write( writeContent.getBytes( StandardCharsets.UTF_8 ));
			F.close();
			
	    	File f = new File(path);
	    	long lm = f.lastModified();
	    	
	    	
	    	GregorianCalendar gc = new GregorianCalendar();
	    	gc.setTime(new Date(lm));
		} catch ( IOException ex ) {
			LOGGER.error("Fehler beim Schreiben der MST-Datei '" + path + "'");
			throw new D2MSTPubException( D2MSTPubException.D2_ERR_WRITE_TO_FILE, 
					 					 ex.getMessage());
		}
	    	
		LOGGER.debug("Ende von D2MSTPublisherVSInfo::writePublication()");
		
    	return valid ? path : null;
	}
}
