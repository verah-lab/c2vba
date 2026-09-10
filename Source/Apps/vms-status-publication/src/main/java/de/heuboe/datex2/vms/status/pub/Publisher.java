package de.heuboe.datex2.vms.status.pub;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.datex2.vms.service.D2VMSError;
import de.heuboe.datex2.vms.service.D2VMSErrorType;
import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSOperationReason;
import de.heuboe.datex2.vms.service.D2VMSPictogram;
import de.heuboe.datex2.vms.service.D2VMSUnitMessage;
import de.heuboe.datex2.vms.service.NumericalValueType;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.OperationCodeType;
import de.heuboe.datex2.vms.service.VMSPublicationMode;
import de.heuboe.datex2.vms.status.pub.Reader.DWiStaTextArea;
import de.heuboe.datex2.vms.status.pub.config.Properties;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.CodedReasonForSettingMessageEnum;
import eu.datex2.schema._2._2_0.D2LogicalModel;
import eu.datex2.schema._2._2_0.OperatingModeEnum;
import eu.datex2.schema._2._2_0.Subscription;
import eu.datex2.schema._2._2_0.SubscriptionStateEnum;
import eu.datex2.schema._2._2_0.Target;
import eu.datex2.schema._2._2_0.UpdateMethodEnum;
import eu.datex2.schema._2._2_0.Vms;
import eu.datex2.schema._2._2_0.VmsDatexPictogramEnum;
import eu.datex2.schema._2._2_0.VmsFault;
import eu.datex2.schema._2._2_0.VmsFaultEnum;
import eu.datex2.schema._2._2_0.VmsMessage;
import eu.datex2.schema._2._2_0.VmsPictogram;
import eu.datex2.schema._2._2_0.VmsPictogramDisplayArea;
import eu.datex2.schema._2._2_0.VmsPublication;
import eu.datex2.schema._2._2_0.VmsText;
import eu.datex2.schema._2._2_0.VmsTextLine;
import eu.datex2.schema._2._2_0.VmsUnit;
import eu.datex2.schema._2._2_0._TextPage;
import eu.datex2.schema._2._2_0._VmsMessageIndexVmsMessage;
import eu.datex2.schema._2._2_0._VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea;
import eu.datex2.schema._2._2_0._VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram;
import eu.datex2.schema._2._2_0._VmsTextLineIndexVmsTextLine;
import eu.datex2.schema._2._2_0._VmsUnitRecordVersionedReference;
import eu.datex2.schema._2._2_0._VmsUnitTableVersionedReference;
import eu.datex2.schema._2._2_0._VmsUnitVmsIndexVms;


/**
 * 
 * Creates DATEX-II publication 
 * 
 * @author peters
 *
 */
public class Publisher
{
	private static final String D2_PACKAGE = "eu.datex2.schema._2._2_0";
	public static final String SCHEMA_FILE = "schema/DATEXIISchema_2_2_3_vms.xsd";
	private static final Logger LOGGER = Logger.getLogger( Publisher.class );
	
	private Properties properties;
	private String tableId;
	
	private Date subscriptionStartTime = null;
	private VMSPublicationMode publicationMode;
	private D2LogicalModel publicationTemplate;
	private JAXBUtil jaxbUtil =  null;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param properties		Properties
	 */
	public Publisher( Properties properties ) {
		this.properties = properties;
	}
	
	/**
	 * 
	 * Initialisaton
	 * 
	 * @throws VMSException	Error
	 */
	public void init()
			throws VMSException	
	{
		try
		{
			InputStream is = getClass().getClassLoader().getResourceAsStream( "template/VMSPublication.xml" );	
			String publication = IOUtils.toString( is );
			
			this.tableId = properties.getTableId();
			
        	String d2SchemaLocation = properties.getD2SchemaLocation();
        	Map<String,String> schemaLocations = new HashMap<>();
        	if( ( d2SchemaLocation != null ) && !d2SchemaLocation.isEmpty() ) 
        	{
        		schemaLocations.put( SchemaUtil.DATEX2_SCHEMA_NS, d2SchemaLocation );
        	}
			
			jaxbUtil = new JAXBUtil( SCHEMA_FILE, 
									 D2_PACKAGE, 
					                 "http://datex2.eu/schema/2/2_0", 
					                 schemaLocations,
					                 true );
			
			//jaxbUtil.setMarshalerProperty( "com.sun.xml.bind.marshaller.CharacterEscapeHandler", 		// NOSNAR   
			//		                       MinimumEscapeHandler.theInstance );							// NOSNAR
			
			publicationTemplate = jaxbUtil.getObject( publication );
			
			publicationMode = properties.getPubMode();
		} catch( IOException | JAXBException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, 
								    ex.toString(), ex );
		}
	}
	
	private D2LogicalModel getPublication()
	{
		D2LogicalModel pub = (D2LogicalModel)publicationTemplate.clone();
		publicationTemplate.copyTo( pub );
		
		VmsPublication vmsp = (VmsPublication)pub.getPayloadPublication();
		vmsp.getVmsUnit();
		
		return pub;
	}
	
	
	@SuppressWarnings("unused")
	private CodedReasonForSettingMessageEnum  toD2OperationReason( D2VMSOperationReason reason )
	{
		if( reason == null ) {
			return null;
		}
		
		return CodedReasonForSettingMessageEnum.fromValue( reason.value() );
	}
	
	private VmsFaultEnum toD2VmsFault( D2VMSErrorType type )
	{
		switch( type )
		{
			case CommunicationsFailure:
				return VmsFaultEnum.COMMUNICATIONS_FAILURE;
			case IncorrectMessageDisplayed:
				return VmsFaultEnum.INCORRECT_MESSAGE_DISPLAYED;
			case IncorrectPictogramDisplayed:
				return VmsFaultEnum.INCORRECT_PICTOGRAM_DISPLAYED;
			case  OutOfService:
				return VmsFaultEnum.OUT_OF_SERVICE;
			case PowerFailure:
				return VmsFaultEnum.POWER_FAILURE;
			case UnableToClearDown:
				return VmsFaultEnum.UNABLE_TO_CLEAR_DOWN;
			case Unknown:
				return VmsFaultEnum.UNKNOWN;
			case Other:
			default:	
				return VmsFaultEnum.OTHER;
		}
	}

	private void setTLSCodes( int tlsCode, VmsPictogram pictogram )
	{
		VmsDatexPictogramEnum vdpe = Util.tlsCode2VmsPictogramType(tlsCode);
		pictogram.getPictogramDescription().add( vdpe );
		
		NumericalValueType nvt = Util.tlsCode2NumericalValueType( tlsCode );
		Double value = Util.tlsCode2NumericalValue( tlsCode );
		
		pictogram.setPictogramCode( "" + tlsCode);
		
		if( ( nvt != null ) && ( value != null ) ) 
		{
			switch( nvt )	
			{
				case DISTANCE: 
					pictogram.setDistanceAttribute( BigInteger.valueOf( Math.round( value ) ) ); 
					break;
				case HEIGHT:
					pictogram.setHeightAttribute( value.floatValue() ); 
					break;
				case WIDTH:
					pictogram.setWidthAttribute( value.floatValue() ); 
					break;
				case LENGTH:
					pictogram.setLengthAttribute( value.floatValue()); 
					break;
				case SPEED:
					pictogram.setSpeedAttribute( value.floatValue() ); 
					break;
				case WEIGHT:
					pictogram.setWeightAttribute( value.floatValue() ); 
					break;
			}
		}
	}
	
	private void setError( Vms vms, D2VMSError error ) {
		
		VmsFault fault = new VmsFault();
		fault.setFaultLastUpdateTime( error.getLastUpdateTime() );
		
		fault.setFaultIdentifier( error.getErrorCode() );
		String description = error.getDescription();
		if( ( description != null ) && 
			!description.isEmpty() && 	
			!description.equals( "true" ) && 
			!description.equals( "false" ) ) {
			fault.setFaultDescription( description );
		} else {
			fault.setFaultDescription( null );
		}
		
		fault.setVmsFault( toD2VmsFault( error.getType() ) );
		
		vms.getVmsFault().add( fault );
		
	}
	
	private void addPictograms( VmsMessage message, List<VmsPictogram> pictograms ) {
		
		_VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea vmpdaivpda = 
				new _VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea();
		
		VmsPictogramDisplayArea vpda = new VmsPictogramDisplayArea();

		int index = 0;
		for( VmsPictogram pictogram : pictograms ) {
			
			_VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram vpdapsivp = 
					new _VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram();
			vpdapsivp.setVmsPictogram( pictogram );
			vpdapsivp.setPictogramSequencingIndex( index );
			
			vpda.getVmsPictogram().add( vpdapsivp );
			index++;
		}
		
		vmpdaivpda.setPictogramDisplayAreaIndex( 0 );
		vmpdaivpda.setVmsPictogramDisplayArea( vpda );
		
		message.getVmsPictogramDisplayArea().add( vmpdaivpda );
	}
	
	private void addTextLines( VmsMessage message, List<String> lines ) {
		
		_TextPage tp = new _TextPage();
		tp.setPageNumber( 0 );
		
		VmsText text = new VmsText();
		
		int index = 0;
		for( String line : lines ) {
			VmsTextLine tl = new  VmsTextLine();
			tl.setVmsTextLine( line );
			
			_VmsTextLineIndexVmsTextLine tli = new _VmsTextLineIndexVmsTextLine();
			tli.setLineIndex( index );
			tli.setVmsTextLine( tl );
			
			text.getVmsTextLine().add( tli );
			
			index++;
		}
		tp.setVmsText(text );
		
		message.getTextPage().add( tp );
	}
	
	private _VmsUnitVmsIndexVms createDWiStaTextGroupVms( DWiStaTextArea textArea,      // NOSONAR
														  List<D2VMSMessage> vrms ) {
		
		Date timeLastSet = new Date( 1500000000000L );
		D2VMSError error = null;
		
		Map< ObjectKey, D2VMSMessage > objKey2Msg = new HashMap<>();
		for( D2VMSMessage vrm : vrms ) {
			objKey2Msg.put( vrm.getObjectKey(), vrm ); 
		}
		
		Vms vms = new Vms();
		VmsMessage message = new VmsMessage();
		
		List<VmsPictogram> pictograms = new ArrayList<>();
		for( ObjectKey key : textArea.getPictogramKeys() ) {
			D2VMSMessage msg = objKey2Msg.get( key );
			if( msg == null ) {
				LOGGER.error( "No data for dWiSta pictogram Wzg with ID <" + key.getId() + ">" );
			    continue;
			}
			
			D2VMSError err = msg.getError(); 
			if( err != null ) {
				if( ( error == null ) || err.getLastUpdateTime().after( error.getLastUpdateTime() ) ) {   // NOSONAR
					error = err;
				}
			}
			
			if( msg.getTimeLastSet().after( timeLastSet ) ) {
				timeLastSet = msg.getTimeLastSet();
			}
			
			D2VMSPictogram d2VMSPictogram = msg.getPictograms().get(0);
			VmsPictogram pictogram = new VmsPictogram();
			pictogram.setPictogramCode( msg.getOperationCode() );
			
			if( properties.isWritePictogramUrl() ) {
				pictogram.setPictogramUrl( d2VMSPictogram.getImageFilePath() );
			}
			
			if( msg.getOperationCodeType() == OperationCodeType.StandardTLS )
			{
				String code = msg.getOperationCode();	
				if( code != null )
				{
					Integer value = Util.toInteger( code );
					if( value != null )
					{
						setTLSCodes( value, pictogram );
					}
				}
			}
			
			pictograms.add( pictogram );
		}
		addPictograms( message, pictograms );
		
		List<String> textLines = new ArrayList<>();
		for( ObjectKey key : textArea.getTextKeys() ) {
			D2VMSMessage msg = objKey2Msg.get( key );
			
			if( msg == null ) {
				LOGGER.error( "No data for dWiSta text Wzg with ID <" + key.getId() + ">" );
				textLines.add( "" );
			} else {
				
				if( msg.getTimeLastSet().after( timeLastSet ) ) {
					timeLastSet = msg.getTimeLastSet();
				}
				
				D2VMSError err = msg.getError(); 
				if( err != null ) {
					if( ( error == null ) || err.getLastUpdateTime().after( error.getLastUpdateTime() ) ) {   // NOSONAR
						error = err;
					}
				}
				
				textLines.add( ( msg.getText() == null ) ? "" : msg.getText() );
			}
		}
		
		addTextLines( message, textLines );
		
		
		if( error != null ) {
			vms.setVmsWorking( false );
			setError( vms, error );
		} else {
			vms.setVmsWorking( true );
		}
		
		message.setTimeLastSet( timeLastSet );

		_VmsMessageIndexVmsMessage vmivm = new _VmsMessageIndexVmsMessage();
		vmivm.setMessageIndex( 0 );
		vmivm.setVmsMessage( message );
		vms.getVmsMessage().add( vmivm );
		
		_VmsUnitVmsIndexVms vuviv = new _VmsUnitVmsIndexVms();
		
		vuviv.setVmsIndex( textArea.getVmsIndex() );
		vuviv.setVms( vms );
		
		return vuviv;
	}
	
	
	private _VmsUnitVmsIndexVms createVms( D2VMSMessage vrm, Reader reader ) {   // NOSONAR
				
		Vms vms = new Vms();
		
		D2VMSError error = vrm.getError();
		vms.setVmsWorking( error == null );
		if( error != null )
		{
			VmsFault fault = new VmsFault();
			fault.setFaultLastUpdateTime( error.getLastUpdateTime() );
			
			fault.setFaultIdentifier( error.getErrorCode() );
			String description = error.getDescription();
			if( ( description != null ) && 
				!description.isEmpty() && 	
				!description.equals( "true" ) && 
				!description.equals( "false" ) 
			   ) {
				fault.setFaultDescription( description );
			} else {
				fault.setFaultDescription( null );
			}
			
			fault.setVmsFault( toD2VmsFault( error.getType() ) );
			
			vms.getVmsFault().add( fault );
		}
		
		VmsMessage message = new VmsMessage();
		message.setTimeLastSet( vrm.getTimeLastSet() );
		String rfs = vrm.getReasonForSetting();
		if( ( rfs != null ) && !rfs.isEmpty() ) {
			message.setReasonForSetting( Util.toD2String( rfs ) );
		}
		
		// not supported on server side
		// skipped !
		// message.setCodedReasonForSetting( toD2OperationReason( vrm.getOperationReason() ) );  // NOSONAR
	
		for( D2VMSPictogram d2VMSpictogram : vrm.getPictograms() )
		{
			VmsPictogram pictogram = new VmsPictogram();
			pictogram.setPictogramCode( d2VMSpictogram.getCode() );
			
			if( properties.isWritePictogramUrl() ) {
				pictogram.setPictogramUrl( d2VMSpictogram.getImageFilePath() );
			}
			
			if( d2VMSpictogram.getDistanceAttribute() != null ) {
				pictogram.setDistanceAttribute( BigInteger.valueOf( d2VMSpictogram.getDistanceAttribute() ) ); 
			}
			if( d2VMSpictogram.getHeightAttribute() != null ) {
				pictogram.setHeightAttribute( d2VMSpictogram.getHeightAttribute().floatValue() ); 
			}
			if( d2VMSpictogram.getWidthAttribute() != null ) {
				pictogram.setWidthAttribute( d2VMSpictogram.getWidthAttribute().floatValue() ); 
			}
			if( d2VMSpictogram.getLengthAttribute() != null ) {
				pictogram.setLengthAttribute( d2VMSpictogram.getLengthAttribute().floatValue() ); 
			}
			if( d2VMSpictogram.getSpeedAttribute() != null ) {
				pictogram.setSpeedAttribute( d2VMSpictogram.getSpeedAttribute().floatValue() ); 
			}
			if( d2VMSpictogram.getWeightAttribute() != null ) {
				pictogram.setWeightAttribute( d2VMSpictogram.getWeightAttribute().floatValue() ); 
			}
			
			if( vrm.getOperationCodeType() == OperationCodeType.StandardTLS )
			{
				String code = vrm.getOperationCode();	
				if( code != null )
				{
					Integer value = Util.toInteger( code );
					if( value != null )
					{
						setTLSCodes( value, pictogram );
					}
				}
			}
			
			_VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea vmpdaivpda = 
					new _VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea();
			
			VmsPictogramDisplayArea vpda = new VmsPictogramDisplayArea();
			
			_VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram vpdapsivp = 
					new _VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram();
			
			vpdapsivp.setPictogramSequencingIndex( 0 );
			vpdapsivp.setVmsPictogram( pictogram );
			
			vpda.getVmsPictogram().add( vpdapsivp );
			
			vmpdaivpda.setPictogramDisplayAreaIndex( 0 );
			vmpdaivpda.setVmsPictogramDisplayArea( vpda );
			message.getVmsPictogramDisplayArea().add( vmpdaivpda );
		}
		
		_VmsMessageIndexVmsMessage vmivm = new _VmsMessageIndexVmsMessage();
		vmivm.setMessageIndex( 0 );
		vmivm.setVmsMessage( message );
		vms.getVmsMessage().add( vmivm );
		
		_VmsUnitVmsIndexVms vuviv = new _VmsUnitVmsIndexVms();
		
		vuviv.setVmsIndex( reader.getVmsIndex( vrm.getObjectKey() ) );
		vuviv.setVms( vms );
		
		return vuviv;
	}
	
	/**
	 * 
	 * Erzeugt zu Schaltdaten die VMS-Publikation
	 * 
	 * 
	 * @param tableVersion			Version der VMS-Table-Publikation
	 * @param vums					D2VMSUnitMessages
	 * @param snapshot				true: Schaltdaten aller AQs werden publiziert
	 * @param reader				Data reader 
	 * @return						DATEX-II-VMS-Publikation	
	 * @throws VMSException			Error
	 */
	public D2LogicalModel createPublication( String tableVersion,                      // NOSONAR
			                         		 List<D2VMSUnitMessage> vums,
			                         		 boolean snapshot,
			                         		 Reader reader )
	{
		Date now = new Date();
		D2LogicalModel pub = getPublication();
		
		VmsPublication vmsPublication = (VmsPublication)pub.getPayloadPublication();
		vmsPublication.getVmsUnit().clear();
		
		vmsPublication.setPublicationTime( now );
		
		String sender = properties.getSender();
		if( ( sender != null ) && !sender.isEmpty() )
		{
			pub.getExchange().getSupplierIdentification().setNationalIdentifier( sender );
			vmsPublication.getPublicationCreator().setNationalIdentifier( sender );
		}
		
		
		if( publicationMode == VMSPublicationMode.OnUpdateCyclicSnapshot )
		{
			Subscription subscription = new Subscription();
			
			if( subscriptionStartTime == null ) {
				subscriptionStartTime = now;
			}
			subscription.setSubscriptionStartTime( subscriptionStartTime );
			
			Target target = new Target();
			target.setProtocol( "SOAP" );
			target.setAddress( properties.getDatex2Target() );
			subscription.getTarget().add( target );
			
			subscription.setOperatingMode( OperatingModeEnum.OPERATING_MODE_1 );  
			subscription.setSubscriptionState( SubscriptionStateEnum.ACTIVE );
		     
			if( snapshot ) {
				subscription.setUpdateMethod( UpdateMethodEnum.SNAPSHOT );
			} else {
				subscription.setUpdateMethod( UpdateMethodEnum.ALL_ELEMENT_UPDATE );
			}
			
			pub.getExchange().setSubscription( subscription );
		}
		
		
		if( ( vums == null ) || vums.isEmpty() )
		{
			LOGGER.info( "No VmsUnitMessage for publication" );
			return null;
		}
		
		List<String> aqsWithoutMsgs = new ArrayList<>();
		
		for( D2VMSUnitMessage vum : vums )
		{
			VmsUnit vmsUnit = new VmsUnit();
			_VmsUnitTableVersionedReference vutvr = new _VmsUnitTableVersionedReference();
			vutvr.setId( tableId );
			vutvr.setVersion( tableVersion );
			vutvr.setTargetClass( "VmsUnitTable" );
			vmsUnit.setVmsUnitTableReference( vutvr );
			
			_VmsUnitRecordVersionedReference vuvr = new _VmsUnitRecordVersionedReference();
			vuvr.setId( vum.getId() );
			vuvr.setVersion( tableVersion );
			vuvr.setTargetClass( "VmsUnitRecord" );
			vmsUnit.setVmsUnitReference( vuvr );
			
			List<D2VMSMessage> vrms = vum.getVmsMessages();
			
			Map<Integer,D2VMSMessage> vrmsI = new HashMap<>();
			
			Set<ObjectKey> taObjKeys = new HashSet<>(); 
			List<DWiStaTextArea> textAreas = reader.getDWiStaTextAreas( vum.getId() ); 
			
			if( ( textAreas != null ) && !textAreas.isEmpty() ) {
				
				for( DWiStaTextArea textArea : textAreas ) {
					taObjKeys.addAll( textArea.getObjectKeys() );
					_VmsUnitVmsIndexVms vuviv = createDWiStaTextGroupVms( textArea, vrms );
					vmsUnit.getVms().add( vuviv );
				}
			}
			
			if( vrms.isEmpty() && ( ( textAreas == null ) || textAreas.isEmpty() ) ) {
				aqsWithoutMsgs.add( vum.getId() );
			}
			
			for( D2VMSMessage vrm : vrms )  // NOSONAR
			{
				if( vrm == null )
				{
					LOGGER.error( "!!! Empty D2VMSMessage object !!!" );
					LOGGER.error( "Unit: " + vum.getId() );
					continue;
				}
				
				ObjectKey objKey = vrm.getObjectKey();
				if( !taObjKeys.contains( objKey ) ) {
					
					Integer vmsIndex = reader.getVmsIndex( objKey );
					if( vmsIndex == null )
					{
						LOGGER.error( "No vmsIndex for object <" + vrm.getObjectKey() + ">" );
						continue;
					}
					
					vrmsI.put( vmsIndex, vrm );
				}
			}	
			
			for( D2VMSMessage vrm : vrmsI.values() )
			{
				_VmsUnitVmsIndexVms vuviv = createVms( vrm, reader );
				vmsUnit.getVms().add( vuviv );
			}
			
			vmsPublication.getVmsUnit().add( vmsUnit );
		}
		
		if( !aqsWithoutMsgs.isEmpty() ) {
			LOGGER.debug( "" );
			LOGGER.debug( "AQs without messages:" );
			for( String id : aqsWithoutMsgs ) {
				LOGGER.debug( id );
			}
			LOGGER.debug( "" );
		}
		
		return pub;
	}
	
	
	/**
	 * 
	 * Serializes DATEX-II publication 
	 * 
	 * @param publication		DATEX-II publication
	 * @return					XML document
	 * @throws VMSException		Conversion error
	 */
	public String toString( D2LogicalModel publication )
			throws VMSException
	{
        try 
        {
    		return jaxbUtil.getDocument( publication, 
    				              		 "d2LogicalModel", 
    				              		 D2LogicalModel.class );
    		
        } catch ( Exception ex) {
	       	throw new VMSException( VMSException.ERROR_XML, ex.toString(), ex );
        }
	}
	
	
}