package de.heuboe.sdbby.strategy.matching;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.protobuf.Timestamp;

import de.heuboe.c2vba.data.StrategyState;
import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.c2vba.data.pojo.PStrategyStates;
import de.heuboe.kafka.producer.KafkaProducer;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.data.db.Strategy;
import de.heuboe.sdbby.strategy.matching.Config.WzgInfo;
import de.heuboe.sdbby.strategy.matching.db.StrategyRepository;
import de.heuboe.wls.basic.util.SimpleDateFormatter;


/**
 * 
 * Receives all switching data, re-evaluates strategy conditions and saves new strategy states 
 * 
 * @author peters
 *
 */
public class DataHandler {

	private static final Logger LOGGER = Logger.getLogger(DataHandler.class);
	
	public static final String DK_BETRIEBS_ART = "WZGBetriebsart";
	public static final String DK_WZG_STELLZUSTAND = "WZGStellzustand";
	public static final String DK_WZG_DE_FEHLER = "WZGDeFehler";
	public static final String DK_WZG_KANAL_STEUERUNG = "WZGKanalSteuerung";
	
	
	private static final String TEXT_ZEICHEN = "textzeichen";

	@Value("${de.heuboe.sdbby.service.logFileDir:}")
	private String logFileDir = null;
	
	@Autowired
	private StrategyRepository strategieRepository;
	
	@Autowired
	KafkaProducer<PStrategyStates> strategyStatesProducer;
	
	@Autowired
	private DataReceiver dataReceiver;
	
	private Set<String> wzgIdsWithData = new HashSet<>();
	
	private boolean checkCompletenessExecuted = false;

	private RuleMan ruleMan;
	public RuleMan getRuleMan() {
		return ruleMan;
	}

	private int updateCycle = 1000;
	private int precedingFontChars = 0;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param updateCycle			Minimum time between rule evaluations (ms)
	 * @param precedingFontChars	Number of font-describing bytes preceding the displayed characters in a WZG text
	 */
	public DataHandler( int updateCycle, int precedingFontChars ) {
		this.updateCycle = updateCycle;  
		this.precedingFontChars = precedingFontChars;
	}
	
	/**
	 * 
	 * Starts data reception
	 * 
	 * @param ruleMan	Strategy conditions evaluator
	 */
	public void start( RuleMan ruleMan ) {
		this.ruleMan = ruleMan;
		
		DataConsumer consumer = new Consumer();
		dataReceiver.subscribe( consumer );
		
		dataReceiver.start();
	}
	
	/**
	 * 
	 * Receives all data updates related to WWWs and re-evaluates strategy conditions 
	 * 
	 * @author peters
	 *
	 */
	public class Consumer implements DataConsumer {
		
		private boolean changed = false;
		
		/**
		 * 
		 * Constructor
		 * 
		 */
		public Consumer() {
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					evaluateStrategiesIfChanged();
				}
			};
			( new Timer() ).schedule( tt, 1000L, updateCycle ); 
		}
		
		private synchronized void evaluateStrategiesIfChanged() {
			if( changed ) {
				evaluateStrategies();
				changed = false;
			}			
		}
		
		@Override
		public synchronized void consumeState( String datakind, List<Dataset> datasets ) {
			// Should not be called !
			consume( datasets );
		}
		
		private void logWzgWithoutCurrentSwitchingData( Set<String> wzgIds ) {
			if( ( logFileDir != null ) && !logFileDir.isEmpty() ) {
				File dir = new File( logFileDir ); 
				
				dir.mkdirs();
				
				if( !dir.exists() || !dir.isDirectory() )
				{
					LOGGER.warn( "Cannot access logging dir '" + logFileDir +"'" );
				}
				
				LinkedList<String> lines = new LinkedList<>(); 
				for( String wzgId : wzgIds ) {
					String aqId = ruleMan.getConfig().getAqId( wzgId );
					Integer knNr = null;
					String de = "";
					String uz = "";
					WzgInfo wi = ruleMan.getConfig().getWzgInfo( wzgId );
					if( wi != null ) {
						knNr = wi.getKnNr();
						de = wi.getDe();
						uz = wi.getUzName();
					}
					
					String line = ( ( uz == null) ? "": uz ) +  ";" + 
								  ( ( aqId == null) ? "": aqId ) + ";" + 
								  wzgId + ";" + 
								  ( ( knNr == null) ? "": knNr ) + ";" + 
								  ( ( de == null) ? "": de );
					lines.add( line );
				}
				Collections.sort( lines );
				lines.addFirst( "UZ;AQ;WZG;KnNr;De" );

				String fn = "";
				try {
					SimpleDateFormatter sdf = new SimpleDateFormatter( "yyyy-MM-dd---HH-mm-ss" );
					fn = dir + File.separator + "wzgWithoutCurrentSwitchingData-" + sdf.format( new Date() ) + ".csv";
					FileUtils.writeLines( new File(fn), StandardCharsets.ISO_8859_1.name(), lines );
				} catch (IOException e) {
					LOGGER.warn( "Cannot write logging file '" + fn +"'" );
				}
			}
		}

		
		private void checkCompleteness( List<Dataset> datasets ) {
			
			if( checkCompletenessExecuted ) {
				return;
			}
			
			checkCompletenessExecuted = true;
			
			Set<String> wzgIds = new HashSet<>( ruleMan.getConfig().getAllPrismIds() );
			LOGGER.info( "" );
			LOGGER.info( "Number of Wzgs: " + wzgIds.size() );
			
			Set<String> wzgIdsData = new HashSet<>();
			Set<String> wzgIdsNotInConfig = new HashSet<>();
			Date lastDay = new Date( (new Date()).getTime() - ( 86400L + 2 * 3600L ) * 1000L  );

			for( Dataset ds : datasets ) {
				String id = ds.value("id").getString();
				
				if (ds.getDatakind().equals( DK_WZG_STELLZUSTAND )) {
					
					long time = ds.value("time").getLong();
					Date date = new Date( time * 1000L );

					if( ruleMan.getConfig().getAllPrismIds().contains( id ) ) {
						if( date.after( lastDay ) ) {
							wzgIds.remove( id );
							wzgIdsData.add( ds.value("id").getString() );
						}
					} else {
						if( date.after( lastDay ) ) {
							wzgIdsNotInConfig.add( id );
						}
					}
				}
			}
			
			logWzgWithoutCurrentSwitchingData( wzgIds );
			
			LOGGER.info( "No (current) data for " + wzgIds.size() + " Wzgs");
			LOGGER.info( "" );
		}
			
		@Override
		public synchronized void consume( List<Dataset> datasets ) {    // NOSONAR
			
			
			checkCompleteness( datasets );  // NOSONAR
			
			Iterator<Dataset> it = datasets.iterator();
			
			int numWzgWithData = wzgIdsWithData.size();
			
			boolean referencesObj = false;
			while (it.hasNext()) {
				boolean ro = false;
				boolean ros = false;
				boolean rot = false;
				Dataset dataset = it.next(); 	
				if (dataset.getDatakind().equals(DK_WZG_DE_FEHLER)) {
					ro = newFehlerDE(dataset);
				}
				if (dataset.getDatakind().equals( DK_WZG_STELLZUSTAND )) {
					ros = newStellzustand(dataset);
				}
				if (dataset.getDatakind().equals( DK_WZG_STELLZUSTAND )) {
					rot = newTextCDIst(dataset);
				}
				if (dataset.getDatakind().equals( DK_WZG_KANAL_STEUERUNG )) {
					ro = newKanalIst(dataset);
				}
				if (dataset.getDatakind().equals( DK_BETRIEBS_ART )) {
					ro = newBetrArtIst(dataset);
				}
				if( ro || ros || rot ) {
					referencesObj = true;
				}
			}
			
			if( numWzgWithData < wzgIdsWithData.size() ) {
				LOGGER.info( "# WZGs:           " +  ruleMan.getConfig().getNumPrisms() );
				LOGGER.info( "# WZGs with data: " +  wzgIdsWithData.size() );
				
				Map< String, List<String> > aq2wzg = new HashMap<>();
				LOGGER.info( "WZG without data:" );
				for( String wzgId :ruleMan.getConfig().getPrismIds() ) {
					if( !wzgIdsWithData.contains( wzgId ) ) {
						String aqId = ruleMan.getConfig().getAqId( wzgId );
						aq2wzg.computeIfAbsent( aqId, a -> new ArrayList<>() ).add( wzgId );
					}
				}
				
				for( Map.Entry< String, List<String> > entry : aq2wzg.entrySet() ) {
					LOGGER.info( entry.getKey() );
					for( String wzgId : entry.getValue() ) {
						LOGGER.info( "    " + wzgId );
					}
				}
			}
			
			if( referencesObj ) {
				changed = true;
			}
		}

		private synchronized void evaluateStrategies() {
			boolean strategiesChanged = ruleMan.evaluateStrategies(); 
			if (strategiesChanged) {
				LOGGER.info("Writing changed strategies to database");
				Collection<Strategie> strategies = ruleMan.getConfig().getStrategies();
				for(Strategie strategy : strategies) {
					LOGGER.info("Strategy " + strategy.getId() + " is " + (strategy.isActive() ? "" : " NOT ") + " active.");
				}
				writeStrategies( strategies );
			} else {
				LOGGER.info("evaluateStrategies: no changes");
			}
		}

		private boolean newFehlerDE(Dataset dataset) {
			String id = dataset.value("id").getString();
			WWWPrisma prisma = ruleMan.getConfig().getPrisma(id);
			if (prisma != null) {
				Dataset.Value v = dataset.value("fehlercode");
				if( v != null ) {
					prisma.setErrorCode(v.getInt());
				} else {
					LOGGER.warn( "WZGDeFehler without fehlercode" );
					prisma.setErrorCode( 0 );
				}
				return true;
			}
			return false;
		}

		private boolean newStellzustand(Dataset dataset) {
			String id = dataset.value("id").getString();
			WWWPrisma prisma = ruleMan.getConfig().getPrisma(id);
			if (prisma != null) {
				prisma.setStellcode(dataset.value("stellcode").getInt());
				prisma.setFunktionsbyte(dataset.value("funktionsbyte").getInt());
				
				wzgIdsWithData.add( id );
				return true;
			}
			return false;
		}

		private boolean newTextCDIst(Dataset dataset) {
			String id = dataset.value("id").getString();
			WWWPrisma prisma = ruleMan.getConfig().getPrisma(id);
			if (prisma != null) {
				int anz = dataset.value("anzahl").getInt();
				String text = "";
				if( !( ( dataset.value(TEXT_ZEICHEN) == null ) || dataset.value(TEXT_ZEICHEN).isNull() ) ) {
					text = dataset.value(TEXT_ZEICHEN).getString();
					if( text.length() > anz ) {
						text = text.substring( 0, anz );
					}
					if( precedingFontChars != 0 ) {
						if( text.length() >= precedingFontChars ) {    // NOSONAR
							text = text.substring( precedingFontChars );
						}
					}
					prisma.setDisplayedText( text );
				}
				
				prisma.setStellcode(dataset.value("stellcode").getInt());
				prisma.setFunktionsbyte(dataset.value("funktionsbyte").getInt());
				
				wzgIdsWithData.add( id );
				return true;
			}
			return false;
		}
		
		private boolean newKanalIst(Dataset dataset) {
			String id = dataset.value("id").getString();
			WWWPrisma prisma = ruleMan.getConfig().getPrisma(id);
			if (prisma != null) {
				prisma.setKanalSteuerung(dataset.value("Steuerbyte").getInt());
				return true;
			}
			return false;
		}

		private boolean newBetrArtIst(Dataset dataset) {
			String id = dataset.value("id").getString();
			WWWPrisma wzg = ruleMan.getConfig().getPrisma(id);
			if (wzg != null) {
				wzg.setBetriebsArt(dataset.value("WVZBetrArt").getInt());
				return true;
			}
			return false;
		}
		
		private void writeStrategies( Collection<Strategie> strategies ) {
			
			// MongoDB
			Date now = new Date();
			List<Strategy> strategys = new ArrayList<>();
			for( Strategie strategie : strategies ) {
				Strategy strategy = new Strategy();
				strategy.setTime( now );
				strategy.setId( strategie.getId() );
				strategy.setActive( strategie.isActive() );
				strategys.add( strategy );
			}
			strategieRepository.saveAll( strategys );
			
			// Kafka
			
			StrategyStates.Builder sssb = StrategyStates.newBuilder();
	        Instant time = Instant.now();
	        sssb.setTime( Timestamp.newBuilder().setSeconds(time.getEpochSecond()).setNanos(time.getNano()).build() );
			for( Strategie s :strategies ) {
				StrategyState ss = StrategyState.newBuilder().setId( s.getId() ).setActive( s.isActive() ).build();
				sssb.addStrategyState( ss );
			}

			sssb.setIid( "1" );
			Map<String,String> headers = new HashMap<>();
			strategyStatesProducer.sendData( "1", headers, PStrategyStates.from( sssb.build() ) );
		}

	}	

}
