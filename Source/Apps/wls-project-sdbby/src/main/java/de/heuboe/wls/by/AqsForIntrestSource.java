package de.heuboe.wls.by;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.Filter;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.tcc.TccPointFeature;
import de.heuboe.wls.data.tcc.TccPointFeatureGroup;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.roadmap.RoadNet;
import de.heuboe.wls.utils.CoordinateTransformer;
import de.heuboe.wls.utils.MultiSource;

/**
 * This is a Source for SDBBY AQs used to generate a shapefile including all relevant 
 * AQ data. This serves as an exchange format with the INTREST database.
 * 
 * @author ralfz
 *
 */
public class AqsForIntrestSource implements MultiSource {

	private static final Logger LOGGER = Logger.getLogger(AqsForIntrestSource.class);

	private static List<String> typeNames = Arrays.asList(
			TccPointFeature.class.getName(),
			TccPointFeatureGroup.class.getName()
			);
	
	private static final String TYPE_FIELD = "TYP";
	private static final String ANLAGE_FIELD = "Tabellenblatt";
	private static final String ID_FIELD = "ID";
	private static final String NAME_FIELD = "NAME";
	private static final String BABNR_FIELD = "BAB";
	private static final String BABKM_FIELD = "BABKM";
	private static final String FAHRTRICHTUNG_FIELD = "FAHRTRICHTUNG";
	private static final String LOC_FIELD = "LOC";
	private static final String DIST_FIELD = "DIST";
	private static final String DE_FIELD = "DE";
	private static final String IB_FIELD = "PORT";
	private static final String OSI2_FIELD = "SLAVE";
	private static final String EAK_FIELD = "EAK";
	private static final String EA_FIELD = "EA";
	private static final String AP_FIELD = "AB_ANZEIGEPRINZIP";
	private static final String SP_FIELD = "AB_STEUERUNG";
	private static final String WZGTYP_FIELD = "43";
	private static final String FS_FIELD = "49";
	private static final String LATITUDE_FIELD = "LATITUDE";
	private static final String LONGITUDE_FIELD = "LONGITUDE";
	
	private static final String FAHRTRICHTUNG_PROPERTY = "FAHRTRICHTUNG";
	private static final String BABNR_PROPERTY = "BAB";
	private static final String BABKM_PROPERTY = "BABKM";
	private static final String ANLAGE_PROPERTY = "ANLAGE";
	private static final String AQID_PROPERTY = "AQID";
	private static final String AQTYPE_PROPERTY = "AQTYPE";
	private static final String NODE_PROPERTY = "NODE";
	private static final String FG_PROPERTY = "FG";
	private static final String DE_PROPERTY = "DE";
	private static final String TLSTYP_PROPERTY = "TLSTYP";
	private static final String IB_PROPERTY = "IB";
	private static final String OSI2_PROPERTY = "OSI2";
	private static final String EAK_PROPERTY = "EAK";
	private static final String EA_PROPERTY = "EA";
	private static final String AP_PROPERTY = "ANZEIGEPRINZIP";
	private static final String SP_PROPERTY = "STEUERPRINZIP";
	private static final String WZGTYP_PROPERTY = "WZGTYP";
	private static final String FS_PROPERTY = "FS";

	private String filenameAq;
	private int wlsSrid;
	private GeometryFactory factory;
	private Map<Integer, CoordinateTransformer> transformers = new HashMap<>();
	
	private RoadNet roadNet;
	
	private List<TccPointFeature> features;
	private List<TccPointFeatureGroup> featureGroups;
	
	@Override
	public void init() {
		if (filenameAq == null || wlsSrid == 0 || roadNet == null) {
			throw new IllegalArgumentException("SdbbyFeatureSource.init: not properly initialized");			
		}
		features = new ArrayList<>();
		featureGroups = new ArrayList<>();
		
		factory = new GeometryFactory(new PrecisionModel(), wlsSrid);
		
		readAqs();
	}

	private Geometry createPointGeometry(int srid, double x, double y) throws FactoryException, TransformException {
		if (srid != wlsSrid) {
			CoordinateTransformer transformer = transformers.get(srid);   // NOSONAR
			if (transformer == null) {
				transformer = new CoordinateTransformer(srid, wlsSrid);
				transformers.put(srid, transformer);
			}
			Coordinate[] c = transformer.transform(new Coordinate(x, y));
			return factory.createPoint(c[0]);
		}
		return factory.createPoint(new Coordinate(x, y));
	}
	
	private void readAqs() {
		String[] filenames = filenameAq.split(";");
		for(String filename : filenames) {
			LOGGER.info("reading AQ file: " + filename);
			readAq(filename);
		}
	}
	
	private void readAq(String filename) {
		Properties props = new Properties();
		props.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");

		DataStore store = new CsvDataStore(null,filename,props);
		DataReader reader = store.getReader((Filter)null);		
		String suffix = null;
		try {
			while (reader.hasNext()) {
				Data record = reader.next();
				String type = record.getMember(TYPE_FIELD).getAsString();
				if (!"AQ".equals(type)) {
					continue;
				}
				int de = record.getMember(DE_FIELD).getAsInt();
				if (suffix == null) {
					suffix = "_" + de;
				}
				int node = record.getMember(LOC_FIELD).getAsInt() * 256 + record.getMember(DIST_FIELD).getAsInt();
				
				String id = record.getMember(ID_FIELD).getAsString();
				String name = record.getMember(NAME_FIELD).getAsString() + " DE " + de;
				List<Property> properties = new ArrayList<>();
				addProperty(properties, ANLAGE_PROPERTY, ANLAGE_FIELD, record);
				addProperty(properties, BABNR_PROPERTY, BABNR_FIELD, record);
				addProperty(properties, BABKM_PROPERTY, BABKM_FIELD, record);
				addProperty(properties, FAHRTRICHTUNG_PROPERTY, FAHRTRICHTUNG_FIELD, record);
				addProperty(properties, BABNR_PROPERTY, BABNR_FIELD, record);
				addProperty(properties, BABNR_PROPERTY, BABNR_FIELD, record);
				addProperty(properties, IB_PROPERTY, IB_FIELD, record);
				addProperty(properties, OSI2_PROPERTY, OSI2_FIELD, record);
				addProperty(properties, EAK_PROPERTY, EAK_FIELD, record);
				addProperty(properties, EA_PROPERTY, EA_FIELD, record);
				addProperty(properties, AP_PROPERTY, AP_FIELD, record);
				addProperty(properties, SP_PROPERTY, SP_FIELD, record);
				addProperty(properties, WZGTYP_PROPERTY, WZGTYP_FIELD, record);
				addProperty(properties, FS_PROPERTY, FS_FIELD, record);
								
				properties.add(new Property(AQTYPE_PROPERTY, "AQ"));
				properties.add(new Property(FG_PROPERTY, "4"));
				properties.add(new Property(DE_PROPERTY, ""+de));
				properties.add(new Property(TLSTYP_PROPERTY, "55"));
				properties.add(new Property(NODE_PROPERTY, ""+node));
				properties.add(new Property(AQID_PROPERTY, id));
				double lat = record.getMember(LATITUDE_FIELD).getAsDouble();
				double lon = record.getMember(LONGITUDE_FIELD).getAsDouble();
				if (!id.isEmpty() && lat > 0. && lon > 0.) {
					try {
						Geometry geometry = createPointGeometry(CoordinateTransformer.WGS84_ID, lat, lon);
						id = "AQ_" + id + suffix;
						TccPointFeature feature = new TccPointFeature(id, name, geometry, wlsSrid, properties);
						features.add(feature);
					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create AQ " + id + ": " + e);
					}
					suffix = null;
				}
			}
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
	}
	
	private void addProperty(List<Property> properties, String propName, String field, Data record) {
		String propValue = record.getMember(field).getAsString();
		properties.add(new Property(propName, propValue));
	}
	
	public void setFilenameAq(String filename) {
		this.filenameAq = filename;
	}

	public void setSrid(int srid) {
		this.wlsSrid = srid;
	}

	public void setRoadNet(RoadNet roadNet) {
		this.roadNet = roadNet;
	}

	@Override
	public Set<String> getTypeNames() {
		return new HashSet<>(typeNames);
	}

	@Override
	public Collection<Location> getLocations(String type) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(type);
		} catch (ClassNotFoundException e) {
			LOGGER.error("sdbbyFeatureSource.getLocations: " + e);
			return null;
		}
		if (clazz.isAssignableFrom(TccPointFeature.class)) {
			return new ArrayList<Location>(features);
		}
		if (clazz.isAssignableFrom(TccPointFeatureGroup.class)) {
			return new ArrayList<>(featureGroups);
		}
		return null;
	}

}
