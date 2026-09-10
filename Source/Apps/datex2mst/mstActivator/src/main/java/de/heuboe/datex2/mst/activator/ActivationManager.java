package de.heuboe.datex2.mst.activator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.heuboe.datex2.mst.builder.db.D2MSTConfReader.DefinitionInfo;
import de.heuboe.ddp.Connection;

public class ActivationManager
{
	public enum LocBuilderMode
	{
		LOC_CORBA_SERVER,
		LOC_WEB_SERVICE
	}
	
	private LocBuilderMode locBuilderMode;
	private String locBuilderAddress;
	private String mstBuilderSvcUrl;
	
	private Persistence persistence;
	
	private Map< MstKey, DefinitionInfo> mstDefinitions = new HashMap<>();
	private Map< MstKey, Mst> msts = new HashMap<>();
	
	public ActivationManager( Connection connection,
							  LocBuilderMode locBuilderMode, 
							  String locBuilderAddress,	
							  String mstBuilderSvcUrl )
	{
		this.locBuilderMode = locBuilderMode;
		this.locBuilderAddress = locBuilderAddress;
		this.mstBuilderSvcUrl = mstBuilderSvcUrl;
	}
	
	public void start()
	{
		List<DefinitionInfo> mstDefinitions = persistence.readMSTDefinitions();
		//readMSTDefinitions()
		
	}
}
