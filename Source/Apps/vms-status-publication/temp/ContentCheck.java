package de.heuboe.datex2.vms.status.pub.check;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSUnitMessage;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.datex2.vms.status.pub.Reader;
import de.heuboe.datex2.vms.status.pub.check.DbData.DbDataSet;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;

public class ContentCheck implements UpdateObserver
{
	private static final Logger LOGGER = Logger.getLogger( ContentCheck.class );
	private Persistence persistence; 
	private Reader reader;
	private Map<ObjectKey,Date> vms2UpdTime = new HashMap<>();
	
	public ContentCheck( Persistence persistence, Reader reader )
	{
		this.persistence = persistence;
		this.reader = reader;
	}

	@Override
	public void notifyUpdate( List<D2VMSUnitMessage> msgs, boolean snapshot )
	{
		boolean initial = vms2UpdTime.isEmpty();
		
		
		try
		{
			LOGGER.info( "ContentCheck.checkData()" );
			if( snapshot )
			{
				LOGGER.info( "snapshot" );
			}
			
			Date now = new Date();
			Date lastUpdateTime = new Date( now.getTime() - 3000L );
			
			Set<String> ids = new HashSet<>();
			for( D2VMSUnitMessage msg : msgs ) 
			{
				List<D2VMSMessage> wzgMsgs = msg.getVmsMessages();
				for( D2VMSMessage wzgMsg : wzgMsgs )
				{
					ids.add( wzgMsg.getObjId() );
				}
				
				D2VMSUnit unit = reader.getUnit( msg.getId() );
				List<D2VMSDisplay> displays = unit.getDisplays();
				for( D2VMSDisplay display : displays )
				{
					for( D2VMSDisplayPart part : display.getDisplayParts() )
					{
						ids.add( part.getInternalId() );
					}
				}
			}
			DbDataSet dataSet = persistence.getData( ids );
			
			LOGGER.info( "# D2VMSUnitMessage: " + msgs.size() );
			for( D2VMSUnitMessage msg : msgs ) 
			{
				List<D2VMSMessage> wzgMsgs = msg.getVmsMessages();
				for( D2VMSMessage wzgMsg : wzgMsgs )
				{
					DbData data = null;
					String id = null;
					if( wzgMsg.getObjType().equals( ObjectKey.TYPE_WZG ) )
					{
						id = persistence.getEaId( wzgMsg.getObjId() );
						data = dataSet.getData( id );	
						
						if( data != null )
						{
							Date tls = wzgMsg.getTimeLastSet();
							Date time = data.getTime();
							Date systemTime = data.getSystemTime();
							
							/*
							 * Bei Snapshots werden nur Daten gecheckt, 
							 * die älter als 3 Sekunden sind:
							 * während der Anforderung der Snapshot-Daten
							 * könnte sich der Datenbankbestand aktualisiert werden.
							 */
							if( !snapshot || lastUpdateTime.after( systemTime ) )
							{
								String pref = "PUB_CHECK";
								if( lastUpdateTime.after( systemTime ) )
								{
									pref += "_M  ";
								}
								else
								{
									pref += "  ";
								}
								
								if( !tls.equals( time ) )
								{
									LOGGER.error( pref + "Timestamp difference for ID <" + id + "/" + wzgMsg.getObjId() + ">: " + 
								                  "D2 '" + tls.toString() + "' <---> DB '"  + 
											      time.toString() + "'"  );
								}
								
								if( 
									 ( wzgMsg.getOperationCode() == null ) 
									 && 
									 ( wzgMsg.getError() == null )
								  )
								{
									LOGGER.error( "No Datex2 data for ID <" + id + ">" );
								}
								if( 
									( wzgMsg.getOperationCode() != null ) 
									&& 
									!wzgMsg.getOperationCode().equals( "" + data.getCode() ) 
								  )
								{
									LOGGER.error( pref + "'stellcode' difference for ID <" + id + "/" + wzgMsg.getObjId() + ">: " + 
								                  "D2 '" + wzgMsg.getOperationCode() + "' <---> DB '"  + 
								                  data.getCode() + "'"  );
								}
								
								if( ( wzgMsg.getError() != null ) != data.isError() )
								{
									LOGGER.error( "Error difference for ID <" + id + ">" );
								}
							}
							
							if( !initial && snapshot && lastUpdateTime.after( systemTime ) ) {
								
								Date t = vms2UpdTime.get( wzgMsg.getObjectKey() );
								long diff = 0;
								if( t != null ) {
									diff = Math.abs( ( t.getTime() - tls.getTime() ) / 1000L );
								}
								if( ( t == null ) || ( diff > 10 ) ) {
									String pref = "NOTIFICATION_CHECK  ";
									LOGGER.error( pref + "no notification for ID <" + id + 
											      "> and time <" + tls.toString() + ">" );
								}
							}
						}
						else
						{
							LOGGER.error( "No data for ID <" + id + ">" );
						}
						
						vms2UpdTime.put( wzgMsg.getObjectKey(), wzgMsg.getTimeLastSet() );
					}
					else // WWW, dWiSta
					{
						D2VMSUnit unit = reader.getUnit( msg.getId() );
						List<D2VMSDisplay> displays = unit.getDisplays();
						for( D2VMSDisplay display : displays )
						{
							for( D2VMSDisplayPart part : display.getDisplayParts() )
							{
								String iid =part.getInternalId();
								id = persistence.getEaId( iid );
								DbData d = dataSet.getData( id );	
								if( d != null )
								{
									if( data == null )
									{
										data = d;
									}
									else
									{
										if( data.getTime().before( d.getTime() ) )
										{
											data = d;
										}
									}
								}
							}
						}
						
						if( data != null )
						{
							Date tls = wzgMsg.getTimeLastSet();
							Date time = data.getTime();
							Date systemTime = data.getSystemTime();
							
							if( !snapshot || lastUpdateTime.after( systemTime ) )
							{
								String pref = "PUB_CHECK";
								if( lastUpdateTime.after( systemTime ) )
								{
									pref += "_M  ";
								}
								else
								{
									pref += "  ";
								}
								
								if( !tls.equals( time ) )
								{
									LOGGER.error( pref + "Timestamp difference for ID <" + 
								                  msg.getId() + ":" + id + 
								                  ">: " + 
								                  "D2 '" + tls.toString() + "' <---> DB '"  + 
											      time.toString() + "'"  );
								}
							}
							
							if( !initial && snapshot && lastUpdateTime.after( systemTime ) ) {
								
								Date t = vms2UpdTime.get( wzgMsg.getObjectKey() );
								long diff = 0;
								if( t != null ) {
									diff = Math.abs( ( t.getTime() - tls.getTime() ) / 1000L );
								}
								if( ( t == null ) || ( diff > 10 ) ) {
									String pref = "NOTIFICATION_CHECK  ";
									LOGGER.error( pref + "missed notification for ID <" + id + 
											      "> and time <" + tls.toString() + ">" );
								}
							}
							
						}
						else
						{
							LOGGER.error( "No data for ID <" + id + ">" );
						}
						
						vms2UpdTime.put( wzgMsg.getObjectKey(), wzgMsg.getTimeLastSet() );

					}
				}
			}
		}
		catch( Exception ex )
		{
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
		}
	}

}
