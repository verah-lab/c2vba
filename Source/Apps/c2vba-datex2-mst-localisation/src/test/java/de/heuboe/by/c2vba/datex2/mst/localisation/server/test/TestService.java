package de.heuboe.by.c2vba.datex2.mst.localisation.server.test;

import org.junit.BeforeClass;
import org.junit.Test;

import de.heuboe.test.result.comparator.ResultComparator;

public class TestService
{
	private static final String userDir = System.getProperty("user.dir");
	private static final String testResPath = userDir + "/src/test/resources";
	
	private static ResultComparator trc = null;
	
	@BeforeClass
	public static void setUp() 
			throws Exception
	{
		ResultComparator.createFolders( true );
		trc = ResultComparator.createComparator(
										testResPath + "/checkResult",
										testResPath + "/result" );
		
		trc.setMaxCompareLines( ResultComparator.SIMPLE_FILE_COMPARISON_NUM );
		
		System.setProperty( "com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true" );
	}
	
	

	@Test
	public void testService()
			throws Exception
	{
	}
}
