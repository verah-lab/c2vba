package de.heuboe.c2vba.datex2.table.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.w3c.tidy.Tidy;

import de.heuboe.log.Logger;

public class XMLFormatter {

	private static final Logger LOGGER = Logger.getLogger( XMLFormatter.class );
	private static final String UTF8 = "UTF-8";
	
	private XMLFormatter() {
	}
	
	public static String formatXML( String content ) {
		
		try
		{
	    	Tidy tidy = new Tidy();
	        tidy.setInputEncoding( UTF8 );
	        tidy.setOutputEncoding( UTF8 );
	        tidy.setWraplen(Integer.MAX_VALUE);
	        tidy.setXmlTags( true );
	        tidy.setXmlOut(true);
	        tidy.setSpaces( 4 );
	        tidy.setSmartIndent(true);
	        ByteArrayInputStream inputStream = new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) );
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        tidy.parseDOM(inputStream, outputStream);
	        
	        if( tidy.getParseErrors() == 0 ) {
	        	return outputStream.toString( StandardCharsets.UTF_8 );
	        }
	        else {
				LOGGER.warn( "Error formatting publication !");
				return content;
	        }
		} catch( Throwable ex ) {     // NOSONAR
			return content;
		}
	}
}
