package de.heuboe.datex2.vms.status.pub.check;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.datex2.vms.status.pub.VMSException;
import de.heuboe.datex2.vms.status.pub.check.DbData.DbDataSet;
import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.DataFilter;
import de.heuboe.ddp.DatakindFilter;
import de.heuboe.ddp.Dataset;

public class Persistence
{
	private static final String[] DEFAULT_DATA_DK = { "DaWVZStellzustandABIst",
											          "DaWVZStellzustandCDIst", 
											          "DaWVZStellzustandEIst"};
	
	private static final String DK_WECHSEL_TEXT = "DaWVZWechseltextIst";
	private static final String DK_SCHALT_IST = "DaWVZSchaltIst";

	private static final String[] ERROR_DK = { "DaWVZFehlerDEIst" };
	
	private static final String PERMANENT_ID_DK = "unl_nb_permanent_id";
	
	private Map<String,String> permanentId2EaId = new HashMap<>();
	
	private Connection connection;
	private Set<String> datakinds = null;
	
	public Persistence( Connection connection ) throws VMSException
	{
		this( connection, null );
	}

	public Persistence( Connection connection, Set<String> datakinds ) throws VMSException
	{
		try {
		this.connection = connection;
		this.datakinds = datakinds;
		
		init(); 
		} catch( DDPException ex ) {
			throw new VMSException( VMSException.ERROR_DB, "Error initialising database test supply", ex );
		}
	}
	
	private void init() throws DDPException {
		
		DatakindFilter dkFilter = new DatakindFilter( PERMANENT_ID_DK );
        Container[] results = connection.read( new DataFilter(dkFilter) ); 
        for( Container co : results )
        {
        	for( Dataset ds : co )
        	{
        		permanentId2EaId.put( ds.value("permanent_id").getString(), ds.value("id").getString() );
        	}
        }
	}
	
	private List<String> getDatakinds() {
		if( ( datakinds == null ) || ( datakinds.isEmpty() ) ) {
			return Arrays.asList( DEFAULT_DATA_DK );
		} else {
			return datakinds.stream().collect( Collectors.toList());
		}
	}
	
	private Date getTime( Dataset ds, String datakind ) throws DDPException {
		
		if( datakind.equals( DK_WECHSEL_TEXT ) || datakind.equals( DK_SCHALT_IST ) ) {
			return ds.value( "time" ).getDate();
		}
		
		return ds.value( "time" ).getDate();
	}
	
	private Date getSystemTime( Dataset ds, String datakind ) throws DDPException {
		
		return ds.value( "time" ).getDate();
	}

	private short getStellcode( Dataset ds, String datakind ) throws DDPException {
		
		if( datakind.equals( DK_SCHALT_IST ) ) {
			return ds.value( "WVZCode" ).getShort();
		}
		if( datakind.equals( DK_WECHSEL_TEXT ) ) {
			return 0;
		}
		
		return ds.value( "stellcode" ).getShort();
	}
	
 	
	private boolean isEnabled( Dataset ds, String datakind ) throws DDPException {
		if( datakind.equals( DK_WECHSEL_TEXT ) || datakind.equals( DK_SCHALT_IST ) ) {
			return ( ds.value( "Funktion" ).getShort() == 0 );
		}

		return  ( ds.value( "funktionsbyte" ).getShort() == 0 );
	}

	public DbDataSet getData( Set<String> ids )
			throws DDPException
	{
		DbDataSet dataSet = new DbDataSet();
		
		List<String> dks = getDatakinds();
		
		for( String dataDk : dks )
		{
			DatakindFilter dkFilter = new DatakindFilter( dataDk );
	
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	
	        for( Container co : results )
	        {
	        	for( Dataset ds : co )
	        	{
	        		String id = "" + ds.value( "eaid" );
	        		
	        		
	        		int code = getStellcode( ds, dataDk );
	        		Date time = getTime( ds, dataDk );
	        		Date system_time = getSystemTime( ds, dataDk ); 
	        		
	        		DbData data = dataSet.getData( id );
	        		if( data == null )
	        		{
	        			data = new DbData();
	        			data.setId( id );
	        			dataSet.add( data );
	        		}
	        		data.setTime( time );
	        		data.setSystemTime( system_time );
	        		
	        		if( isEnabled( ds, dataDk ) )
	        		{
	        			data.setCode( -1 );
	        		}
	        		else
	        		{
	        			data.setCode(code );
	        		}
		        }
	        }
		}
		
		for( String errorDk : ERROR_DK )
		{
			DatakindFilter dkFilter = new DatakindFilter( errorDk );
	
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	
	        for( Container co : results )
	        {
	        	for( Dataset ds : co )
	        	{
	        		String id = "" + ds.value( "eaid" );
	        		Date time = getTime( ds, errorDk );
	        		Date system_time = getSystemTime( ds, errorDk ); 
	        		
	        		DbData data = dataSet.getData( id );
	        		if( data != null )
	        		{
		        		int error = ds.value( "Fehlercode" ).getShort();
		        		if( error != 0 )
		        		{
		        			data.setError( true );
		        		}
		        		
		        		if( time.after( data.getTime()  ) ) {
		        			data.setTime( time );
		        		}
		        		if( system_time.after( data.getSystemTime()  ) ) {
		        			data.setSystemTime(system_time);
		        		}
	        		}
		        }
	        }
		}
		
		
		return dataSet;
	}
	
	public String getEaId( String pId )
	{
		return permanentId2EaId.get( pId );
	}
}
