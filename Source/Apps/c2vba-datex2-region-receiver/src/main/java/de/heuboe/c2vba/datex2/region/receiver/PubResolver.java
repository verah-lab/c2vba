package de.heuboe.c2vba.datex2.region.receiver;

import de.heuboe.c2vba.datex2.kafka.C2VbaException;
import de.heuboe.c2vba.datex2.kafka.PublicationResolver;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;
import eu.datex2.schema._2._2_0.MeasuredDataPublication;
import eu.datex2.schema._2._2_0.PayloadPublication;
import eu.datex2.schema._2._2_0.SituationPublication;
import eu.datex2.schema._2._2_0.VmsPublication;


/**
 * 
 * Retrieves content from a publication
 * 
 * @author peters
 *
 */
public class PubResolver extends PublicationResolver<D2LogicalModel> {

	public PubResolver(Class<D2LogicalModel> clasT, String schema, JAXBUtil jaxbUtil) {
		super(clasT, schema, jaxbUtil);
	}

	@Override
	public int getElementCount( D2LogicalModel d2lm ) throws C2VbaException {
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof SituationPublication ) {
			return ((SituationPublication)pp).getSituation().size();
		} else if( pp instanceof MeasuredDataPublication ) {
			return ((MeasuredDataPublication)pp).getSiteMeasurements().size();
		} else if( pp instanceof VmsPublication ) {
			return ((VmsPublication)pp).getVmsUnit().size();
		}
		return 0;
	}

	@Override
	public String getTableId( D2LogicalModel d2lm ) throws C2VbaException { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof MeasuredDataPublication ) {
			return ((MeasuredDataPublication)pp).getMeasurementSiteTableReference().getId();
		} else if( pp instanceof VmsPublication && !((VmsPublication)pp).getVmsUnit().isEmpty() ) {
			return ((VmsPublication)pp).getVmsUnit().get(0).getVmsUnitTableReference().getId();
		}
		return "";
	}

	@Override
	public String getTableVersion( D2LogicalModel d2lm ) throws C2VbaException { 
		PayloadPublication pp = d2lm.getPayloadPublication();
		if( pp instanceof MeasuredDataPublication ) {
			return ((MeasuredDataPublication)pp).getMeasurementSiteTableReference().getVersion();
		} else if( pp instanceof VmsPublication && !((VmsPublication)pp).getVmsUnit().isEmpty() ) {
			return ((VmsPublication)pp).getVmsUnit().get(0).getVmsUnitTableReference().getVersion();
		}
		return "";
	}

}
