package de.heuboe.datex2.vms.service.table.data;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import de.heuboe.datex2.vms.service.uz.Util;
import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.Location;

public class D2VMSLocationPart 
{
	private static final Logger LOGGER = Logger.getLogger( D2VMSLocationPart.class );
    public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
	private static final int LOC_PART_CHUNK_SIZE = 1900;
	
	private static Marshaller marshaller;
	
	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance( "eu.datex2.schema._2._2_0" );
			marshaller = context.createMarshaller();
		}
		catch( JAXBException ex )
		{
			LOGGER.fatal( "Cannot load Datex II XML schema !" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( "Process will be terminated !" );
			Util.exit( -1 );
		}
	}	

	private Date time;
	private int  count;
	private String content;
	
	public Date getTime() 
	{
		return time;
	}
	
	public void setTime(Date time)
	{
		this.time = time;
	}
	
	public int getCount() 
	{
		return count;
	}
	public void setCount(int count) 
	{
		this.count = count;
	}
	
	public String getContent() 
	{
		return content;
	}
	
	public void setContent(String content) 
	{
		this.content = content;
	}
	
	
	public static final <T> String getDocument( Location object ) 
			throws JAXBException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		JAXBElement<Location> jaxbElement = new JAXBElement<Location>(
							new QName( "http://datex2.eu/schema/2/2_0",
									   "location"), 
						    Location.class, 
						    object );

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(jaxbElement, baos);

		try
		{
			String encoding = "UTF-8";
			Object obj = marshaller.getProperty(Marshaller.JAXB_ENCODING);
			if (obj != null)
			{
				encoding = obj.toString();
			}

			return baos.toString(encoding);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new JAXBException("UnsupportedEncodingException", ex);
		}
	}
	
	public static List<D2VMSLocationPart> stringToContentList( Location location,
															   Date date ) 
			throws JAXBException
	{
		String locString = null;
		
		locString = getDocument( location ); 
		
		List<D2VMSLocationPart> parts = new ArrayList<>();
		if( locString.isEmpty() ) 
		{
			D2VMSLocationPart part = new D2VMSLocationPart();
			part.setTime(  date );
			part.setCount(1);
			part.setContent("");
			parts.add( part );
		} 
		else 
		{
			String workingString = locString;
			int count = 1;
			do 
			{
				int partSize = Math.min(workingString.length(), LOC_PART_CHUNK_SIZE);
				String chunk = workingString.substring(0, partSize);
				D2VMSLocationPart part = new D2VMSLocationPart();
				part.setTime(  date );
				part.setCount( count++ );
				part.setContent( chunk );
				parts.add( part );
				workingString = workingString.substring(partSize);
			} 
			while (!workingString.isEmpty());
		}
		
		return parts;
	}
}
