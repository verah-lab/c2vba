package de.heuboe.datex2.vms.table.pub;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.datex2.schema.Transformer;
import de.heuboe.datex2.schema.v2_3.Instances;
import de.heuboe.datex2.vms.service.VMSService;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.AddressedTrafficFlow;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayPosition;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.FunctionalType;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartMode;
import de.heuboe.datex2.vms.service.table.data.D2VMSLocationPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.datex2.vms.service.table.data.D2VMSTableConfig;
import de.heuboe.datex2.vms.service.table.data.D2VMSTableConfigPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit.UnitType;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.vms.D2LogicalModel;
import eu.datex2.schema._2._2_0.vms.Location;
import eu.datex2.schema._2._2_0.vms.PhysicalMountingEnum;
import eu.datex2.schema._2._2_0.vms.PictogramCodeType;
import eu.datex2.schema._2._2_0.vms.PositionAbsoluteEnum;
import eu.datex2.schema._2._2_0.vms.PositionRelativeEnum;
import eu.datex2.schema._2._2_0.vms.VmsPictogramDisplayCharacteristics;
import eu.datex2.schema._2._2_0.vms.VmsPictogramDisplayCharacteristicsExtended;
import eu.datex2.schema._2._2_0.vms.VmsRecord;
import eu.datex2.schema._2._2_0.vms.VmsRecordExtended;
import eu.datex2.schema._2._2_0.vms.VmsRecordFunctionalType;
import eu.datex2.schema._2._2_0.vms.VmsTablePublication;
import eu.datex2.schema._2._2_0.vms.VmsTextDisplayCharacteristics;
import eu.datex2.schema._2._2_0.vms.VmsTypeEnum;
import eu.datex2.schema._2._2_0.vms.VmsUnitRecord;
import eu.datex2.schema._2._2_0.vms.VmsUnitRecordExtended;
import eu.datex2.schema._2._2_0.vms.VmsUnitTable;
import eu.datex2.schema._2._2_0.vms.VmsUnitType;
import eu.datex2.schema._2._2_0.vms._VmsPictogramDisplayCharacteristicsExtensionType;
import eu.datex2.schema._2._2_0.vms._VmsRecordExtensionType;
import eu.datex2.schema._2._2_0.vms._VmsRecordPictogramDisplayAreaIndexVmsPictogramDisplayCharacteristics;
import eu.datex2.schema._2._2_0.vms._VmsUnitRecordExtensionType;
import eu.datex2.schema._2._2_0.vms._VmsUnitRecordVmsIndexVmsRecord;


public class Publisher
{
	private static final Logger LOGGER = Logger.getLogger( Publisher.class );
	private static final String D2_PACKAGE = "eu.datex2.schema._2._2_0.vms";
	private static final String SCHEMA_FILE = "schema/DATEXIISchema_2_2_3_vms.xsd";
	
	private static final int CFG_PART_LEN = 1200;
	
	private VMSService vmsService = null;
	private String publicationTemplate;
	private String unitTemplate;
	private String tableVersion = "1";
	
	private Transformer transformer;

	
	public void init()
			throws VMSException	
	{
		try
		{
			InputStream is = getClass().getClassLoader().getResourceAsStream( "template/VMSTable.xml" );	
			publicationTemplate = IOUtils.toString( is );

			is = getClass().getClassLoader().getResourceAsStream( "template/VMSUnit.xml" );	
			unitTemplate = IOUtils.toString( is );
			
			if( !connect( Parameter.instance().getVmsServiceUrl() ) )
			{
				throw new VMSException( VMSException.ERROR_WS_CONN, "Cannot connect to VMSService" );
			}
			
			transformer = new Transformer( Instances.PROFILE_D2_3, Instances.PROFILE_VMS );
		}
		catch( IOException | JAXBException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	public boolean connect( String url )
	{
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass( VMSService.class );
		factory.setAddress( url );
		
		vmsService = (VMSService)factory.create();

        Client client = ClientProxy.getClient( vmsService );
        
        if (client != null) 
        {
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout( Parameter.instance().getConnTimeout() );
			policy.setReceiveTimeout( Parameter.instance().getConnTimeout() );
			conduit.setClient(policy);
        }
        
        vmsService.ping( "MDM DATEX II VMSTablePublication" );
        
        LOGGER.info( "Connected to <" + url + ">" );
        
        return true;
	}
	
	
	public VmsUnitRecord createUnitRecord()
			throws VMSException
	{
		try
		{
			return  JAXBUtil.getObject( unitTemplate, 
					     				"schema/DATEXIISchema_2_2_3_vms.xsd", 
					     				D2_PACKAGE );
		} catch( JAXBException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	
	private void setTableConfig( D2VMSTable d2VmsTable, VMSTableConfiguration config ) throws VMSServiceException {
		
		String xml = VMSTableConfiguration.getConfigAsXML( config );
		List<D2VMSTableConfigPart> parts = new ArrayList<>();
		
		D2VMSTableConfig d2VmsTableConfig = new D2VMSTableConfig();
		d2VmsTableConfig.setTime( d2VmsTable.getTime() );
		
		int num = xml.length() / CFG_PART_LEN;
		if( xml.length() % CFG_PART_LEN != 0 || ( xml.length() == 0 ) ) {
			num++;
		}
		for( int i = 0; i < num; i++ ){
			D2VMSTableConfigPart part = new D2VMSTableConfigPart();
			part.setTime( d2VmsTable.getTime() );
			part.setPartCount( i );
			part.setXmlPart( xml.substring( i * CFG_PART_LEN, Math.min( xml.length(), (i+1) * CFG_PART_LEN ) ) );
			parts.add( part );
		}
		
		d2VmsTableConfig.setParts( parts );
		
		d2VmsTable.setConfiguration( d2VmsTableConfig );
	}
	
	private Location toD2VmsLocation( eu.datex2.schema._2._2_0.Location location ) throws JAXBException {
		
		return transformer.transform( location, eu.datex2.schema._2._2_0.Location.class );
	}
	
	private VmsPictogramDisplayCharacteristics getVmsPictogramDisplayCharacteristics( DisplayPosition position ) {
		VmsPictogramDisplayCharacteristics vpdc = new VmsPictogramDisplayCharacteristics();
		
		
		_VmsPictogramDisplayCharacteristicsExtensionType extension = new _VmsPictogramDisplayCharacteristicsExtensionType();
		
		VmsPictogramDisplayCharacteristicsExtended extended = new VmsPictogramDisplayCharacteristicsExtended();
		extended.setPictogramCodeType( PictogramCodeType.TLS );
		extension.setVmsPictogramDisplayCharacteristicsExtended( extended );
		vpdc.setVmsPictogramDisplayCharacteristicsExtension( extension );
		
		if( position != null ) {
			switch( position ) {
				case RIGHT:
					vpdc.setPictogramPositionRelativeToText( PositionRelativeEnum.TO_THE_RIGHT );
					break;
				case LEFT:
				default:	
					vpdc.setPictogramPositionRelativeToText( PositionRelativeEnum.TO_THE_LEFT );
					break;
			}
		}
		
		return vpdc;
	}
	
	private void setUnitType( VmsUnitRecord unit, UnitType unitType ) {
		
		_VmsUnitRecordExtensionType extension = new _VmsUnitRecordExtensionType();
		VmsUnitRecordExtended extended = new VmsUnitRecordExtended();
		switch( unitType ) {
			case DWISTA:
				extended.setVmisUnitType( VmsUnitType.D_WI_STA_SITE );
				break;
			case WWW:
				extended.setVmisUnitType( VmsUnitType.WWW_SITE );
				break;
			case AQ:
			default:	
				extended.setVmisUnitType( VmsUnitType.WVZ_SITE );
				break;
		}
		
		extension.setVmsUnitRecordExtended( extended );
		unit.setVmsUnitRecordExtension( extension );
	}
	
	private void setVmsExtension( VmsRecord record, 
			                      FunctionalType functionalType, 
			                      AddressedTrafficFlow trafficFlow ) {
		
		_VmsRecordExtensionType extension = new _VmsRecordExtensionType();
		VmsRecordExtended extended = new VmsRecordExtended();
		switch( functionalType ) {
			case OVERVIEW:
				extended.setVmsRecordFunctionalType( VmsRecordFunctionalType.OVERVIEW );
				break;
			case DETAIL:
				extended.setVmsRecordFunctionalType( VmsRecordFunctionalType.DETAIL );
				break;
			case FUNCTIONAL_DETAIL:
				extended.setVmsRecordFunctionalType( VmsRecordFunctionalType.FUNCTIONAL_DETAIL );
				break;
			case OTHER:
			default:	
				extended.setVmsRecordFunctionalType( VmsRecordFunctionalType.OTHER );
				break;
		}
		switch( trafficFlow ) {
			case ALL:
				extended.setAddressedTrafficFlow( eu.datex2.schema._2._2_0.vms.AddressedTrafficFlow.ALL );
				break;
			case MAIN_CARRIAGEWAY_FOLLOWING_TRAFFIC:
				extended.setAddressedTrafficFlow( eu.datex2.schema._2._2_0.vms.AddressedTrafficFlow.MAIN_CARRIAGEWAY_FOLLOWING_TRAFFIC );
				break;
			case RIGHT_DIRECTION_FOLLOWING_TRAFFIC:
				extended.setAddressedTrafficFlow( eu.datex2.schema._2._2_0.vms.AddressedTrafficFlow.RIGHT_DIRECTION_FOLLOWING_TRAFFIC );
				break;
			case LEFT_DIRECTION_FOLLOWING_TRAFFIC:
				extended.setAddressedTrafficFlow( eu.datex2.schema._2._2_0.vms.AddressedTrafficFlow.LEFT_DIRECTION_FOLLOWING_TRAFFIC );
				break;
			case OTHER:
			default:	
				extended.setAddressedTrafficFlow( eu.datex2.schema._2._2_0.vms.AddressedTrafficFlow.OTHER );
				break;
		}
		
		extension.setVmsRecordExtended( extended );
		record.setVmsRecordExtension( extension );
	}
	
	
	public D2LogicalModel  getPublication()
			throws VMSException
	{
		D2VMSTable d2VmsTable = null;
		String tableId = Parameter.instance().getTableId();
		
		List<String> filterSBAs = Parameter.instance().getFilterVBAs();
		List<String> filterAQs = Parameter.instance().getFilterAQs();
		VMSTableConfiguration config = new VMSTableConfiguration();
		try
		{
			config.setSbaIds( filterSBAs );
			config.setAqIds( filterAQs );
			config.setAddTmcLocations( Parameter.instance().addTmcLocations() );
			config.setAddOpenLRLocations( Parameter.instance().addOpenLRLocations() );
			config.setCfgFile( Parameter.instance().getCfgFile() );
			
			config.setdWiStaMode( Parameter.instance().getDWiStaMode() );
			config.setDWiStaTextAreaLayouts( Parameter.instance().getdWiStaTextAreaLayouts() );
			
			List<D2VMSUnit> units = vmsService.getD2VMSUnits( config );
			d2VmsTable = new D2VMSTable();
			d2VmsTable.setUnits( units );
			
			tableVersion = Parameter.instance().getTableVersion();
			if( tableVersion == null )
				tableVersion = vmsService.getNextTableVersion( tableId );
		}
		catch( VMSServiceException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, 
								    ex.toString(), ex );
		}
		
		try
		{
			D2LogicalModel pub = JAXBUtil.getObject( publicationTemplate, 
							   					     "schema/DATEXIISchema_2_2_3_vms.xsd", 
							   					     D2_PACKAGE );
			
			VmsTablePublication vmsp = (VmsTablePublication)pub.getPayloadPublication();
	
			Date now = new Date();
			vmsp.setPublicationTime( now );
			
			String sender = Parameter.instance().getSender();
			if( ( sender != null ) && !sender.isEmpty() )
			{
				pub.getExchange().getSupplierIdentification().setNationalIdentifier( sender );
				vmsp.getPublicationCreator().setNationalIdentifier( sender );
			}
			
			VmsUnitTable table = vmsp.getVmsUnitTable().get(0);
			table.setId( tableId );
			table.setVmsUnitTableIdentification( tableId );
			table.setVersion( tableVersion );
			
			table.getVmsUnitRecord().clear();
			
			List<D2VMSUnit> units = d2VmsTable.getUnits();
			for( D2VMSUnit unit : units )
			{
				VmsUnitRecord vmsUnit = new VmsUnitRecord();
				setUnitType( vmsUnit, unit.getUnitType() );
				
				unit.setD2Id( unit.getInternalId() );
				unit.setTime( now );
				vmsUnit.setId( unit.getD2Id() );
				vmsUnit.setVersion( tableVersion );
				vmsUnit.setVmsUnitIdentifier( unit.getDescription() );	
				
				BigInteger numVms = BigInteger.valueOf( unit.getDisplays().size() );
				vmsUnit.setNumberOfVms( numVms  );
				
				List<D2VMSDisplay> displays = unit.getDisplays();
				
				// Reihenfolge 'index' aufsteigend nach Permanent-ID
				// ( garantiert, dass bei Wiederholung der Erzeugung 
				//   auf derselben Konfiguration ein identischer VMS-Table 
				//   erzeugt wird.)
				Map<String,D2VMSDisplay> id2Display = new TreeMap<>();
				for( D2VMSDisplay display : displays ) 
				{
					id2Display.put( display.getInternalId(), display );
				}
				
				int index = 0;
				for( D2VMSDisplay display : id2Display.values() )
				{
					VmsRecord vms = new VmsRecord();
					setVmsExtension( vms, display.getFunctionalType(), display.getAddressedTrafficFlow() );
					
					{
						if( display.getDisplayType() == DisplayType.WZG )
						{
							vms.setVmsType( VmsTypeEnum.MATRIX_SIGN );
							vms.setVmsPhysicalMounting( PhysicalMountingEnum.OVERHEAD_BRIDGE_MOUNTED );
							vms.setNumberOfPictogramDisplayAreas( BigInteger.valueOf( 1L ) );
							
							vms.setVmsDescription( Util.toD2String( "Wechselzeichengeber " + display.getDescription() ) ); 
						}
						
						if( ( display.getDisplayType() == DisplayType.DWISTA_TXT_WZG ) || 
							( display.getDisplayType() == DisplayType.DWISTA_TXT_GRP )	) {
							
							vms.setVmsType( VmsTypeEnum.MATRIX_SIGN );
							vms.setVmsPhysicalMounting( PhysicalMountingEnum.OVERHEAD_BRIDGE_MOUNTED );
							vms.setNumberOfPictogramDisplayAreas( BigInteger.valueOf( 0L ) );
							
							vms.setVmsDescription( Util.toD2String( display.getDescription() ) ); 
						}
						
						if( display.getDisplayType() == DisplayType.WWW )
						{
							vms.setVmsTypeCode( "WWW" );
							
							vms.setVmsDescription( Util.toD2String( "Wechselwegweiser " + display.getDescription() ) ); 
						}						
						
						if( display.getDisplayType() == DisplayType.DWISTA )
						{
							vms.setVmsTypeCode( "DWISTA" );
							
							vms.setVmsDescription( Util.toD2String( "dWiSta " + display.getDescription() ) ); 
						}						

						vms.setVmsLocation( toD2VmsLocation( display.getLocation() ) );
						
						if( ( display.getDisplayType() == DisplayType.DWISTA_TXT_WZG ) || 
						    ( display.getDisplayType() == DisplayType.DWISTA_TXT_GRP ) ) {
							VmsTextDisplayCharacteristics vmsTdc = new VmsTextDisplayCharacteristics();
							
							vmsTdc.setMaxNumberOfRows( BigInteger.valueOf( 3L ) );
							
							DisplayPosition dp = display.getDisplayPosition(); 
							if( dp != null ) {
								switch( dp ) {
									case LEFT:
										vmsTdc.setTextPositionAbsolute( PositionAbsoluteEnum.ON_LEFT );
										break;
									case RIGHT:
										vmsTdc.setTextPositionAbsolute( PositionAbsoluteEnum.ON_RIGHT );
										break;
									case TOP:
										vmsTdc.setTextPositionAbsolute( PositionAbsoluteEnum.AT_TOP );
										break;
									case BOTTOM:
										vmsTdc.setTextPositionAbsolute( PositionAbsoluteEnum.AT_BOTTOM );
										break;
									default:
										throw new VMSException( VMSException.ERROR_CFG, 
												                "Invalid text area position <" + 
												                dp.name() + "> for diaplay with ID <" + 
												                display.getInternalId() + ">!" );
								}
							}
							vms.setVmsTextDisplayCharacteristics( vmsTdc );
							
							if( display.getDisplayType() == DisplayType.DWISTA_TXT_GRP ) {
								
								int numImages = 0;
								for( D2VMSDisplayPart part : display.getDisplayParts() ) {
									
									if( part.getDisplayPartMode() == DisplayPartMode.IMAGE ) {
										_VmsRecordPictogramDisplayAreaIndexVmsPictogramDisplayCharacteristics indexPicto = new _VmsRecordPictogramDisplayAreaIndexVmsPictogramDisplayCharacteristics();
										indexPicto.setPictogramDisplayAreaIndex( part.getPictogramIndex() );
										indexPicto.setVmsPictogramDisplayCharacteristics( getVmsPictogramDisplayCharacteristics( part.getDisplayPosition() ) );
										vms.getVmsPictogramDisplayCharacteristics().add( indexPicto );
										numImages++;
									}
								}
								
								vms.setVmsTypeCode( "DWISTA-TEXT-AREA" );
								vms.setNumberOfPictogramDisplayAreas( BigInteger.valueOf( (long)numImages ) );
								
								long numTexts = (long)( display.getDisplayParts().size() - numImages );
								vmsTdc.setMaxNumberOfRows( BigInteger.valueOf( numTexts ) );
								
							} else {
								vms.setVmsTypeCode( "DWISTA-TEXT-LINE" );
							}
						} else {							
							_VmsRecordPictogramDisplayAreaIndexVmsPictogramDisplayCharacteristics indexPicto = new _VmsRecordPictogramDisplayAreaIndexVmsPictogramDisplayCharacteristics();
							indexPicto.setPictogramDisplayAreaIndex( 0 );
							indexPicto.setVmsPictogramDisplayCharacteristics( getVmsPictogramDisplayCharacteristics( null ) );
							vms.getVmsPictogramDisplayCharacteristics().add( indexPicto );
							
						}
						
						_VmsUnitRecordVmsIndexVmsRecord indexdVms = new _VmsUnitRecordVmsIndexVmsRecord();
						indexdVms.setVmsIndex( index );
						display.setVmsIndex( index );
						display.setTime( now );
						indexdVms.setVmsRecord( vms );
						vmsUnit.getVmsRecord().add( indexdVms );
						
						display.setLocationParts( D2VMSLocationPart.stringToContentList( display.getLocation(), now ) );
					}
					
					int pIndex = 0;
					for( D2VMSDisplayPart displayPart : display.getDisplayParts() )
					{
						displayPart.setPictogramIndex( pIndex );
						displayPart.setTime( now ); 
						
						if( 
						    ( display.getDisplayType() == DisplayType.WZG )
							&&
							( pIndex == 0 )
						   )
						{
							if( displayPart.getDisplayPartType() != null ) {  // NOSONAR
								vms.setVmsTypeCode( displayPart.getDisplayPartType().toString() );
							}
						}
						
						pIndex++;
					}
					
					index++;
				}
				
				table.getVmsUnitRecord().add( vmsUnit );
			}
			
			try {
				d2VmsTable.setTime( now );
				d2VmsTable.setD2Id( tableId );
				d2VmsTable.setD2Version( tableVersion );
				d2VmsTable.setUnits( units );

				setTableConfig( d2VmsTable, config );
				
				vmsService.saveVMSTable( d2VmsTable );
			}
			catch( /*VMSService*/Exception ex )
			{
				throw new VMSException( VMSException.ERROR_DB, 
									    "Error saving VMS table", ex );
			}
			
			return pub;
		}
		catch( JAXBException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	
	public String toString( D2LogicalModel publication )
			throws VMSException
	{
        try 
        {
        	String d2SchemaLocation = Parameter.instance().getD2SchemaLocation();
        	Map<String,String> schemaLocations = new HashMap<>();
        	if( !d2SchemaLocation.isEmpty() ) 
        	{
        		schemaLocations.put( SchemaUtil.DATEX2_SCHEMA_NS, d2SchemaLocation );
        	}
        	
        	return  JAXBUtil.getDocument( publication, 
										  SCHEMA_FILE, 
										  D2_PACKAGE, 
										  "http://datex2.eu/schema/2/2_0", 
										  schemaLocations,
										  "d2LogicalModel", 
										  D2LogicalModel.class,
										  true );
        } catch ( Exception ex) 
        {
	       	throw new VMSException( VMSException.ERROR_XML, ex.toString(), ex );
        }
	}
	
	public String getTableVersion()
	{
		return tableVersion;
	}
}