package de.heuboe.datex2.mst.activator;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.datex2.mst.builder.db.D2MSTConfReader.DefinitionInfo;
import de.heuboe.ddp.Connection;

public class Persistence
{
	Connection connection;
	
	public Persistence( Connection connection )
	{
		this.connection = connection;
	}
	
	
	public List<DefinitionInfo> readMSTDefinitions()
	{
		List<DefinitionInfo> mstDefintions = new ArrayList<>();
		
		return mstDefintions;
	}
	
	public List<DefinitionInfo> readMSTs()
	{
		List<DefinitionInfo> mstDefintions = new ArrayList<>();
		
		return mstDefintions;
	}

}
