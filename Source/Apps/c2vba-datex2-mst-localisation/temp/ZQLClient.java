package de.heuboe.by.c2vba.datex2.mst.localisation.server.zql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.heuboe.by.c2vba.datex2.mst.localisation.server.zql.MLFCorbaClient.CORBAException;
import de.heuboe.log.Logger;
import de.heuboe.zqlServerProxy.ErrorSequence;
import de.heuboe.zqlServerProxy.LaneDescResult;
import de.heuboe.zqlServerProxy.managerHelper;
import de.heuboe.zqlServerProxy.managerOperations;
import eu.datex2.schema._2._2_0.LaneEnum;


/**
 * 
 * ZQL-Server-Proxy
 * 
 * @author peters
 *
 */
public class ZQLClient
{
    
    private static final Logger LOGGER = Logger.getLogger( ZQLClient.class ); 
    
    
	private MLFCorbaClient<managerOperations> zqlServer = null;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param zqlSrvAddress   ZQL-Server CORBA address
	 * @throws Exception      Exception
	 */
	public ZQLClient( String zqlSrvAddress ) throws Exception  // NOSONAR
	{
		zqlServer = new MLFCorbaClient<>( zqlSrvAddress, managerHelper.class);
		
	}
	
	/**
	 * 
	 * Retrieve lanes for MQs
	 * 
	 * @param qids     MQ-IDs
	 * @param type     Q-Type
	 * @return         LaneData
	 */
	public Map< Integer, List<LaneData> > qid2LaneData( List<Integer> qids, int type ) {
	    
	    
	    Map< Integer, List<LaneData> > result = new HashMap<>();
	    
        try
        {
            LaneDescResult[]  laneDescss = zqlServer.getService().getLaneDescs( qids.stream().mapToInt(i->i).toArray(), "Q", -1, false );
            for( LaneDescResult laneDescs : laneDescss )
            {
                List<LaneData> laneDatas = Arrays.stream( laneDescs.lanes )
                                                       .map( l -> new LaneData( l.laneid, l.lanetxt, l.lanelage ) )
                                                       .collect( Collectors.toList() );
                result.computeIfAbsent( laneDescs.id, q -> new ArrayList<>() ).addAll( laneDatas );
            }
            
            return result;
        } catch( CORBAException| ErrorSequence ex ) {  
            throw new RuntimeException( "Error calling 'qid2LaneData()'", ex );    // NOSONAR
        }
	    
	}
}
