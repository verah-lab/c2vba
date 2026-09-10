package de.heuboe.srb.datex2.srp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;
import eu.datex2.schema._2._2_0.srp.Location;
import eu.datex2.schema._2._2_0.srp.Situation;

/**
 * 
 * Publisher
 * 
 * @author peters
 *
 */
public class Publisher
{
	public static final String D2_PACKAGE = "eu.datex2.schema._2._2_0.srp";
	public static String SCHEMA_FILE = "d2Schema/StrategicRouting_withSubscriptionLifecycle-c2vba.xsd";  // NOSONAR
	
	private String publication;
	private String situation;
	private String area;
	private String point;
	private String linear;
 	
	private JAXBUtil jaxbUtil = null;
	
	/**
	 * 
	 * Initialization
	 * 
	 * @param pubMode PublicationMode
	 * @param d2SchemaLocation Schema file
	 * @throws SRPException Exception
	 */
	public void init( PublicationMode pubMode, String d2SchemaLocation  )
			throws SRPException	
	{
		try
		{
//			if( pubMode != PublicationMode.OnUpdateCyclicSnapshot ) {
//				SCHEMA_FILE = "d2Schema/StrategicRouting.xsd"; // NOSONAR
//			}
			
			InputStream is = getClass().getClassLoader().getResourceAsStream( "d2Template/SRPPublication.xml" );	
			publication = IOUtils.toString( is );
			
			is = getClass().getClassLoader().getResourceAsStream( "d2Template/Situation.xml" );	
			situation = IOUtils.toString( is );

			is = getClass().getClassLoader().getResourceAsStream( "d2Template/AreaLocation.xml" );	
			area = IOUtils.toString( is );

			is = getClass().getClassLoader().getResourceAsStream( "d2Template/PointLocation.xml" );	
			point = IOUtils.toString( is );
			
			is = getClass().getClassLoader().getResourceAsStream( "d2Template/LinearLocation.xml" );	
			linear = IOUtils.toString( is );

        	Map<String,String> schemaLocations = new HashMap<>();
        	if( !d2SchemaLocation.isEmpty() ) 
        	{
        		schemaLocations.put( SchemaUtil.DATEX2_SCHEMA_NS, d2SchemaLocation );
        	}
        	
			jaxbUtil = new JAXBUtil( SCHEMA_FILE, D2_PACKAGE, "http://datex2.eu/schema/2/2_0", schemaLocations, true );
		} catch( IOException | JAXBException ex )
		{
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	/**
	 * 
	 * Creates D2LogicalModel object 
	 * 
	 * @return D2LogicalModel object 
	 * @throws SRPException Exception
	 */
	public D2LogicalModel  getPublication()
			throws SRPException
	{
		try
		{
			return jaxbUtil.getObject( publication );
		} catch( JAXBException ex ) {
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	public Location getAreaLocation()
			throws SRPException
	{
		try
		{
			return jaxbUtil.getObject( area );
		} catch( JAXBException ex ) {
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	public Location getPointLocation()
			throws SRPException
	{
		try
		{
			return jaxbUtil.getObject( point );
		} catch( JAXBException ex ) {
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	public Location getLinearLocation()
			throws SRPException
	{
		try
		{
			return jaxbUtil.getObject( linear );
		} catch( JAXBException ex ) {
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	/**
	 * 
	 * Creates Situation object
	 * 
	 * @return Situation object
	 * @throws SRPException  Error
	 */
	public Situation createSituation()
			throws SRPException
	{
		try
		{
			return jaxbUtil.getObject( situation );
		} catch( JAXBException ex ) {
			throw new SRPException( SRPException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	/**
	 * 
	 * D2LogicalModel object as String 
	 * 
	 * @param publication D2LogicalModel object
	 * @return String representation
	 * @throws SRPException Exception 
	 */
	public String toString( D2LogicalModel publication )
			throws SRPException
	{
        try 
        {
        	return jaxbUtil.getDocument( publication,
										 "d2LogicalModel", 
										 D2LogicalModel.class );
        } catch ( Exception ex) {
        	throw new SRPException( SRPException.ERROR_XML, ex.toString(), ex );
        }
	}
	
	
}