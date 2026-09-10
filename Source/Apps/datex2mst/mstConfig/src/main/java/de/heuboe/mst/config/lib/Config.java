package de.heuboe.mst.config.lib;

import java.util.List;

import de.heuboe.mst.config.DatakindMapping;
import de.heuboe.mst.config.EnumValueDataType;
import de.heuboe.mst.config.MappingConfig;

public class Config
{
	private MappingConfig mappingConfig;
	
	public Config( MappingConfig mappingConfig )
	{
		this.mappingConfig = mappingConfig;
	}
	
	public DatakindConfig getDatakindConfig( String datakind )
	{
		List<DatakindMapping> dkms = mappingConfig.getDatakindMapping();
		for( DatakindMapping dkm : dkms )
		{
			if( dkm.getDatakindDesc().getDatakind().equals( datakind ) )
				return new DatakindConfig( dkm );
		}
		
		return null;
	}

	public static EnumValueDataType getDefaultValueDataType()
	{
		return EnumValueDataType.INTEGER;
	}
}
