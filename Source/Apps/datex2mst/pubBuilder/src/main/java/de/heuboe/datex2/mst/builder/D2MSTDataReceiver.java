package de.heuboe.datex2.mst.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.base.D2SchemaInfo;
import de.heuboe.datex2.base.D2SchemaInfo.Info;
import de.heuboe.datex2.config.persistence.D2MstDefinitionInfo;
import de.heuboe.datex2.config.persistence.D2MstPersistence;
import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.mst.builder.writer.D2MSTPubWriter;
import de.heuboe.datex2.push.D2Publisher;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;


/**
 * 
 * Reads MST from database and writes XML file
 * 
 * @author peters
 *
 */
public class D2MSTDataReceiver 
{
	private static Logger logger = Logger.getLogger( D2MSTDataReceiver.class );
	
	@Autowired
	D2MstPersistence persistence;
	
	D2MSTConf conf;
    private String defVersion = "";
    private String pubFilePath;
    private String country;
    private String natId;
    private String lang;
    private D2MSTDataReader dataReader;
    private D2Publisher publisher; 
    
    /**
     * 
     * Constructor
     * 
     * @param conf			Configuration
     * @param dataReader	MST data reader	
     * @param pubFilePath	Path to put MST XML file
     * @param country		DATEX-II country code
     * @param natId			DATEX-II national identifier
     * @param lang			DATEX-II language code
     * @param publisher		Publisher (file writer)
     */
    public D2MSTDataReceiver(   D2MSTConf conf,
    		                    D2MSTDataReader dataReader,
    							String pubFilePath,
    							String country,
    							String natId,
    							String lang, 
    							D2Publisher publisher )
    {
    	this.conf = conf;
        this.pubFilePath = pubFilePath;
        this.country = country;
        this.natId = natId;
        this.lang = lang;
        
        this.dataReader = dataReader;
        this.publisher = publisher; 
    }
    
    private void updateConf( String mst, String defVersion, 
    						 String preferredLocEncoding )
    		throws D2Exception				 
    {
    	this.defVersion = defVersion;

    	D2MstDefinitionInfo defInfo = persistence.getD2SchemaInfo( mst, defVersion );
    	
    	if( defInfo == null )
    	{
    		throw new D2Exception( "No schema file for MST-ID + <" + mst + "> and defVersion <" + defVersion + "> !" );
    	}
    	
    	String d2SchemaFile = defInfo.getD2SchemaFile(); 
    	String d2SchemaCategory = defInfo.getD2SchemaCategory(); 
    	
    	D2SchemaInfo d2si = new D2SchemaInfo();
    	Info info = d2si.getInfo( conf.getD2SchemaPath() + File.separator + d2SchemaFile );

    	conf.setD2SchemaFile( d2SchemaFile );
    	conf.setD2SchemaCategory( d2SchemaCategory );
    	conf.setD2SchemaModelBaseVersion( info.getModelBaseVersion() );
    	conf.setD2SchemaNameSpace( info.getNameSpace() );
    	conf.setPreferredLocEncoding( preferredLocEncoding );
    }
   
	
	public void writeMST( String instance, 
						  String version, 
						  String defVersion,
						  String pubNatId,
						  String preferredLocEncoding )
			throws D2ExceptionBase						   
	{
		updateConf( instance, defVersion, preferredLocEncoding );
		
		writeMST( instance, version, pubNatId, preferredLocEncoding );
	}
	
	private void writeMST( String instance, 
						   String version, 
						   String pubNatId,
						   String preferredLocEncoding )
			throws D2ExceptionBase						   
	{
		String nid = pubNatId;
		if( ( nid == null ) || ( nid.isEmpty() ) ) {
			nid = natId;
		}
		
		try
		{
			conf.setPreferredLocEncoding( preferredLocEncoding );
			
			dataReader.readDBSupply( instance, version, false );
			
			String	pfp = pubFilePath + 
							       System.getProperty( "file.separator" ) +  
							       instance;
			
			List<D2Publisher> publishers = new ArrayList<>();
			publishers.add( publisher );
			D2MstDefinitionInfo defInfo = persistence.getD2SchemaInfo( instance, defVersion );
			
			if( defInfo.isAutoUpdate() ) {
				// Not supported
				// D2Publisher mdmPublisher = new D2PubSenderMDM( defInfo.getConnPropFile() );
				// publishers.add( mdmPublisher );
			}
			
			String fileName = D2MSTPubWriter.write( conf,
												    dataReader,
												    instance, 
												    pfp,
												    country,
												    nid,
												    lang,
												    publishers );
			
			if( ( fileName == null ) || fileName.isEmpty() ) {
				logger.error( "Keine valide XML-Datei geschrieben !" );
				throw new D2MSTPubException( D2MSTPubException.D2_ERR_INVALID_PUBLICATION,
											 "Internal Error: Generated publication invalid ! s. log files" );
			}
		} catch( Exception ex ) {
			CallStack.getStackTraceAsString( ex );
			logger.error( ex.getMessage() );
			
			throw new D2MSTPubException( "Error creating MST XML file", ex );
		}
	}
}
