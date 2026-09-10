package de.heuboe.datex2.mst.builder.test;

import org.junit.Test;

import de.heuboe.datex2.mst.builder.client.MSTDefinitionImporterClient;

public class TestMSTDefinitionWriter {

	@Test
	public void writedMSTConfig() {  // NOSONAR
		
		String[] args = new String[7];
		args[0] = "-mstId=D2";
		args[1] = "-defVersion=D2";
		args[2] = "-d2Version=D2";
		args[3] = "-schemaCategory=D2";
		args[4] = "-schemaFile=D2";
		args[5] = "-mongoUrl=mongodb://adminuser:password123@172.31.246.1:32000/c2vba-ps?appName=strategie-matching&authSource=admin";
		args[6] = "-xmlDefFile=D:/by/importMDM_D2DataMapping/data/mstDefinition_LVE.xml";
		
		MSTDefinitionImporterClient.main( args );
	}
}
