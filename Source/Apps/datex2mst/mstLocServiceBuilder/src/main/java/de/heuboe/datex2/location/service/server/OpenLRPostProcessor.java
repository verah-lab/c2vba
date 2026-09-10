package de.heuboe.datex2.location.service.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.location.service.D2MeasurementSite;
import de.heuboe.datex2.mst.builder.D2MSTPubPostProcessor;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;
import eu.datex2.schema._2._2_0.GroupOfLocations;
import eu.datex2.schema._2._2_0.MeasurementSiteRecord;
import eu.datex2.schema._2._2_0.MeasurementSiteTablePublication;
import eu.datex2.schema._2._2_0.Point;

public class OpenLRPostProcessor implements D2MSTPubPostProcessor {
	
	private Map<String,D2MeasurementSite> sites;
	private JAXBUtil jaxbUtil;
	
	public OpenLRPostProcessor() throws JAXBException {
		jaxbUtil = new JAXBUtil( SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE,
								 SchemaUtil.DATEX2_SCHEMA_PACK, 
								 "http://datex2.eu/schema/2/2_0", 
								 true );
		
	}

	public void setSites(List<D2MeasurementSite> msites ) {
		sites = new HashMap<>(); 
		msites.forEach( s -> sites.put( s.getObjId(), s ) );;
	}

	@Override
	public String process(String content) throws D2ExceptionBase {
		
		try {
			D2LogicalModel d2lm = jaxbUtil.getObject( content );
			MeasurementSiteTablePublication mst = (MeasurementSiteTablePublication)d2lm.getPayloadPublication();
			for( MeasurementSiteRecord ms : mst.getMeasurementSiteTable().get(0).getMeasurementSiteRecord() ) {
				String id = ms.getId();
				D2MeasurementSite site = sites.get( id );
				if( site != null ) {
					GroupOfLocations openLRLocation = site.getOpenLRLocation(); 
					if( openLRLocation != null ) {
						Point point = (Point)ms.getMeasurementSiteLocation();
						Point p = (Point)openLRLocation;
						point.setPointExtension( p.getPointExtension() );
					}
				}
			}
			
			String extContent = jaxbUtil.getDocument( d2lm, "d2LogicalModel", D2LogicalModel.class );
			return extContent;
		} catch (JAXBException ex ) {
			throw new D2Exception( D2Exception.D2_ERR_GENERAL_PUB_EXCEPTION, 
					               "Error adding OpenLR locations", ex );
		}
	}

}
