package de.heuboe.datex2.shp.writer.shapefile;

import de.heuboe.datex2.base.D2CoordTrans;
import de.heuboe.datex2.measure.D2MeasureDataReaderImpl;
import de.heuboe.datex2.shp.writer.shapefile.D2RdsTmcShpWriter.D2ShpConfig;
import de.heuboe.ddp.Connection;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			D2CoordTrans.init();
			
			Parameter.createInstance( args );
			
			Connection connection = Parameter.instance().getConnection();
			
			D2ShpRdsTmcSupply.setConnection( connection );
	
			D2RdsTmcShpWriter sfw = new D2RdsTmcShpWriter();
			D2ShpConfig cfg = new D2ShpConfig();
			cfg.setLocSrvAddress( Parameter.instance().getLocStatelessServerAddress() );
			cfg.setShapefilePath( Parameter.instance().getShapefilePath() );
			
			D2MeasureDataReaderImpl locationReader = new D2MeasureDataReaderImpl();
			locationReader.readDBSupply( connection, 
										 Parameter.instance().getMstId(), 
										 Parameter.instance().getMstVersion(), false );
			
			sfw.write( cfg, locationReader.getLocations().values() );
			
			
		}
		catch( Throwable ex )
		{
			System.out.println( ex.toString() );
			ex.printStackTrace();
		}
		
	}
}
