package de.heuboe.tls.rcv.transf.impl;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.DeBlockDefinitionIf;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.receiver.rdr.core.DeBlockDefinition;
import de.heuboe.tls.receiver.rdr.core.TlsReceiverException;
import de.heuboe.tls.receiver.rdr.core.TransformationRules;
import de.heuboe.tls.receiver.rdr.getter.AbstractGetter;
import de.heuboe.tls.receiver.rdr.getter.ByteGetter;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import de.heuboe.tls.receiver.rdr.impl.DataObject.DeMeta;
import de.heuboe.tls.receiver.rdr.impl.DataObject.ETelMeta;
import de.heuboe.tls.receiver.rdr.item.GregorianItem;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;
import de.heuboe.tls.receiver.rdr.item.StringItem;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.meta.Helper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.Setter;

/**
 * An implementation of the interface Transformer With these objects a transformation of a TLS Sammeltelegramm into DataObjects is implemented To operate a
 * TransformationRulesContainer and an AddressConverter has to be provided The main method is then transform, the transformation aof a TlsTele into a list of
 * DataObjects
 * 
 * @author ronald
 *
 */
@Component
public class TransformerImpl implements Transformer {

    private static final Logger      LOGGER                   = LogManager.getLogger( TransformerImpl.class );
    private Behaviour                behaviour;

    private AddressConverter         addressConverter;
    private TransformationRules      transformationRules;
    private boolean                  initChecked              = false;
    private String                   timezoneId               = null;
    private TimeZone                 timeZone                 = null;
    final GetterRule                 ruleGetTimestampGPRS     = new TimeGetter( "U", "zeitstempel", "unix-time" );
    final GetterRule                 ruleGetIntervalllaengeHE = new ByteGetter( "intervalllaenge", false, null, null, "1nt32" );

    @Setter
    private SystemMessageManagement  smm                      = null;
    @Setter
    @Getter
    private boolean                  gprsUZ                   = false;

    // 'simulation' of timestamps
    boolean fakeTimeStamp = false;
    private int intervallLaenge;
    private int intervallArt;
                                                                                                                                // of Hessen. Timestam is in
                                                                                                                                // each DE-Block
    @Autowired
    private MeterRegistry meterRegistry;
    
    private Counter counterGetterErrors = null;

    
    private static DeBlockDefinition unknownDeBlock;
    static {
        unknownDeBlock = new DeBlockDefinition();
        unknownDeBlock.setName( "-withoutScript:Fg/Id/Typ-" );
    }
    
    public TransformerImpl() { // NOSONAR check bean 
        int i = 0; i = i + 1;
    }

    @Override
    public void setAddressConverter( AddressConverter addressConverter ) {
        this.addressConverter = addressConverter;

    }

    @Override
    public void setTransformationRules( TransformationRulesContainer transformationRules ) {
        if ( !( transformationRules instanceof TransformationRulesContainer ) ) {
            throw new IllegalStateException( "Only expecting TransformationRules as implementation of TransformationRulesContainer" );
        }
        this.transformationRules = (TransformationRules) transformationRules;
    }

    @Override
    public void setTimezoneId( String timezoneId ) {
        timeZone = TimeZone.getTimeZone( timezoneId );
        if ( !timezoneId.equals( timeZone.getID() ) ) {
            throw new IllegalArgumentException( timezoneId + " seems to be no valid timezone id" );
        }
        LOGGER.info( "Using timezone " + timeZone.getID() + " for each current timestamp" );
    }
    
    public  void setIntervallArtIntervallLaenge( String intervallArtIntervallLaenge ) {
        String[] s = intervallArtIntervallLaenge.split( ":" );
        if( s.length != 2 ) {
            throw new IllegalStateException(
                     "Bad value for intervallArtIntervallLaenge (expect 'a:l'): " +
                     intervallArtIntervallLaenge );
        }
        intervallArt = Integer.parseInt( s[0] );
        intervallLaenge = Integer.parseInt( s[1] );
        fakeTimeStamp = true;
    }

    @Override
    public void init() {
        init( Behaviour.create() );
    }

    @Override
    public void init( Behaviour behaviour ) {
        this.behaviour = behaviour;
        if ( addressConverter == null ) {
            throw new IllegalStateException( "TransformerImpl: Address converter is not set" );
        }
        if ( transformationRules == null ) {
            throw new IllegalStateException( "TransformerImpl: transformation rules are not set" );
        }
        if ( timeZone == null ) {
            throw new IllegalStateException( "TransformerImpl: time zone is not set" );
        }
        
        if ( null != meterRegistry ) {
            counterGetterErrors = Counter.builder( "getterErrors" )
                    .description( "Errors encountered during execution of Getter" )
                    .baseUnit( "ErrsDuringGetter" )
                    .register( meterRegistry );
        }

        initChecked = true;
    }

    @Override
    public List<DataObjectIf> transform( TlsTele tlsTele ) {
        if ( !initChecked ) {
            throw new IllegalStateException( "Init was no called" );
        }
        boolean deliverBadBlocks = behaviour.isBadBlocksDelivery();
        int node = tlsTele.getLogAddress();
        List<DataObjectIf> resDataObjects = new ArrayList<>();

        GregorianCalendar rcvTimeBase = tlsTele.getRcvTimeBase();

        // here is the main place where interpretation of time starts
        GregorianCalendar actualTime = new GregorianCalendar( this.timeZone );
        actualTime.setTimeInMillis( tlsTele.getTimeStamp().getTime() ); // set time to the time recorded at the telegram receive time

        for ( TlsETel etel : tlsTele.getEtels() ) {
            List<DataObjectIf> dataObjects = new ArrayList<>();
            if ( LOGGER.isTraceEnabled() ) {
                // @formatter:off
                String msg = "ETele Fg:"
                        + etel.getFg()
                        + " Id:"
                        + etel.getTlsId()
                        + " JobNr:"
                        + etel.getJob()
                        + " #DE:"
                        + etel.getDeblockCount()
                        ; // @formatter:off
                LOGGER.log( Level.TRACE, msg );
            }
            eTelAnalysis( tlsTele, etel, node, dataObjects, rcvTimeBase, actualTime, deliverBadBlocks );
            
            if (fakeTimeStamp) {
                // Fg 1 Identifier 4 Results ~ Fg 1 Identifier 4 Ergebnisse | + 128 response direction
                if (1 == etel.getFg() && 132 == etel.getTlsId()) {
                    DataObject spJob = new DataObject();
                    spJob.addItem( new IntegerItem( "intervallArt", intervallArt, 0 ) );
                    spJob.addItem( new IntegerItem( "intervalllaenge", intervallLaenge, 0 ) );
                    spreadHeader( dataObjects, spJob );
                }
            }

            resDataObjects.addAll( dataObjects );
        }
        return resDataObjects;
    }
	
	private static final String ACTUAL_TIME = "#ActualTime";

        private void eTelAnalysis( TlsTele tlsTele, TlsETel etel, int node, List<DataObjectIf> dataObjects, GregorianCalendar rcvTimeBase,
                        GregorianCalendar actualTime, boolean deliverBadBlocks ) {
                int fg = etel.getFg();
                int id = etel.getTlsId();
                boolean subsequent = false;
                DataObject header = null;
                int cnt = 0;
                
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put("#Node", new IntegerItem("#Node", node, 0)); // log address
                etelVars.put("#TlsFg", new IntegerItem("#TlsFg", etel.getFg(), 0));
                etelVars.put("#TlsId", new IntegerItem("#TlsId", etel.getTlsId(), 0));
                etelVars.put("#TlsJob", new IntegerItem("#TlsJob", etel.getJob(), 0));
                
                etelVars.put(ACTUAL_TIME, new GregorianItem(ACTUAL_TIME, actualTime, 0)); // should possibly have the right timezone here
                if (null != rcvTimeBase) {
                        etelVars.put("#RcvTimeBase", new GregorianItem("#RcvTimeBase", rcvTimeBase, 0));
                        subsequent = true;
                }
                ETelMeta etelMeta = new ETelMeta(etel.getFg(), etel.getTlsId(), etel.getJob(), etel.getDeblocks().size());
                for(TlsDeBlock deblock : etel.getDeblocks()) {
                	header = handleDeBlock( tlsTele, etel, node, dataObjects, fg, id, subsequent, header, cnt, etelVars, etelMeta,
                                        deblock, deliverBadBlocks );
                	cnt++;
                }
                
                DataObject spHeader = null;
                if (header != null) {
                        spHeader = header;
                } else {
                        if (null != rcvTimeBase) {
                            String msg = "Subsequent delivered data without timestamp: telegram skipped";
                                LOGGER.error( msg );
                                
                                if (null != smm) {
                                    smm.sendMessage( msg );
                                }

                                throw new IllegalStateException( "Subsequent delivered data without timestamp: telegram skipped" );
                        }
                        if (254 != fg && !isGprsUZ() && ( 0 == etel.getJob() ) ) {
                            String msg = String.format( 
                                            "Telegram without timestamp: Node %d (%d-%d) fg %d id %d job %d", 
                                            tlsTele.getLogAddress(),tlsTele.getLogAddress()/256,tlsTele.getLogAddress()%256,
                                            etel.getFg(),
                                            etel.getTlsId(),
                                            etel.getJob() );
                            
                            LOGGER.warn( msg );
                            
                            if (null != smm) {
                                smm.sendMessage( msg );
                            }
                        }
                        if (this.behaviour.isAddMissingTimestamp() && !isGprsUZ()) { // prevent generating timestamps with externally added timestamps in data
                                DataObject tmpHeader = new DataObject();
                                DataItem tm = etelVars.get(ACTUAL_TIME ).copy();
                                tm.setName( "zeitstempel" );
                                tmpHeader.addItem( tm );
                                spHeader = tmpHeader;
                        }
                }
                // add data of header deblock to all deblocks if present
                if (null != spHeader) {
                    // some telegrams are >routed< to the SSTs below the GPRS-UZ. These may have timestamp DEs.
//                    if (isGprsUZ()) {
//                        String msg = String.format( 
//                                "Did not expect header DE (DeNr=255) in GPRS context: Node %d (%d-%d) fg %d id %d job %d", 
//                                tlsTele.getLogAddress(),tlsTele.getLogAddress()/256,tlsTele.getLogAddress()%256,
//                                etel.getFg(),
//                                etel.getTlsId(),
//                                etel.getJob() );
//                
//                        LOGGER.warn( msg );
//                    }
                    spreadHeader( dataObjects, spHeader );
                }
                
                DataItem job =  new IntegerItem( "jobnummer", etel.getJob(), 0 );
                DataObject spJob = new DataObject();
                spJob.addItem( job );
                spreadHeader( dataObjects, spJob );
        }

        /**
         * Spread elements of header object to all dataobjects
         * @param dataObjects
         * @param header
         */
        private void spreadHeader( List<DataObjectIf> dataObjects, DataObject header ) {
            for(DataItem headerItem : header.getItems()) {
                for(DataObjectIf dataObject : dataObjects) {
                    // if timestamp is included in data of DE-Block, this one is preferred
                    if (headerItem.getName().equals( "zeitstempel" ) && dataObject.getItemMap().containsKey( "zeitstempel" )) {
                        continue;
                    }
                    dataObject.addItem( headerItem );
            	}
            }
        }
        
//        private FileOutputStream fs = null;

        private DataObject handleDeBlock( final TlsTele tlsTele, final TlsETel etel, int node, List<DataObjectIf> dataObjects, int fg, // NOSONAR too cost intensive with little benefit
                        int id, boolean subsequent, DataObject header, int cnt, Map<String, DataItem> etelVars, ETelMeta etelMeta, TlsDeBlock deblock,
                        boolean deliverBadBlocks ) {
                boolean unknownType = false;
                
                int de = deblock.getDeNr();
                int typ = deblock.getDeTyp();
                etelVars.put( "#De", new IntegerItem( "#De", de, 0 ) );
                etelVars.put( "#DeLen", new IntegerItem( "#DeLen", deblock.getSize(), 0 ) );
                
                String address = addressConverter.convert( node, fg, de );
                DeBlockDefinition deBlockDefinition = getRulesForDeBlock( node, fg, id, typ );
                if ( deBlockDefinition == null ) {
                    if ( ((id & 0x80) != 0) && isGprsUZ() && ((id & 0x20) != 0)  ) { // receive from below from gprs-uz 32/0x20 => subsequent
                        int idt = id ^ 0x20;
                        deBlockDefinition = getRulesForDeBlock( node, fg, idt, typ );
                    }
                    if ( deBlockDefinition == null ) {
//                      { // analysis HE only
//                      if (null == fs) {
//                          try {
//                              fs = new FileOutputStream( "missingScripts.txt" );
//                          }
//                          catch ( FileNotFoundException e ) {
//                              // TODO Auto-generated catch block
//                              e.printStackTrace();
//                          }
//                      }
//                      
//                      try {
//                          fs.write( ("Unknown Fg/Id/Typ: " + fg + "/" + id + "/" + typ + "\n").getBytes( StandardCharsets.UTF_8 ) );
//                      }
//                      catch ( IOException e ) {
//                          // TODO Auto-generated catch block
//                          e.printStackTrace();
//                      }
//                  }
                        logBadDeBlock( etel, deblock, node, "Unknown Fg/Id/Typ: " + fg + "/" + id + "/" + typ );
                        if ( !deliverBadBlocks ) {
                                return header;
                        }
                        unknownType = true;
                        deBlockDefinition = unknownDeBlock;
                    } else {
                        subsequent = true;
                    }
                }
                etelVars.put( "#Address", new StringItem( "#Address", buildAddressItem( node, fg, de, address ), 0 ) );
                
                DeMeta deMeta = new DeMeta( de, typ, cnt, deblock.getContent() );

                DataObject dataObject = new DataObject( deBlockDefinition.getName(), address );
                dataObject.setEtelMeta( etelMeta );
                dataObject.setDeMeta( deMeta );
                dataObject.setEtel( etel );
                dataObject.setTele( tlsTele );
                dataObject.setSubsequent( subsequent );

                int ofs = 0;
                boolean ok = true;
                
                
                for ( GetterRule rule : deBlockDefinition.getGetterRules() ) {
                        DataItem item = null;
                        try {
                                item = rule.get( deblock.getContent(), ofs, etelVars );
                                if (!isSubsequentItem( item, dataObject, subsequent )) {
                                        ofs += item.getConsumedSize();
                                        addItemByType( etelVars, dataObject, item );
                                }
                        } catch ( IllegalArgumentException | TlsReceiverException e ) {
                                if ( null != counterGetterErrors ) {    
                                    counterGetterErrors.increment();
                                }
                                logBadDeBlock( etel, deblock, node, e.getMessage() );
                                ok = false;
                                if ( !deliverBadBlocks ) {
                                        return header;
                                }
                                break;
                        }
                }
                if ( !ok ) {
                        addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorGetterRules-" );
                        return header;
                }
                
                int deContentSize = deblock.getSize() - 2;
                if ((!unknownType) && isGprsUZ() /*&& isHeSpecialTimestamp(fg, id, typ)*/ && ofs + 4 <= deContentSize) { // 4 byte unix timestamp special HE
                    DataItem item = null;
                    try {
                        // extra special for Fg 1 Id 132 Typ  52 -> LVEErgebnisVersion3
                        if (( 1 == fg ) && ( (132 == id) || (164 == id) ) && ( typ == 52 )) {
                            item = ruleGetIntervalllaengeHE.get( deblock.getContent(), ofs, etelVars );
                            ofs += item.getConsumedSize();
                            addItemByType( etelVars, dataObject, item );
                            DataItem item2 = new IntegerItem( "intervallArt", 1, 1 );
                            addItemByType( etelVars, dataObject, item2 );
                        }
                        if (4 == fg ) {
                            if (!dataObject.getItemMap().containsKey( "folgenummer" ) ) {
                                DataItem item2 = new IntegerItem( "folgenummer", 0, 1 );
                                addItemByType( etelVars, dataObject, item2 );
                            }
                        }
                        item = ruleGetTimestampGPRS.get( deblock.getContent(), ofs, etelVars );
                        ofs += item.getConsumedSize();
                        addItemByType( etelVars, dataObject, item );
                    } catch ( IllegalArgumentException e ) {
                        logBadDeBlock( etel, deblock, node, e.getMessage() );
                        ok = false;
                        if ( !deliverBadBlocks ) {
                            return header;
                        }
                    }
                }

                if ( (!unknownType) && ( ofs < deContentSize ) ) {
                    logBadDeBlock( etel, deblock, node, "DE Block too long" );
                    addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorDEBlock2long-" );
                    return header;
                }
                if ( deBlockDefinition.isHeader() ) {
                        if ( cnt != 0 ) {
                                logBadDeBlock( etel, deblock, node, "Header DE Block is not the first one in ETel" );
                                addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorHeaderNotFirst-" );
                                return header;
                        }
                        if ( deblock.getDeNr() != 255 ) {
                                logBadDeBlock( etel, deblock, node, "DE number of Header DE Block is not 255" );
                                addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorHeaderNot#255-" );
                                return header;
                        }
                        header = dataObject;
                } else {
                        dataObjects.add( dataObject );
                }
                return header;
        }
        
        private LinkedHashSet<Integer> heSpecialTimestampTriples = null;

        // NOSONAR kept for potential later use in HE
        private boolean isHeSpecialTimestamp( int fg, int id, int typ ) {
            Integer num = fg * 256 * 256 + id * 256 + typ;
            return heSpecialTimestampTriples.contains( num );
        }
        
        // prepare a tble of triples of fg/id/typ for special timestamp handling
        // format is <fg>/<id>/<typ>, ...
        
        public void setHeSpecialTimestamps( String tripleList ) {
            heSpecialTimestampTriples = new LinkedHashSet<>();
            setGprsUZ( true );
            String[] triples = tripleList.split( "," );
            for ( String triple : triples ) {
                triple = triple.trim();
                String[] tripleNums = triple.split( "/" );
                if (tripleNums.length != 3) {
                    throw new IllegalArgumentException( "specialTimestamps: '" + tripleNums + "' has not 3 components when split by '/'"  );
                }
                int fg = Integer.parseInt( tripleNums[0].trim() );
                int id = Integer.parseInt( tripleNums[1].trim() );
                int typ = Integer.parseInt( tripleNums[2].trim() );
                Integer num = fg * 256 * 256 + id * 256 + typ;
                heSpecialTimestampTriples.add( num );
            }
        }

        /**
         * @param node
         * @param fg
         * @param de
         * @param address
         * @return the adrdress 
         */
        private String buildAddressItem( int node, int fg, int de, String address ) {
                String adrVar;
                if ( address != null ) {
                        adrVar = address;
                } else {
                        adrVar = String.format( "-UnknownIdOf-%d-%d-%d-FG=%d", node/256, node % 256, de, fg );
                }
                return adrVar;
        }

        /**
         * @param node If definitions are location specific we have to test against this node 
         * @param fg
         * @param id
         * @param typ
         * @return DeBlockDefinition The set of rules to be applied to an DE-Block
         */
        private DeBlockDefinition getRulesForDeBlock( int node, int fg, int id, int typ ) {
                List<DeBlockDefinitionIf> deBlockDefinitions = transformationRules.getDefinition( fg, id, typ );
                if (null == deBlockDefinitions || deBlockDefinitions.isEmpty()) {
                        return null;
                }
                if (1 == deBlockDefinitions.size()) {
                        return (DeBlockDefinition) deBlockDefinitions.get( 0 );
                } else {
                        for (DeBlockDefinitionIf deBlockDefinitionLoopV : deBlockDefinitions) {
                                DeBlockDefinition deBlockDefinitionLoop = (DeBlockDefinition) deBlockDefinitionLoopV;
                                Set<Integer> locs = deBlockDefinitionLoop.getSpecialLocations();
                                boolean match = false;
                                if (locs.contains( node )) {
                                        match = true;
                                }
                                if (null != deBlockDefinitionLoop.getSpecialLocationsExcludedName()) {
                                        if (match) {
                                                continue;
                                        } else {
                                                deBlockDefinitionLoop.getGetterRules().forEach( r -> setLocationContext( r ) ); // NOSONAR false positive for squid:S1612
                                                return deBlockDefinitionLoop;
                                        }
                                }
                                if (match && null != deBlockDefinitionLoop.getSpecialLocationsName()) {
                                        deBlockDefinitionLoop.getGetterRules().forEach( r -> setLocationContext( r ) ); // NOSONAR false positive for squid:S1612
                                        return deBlockDefinitionLoop;
                                }
                        }
                }
                return null;
        }
        
        private void setLocationContext( GetterRule r ) {
                if (r instanceof AbstractGetter) {
                        ((AbstractGetter)r).setLocationContext( true );
                } else {
                        throw new IllegalStateException( "setLocationContext is not applicable" );
                }
        }

        boolean isSubsequentItem( DataItem item, DataObject dataObject, boolean calledAsubsequent ) throws TlsReceiverException {
                if (item.getName().equalsIgnoreCase( "#Puffer" )) {
                        if (item.getType() == DataItemType.INTEGER && 0 == item.getConsumedSize()) {
                                if (0 != item.getAsLong().intValue()) {
                                        dataObject.setSubsequent( true );
                                } else {
                                        dataObject.setSubsequent( calledAsubsequent );
                                }
                        } else {
                                throw new TlsReceiverException( "#Puffer has illegal properties: either type or consumed size" );
                        }
                        return true;
                } else {
                        return false;
                }
        }

        private void addItemByType( Map<String, DataItem> etelVars, DataObject dataObject, DataItem item ) {
                if ( item.getType() == DataItemType.LIST ) {
                        for ( DataItem itm : item.getAsItemList() ) {
                                addItemByType( etelVars, dataObject, itm );
                        }
                } else {
                        addItem( item, etelVars, dataObject );
                }
        }

        private void addOnlyIfWanted( List<DataObjectIf> dataObjects, boolean deliverBadBlocks, DataObject dataObject, String name ) {
                if ( deliverBadBlocks ) {
                        dataObject.setName( name );
                        dataObjects.add( dataObject );
                }
        }

        private void addItem( DataItem item, Map<String, DataItem> etelVars, DataObject dataObject ) {
                if ( item.getName().startsWith( "$" ) ) {
                        // add to etel vars
                        etelVars.put( item.getName(), item );
                } else {
                        // add to data object
                        dataObject.addItem( item );
                }
        }

	private void logBadDeBlock(TlsETel etel, TlsDeBlock deblock, int logAddress, String message) {
        String msg = "Bad DE Block: " + message + ": Node: " + logAddress/256 + "-" + logAddress%256 + 
        ", Fg " + etel.getFg() + ", Id " + etel.getTlsId() + ", Typ " + deblock.getDeTyp() + 
        ", DE Block(including header): /" + Helper.hexdump( deblock.getBytes() ) + "/"; 
		LOGGER.error( msg );
		
		if (null != smm) {
		    smm.sendMessage( msg );
		}
	}
}
