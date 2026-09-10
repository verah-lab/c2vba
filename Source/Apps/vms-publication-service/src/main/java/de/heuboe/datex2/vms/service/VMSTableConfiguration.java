package de.heuboe.datex2.vms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayPosition;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.util.JavaObject2JSON;

@XmlRootElement
public class VMSTableConfiguration
{
	public enum DWiStaMode {
		// Ein VmsRecord
		COMPLETE,
		
		// COMPLETE und je Textarea ein VmsRecord, Piktogramme sind jeweils 
		// einer Textarea untergeordnet. 
		COMPLETE_TEXTAREA,
		
		// Jedes WZG ein VmsRecord
		SINGLE_WZG,
	}
	
	public static class WzgPosition implements Comparable<WzgPosition> {
		
		private int lane;
		private int row;
		
		public WzgPosition() {
		}
		
		public WzgPosition( int lane, int row ) {
			this.lane = lane;
			this.row = row;
		}

		public int getLane()
		{
			return lane;
		}
		public void setLane(int lane)
		{
			this.lane = lane;
		}
		public int getRow()
		{
			return row;
		}
		public void setRow(int row)
		{
			this.row = row;
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + lane;
			result = prime * result + row;
			return result;
		}
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WzgPosition other = (WzgPosition) obj;
			if (lane != other.lane)
				return false;
			if (row != other.row)
				return false;
			return true;
		}
		
		@Override
		public int compareTo(WzgPosition p)
		{
			if( lane < p.lane ) {
				return -1;
			}
			if( lane > p.lane ) {
				return 1;
			}
			if( row < p.row ) {
				return -1;
			}
			if( row > p.row ) {
				return 1;
			}

			return 0;
		}
	}
	
	public static class DWiStaTextAreaImage {
		
		// For image areas with identical position the vertical oder is given
		// by row (row=0 is the top position)
		private int row;
		private DisplayPosition position;
		
		public DWiStaTextAreaImage() {
		}
		
		public DWiStaTextAreaImage( int row, DisplayPosition position ) {
			this.row = row;
			this.position = position;
		}
		
		public DisplayPosition getPosition()
		{
			return position;
		}
		public void setPosition( DisplayPosition position )
		{
			this.position = position;
		}
		public int getRow()
		{
			return row;
		}
		public void setRow( int row )
		{
			this.row = row;
		}
	}
	
	public static class DWiStaTextArea {
		
		public void setId(int id)
		{
			this.id = id;
		}

		public void setNumLines(int numLines)
		{
			this.numLines = numLines;
		}

		private int id;
		private DisplayPosition position;
		private int numLines;
		private List<DWiStaTextAreaImage> images;
		
		public DWiStaTextArea() {
		}
		
		public DWiStaTextArea( 
				               int id,
				               DisplayPosition position, 
							   int numLines, 
				               List<DWiStaTextAreaImage> images ) {
			this.id = id;
			this.position = position;
			this.numLines = numLines;
			this.images = images;
		}

		public DisplayPosition getPosition()
		{
			return position;
		}
		public void setPosition(DisplayPosition position)
		{
			this.position = position;
		}
		public List<DWiStaTextAreaImage> getImages()
		{
			return images;
		}
		public void setImages(List<DWiStaTextAreaImage> images)
		{
			this.images = images;
		}

		public int getNumLines()
		{
			return numLines;
		}

		public int getId() {
			return id;
		}
	}	
	
	public static class DWiStaTextAreaLayout {
		
		private List<DWiStaTextArea> textAreas;
		
		public DWiStaTextAreaLayout() {
		}
		
		public DWiStaTextAreaLayout( List<DWiStaTextArea> textAreas ) {
			this.textAreas = textAreas;
		}

		public List<DWiStaTextArea> getTextAreas()
		{
			return textAreas;
		}

		public void setTextAreas(List<DWiStaTextArea> textAreas)
		{
			this.textAreas = textAreas;
		}
		
		
	}
	
	private static final Logger LOGGER = Logger.getLogger( VMSTableConfiguration.class );
	private static JAXBContext jaxbContext = null;
	private static Marshaller marshaller = null;
	private static Unmarshaller unmarshaller = null;
	private static String jaxbEncoding;
	
	static {
		
		try {
			jaxbContext = JAXBContext.newInstance( VMSTableConfiguration.class );
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
			
			jaxbEncoding = System.getProperty( Marshaller.JAXB_ENCODING );
			if( ( jaxbEncoding == null ) || jaxbEncoding.isEmpty() ) {
				jaxbEncoding = "UTF-8";
			}
			
		} catch( JAXBException ex ) {
			throw new RuntimeException( "Cannot initialize JAXBContext for class VMSTableConfiguration", ex  ); 
		}
	}
	
	public static synchronized VMSTableConfiguration getConfigFromXML( String xmlConfig ) throws VMSServiceException {
		
		try {
			ByteArrayInputStream baos = new ByteArrayInputStream( xmlConfig.getBytes( jaxbEncoding ) ); 
			VMSTableConfiguration config = (VMSTableConfiguration)unmarshaller.unmarshal( baos );
			return config;
		} catch ( Throwable ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_DB_SERIALIZE_CFG, "Error de-serialising VMSTableConfiguration", ex );
		}
	}

	public static synchronized String getConfigAsXML( VMSTableConfiguration config ) throws VMSServiceException {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal( config, baos);
			return new String( baos.toByteArray(), jaxbEncoding );
		} catch ( Throwable ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_DB_SERIALIZE_CFG, "Error serialising VMSTableConfiguration", ex );
		}
	}
	
	
	private boolean addTmcLocations;
	private boolean addOpenLRLocations;
	private String cfgFile = null;
	private DWiStaMode dWiStaMode = null;
	private List<DWiStaTextAreaLayout> dWiStaTextAreaLayouts = null;
	private List<String> wzgSubTypes = null;
	private List<String> sbaIds = null;
	private List<String> aqIds = null;

	public VMSTableConfiguration()
	{
	}
	
	public VMSTableConfiguration( List<String> wzgSubTypes )
	{
		this.wzgSubTypes = wzgSubTypes;
	}
	
	public List<String> getWzgSubTypes()
	{
		return wzgSubTypes;
	}
	
	public void setWwzgSubTypes( List<String> wzgSubTypes )
	{
		this.wzgSubTypes = wzgSubTypes;
	}
	
	public List<String> getSbaIds()
	{
		return sbaIds;
	}

	public void setSbaIds( List<String> sbaIds )
	{
		this.sbaIds = sbaIds;
	}
	
	public List<String> getAqIds()
	{
		return aqIds;
	}
	
	public List<String> toAqIds() {
		List<String> ids = new ArrayList<>();
		
		if( aqIds != null ) {
			for( String aqId : aqIds ) {
				int pos = aqId.indexOf( "::" );
				if( pos == -1 ) {
					ids.add( aqId );
				} else {
					ids.add( aqId.substring( 0, pos ) );
				}
			}
		}
		
		return ids;
	}

	public String toLocationId( String aqId ) {
		
		if( aqIds != null && !aqIds.isEmpty() ) {
			for( String ai : aqIds ) {
				int pos = ai.indexOf( "::" );
				if( ( pos == -1 ) && ( ai.equals( aqId ) ) ) {
					return aqId;
				} else if ( ( pos != -1 ) && aqId.equals( ai.substring( 0, pos ) ) ) {
					return ai.substring( pos + 2 );
				}
			}
			return null;
		}
		
		return aqId;
	}

	public void setAqIds( List<String> aqIds )
	{
		this.aqIds = aqIds;
	}
	
	
	
	@Override
	public String toString()
	{
		try
		{
			return JavaObject2JSON.j2String( this );
		}
		catch( Exception ex )
		{
			return "???";
		}
	}

	public boolean isAddTmcLocations()
	{
		return addTmcLocations;
	}

	public void setAddTmcLocations(boolean addTmcLocations)
	{
		this.addTmcLocations = addTmcLocations;
	}
	
	public boolean isAddOpenLRLocations()
	{
		return addOpenLRLocations;
	}

	public void setAddOpenLRLocations(boolean addOpenLRLocations)
	{
		this.addOpenLRLocations = addOpenLRLocations;
	}
	
	public String getCfgFile()
	{
		return cfgFile;
	}

	public void setCfgFile(String cfgFile)
	{
		this.cfgFile = cfgFile;
	}

	public DWiStaMode getdWiStaMode()
	{
		return dWiStaMode;
	}

	public void setdWiStaMode(DWiStaMode dWiStaMode)
	{
		this.dWiStaMode = dWiStaMode;
	}

	public List<DWiStaTextAreaLayout> getDWiStaTextAreaLayouts()
	{
		return dWiStaTextAreaLayouts;
	}

	public void setDWiStaTextAreaLayouts(List<DWiStaTextAreaLayout> dWiStaTextAreaLayouts)
	{
		this.dWiStaTextAreaLayouts = dWiStaTextAreaLayouts;
	}
}
