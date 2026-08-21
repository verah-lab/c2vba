package de.heuboe.tls.receiver.core.telein;

import static de.heuboe.tls.tlstele.meta.Helper.hexdump;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TeleReceiver;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.meta.Direction;
import lombok.extern.slf4j.Slf4j;

/**
 * Objects of this class enable the receipt of telegrams (TLS Sammeltelegramm) via apache kafka.
 * The topic where the telegrams are received is 'TeleSReceived'
 * Since the listener is constructed using spring, ther can only be one.
 * @author ronald
 * 
 * Notable: implements seekToEnd of kafka partition(s), ConsumerSeekAware, onPartitionsAssigned, callback.seekToEnd
 *
 */


@Component
@EnableKafka
@Slf4j
public class TlsKafkaTelgramReceiver implements TeleReceiver, ConsumerSeekAware {
    
    private final AtomicInteger counter = new AtomicInteger(0);
    boolean endOfQ = false;
    TlsTele poison = new TlsTele(new Date(), Direction.RECEIVE, 0, 0);
    
    @Value("${de.heuboe.asfinag.tls.receiver.inputTopic}")
    String topicNameFromSpring;

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;
    
    @Autowired
    SystemMessageManagement smm;
    
    private BlockingQueue<TlsTele> receiveQueue;
    
    // ensure that seeking on topics is only done at start
    private final HashSet<TopicPartition> initialSeekDone = new LinkedHashSet<>();
    
    /**
     * Constructor. Nothing special.
     * Constructs the input queue.
     */
    public TlsKafkaTelgramReceiver() {
        log.info( "creating TlsKafkaTelgramReceiver" );
        receiveQueue = new LinkedBlockingQueue<>();
    }
    
    
    /**
     * log name of topic to receive 
     */
    public void checkReceiver() {
        log.info( "Config: Receive topic: {}", topicNameFromSpring );
    }

    // example take from https://gitlab.heuboe.hbintern/VMIS2/control/control-service/vmis2-control-service/-/blob/master/src/main/java/de/heuboe/asfinag/vmis2/controlservice/service/ControlServiceImpl.java
    // a 'seekToLatest' may be found there. Thus one could read the last record of a topic

    @Override
    public void onPartitionsAssigned(
        java.util.Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback
    ) {
        //seek to end of topic(s) related to receiver input
        synchronized( this ) {
            for( TopicPartition partition : assignments.keySet() ) {
                if( topicNameFromSpring.equals( partition.topic() ) ) {
                    if( !initialSeekDone.contains( partition ) ) {
                        log.info( "Seek to end of topic {} partition {} in ThrId {}",
                                 partition.topic(),
                                 partition.partition(), Thread.currentThread().getId() );
                        callback.seekToEnd( partition.topic(), partition.partition() );
                        initialSeekDone.add( partition );
                        log.info( "Seek to end done" );
                    } else {
                        log.warn( "Topic {} partition {} assigned more than once (ignored)",
                                 partition.topic(),
                                 partition.partition() );
                    }
                }
            }
            log.info( "Sought to end of all relevant topics (TelegramReceiver)" );
        }
        
    }
    
    /**
     * This method receives the telegrams via topic 'TeleSReceived'
     * @param teleIn Telegram coming via kafka (TLS Sammeltelegramm)
     */
    @KafkaListener(topics = "${de.heuboe.asfinag.tls.receiver.inputTopic}")
    public void receive(final TeleSReceived teleIn) {
        log.trace("ThrId {} - received TeleSReceived payload='{}'", Thread.currentThread().getId(), teleIn);
        int realAddress = teleIn.getRealAddress();
        byte[] stele = teleIn.getTlsSTel().toByteArray();
        
        try {
            handleInputTelegram( realAddress, teleIn.getFlags(), stele );
        }
        catch ( InterruptedException e ) { // NOSONAR dont wanna get iterrupted
            String msg = "Interrupted handling telegram from " + Integer.toString( realAddress ) + ": ";
            log.error( msg, e );
            
            if (null != smm) {
                smm.sendMessage( msg + e.toString() );
            }
            
            Thread.currentThread().interrupt();
        }
    }
    
    //
    // portions of the following code could possibly be moved to a library / interface between iface and app
    //
    
    private void handleInputTelegram( int realAddress, int flags, byte[] stele ) throws InterruptedException {
        
        // handle status messages
        if (0 == realAddress) {
            handleCommunicationStatusMessage( stele );
        } else {
            putTeleToQ( realAddress, flags, stele );
        }
    }
    
    private enum CommToken { StartComm, StopComm, WerLebt, CommStat, TimeSync, CommStatDetail; // NOSONAR match names given in the past
        private static Map<Integer,CommToken> createByVal;
        static {
            createByVal = new HashMap<>();
            for ( CommToken tok : CommToken.values() ) {
                createByVal.put( tok.ordinal(), tok );
            }
        }
        
        public static CommToken valueOf( int v ) {
            return createByVal.get( v );
        }
        
    } // ~ gdtls.h

    private void handleCommunicationStatusMessage( byte[] stat ) throws InterruptedException { // s.a. gdTls.cpp getStat
        
        CommToken statTok = CommToken.valueOf( stat[3] );
        if (null == statTok) {
            String msg = String.format( "unbekannte Statusmeldung: %d from [%s]", (int)stat[3], hexdump( stat ) );
            
            log.error( msg );
            
            if (null != smm) {
                smm.sendMessage( msg );
            }

            return;
        }
        switch(/*stat[3]*/statTok) {
            case CommStat/* .ordinal() */:
                handleTokenCommStat( stat );
              break;

            case CommStatDetail:
                // message type seems to be reserved for iface tools
                // so ignore it here
                //
                // handleTokenCommStatDetail( stat )
              break;
              
            default:
                String msg = String.format( "unzulässige Statusmeldung: %d from [%s]", (int)stat[3], hexdump( stat ) );
                
                log.error( msg );
                
                if (null != smm) {
                    smm.sendMessage( msg );
                }
          }
    }

    @SuppressWarnings( "unused" )
    private void handleTokenCommStatDetail( final byte[] stat ) throws InterruptedException { // NOSONAR commStatDetail may be handled here later again
        // Kommunikationsstatus
        // Wenn spontan, dann DaSYSFehlerDUE-Telegramm generieren und an Anwendung schicken
        if ( stat.length != 6 ) {
            throw new IllegalArgumentException( "Kommunikationsstatus (detail) mit falscher Laenge empfangen" + hexdump( stat )  );
        }
        byte[] tele = new byte[14];
        tele[0] = stat[0];
        tele[1] = stat[1];
        tele[2] = stat[2];
        tele[3] = 1; // Anzahl Einzeltelegramme
        tele[4] = 9; // Laenge Etel
        tele[5] = (byte) 254; // FG
        tele[6] = (byte) 129; // Id
        tele[7] = 0; // Job
        tele[8] = 1; // Anzahl DE-Bloecke
        tele[9] = 4; // Laenge DE-Block
        tele[10] = 0; // DE-Nummer
        tele[11] = (byte) 132; // DE-Typ
        tele[12] = stat[4]; // Fehlercode lowbyte
        tele[13] = stat[5]; // Fehlercode highbyte

        int knoten = ( tele[0] & 0xFF ) + ( ( tele[1] & 0xFF ) * 256 ) + ( ( tele[2] & 0xFF ) * ( 256 * 256 ) );

        putTeleToQ( knoten, 0, tele );
        log.info( "CommStatDetail {} {} {}", knoten, stat[4] & 0xff, stat[5] & 0xff  );
    }

    private void handleTokenCommStat( byte[] stat ) throws InterruptedException {
        // Kommunikationsstatus
        // Wenn spontan, dann DaSYSFehlerDUE-Telegramm generieren und an Anwendung schicken
        if ( stat.length != 6 ) {
            throw new IllegalArgumentException( "Kommunikationsstatus mit falscher Laenge empfangen: " + hexdump( stat ) );
        }
        if ( stat[5] == 0 ) { // only do something if the state is >>>not<<< queried !!!
            byte[] tele = new byte[14];
            tele[0] = stat[0];
            tele[1] = stat[1];
            tele[2] = stat[2];
            tele[3] = 1; // Anzahl Einzeltelegramme
            tele[4] = 9; // Laenge Etel
            tele[5] = (byte) 254; // FG
            tele[6] = (byte) 129; // Id
            tele[7] = 0; // Job
            tele[8] = 1; // Anzahl DE-Bloecke
            tele[9] = 4; // Laenge DE-Block
            tele[10] = 0; // DE-Nummer
            tele[11] = (byte) 131; // DE-Typ
            tele[12] = stat[4]; // Fehlercode
            tele[13] = 6; // Hersteller Heusch Boesefeldt

            int knoten = ( tele[0] & 0xFF ) + ( ( tele[1] & 0xFF ) * 256 ) + ( ( tele[2] & 0xFF ) * ( 256 * 256 ) );

            putTeleToQ( knoten, 0, tele );
            log.info( "CommStat node {} state {} [0 = alive]", knoten, stat[4] & 0xff );

            if ( stat[4] != 0 ) {
                putRouteDead( knoten );
            }
        }
    }
    
    @Autowired
    private AddressConverter ac;
    
    private void putRouteDead( int realAddress ) throws InterruptedException {
        Collection<Integer/*node numbers of children*/> childNodes = ac.descendants( realAddress );
        for (int nodeNum : childNodes) {
            byte[] tele = new byte[14];
            tele[0] = (byte) (nodeNum & 0xff);
            tele[1] = (byte) ((nodeNum & 0xff00) >> 8);
            tele[2] = (byte) ((nodeNum & 0xff0000) >> 16);
            tele[3] = 1;                            // Anzahl Einzeltelegramme
            tele[4] = 9;                            // Laenge Etel
            tele[5] = (byte) 254;                   // FG
            tele[6] = (byte) 129;                   // Id
            tele[7] = 0;                            // Job
            tele[8] = 1;                            // Anzahl DE-Bloecke
            tele[9] = 4;                            // Laenge DE-Block
            tele[10]= 0;                            // DE-Nummer
            tele[11]= (byte) 131;                   // DE-Typ
            tele[12]= 2;                            // Fehlercode fuer Route tot
            tele[13]= 6;                            // Hersteller Heusch Boesefeldt

            putTeleToQ( nodeNum, 0, tele );
            log.info( "RouteDead {}", nodeNum );
        }
    }

    private void putTeleToQ( int realAddress, int flags, byte[] stele ) throws InterruptedException { // NOSONAR flags kept for legacy issues
        
        // remember we could have flags here: teleIn.getFlags()
        TlsTele tel = null;
        try {
            tel = new TlsTele(new Date(), Direction.RECEIVE, realAddress , stele, 0, stele.length);
        } catch ( TlsBadTele e ) {
            String msg = String.format( "Telegram skipped. Error constructing TlsTele received via Kafka: %s", e.getMessage() );
            log.error( msg );
            
            if (null != smm) {
                smm.sendMessage( msg );
            }

            return;
        }
        
        counter.incrementAndGet();
        receiveQueue.put( tel );
        log.trace("receiveQueue size='{}'", receiveQueue.size());
    }

    /**
     * Get one or more telegrams that have been received in the past
     * This may be a blocking call, if no telegrams are present at the time of the call 
     * Thus it is supposed that there is a dedicated thread that reads the telegrams
     * @return A list of one or more telegrams
     */
    @Override
    public List<TlsTele> receive() {
        log.trace("> receive" );
        if (endOfQ) {
            String msg = "Should not call receive() due to calling stopReceive";
            log.error( msg );
            
            if (null != smm) {
                smm.sendMessage( msg );
            }

            return null; // NOSONAR this behaviour is expected by caller
        }
        
        int num = receiveQueue.size();

        List<TlsTele> res = new LinkedList<>();

        if ( 0 == num ) {
            try {
                res.add( receiveQueue.take() );
            } catch ( InterruptedException e ) { // NOSONAR ignore interrupt
                // NOSONAR ignore
            }
        } else {
            receiveQueue.drainTo( res );
        }

        if ( res.contains( poison ) ) {
            res.remove( poison );
            endOfQ = true;
        }
        
        log.trace("< receive" );
        return res;
    }

    /**
     * This method can be called to stop a receiving thread
     * It places a poison object in the queue that can be detected by the sink (caller of public List&lt;TlsTele&gt; receive())
     */
    @Override
    public void stopReceive() {
        try {
            receiveQueue.put( poison );
        } catch ( InterruptedException e ) {  // NOSONAR ignore interrupt
            String msg = "Could not drop poison to stop";
            log.error( msg );
            
            if (null != smm) {
                smm.sendMessage( msg );
            }

            return;
        }
        counter.incrementAndGet();
    }

}
