package de.heuboe.datex2.vms.service.uz.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import de.heuboe.datex2.vms.service.D2Location;
import de.heuboe.datex2.vms.service.uz.LocationService;
import de.heuboe.util.JavaObject2JSON;

public class TestLocationService
{
	@Test
	public void testLocationService()
			throws Exception
	{
		String tempFolder = System.getProperty("user.dir") + "/src/test/resources/temp";
		( new File( tempFolder ) ).mkdirs();
		
		LocationService locationService = new LocationService( 31468, false );
		locationService.connect( "http://sdbby-w7v:8085/WebLocationServerSrv?wsdl" );
		
		List<D2Location> locations = 
				locationService.getLocations( "AQ", 
											  new HashSet<String>(Arrays.asList( 
														"AQ_A009A370.2_33", 
														"WWQ_92584", 
														"WWQ_92_AK_Neuf_1454_UM" ) ),
											   true, true );
		
		String result = JavaObject2JSON.toString( locations );
		
		System.out.println( result );
	}
	
}
