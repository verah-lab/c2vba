package de.heuboe.c2vba.datex2.region.receiver;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.heuboe.log.Logger;


/**
 * 
 * Checks that connections are alive
 * 
 * @author peters
 *
 */
public class ConnectionAliveObserver implements Datex2Producer.Observer {
	
	private static final Logger LOGGER = Logger.getLogger( ConnectionAliveObserver.class );
	
	private Date startTime = new Date();
	
	private Map<String,Integer> region2RefreshInterval = new HashMap<>();
	
	private Runnable check = new Runnable() {  // NOSONAR
		public void run() {
				while( true ) {   // NOSONAR
					try {
						
						Thread.sleep( 10000L );
						
						// Süd
						
						{ 																	// NOSONAR
							Date lu = getLastUpdate( Properties.SUED );
							if( lu == null ) {
								lu = startTime;
							}
							
							int seconds = (int) ( ( (new Date()).getTime() - lu.getTime() ) / 1000L ); 
							if( seconds > region2RefreshInterval.get( Properties.SUED ) ) {
								LOGGER.error( "No data for region '" + Properties.SUED + "' for more than " + region2RefreshInterval.get( Properties.SUED ) + " seconds: EXIT");
								System.exit( -1 );
							}
						}
						
						// Nord
						
						{ 																	// NOSONAR
							Date lu = getLastUpdate( Properties.NORD );
							if( lu != null ) {
								int seconds = (int) ( ( (new Date()).getTime() - lu.getTime() ) / 1000L ); 
								if( seconds > region2RefreshInterval.get( Properties.NORD ) ) {
									LOGGER.error( "No data for region '" + Properties.NORD + "' for more than " + region2RefreshInterval.get( Properties.NORD ) + " seconds: EXIT");
									System.exit( -1 );
								}
							}
						}
		
					} catch ( InterruptedException ex ) {    // NOSONAR
						LOGGER.error( "InterruptedException: EXIT" );
						System.exit( -1 );
					}
			}
		}
		
		/**
		 * 
		 * Returns last update time
		 * 
		 * @param region   	Region:  Nord or Sued 
		 * @return			Last update time	
		 */
		private synchronized Date getLastUpdate( String region ) {     
			return region2LastUpdate.get( region );
		}

	};
	
	private Map<String,Date> region2LastUpdate = new HashMap<>(); 

	@Override
	public synchronized void notifyUpdate(Date time, String region) {
		region2LastUpdate.put( region, time );
	}
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param region2RefreshInterval  Region refresh interval
	 *                                During an interval of length 'region2RefreshInterval' an update is expected 
	 */
	public ConnectionAliveObserver( Map<String,Integer> region2RefreshInterval ) {
		
		this.region2RefreshInterval = region2RefreshInterval;
		
		(new Thread( check ) ).start();
	}
}
