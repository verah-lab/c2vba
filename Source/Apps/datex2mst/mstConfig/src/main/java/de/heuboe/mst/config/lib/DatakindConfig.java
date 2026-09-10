package de.heuboe.mst.config.lib;

import java.util.List;

import de.heuboe.mst.config.DatakindMapping;
import de.heuboe.mst.config.Mapping;

public class DatakindConfig
{
	private DatakindMapping datakindMapping;
	
	public DatakindConfig( DatakindMapping datakindMapping )
	{
		this.datakindMapping = datakindMapping;
	}
	
	public Mapping getMapping( String valCol, String valColChar )
	{
		
		String vcc = valColChar;
		if( vcc == null ) {
			vcc = "";
		}
		
		List<Mapping> mps = datakindMapping.getMapping();
		for( Mapping mp : mps )
		{
			String characteristic = mp.getIn().getValColumnCharacteristic();
			if( characteristic == null )  {
				characteristic = "";
			}
			if( 
				mp.getIn().getValColumn().equals( valCol ) 
				&&
				characteristic.equals( vcc ) 
			  )	
				return mp;	
		}
		
		return null;
	}
	
	public Datakind getDatakind()
	{
		return new Datakind( datakindMapping.getDatakindDesc() );
	}
}
