package de.heuboe.datex2.vms.status.pub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSUnitMessage;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSService;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartType;
import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;


/**
 * 
 * Fetches data from VMSService
 * 
 * @author peters
 *
 */
public class Reader
{
	/**
	 * 
	 * DWiSta text area
	 * 
	 * @author peters
	 *
	 */
	public static class DWiStaTextArea {
		
		private int vmsIndex;
		private List<ObjectKey>  textKeys = new ArrayList<>();
		private List<ObjectKey>  pictogramKeys = new ArrayList<>();
		
		public int getVmsIndex()
		{
			return vmsIndex;
		}
		
		public void setVmsIndex(int vmsIndex)
		{
			this.vmsIndex = vmsIndex;
		}
		
		public List<ObjectKey> getTextKeys()
		{
			return textKeys;
		}
		
		public void setTextKeys(List<ObjectKey> textKeys)
		{
			this.textKeys = textKeys;
		}
		
		public List<ObjectKey> getPictogramKeys()
		{
			return pictogramKeys;
		}
		
		public void setPictogramKeys(List<ObjectKey> pictogramKeys)
		{
			this.pictogramKeys = pictogramKeys;
		} 
		
		public Set<ObjectKey> getObjectKeys() {
			
			Set<ObjectKey> objectKeys = new HashSet<>();
			objectKeys.addAll( textKeys );
			objectKeys.addAll( pictogramKeys );
			
			return objectKeys;
		}
	}
	
	
	private static final Logger LOGGER = Logger.getLogger( Reader.class );

	private VMSService vmsService;
	private String tableId;
	private String tableVersion;
	private Map<String,D2VMSUnit> unitId2Unit = new HashMap<>();
	private Map<ObjectKey,D2VMSMessage> currentMessages = new HashMap<>();
	private Map<ObjectKey, Set<String> > objKey2UnitIds = new HashMap<>();
	private Map<ObjectKey, Integer > objKey2VmsIndex = new HashMap<>();
	private Map<String, List<DWiStaTextArea> > unitId2DWiStaTextAreas = new HashMap<>();
	
	
	/**
	 * 
	 * Constructor 
	 * 
	 * @param vmsService		VMSService
	 * @param tableId			Table ID
	 * @param tableVersion		Table version
	 */
	public Reader( VMSService vmsService, String tableId, String tableVersion )
	{
		this.vmsService = vmsService;
		this.tableId = tableId;
		this.tableVersion = tableVersion;
	}
	
	/**
	 * 
	 * Init 
	 * 
	 * @throws VMSException	Error
	 */
	public void init(  )
			throws VMSException
	{
		try
		{
			if( ( tableVersion == null ) || tableVersion.isEmpty() )
			{
				this.tableVersion = vmsService.getCurrentTableVersion( tableId );
			}
			
			D2VMSTable table = vmsService.readVMSTable( tableId, tableVersion );
			
			List<D2VMSUnit> units = table.getUnits();
			for( D2VMSUnit unit : units ) 
			{
				List<D2VMSDisplay> displays = unit.getDisplays();
				for( D2VMSDisplay display : displays )
				{
					objKey2VmsIndex.put( display.getObjectKey(), display.getVmsIndex() );
				}
			}
			
			Set<ObjectKey> objKeys = extractObjKeys( table.getUnits() );
			
			List<D2VMSMessage> messages = vmsService.getVMSMessages(objKeys);
			
			if( messages != null )
			{
				for( D2VMSMessage message : messages ) {
					currentMessages.put( message.getObjectKey(), message );
				}
			}
		} catch( VMSServiceException ex ) {
			throw new VMSException( VMSException.ERROR_VMS_SVC, ex.toString() , ex );
		}
	}
	
	public String getTableVersion()
	{
		return tableVersion;
	}
	
	/**
	 * 
	 * Get Unit for ID
	 * 
	 * @param id	ID
	 * @return		D2VMSUnit
	 */
	public D2VMSUnit getUnit( String id )
	{
		return unitId2Unit.get( id );
	}
	
	private void updateObjKey2UnitIds( ObjectKey objKey, String unitId )
	{
		Set<String> unitIds = objKey2UnitIds.get( objKey );   // NOSONAR
		if( unitIds == null )
		{
			unitIds = new HashSet<>();
			objKey2UnitIds.put( objKey, unitIds );
		}
		unitIds.add( unitId );
		
	}
	
	private Set<ObjectKey> extractObjKeys( List<D2VMSUnit> units )    // NOSONAR
	{
		Set<ObjectKey> objKeys = new HashSet<>();
		
		for( D2VMSUnit unit : units )
		{
			String unitId = unit.getInternalId();
			unitId2Unit.put( unit.getD2Id(), unit );
			
			objKeys.add( unit.getObjectKey() );
			updateObjKey2UnitIds( unit.getObjectKey(), unitId );

			List<D2VMSDisplay> displays = unit.getDisplays();
			for( D2VMSDisplay display : displays )
			{
				if( display.getDisplayType() == DisplayType.DWISTA_TXT_GRP ) {
					
					List<DWiStaTextArea> textAreas = unitId2DWiStaTextAreas.get( unitId );
					if( textAreas == null ) {
						textAreas = new ArrayList<>();
						unitId2DWiStaTextAreas.put( unitId, textAreas );
					}
					
					DWiStaTextArea textArea = new DWiStaTextArea();
					textArea.setVmsIndex( display.getVmsIndex() );
					
					List<D2VMSDisplayPart> displayParts = display.getDisplayParts();
					if( displayParts != null )
					{
						Map<Integer,ObjectKey> textKeys = new TreeMap<>();
						Map<Integer,ObjectKey> pictogramKeys = new TreeMap<>();
						for( D2VMSDisplayPart displayPart : displayParts )
						{
							ObjectKey objKey = displayPart.getObjectKey();
							if( displayPart.getDisplayPartType() == DisplayPartType.Pikt ) {
								pictogramKeys.put( displayPart.getPictogramIndex(), objKey );
							} else if( displayPart.getDisplayPartType() == DisplayPartType.Text ) {
								textKeys.put( displayPart.getPictogramIndex(), objKey );
							}
						}
						
						textArea.setPictogramKeys( pictogramKeys.values().stream().collect( Collectors.toList() ) );
						textArea.setTextKeys( textKeys.values().stream().collect( Collectors.toList() ) );
					}
					
					textAreas.add( textArea );
 				} else {
					objKeys.add( display.getObjectKey() );
					updateObjKey2UnitIds( display.getObjectKey(), unitId );
 				}
				
				List<D2VMSDisplayPart> displayParts = display.getDisplayParts();
				if( displayParts != null )
				{
					for( D2VMSDisplayPart displayPart : displayParts )
					{
						objKeys.add( displayPart.getObjectKey() );
						updateObjKey2UnitIds( displayPart.getObjectKey(), unit.getInternalId() );
					}
				}
			}
		}
		
		return objKeys;
	}
	
	private void updateCurrentMessages( Set<ObjectKey> objKeys )
			throws VMSException
	{
		try
		{
			if( ( objKeys != null ) && !objKeys.isEmpty() )
			{
				List<D2VMSMessage> messages = vmsService.getVMSMessages(objKeys);
				
				for( D2VMSMessage message : messages )
				{
					ObjectKey objKey = message.getObjectKey();
					D2VMSMessage cm = currentMessages.get( objKey );
					if( cm != null ) {
						cm.update( message );
					} else {
						currentMessages.put( objKey, message );
					}
				}
			}
		} catch( VMSServiceException ex ) {
			throw new VMSException( VMSException.ERROR_VMS_SVC, ex.toString() , ex );
		}
	}
	
	public List<D2VMSUnitMessage> getVMSMessages( Set<ObjectKey> objKeys,
			                                      boolean complete )
			throws VMSException
	{
		updateCurrentMessages( objKeys );
		
		List<D2VMSUnitMessage> ums = new ArrayList<>();
		
		Set<String> updatedUnitIds = new HashSet<>();
		
		if( !complete )
		{
			for( ObjectKey objKey : objKeys )
			{
				Set<String> unitIds = objKey2UnitIds.get( objKey );
				if( unitIds != null ) {
					updatedUnitIds.addAll( unitIds );
				} else {
					LOGGER.error( "No VmsUnit for key <" + objKey.getId() + ">" );
				}
			}
		} else {
			updatedUnitIds = unitId2Unit.keySet();
		}
		
		for( String unitId : updatedUnitIds )
		{
			D2VMSUnit unit = unitId2Unit.get( unitId );
			if( unit != null )
			{
				Set<ObjectKey> keys = new HashSet<>();
				D2VMSMessage message = currentMessages.get( unit.getObjectKey() );
				if( message != null ) {
					D2VMSUnitMessage uMessage = new D2VMSUnitMessage();
					uMessage.setId( unit.getD2Id() );
					
					// Collect messages of displays

					List<D2VMSDisplay> displays = unit.getDisplays();
					for( D2VMSDisplay display : displays )
					{
						ObjectKey objKey = display.getObjectKey();
						if( display.getDisplayType() != DisplayType.DWISTA_TXT_GRP ) {
							if( !keys.contains( objKey ) ) {     // NOSONAR
								
								D2VMSMessage dMessage = currentMessages.get( objKey );
								
								keys.add( objKey );
								
								if( dMessage == null ) {
									LOGGER.warn( "No message for VMS with ID <" + display.getInternalId() + ">" );
								} else {
									uMessage.getVmsMessages().add( dMessage );
								}
							}
						}
						
						List<D2VMSDisplayPart> displayParts = display.getDisplayParts();
						for( D2VMSDisplayPart displayPart : displayParts ) {
							ObjectKey partKey = displayPart.getObjectKey();
							
							if( !keys.contains( partKey ) ) {
								
								keys.add( partKey );
								D2VMSMessage dMessage = currentMessages.get( partKey );
								if( dMessage == null ) {
									if( objKey2VmsIndex.containsKey( partKey ) ) {
										LOGGER.warn( "No message for VMS part with ID <" + displayPart.getInternalId() + ">" );
									}
								} else {
									uMessage.getVmsMessages().add( dMessage );
								}
							}
						}						
					}
					
					ums.add( uMessage );
				} else {
					LOGGER.error( "No message for VMSUnit with ID <" + unitId + ">" );
				}
			} else {
				LOGGER.error( "No VmsUnit for ID <" + unitId + ">" );
			}
		}
		
		return ums;
	}
	
	/**
	 * 
	 * Get VMS index for object ID
	 * 
	 * @param objKey	Object key
	 * @return			VMS index
	 */
	public Integer getVmsIndex( ObjectKey objKey ) 
	{
		return objKey2VmsIndex.get( objKey );
	}
	
	/**
	 * 
	 * Get DWiStaTextAreas for unit ID
	 * 			
	 * @param unitId	Unit ID
	 * @return			DWiStaTextAreas
	 */
	public List<DWiStaTextArea> getDWiStaTextAreas( String unitId ) {
		return unitId2DWiStaTextAreas.get( unitId ); 
	}
}
