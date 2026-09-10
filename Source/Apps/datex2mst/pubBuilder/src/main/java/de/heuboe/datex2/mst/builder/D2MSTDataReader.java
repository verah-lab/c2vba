package de.heuboe.datex2.mst.builder;

import java.util.List;
import java.util.Map;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.measure.D2MeasureMstObj;

public interface D2MSTDataReader {

	D2MeasureMstObj getMST();
	List<D2MeasureItem> getItems();
	Map<Integer, D2MeasureLoc> getLocations();
	Map<Integer, D2MeasureDataSource> getDataSources();
	
	void readDBSupply( String mstId, String version, boolean onlyActive ) throws D2ExceptionBase;

	public static D2MSTDataReader createReader() { return null; }
}
