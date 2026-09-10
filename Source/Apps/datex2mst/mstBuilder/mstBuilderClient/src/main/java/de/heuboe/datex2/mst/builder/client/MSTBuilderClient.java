package de.heuboe.datex2.mst.builder.client;
/*
 * Created on 09.12.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author peters
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.util.Date;

import de.heuboe.corba.CorbaClient;
import de.heuboe.datex2.base.D2BaseUtil;
import de.heuboe.datex2.mstBuilder.Builder;
import de.heuboe.datex2.mstBuilder.BuilderHelper;
import de.heuboe.datex2.mstBuilder.mstBuilderState;
import de.heuboe.datex2.mstBuilder.state;

public class MSTBuilderClient 
{
	private static String toStateStr( state S )
	{
		switch( S.value() )
		{
			case state._unknown:
				return "unknown";
			case state._buildQueued:
				return "buildQueued";
			case state._buildStarted:
				return "buildStarted";
			case state._configurationParsed:
				return "configurationParsed";
			case state._savingRecords:
				return "savingRecords";
			case state._recordBuildingFinished:
				return "recordBuildingFinished";
			case state._buildFailed:
				return "buildFailed";
			case state._buildAborted:
				return "buildAborted";
			case state._finished:
				return "finished";
			default:
				return "???";
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			String mstId = "";
			String version = "";
			String defVersion = "";
			String refVersion = "";
			
			if( ( args.length < 3 ) || ( args.length > 4 )  )
			{
				System.out.println( "Invalid number of arguments: " + args.length );
				System.out.println( "Provide exactly two or three arguments. First: LVE_MDM, Second: version number of MST, (optional) Third: reference version number of MST" );
				System.exit( -1 );
			}
			
			mstId = args[0];
			version = args[1];
			defVersion = args[2];
			if( args.length == 3 )
				refVersion = args[2];
			
			CorbaClient.initOrb( args, null );
			
			Builder B = BuilderHelper.narrow( CorbaClient.getNamedObject( "mstBuilder/Builder" ) );
			
			
			B.createMST( mstId, version, defVersion, refVersion );
			System.out.println( "" );
			System.out.println( "Erstellung der MST in der DB kann einige Minuten dauern ..." );
			System.out.println( "" );
			
			while( true )
			{
				Thread.sleep( 5000 );
				mstBuilderState bs = B.getStatus( mstId, version );
				
				state s = bs.state.currentState;
				if( ( s == state.buildAborted ) || ( s == state.buildFailed ) )
				{
					System.out.println("Fehler bei Erstellung der MST:");
					System.out.println( bs.state.error );
					System.out.println( "" );
					System.out.println("MST konnte nicht erstellt werden");
					D2BaseUtil.exit(-1);
				}
				
				System.out.println( (new Date()).toString() + " Status: " + toStateStr( bs.state.currentState ) );			
				
				if( bs.state.currentState == de.heuboe.datex2.mstBuilder.state.recordBuildingFinished )
				{
					System.out.println( "" );
					System.out.println( "MST erfolgreich in der DB angelegt" );
					System.out.println( "" );
					System.out.println( "Publication-Datei wird vom Prozess 'd2MSTPubBuilder_MDM' als D:/vzh/data/out/MDM/MST/D2MSTPub_" + mstId + "_" + version + ".xml abgelegt." );
					D2BaseUtil.exit(-1);
				}
			}

		}
		catch (Exception ex) 
        {
			System.out.println( "" );
			System.out.println("Fehler bei Erstellung der MST:");
			System.out.println( "" );
			System.out.println( ex.toString() );
			ex.printStackTrace();
			System.out.println( "" );
			System.out.println("MST konnte nicht erstellt werden");
			D2BaseUtil.exit(-1);
		}
	}
}
