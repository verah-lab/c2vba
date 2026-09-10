package de.heuboe.c2vba.datex2.table.update;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.c2vba.datex2.table.update.mst.MstJoiner;
import de.heuboe.c2vba.datex2.table.update.vms.VmsTableJoiner;
import de.heuboe.datex2.exception.D2CommException;
import de.heuboe.datex2.pull.PullClient;
import de.heuboe.datex2.push.PushClient;

public class Manager {

	@Value("${de.heuboe.c2vba.datex2.table.update.type}") 
	private String type;

	@Value("${de.heuboe.c2vba.datex2.table.update.tableSFile}") 
	private String tableSFile;

	@Value("${de.heuboe.c2vba.datex2.table.update.tableNFile:}") 
	private String tableNFile;
	
	@Value("${de.heuboe.c2vba.datex2.table.update.resultFile:}") 
	private String resultFile;
	
	@Value("${de.heuboe.c2vba.datex2.table.update.versionModeS:}") 
	private String versionModeS;

	@Value("${de.heuboe.c2vba.datex2.table.update.versionModeN:}") 
	private String versionModeN;

	@Autowired
	PullClient<?> pullClient;
	
	@Value("${de.heuboe.c2vba.datex2.table.update.pubUrl:}") 
	private String pubUrl;
	
	@Autowired
	PushClient pushClient;
	
	@Autowired
	private MstJoiner mstJoiner;

	@Autowired
	private VmsTableJoiner vmsTableJoiner;
	 
	public void execute() throws JAXBException, D2CommException, IOException {  // NOSONAR
		
		boolean isJoinMode = ( resultFile != null ) && !resultFile.isEmpty();
		
		if( type.equals( "LVE" ) || type.equals( "UFD" ) )  {
			eu.datex2.schema._2._2_0.mst.D2LogicalModel table1 = mstJoiner.readTable( "Sued", tableSFile );
			
			if( isJoinMode ) {
				eu.datex2.schema._2._2_0.mst.D2LogicalModel table2 = null;
				if( tableNFile != null && !tableNFile.isEmpty() ) {
					table2 = mstJoiner.readTable( "Nord", tableNFile );
				} else {
					table2 = mstJoiner.pullTable( pullClient );
				}
				
				JoinResult<eu.datex2.schema._2._2_0.mst.D2LogicalModel> jr = mstJoiner.joinTables( table1, table2, 
                        																		   versionModeS, versionModeN,
						                                                                           resultFile );
				if( ( pubUrl != null ) && !pubUrl.isEmpty() ) {
					pushClient.getRemoteConfig().getConnectionParameter().setUrl( pubUrl );
					mstJoiner.pushJoinTable( jr.getTable(), pushClient );
				}
			} else {
				mstJoiner.pushJoinTable( table1, pushClient );
			}
			
		} else if ( type.equals( "WWW" ) || type.equals( "SBA" ) )  {
			eu.datex2.schema._2._2_0.vms.D2LogicalModel table1 = vmsTableJoiner.readTable( "Sued", tableSFile );

			if( isJoinMode ) {
				eu.datex2.schema._2._2_0.vms.D2LogicalModel table2 = vmsTableJoiner.pullTable( pullClient );
				
				JoinResult<eu.datex2.schema._2._2_0.vms.D2LogicalModel> jr = vmsTableJoiner.joinTables( table1, table2, 
						                                                                                versionModeS, versionModeN,
						                                                                                resultFile );
				if( ( pubUrl != null ) && !pubUrl.isEmpty() ) {
					pushClient.getRemoteConfig().getConnectionParameter().setUrl( pubUrl );
					vmsTableJoiner.pushJoinTable( jr.getTable(), pushClient );
				}
			} else {
				vmsTableJoiner.pushJoinTable( table1, pushClient );
			}
		} else {
			throw new RuntimeException( "Unknwon table type '" + type + "', valid values are MST and VMST" );    // NOSONAR
		}
	}
}
