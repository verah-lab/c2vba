package de.heuboe.datex2.vms.service.datamsg;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSPictogram;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSDataListener.Identity;
import de.heuboe.datex2.vms.service.uz.DataManager;
import de.heuboe.log.Logger;

public class GeoDataMsgDataListener extends DataManager.DataListener {
	
	private static final Logger LOGGER = Logger.getLogger( GeoDataMsgDataListener.class );
	private GeoDataMsgSvcClient svcClient;
	
	public GeoDataMsgDataListener( GeoDataMsgSvcClient svcClient, Identity identity, Set<ObjectKey> objectKeys ) {
		super( identity, objectKeys );
		this.svcClient = svcClient;
	}

	@Override
	public void notityUpdates( Map<ObjectKey,D2VMSMessage> msgs ) {
		
		for (Map.Entry<ObjectKey, D2VMSMessage> entry : msgs.entrySet() )
		{
			ObjectKey ok = entry.getKey();
			if( objectKeys.contains( ok ) && ok.getType().equals( "AQ" ) ) 
			{
				
				List<D2VMSPictogram> ps = entry.getValue().getPictograms(); 
				if( ( ps != null ) && !ps.isEmpty() ) {
					svcClient.sendDataMsg( ok.getId(), ps.get(0).getImageFilePath() );
				} else {
					LOGGER.info( "No pictogram for AQ " + entry.getKey().getId() );
				}
			}
		}
	}
	
}
