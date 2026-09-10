package de.heuboe.datex2.mst.builder.ws;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.measure.D2MeasureMstObj;
import de.heuboe.datex2.mst.builder.D2MSTDataReader;
import de.heuboe.mst.config.lib.MstContent;


/**
 * 
 * In the course of creating an MST the created MstContent used as data for D2MSTDataProvider
 * 
 * @author peters
 *
 */
public class D2MSTDataProvider implements D2MSTDataReader {
	
	private MstContent mstContent = null;

	@Override
	public D2MeasureMstObj getMST() {
		return new D2MeasureMstObj( mstContent.getMst(), mstContent.getVersion(), mstContent.getDescription() );
	}

	@Override
	public List<D2MeasureItem> getItems() {
		return mstContent.getItems();
	}

	@Override
	public Map<Integer, D2MeasureLoc> getLocations() {
		return mstContent.getLocations().stream().collect( Collectors.toMap( D2MeasureLoc::getId, Function.identity() ) );
	}

	@Override
	public Map<Integer, D2MeasureDataSource> getDataSources() {
		return mstContent.getSources().stream().collect( Collectors.toMap( D2MeasureDataSource::getId, Function.identity() ) );
	}
	
	public void setData( MstContent mstContent ) {
		this.mstContent = mstContent;
	}

	@Override
	public void readDBSupply( String mstId, String version, boolean onlyActive ) throws D2ExceptionBase {
		// Data is provided by setData()
	}
}
