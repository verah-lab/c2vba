package de.heuboe.datex2.vms.status.pub.test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSUnitMessage;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.status.pub.Main;
import de.heuboe.datex2.vms.status.pub.Manager;
import de.heuboe.datex2.vms.status.pub.Parameter;
import de.heuboe.datex2.vms.status.pub.check.DbData;
import de.heuboe.datex2.vms.status.pub.check.DbData.DbDataSet;
import de.heuboe.datex2.vms.status.pub.check.Persistence;
import de.heuboe.datex2.vms.status.pub.check.UpdateObserver;

public class TestContent implements UpdateObserver 
{
	private Persistence   persistence; 
	
	@Test
	public void testContent()
			throws Exception
	{
		String args[] = new String[1];
		args[0] = "@" + System.getProperty("user.dir") + "/src/test/resources/arg/sdbby_all_upd.arg";
		Main main = new Main(); 
		
		Manager manager = main.init( args );
		
		persistence = new Persistence( Parameter.instance().getConnection(), null );
		
		manager.setUpdateObserver( this );
		manager.loop();
	}

	@Override
	public void notifyUpdate( List<D2VMSUnitMessage> msgs, boolean snapshot )
	{
		try
		{
			if( snapshot )
			{
				Set<String> ids = new HashSet<>();
				for( D2VMSUnitMessage msg : msgs ) 
				{
					List<D2VMSMessage> wzgMsgs = msg.getVmsMessages();
					for( D2VMSMessage wzgMsg : wzgMsgs )
					{
						ids.add( wzgMsg.getObjId() );
					}
				}
				DbDataSet dataSet = persistence.getData( ids );
				
				for( D2VMSUnitMessage msg : msgs ) 
				{
					List<D2VMSMessage> wzgMsgs = msg.getVmsMessages();
					for( D2VMSMessage wzgMsg : wzgMsgs )
					{
						if( wzgMsg.getObjType().equals( ObjectKey.TYPE_WZG ) )
						{
							String id = persistence.getEaId( wzgMsg.getObjId() );
							DbData data = dataSet.getData( id );	
							if( data != null )
							{
								Date tls = wzgMsg.getTimeLastSet();
								Date time = data.getTime();
								
								if( !tls.equals( time ) )
								{
									System.out.println( "Timestamp difference for ID <" + id + ">" );
								}
								
								if( 
									 ( wzgMsg.getOperationCode() == null ) 
									 && 
									 ( wzgMsg.getError() == null )
								  )
								{
									System.out.println( "No Datex2 data for ID <" + id + ">" );
								}
								if( 
									( wzgMsg.getOperationCode() != null ) 
									&& 
									!wzgMsg.getOperationCode().equals( "" + data.getCode() ) 
								  )
								{
									System.out.println( "'stellcode' difference for ID <" + id + ">" );
								}
								
								if( ( wzgMsg.getError() != null ) != data.isError() )
								{
									System.out.println( "Error difference for ID <" + id + ">" );
								}
							}
							else
							{
								System.out.println( "No data for ID <" + id + ">" );
							}
						}
					}
				}
			}
		}
		catch( Exception ex )
		{
			System.out.println( ex.toString() );
			ex.printStackTrace();
		}
	}
}
