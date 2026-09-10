package de.heuboe.c2vba.datex2.join.srp;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.heuboe.c2vba.datex2.join.ResolverPub;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;
import eu.datex2.schema._2._2_0.srp.SituationPublication;
import eu.datex2.schema._2._2_0.srp.Situation;

/**
 * 
 * Srp implementation of ResolverPub
 * 
 * @author peters
 *
 */
public class ResolverPubSrp implements ResolverPub<D2LogicalModel> {
	
	@Override
	public boolean isSnapshot( D2LogicalModel d2lm ) {
		return true;
	}
	
	@Override
	public int getElementCount( D2LogicalModel d2lm ) {
		return ((SituationPublication)d2lm.getPayloadPublication()).getSituation().size();
	}
	
	@Override
	public Date getPubTime( D2LogicalModel d2lm ) {
		return ((SituationPublication)d2lm.getPayloadPublication()).getPublicationTime();
	}

	@Override
	public void updatePub( D2LogicalModel pub, D2LogicalModel updatePub ) {
		SituationPublication sitPub = (SituationPublication)pub.getPayloadPublication();
		SituationPublication updateSitPub = (SituationPublication)updatePub.getPayloadPublication();
		Map<String,Situation> id2Sit = sitPub.getSituation()
				                                .stream() 
				                                .collect( Collectors.toMap( Situation::getId, Function.identity() ) );
		for( Situation situation : updateSitPub.getSituation() ) {
			id2Sit.put( situation.getId(), situation );
		}
		
		sitPub.getSituation().clear();
		sitPub.getSituation().addAll( id2Sit.values() );
	}
	
	@Override
	public void updateTableRef( eu.datex2.schema._2._2_0.D2LogicalModel d2lm, String tableId, String tableVersion ) {
		// There are no TableRefs
	}
	
	@Override
	public void clearElements( eu.datex2.schema._2._2_0.D2LogicalModel d2lm ) {
		((eu.datex2.schema._2._2_0.SituationPublication)d2lm.getPayloadPublication()).getSituation().clear();
	}


	@Override
	public D2LogicalModel clone(D2LogicalModel d2lm) {
		return (D2LogicalModel)d2lm.clone();
	}

	@Override
	public String getTableId(D2LogicalModel d2lm) {
		return "";
	}

	@Override
	public String getTableVersion(D2LogicalModel d2lm) {
		return "";
	}
}
