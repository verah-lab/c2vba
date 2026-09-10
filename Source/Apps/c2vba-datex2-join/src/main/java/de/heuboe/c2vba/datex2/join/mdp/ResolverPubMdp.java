package de.heuboe.c2vba.datex2.join.mdp;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.heuboe.c2vba.datex2.join.ResolverPub;
import eu.datex2.schema._2._2_0.mdp.D2LogicalModel;
import eu.datex2.schema._2._2_0.mdp.MeasuredDataPublication;
import eu.datex2.schema._2._2_0.mdp.PayloadPublication;
import eu.datex2.schema._2._2_0.mdp.SiteMeasurements;


/**
 * 
 * Implementation of ResolverPub for MeasuredDataPublication
 * 
 * @author peters
 *
 */
public class ResolverPubMdp implements ResolverPub<D2LogicalModel> {
	
	@Override
	public boolean isSnapshot( D2LogicalModel d2lm ) {
		return true;
	}
	
	@Override
	public int getElementCount( D2LogicalModel d2lm ) {
		return ((MeasuredDataPublication)d2lm.getPayloadPublication()).getSiteMeasurements().size();
	}
	
	@Override
	public Date getPubTime( D2LogicalModel d2lm ) {
		
		return ((MeasuredDataPublication)d2lm.getPayloadPublication()).getPublicationTime();
	}
	

	@Override
	public void updatePub( D2LogicalModel pub, D2LogicalModel updatePub ) {
		MeasuredDataPublication mdp = (MeasuredDataPublication)pub.getPayloadPublication();
		MeasuredDataPublication updateMdp = (MeasuredDataPublication)updatePub.getPayloadPublication();
		Map<String,SiteMeasurements> id2sm = mdp.getSiteMeasurements()
				                                .stream() 
				                                .collect( Collectors.toMap( v -> v.getMeasurementSiteReference().getId(), Function.identity() ) );
		for( SiteMeasurements sm : updateMdp.getSiteMeasurements() ) {
			id2sm.put( sm.getMeasurementSiteReference().getId(), sm );
		}
		
		mdp.getSiteMeasurements().clear();
		mdp.getSiteMeasurements().addAll( id2sm.values() );
	}
	
	@Override
	public void updateTableRef( eu.datex2.schema._2._2_0.D2LogicalModel d2lm, String tableId, String tableVersion ) {
		eu.datex2.schema._2._2_0.MeasuredDataPublication mdp = (eu.datex2.schema._2._2_0.MeasuredDataPublication)d2lm.getPayloadPublication();
		mdp.getMeasurementSiteTableReference().setId( tableId );
		mdp.getMeasurementSiteTableReference().setVersion( tableVersion );
	}
	
	@Override
	public void clearElements( eu.datex2.schema._2._2_0.D2LogicalModel d2lm ) {
		((eu.datex2.schema._2._2_0.MeasuredDataPublication)d2lm.getPayloadPublication()).getSiteMeasurements().clear();
	}


	@Override
	public D2LogicalModel clone(D2LogicalModel d2lm) {
		return (D2LogicalModel)d2lm.clone();
	}
	
	@Override
	public String getTableId( D2LogicalModel d2lm ) { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof MeasuredDataPublication ) {
			return ((MeasuredDataPublication)pp).getMeasurementSiteTableReference().getId();
		}
		return "";
	}

	@Override
	public String getTableVersion( D2LogicalModel d2lm ) { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof MeasuredDataPublication ) {
			return ((MeasuredDataPublication)pp).getMeasurementSiteTableReference().getVersion();
		}		
		return "";
	}

}
