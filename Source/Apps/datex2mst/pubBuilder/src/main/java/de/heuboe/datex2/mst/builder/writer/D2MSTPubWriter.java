package de.heuboe.datex2.mst.builder.writer;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.measure.D2MeasureMstObj;
import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.D2MSTDataReader;
import de.heuboe.datex2.push.D2Publisher;
import de.heuboe.log.Logger;

/**
 * 
 * Erstellt das MST-XML-Dokument aus den vom D2MSTDataReader gelieferten Daten und ruft damit die publish-Methode aller D2Publisher auf.
 * 
 * @author peters
 *
 */
public class D2MSTPubWriter
{
	private static final Logger LOGGER = Logger.getLogger( "de.heuboe.datex2.mst.builder.writer" );
	
	private D2MSTPubWriter() {
		
	}
	
	public static String write( // NOSONAR
			  				  D2MSTConf conf,
			  				  D2MSTDataReader dataReader,
					          String pubInstance, 
					          String pubFilePath,
					          String country,
					          String natId,
					          String lang, 
					          List<D2Publisher> publishers )
			throws D2ExceptionBase
	{
		D2MeasureMstObj mst = dataReader.getMST();
		List< D2MeasureItem > items = dataReader.getItems();
		Map< Integer, D2MeasureLoc > locations = dataReader.getLocations();
		Map< Integer, D2MeasureDataSource >dataSources = dataReader.getDataSources();
		
		Date now = D2MSTConf.now();

		String header = D2MSTPubHeaderWriter.getMSTHeader(  conf,
															mst.getName(), 
															mst.getDescr(), 
															mst.getVersion(), 
															now, 
															country, 
															natId, 
															lang );
		
		// Für jede Location wird ein MeasurementSiteRecord erzeugt
		
		// Items nach Loc-Id sortieren
		
		HashMap< Integer, LinkedList< D2MeasureItem > > locItems = new HashMap<>();
		
		for( D2MeasureItem item : items )
		{
			int locId = item.getLocId();
			
			LinkedList< D2MeasureItem > i = locItems.get( locId );
			
			if( i == null )
			{
				i = new LinkedList<>();
				locItems.put( locId, i );
			}
			
			i.add( item );
		}
		
		// Namen der Locations, werden als MST-Ids verwendet
		Set< String > msIdsSoFar = new HashSet<>();
		
		StringBuffer recordContent = new StringBuffer();    // NOSONAR
	
		Iterator<Map.Entry< Integer, D2MeasureLoc > > it = locations.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	    	Map.Entry< Integer, D2MeasureLoc > e = it.next(); 
		
	    	D2MeasureLoc loc = e.getValue();
	    	
			LinkedList< D2MeasureItem > i1 = locItems.get( loc.getId() );
			
			if( i1 == null )
			{
				LOGGER.error( "Fehler in D2MSTPubWriter::write()" );
				LOGGER.error( "!!! Keine Objekte zur Location '" + loc.getLocName() + "' !!!" );
			}
			else
			{
				LinkedList< D2MeasureItem > i2 = i1;
				
		    	if( i2.isEmpty() ) {
		    		continue;
		    	}
		    	
				String lc = D2MSTPubRecordWriter.getContent( conf,
															 mst.getVersion(),
															 loc, 
														     i2, 
														     dataSources,
														     msIdsSoFar );
				recordContent.append( lc );	
				
				msIdsSoFar.add( loc.getLocName() );
			}
		}

		String content = header + recordContent.toString() +  D2MSTPubHeaderWriter.getMSTTail();
	
		String fileName = null;
		for( D2Publisher publisher : publishers )
		{
			String fn = publisher.publish(content, pubFilePath, mst.getName(), mst.getVersion());
			if( ( fn != null ) && !fn.isEmpty() && ( fileName == null ) )
				fileName = fn;
		}
		
	
		if( ( fileName != null ) && !fileName.isEmpty() )
 			LOGGER.info( "Publication written to <" + fileName + ">" );
		
		return fileName;
	}
}
