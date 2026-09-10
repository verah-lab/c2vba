package de.heuboe.datex2.mst.builder.test;

import org.junit.Test;

import de.heuboe.datex2.mst.builder.client.MSTConfigImporterClient;

public class TestMSTConfigWriter {
	@Test
	public void writedMSTConfig() {   // NOSONAR
		
		String[] args = new String[3];
		args[0] = "-d2Version=D2";
		args[1] = "-Dspring.data.mongodb.uri=mongodb://adminuser:password123@172.31.246.1:32000/c2vba-ps?appName=strategie-matching&authSource=admin";
		args[2] = "-xmlCfgFile=D:/by/importMDM_D2DataMapping/data/mstConfig.xml";
		
		MSTConfigImporterClient.main( args );
	}

}
