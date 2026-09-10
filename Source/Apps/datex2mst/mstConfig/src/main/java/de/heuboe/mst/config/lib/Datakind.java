package de.heuboe.mst.config.lib;

import java.util.List;

import de.heuboe.mst.config.Column;
import de.heuboe.mst.config.DatakindDesc;

public class Datakind
{
	private DatakindDesc datakindDesc;
	
	public Datakind( DatakindDesc datakindDesc ) 
	{
		this.datakindDesc = datakindDesc;
	}
	
	public DatakindDesc getDatakindDesc()
	{
		return datakindDesc;
	}
	
	public Column getColumn( String colName )
	{
		List<Column> cols = datakindDesc.getColumn();
		for( Column col : cols )
		{
			if( col.getValColumn().equals( colName ) )
				return col;
		}
		
		return null;
	}
}
