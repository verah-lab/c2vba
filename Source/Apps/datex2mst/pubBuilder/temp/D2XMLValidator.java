package de.heuboe.datex2.mst.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;


@SuppressWarnings("restriction")
class _ErrHandler implements ErrorHandler
{
	public void error( SAXParseException ex ) throws SAXException 
	{
		errors.add( ex.toString() );
		hasError = true;
	}
	
	public void fatalError(SAXParseException ex ) throws SAXException 
	{
		errors.add( ex.toString() );
		hasError = true;
	}
	
	public void warning(SAXParseException exception) throws SAXException 
	{
	}
	
	public boolean hasError()
	{
		return hasError;
	}
	
	public LinkedList<String> getErrors()
	{
		return errors;
	}
	
	private boolean hasError = false;
	private LinkedList<String> errors = new LinkedList<String>();
};


public class D2XMLValidator
{
	@SuppressWarnings("restriction")
	public static boolean _validate( String xml, String schemaPath )
	{
		try
		{
			DOMParser  P = new DOMParser();
			P.setFeature("http://xml.org/sax/features/validation",true);
			P.setFeature("http://apache.org/xml/features/validation/dynamic",true);
			P.setFeature("http://apache.org/xml/features/validation/schema",true);
			P.setFeature("http://apache.org/xml/features/validation/schema-full-checking",true);
			
			String ns = d2SchemaName; 
			P.setProperty( "http://apache.org/xml/properties/schema/external-schemaLocation",
					       ns + " " + schemaPath );
			_ErrHandler errHandler = new _ErrHandler();
			P.setErrorHandler( errHandler );
			
			ByteArrayInputStream bis = new ByteArrayInputStream( xml.getBytes("UTF-8") );
			P.parse( new InputSource( bis ) );
			
			boolean he = errHandler.hasError();
			if( he )
			{
				lastError = "";
				
				LinkedList<String> errors = errHandler.getErrors();
				
				int i = 0;
				for( String error : errors )
				{
					if( i > 3 )
						break;
					
					lastError += error + "\n";
					
					i++;
				}
				
				return false;
			}
		}
		catch( Exception ex )
		{
			lastError = ex.toString();
			return false;
		}
		
		return true;
	}
	
	public static boolean validate( String xml,
									 String schemaPath )
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			
			// read the XML file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse( new InputSource( new StringReader( xml ) ) );
			
			// create a SchemaFactory and a Schema
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaFile = new StreamSource(new File( schemaPath ));
			Schema schema = schemaFactory.newSchema(schemaFile);
			
			// create a Validator object and validate the XML file
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(doc));
			
			return true;
		} 
        catch (ParserConfigurationException ex) 
        {
			lastError = ex.toString();
			return false;
        }
        catch( IOException ex ) 
        {
			lastError = ex.toString();
			return false;
        }
        catch (SAXException ex) 
        {
			lastError = ex.toString();
			return false;
        }
	}	
	
	
	public static String getLastError()
	{
		return lastError;
	}
	
	public static void setD2SchemaNameSpace( String _d2SchemaName )
	{
		d2SchemaName = _d2SchemaName;
	}
	
	private static String lastError = "";
	private static String d2SchemaName = "http://datex2.eu/schema/1_0/1_0";;
}
