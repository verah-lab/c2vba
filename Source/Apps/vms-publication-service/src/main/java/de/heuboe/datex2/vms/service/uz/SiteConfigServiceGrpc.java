package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Empty;

import de.heuboe.log.Logger;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.ims.SiteConfigServiceIface;
import de.heuboe.sitecfg.grpc.IdList;
import de.heuboe.sitecfg.grpc.SiteConfigServiceGrpc.SiteConfigServiceBlockingStub;
import de.heuboe.sitecfg.grpc.SiteItems;
import de.heuboe.sitecfg.grpc.ViewAggrDesc;
import de.heuboe.sitecfg.grpc.XmlProtoConverter;
import de.heuboe.sitecfg.grpc.data.CfgViewAggrDesc;
import de.heuboe.sitesconfig.CfgAggrDesc;
import de.heuboe.sitesconfig.CfgAggrItem;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQs;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * 
 * GRPC version of SiteConfigService
 * 
 * @author peters
 *
 */
public class SiteConfigServiceGrpc implements SiteConfigServiceIface {
	
	private static final Logger LOGGER = Logger.getLogger( SiteConfigServiceGrpc.class );
	
    @GrpcClient("SiteConfigService")
	private SiteConfigServiceBlockingStub grpcSvc;
    
    boolean useAggregatedAqs = true;
    
	private XmlProtoConverter cnv = new XmlProtoConverter();

	private Map<String,String> aqId2AggrAqId = new HashMap<>();
	private Map<String, List<String> > aggrAqId2AqIds = new HashMap<>();
	
	public SiteConfigServiceGrpc( boolean useAggregatedAqs ) {
		this.useAggregatedAqs = useAggregatedAqs; 
	}
	
	@Override
    public boolean connect() {     // NOSONAR
		
		if( useAggregatedAqs ) {
			try {
				List<ViewAggrDesc> gvads = grpcSvc.getAllViewAggrDescs( Empty.newBuilder().build() ).getItemsList();
				List<CfgViewAggrDesc> vads = cnv.viewAggrDesc().toPojos(gvads);
		
				if( vads != null ) {
					for( CfgViewAggrDesc vad : vads ) {
						for( CfgAggrDesc ad : vad.getItems() ) {
							for( CfgAggrItem ai : ad.getItem() ) {
								aqId2AggrAqId.put( ai.getId(), ad.getId() ); 
								aggrAqId2AqIds.computeIfAbsent( ad.getId(), a -> new ArrayList<>() ).add( ai.getId() );
							}
						}
					}
					
					LOGGER.info( "Aggregated AQs" ); 
					for( Map.Entry<String, List<String> > entry : aggrAqId2AqIds.entrySet()) {
						LOGGER.info( "    " + entry.getKey() );
						entry.getValue().forEach( id -> LOGGER.info( "        " + id ) );
					} 
				}
			} catch( StatusRuntimeException ex ) {
				LOGGER.warn( "SiteConfigService.getAllViewAggrDescs() not implemented" );
			}
		}
		
		return true;
	}
	
	@Override
    public List<String> getSiteIds() {
		return new ArrayList<>( cnv.idSet(grpcSvc.getSiteIds( Empty.newBuilder().build() ) ) );
    }
	
	@Override
	public List<CfgQ> getAqs( List<String> aqIds ) throws SiteConfigError {
		IdList idLst = cnv.toProto(aqIds.stream().collect( Collectors.toSet()));
		return cnv.dq().toPojoW(grpcSvc.getAqs(idLst)).getQ();
	}
	
	@Override
    public CfgQs getSiteAqs( String siteId ) throws SiteConfigError {
		SiteItems sis = grpcSvc.getSiteItems(cnv.toProto(siteId));
		de.heuboe.sitecfg.grpc.data.CfgSiteItems csis = cnv.si().toPojo( sis );
		
		// Build aggregate AQs from parts
		List<CfgQ> cfgs = new ArrayList<>();
		Map<String,CfgQ> id2Aq = csis.getAqs().getQ().stream().collect( Collectors.toMap( CfgQ::getId, Function.identity() ) );
		for( Map.Entry<String, List<String> > entry :  aggrAqId2AqIds.entrySet() ) {
			List<String> ids = entry.getValue();
			List<CfgQ> acfgs = new ArrayList<>();
			for( String id : ids ) {
				CfgQ cfg = id2Aq.remove( id );
				if( cfg != null ) {
					acfgs.add( cfg );
				}
				
			}
			if( !acfgs.isEmpty() ) {
				if( ids.size() != acfgs.size() ) {
					throw new SiteConfigError("Incomplete aggregated AQ <" + entry.getKey() + ">" );
				}
				CfgQ aggrCfg = aggregateAqs( entry.getKey(), acfgs );
				cfgs.add( aggrCfg );
			}
		}
		
		// Add remaining AQs
		cfgs.addAll( id2Aq.values() );
		
		return new CfgQs( cfgs );
    }
	
	@Override
	public String getPartId( String aqId ) {
		List<String> partIds = aggrAqId2AqIds.get( aqId );
		if( partIds != null ) {
			return partIds.get(0);
		}
		
		return aqId;
	}

	
	private CfgQ aggregateAqs( String aggrAqId, List<CfgQ> aqs ) {
		CfgQ aggrAq = new CfgQ();
		aggrAq.setId( aggrAqId );
		
		for( CfgQ aq : aqs ) {
			aggrAq.getDevice().addAll( aq.getDevice() );
		}
		
		return aggrAq;
	}
}
