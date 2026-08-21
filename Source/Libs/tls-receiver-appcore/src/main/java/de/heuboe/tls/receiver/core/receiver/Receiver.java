package de.heuboe.tls.receiver.core.receiver;

import de.heuboe.tls.receiver.core.telein.TlsKafkaTelgramReceiver;
import de.heuboe.tls.receiver.interfaces.*;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import de.heuboe.tls.receiver.rdr.item.TimeItem;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The most relevant class realizing the TLS receive functionality
 * @author ronald
 *
 */
@Slf4j
public class Receiver {
    
    @Autowired // NOSONAR leave it this way for now, since changing may affect a lot of other places
    private TlsKafkaTelgramReceiver teleReceiverK;
    private TeleReceiver teleReceiver;
    private boolean doRun;
    
    //===========================
    
    @Autowired // NOSONAR leave it this way for now, since changing may affect a lot of other places
    private Transformer transformer;

    @Autowired  @Qualifier("uzId") // NOSONAR leave it this way for now, since changing may affect a lot of other places
    private String uzId;
    
    @Autowired @Qualifier("writer") // NOSONAR leave it this way for now, since changing may affect a lot of other places
    private DataWriter dataWriter;
    
    @Autowired // NOSONAR leave it this way for now, since changing may affect a lot of other places
    MeterRegistry meterRegistry;

    @Autowired // NOSONAR leave it this way for now, since changing may affect a lot of other places
    SystemMessageManagement smm;
    
    @Autowired @Qualifier( "timeoffset" ) // NOSONAR leave it this way for now, since changing may affect a lot of other places
    int timeoffset;
    
    @Autowired @Qualifier( "timeoffsetmap" ) // NOSONAR leave it this way for now, since changing may affect a lot of other places
    Map< TlsDatatypeId, Integer /*timeoffset*/ > timeoffsetMap;
    
    Counter counter;
    

    public Receiver() { // NOSONAR no constructor code needed
    }
    
    @PostConstruct
    public void init() throws IOException { // NOSONAR refactoring the Runnable will not make code better
        if ( null != teleReceiver ) {
            log.info( "VMIS2 Tls Receiver: Inhibiting init twice ;-)" );
            return;
        }
        teleReceiver = teleReceiverK;
        teleReceiverK.checkReceiver();
        
        doRun = true;
        
        counter = Counter.builder( "TelegramsReceived" )
                .description( "Telgrams (Sammeltelegramme) received via Kafka" )
                .baseUnit( "Telegramme" )
                .tag( "uzId", uzId )
                .register( meterRegistry );
        
        Runnable worker = new Runnable() {

            @Override
            public void run() {
                try {
                    log.info( "Started VMIS2 Tls Receiver" );
                    boolean doRunLocal = false; // make it obvious NOSONAR
                    synchronized ( this ) {
                        doRunLocal = doRun;
                    }
                    while ( doRunLocal ) {
                        try { // NOSONAR important to let receiver run
                            Receiver.this.receiveTelegrams();
                            synchronized ( this ) {
                                doRunLocal = doRun;
                            }
                        }
                        catch(Exception e) {
                            String msg1 = "Something REALLY bad happenend, but continuing ...";
                            log.error( "          XXX ==>>> " + msg1 + "\n\n\n\n\n", e );
                            
                            if (null != smm) {
                                smm.sendMessage( msg1 + "\n" + e );
                            }
                        }
                    }
                    log.info( "terminating receive thread" );
                }
                catch (Throwable e) {
                    String msg1 = "Something EVIL happenend ...";
                    log.error( msg1, e );
                    
                    if (null != smm) {
                        smm.sendMessage( msg1 + "\n" + e );
                    }
                    
                    System.exit( 15 );
                }
            }
            
        };
        
        Thread tlsReceiver = new Thread( worker, "TlsReceiver" );
        tlsReceiver.start();
    }

    /**
     * Receives available telegrams from {@code teleReceiver}, transforms them and writes
     * them via the configured {@code dataWriter}. Metrics and system messages are also handled.
     * Package-private so it can be unit-tested without spinning up a background thread.
     */
    void receiveTelegrams() {
        List<TlsTele> teleList = teleReceiver.receive();
        if ( !teleList.isEmpty() ) {
            log.info( "received {} telegrams", teleList.size() );
            counter.increment( teleList.size() );
            for ( TlsTele tlsTele : teleList ) {
                log.trace( "Telegram from {} for {}", tlsTele.getRealAddress(), tlsTele.getLogAddress() );
                dataWriter.beginEtel();

                List<DataObjectIf> objList = transformer.transform( tlsTele );

                if ( null == objList ) {
                    String msg = "Telegram transformed to null. Probably an error within. Skipped!";
                    log.warn( msg );

                    if (null != smm) {
                        smm.sendMessage( msg );
                    }

                    continue;
                }

                List<DataObjectIf> objListSubsituted = applyTimeOffset(objList);

                log.debug( "tele transformed to {} objects", objList.size() );
                for ( DataObjectIf obj : objListSubsituted ) {
                    dataWriter.write( obj );
                }
                dataWriter.endEtel();
            }
        }
    }

    /**
     * Applies the configured {@code timeoffset} (in seconds) to all {@link TimeItem} instances
     * contained in the provided list of lists. Non-time objects are passed through unchanged. The
     * returned list is a new list; the provided list is modified.
     * Package-private for unit testing.
     *
     * @param objList list of transformed data objects (may be empty, must not be null)
     * @return new list where {@link TimeItem} dates are shifted by {@code timeoffset}
     */
    List<DataObjectIf> applyTimeOffset(List<DataObjectIf> objList) {
        List<DataObjectIf> objListSubstituted = new ArrayList<>();
        for (DataObjectIf objT : objList) {
            
            DataObject obj = (DataObject) objT;
            for( DataItem item : obj.getItems() ) {
                if( (0 != timeoffset || !timeoffsetMap.isEmpty()) && item instanceof TimeItem ti ) {
                    // provide data for lookup of a special timeshift for a certain typ of DE-Block
                    DataObject.ETelMeta etelInfo = obj.getEtelMeta();
                    DataObject.DeMeta deInfo = obj.getDeMeta();
                    TlsDatatypeId tlsTypeId =
                             new TlsDatatypeId( (short) etelInfo.getFg(), (short) etelInfo.getId(), (short) deInfo.getTyp() );
                    
                    Integer offset;
                    long milliSeconds = ti.getAsDate().getTime();
                    
                    // lookup if there is a special timeshift for a certain typ of DE-Block
                    if ( null != ( offset = timeoffsetMap.get( tlsTypeId ) ) ) {
                        // get time shifted value
                        milliSeconds += offset * 1000L;
                    } else {
                        // get time shifted value
                        milliSeconds += timeoffset * 1000L;
                    }
                    // make new TimeItem
                    ti = new TimeItem( ti.getName(), new Date( milliSeconds ), ti.getConsumedSize() );
                    // replacee entries in itemList and itemMap
                    obj.getItems().set( obj.getItems().indexOf( item ), ti );
                    obj.getItemMap().put( ti.getName(), ti );
                }
            }
            objListSubstituted.add( obj );
        }
        return objListSubstituted;
    }
}
