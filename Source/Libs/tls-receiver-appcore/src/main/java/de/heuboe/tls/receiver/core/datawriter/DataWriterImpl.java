package de.heuboe.tls.receiver.core.datawriter;

import static de.heuboe.tls.tlstele.meta.Helper.hexdump;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.concurrent.ListenableFuture;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.rcv.data.cvtinterface.ConversionRegistryIf;
import de.heuboe.tls.rcv.data.cvtinterface.TlsReceiveDataConverterIf;
import de.heuboe.tls.rcv.data.cvtinterface.TlsReceiveDataConverterIf.ConvertReturn;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;

/**
 * This class implements the conversion of the inner representation of analysed TLS data into an external form
 * In this case it will be protobut messages
 * @author ronald
 *
 */
public class DataWriterImpl implements DataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger( DataWriterImpl.class );

    private Map<String, TlsReceiveDataConverterIf > analysisConvertedDataTypes;
    
    private final Set<String> topics = ConcurrentHashMap.newKeySet();
//    @Autowired
//    private AdminClient adminClient;
    public static final String RETENTION_MS = "retention.ms";
    public static final String TOPIC_PREFIX = "";
    
    @Autowired  @Qualifier("plugins")
    private String plugins;
    @Autowired  @Qualifier("packageName")
    private String packageName;
    @Autowired  @Qualifier("plugJarNameTemplate")
    private String plugJarNameTemplate;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired  @Qualifier("topicPrefix")
    private String topicPrefix;
    @Autowired  @Qualifier("topicPostfix")
    private String topicPostfix;
    @Autowired
    MeterRegistry meterRegistry;
    @Autowired
    Osi7Cfg osi7Cfg;
    @Autowired
    SystemMessageManagement smm;
    
    @Autowired(required = false)  @Qualifier("SubsequentTopicSuffix")
    private String SubsequentTopicSuffix = "Nachg";
    
    @Autowired
    private ConversionRegistryIf convReg;
    
    Counter counterFailedSoft;
    Counter counterFailedHard;
    Counter counterSendedObjects;
    Timer writeTimer;
    
    /**
     * refresh the set of topics used with the kafka server
     * @throws InterruptedException may be interrupted?
     * @throws ExecutionException dont know - see kafka
     */
//    public void refreshTopicCache() throws InterruptedException, ExecutionException {
//        this.topics.addAll(this.adminClient.listTopics().names().get());
//    }
    
    private static final String DATA_OBJECTS_TXT = "DataObjects"; // make sonar happy

    /**
     * after spring construction this method will be called and initialise converter functionality
     * the autowired properties are used therefore
     */
    @PostConstruct
    public void init() {
        analysisConvertedDataTypes = new LinkedHashMap<>();

        counterFailedSoft = Counter.builder( "DataObjects.failed.soft" )
                .description( "DataObjects that missing data during conversion" )
                .baseUnit( DATA_OBJECTS_TXT )
                //.tag( "uzId", uzId )
                .register( meterRegistry );
        counterFailedHard = Counter.builder( "DataObjects.failed.hard" )
                .description( "DataObjects that failed to convert to protbuf" )
                .baseUnit( DATA_OBJECTS_TXT )
                //.tag( "uzId", uzId )
                .register( meterRegistry );
        counterSendedObjects = Counter.builder( "DataObjects.sended" )
                .description( "DataObjects that where sended via kafak" )
                .baseUnit( DATA_OBJECTS_TXT )
                //.tag( "uzId", uzId )
                .register( meterRegistry );
        writeTimer = Timer.builder( "Datawriter.written" )
                .description( "DataObjects converted to protobuf and written via Kafka" )
                .publishPercentiles( 0.5, 0.9, 0.99 )
                //.tag( "uzId", uzId )
                .register( meterRegistry );

        LOGGER.info( "Created all DataWriterImpl" );

    }

    @Override
    public void beginEtel() {
        // NOSONAR unused
        LOGGER.debug( "Begin telegram" );
    }

    @Override
    public void endEtel() {
        // Emit data to kafka
        Set<String> activated = analysisConvertedDataTypes.keySet();
        activated.stream().map( 
                topic -> send( topic, "dummy", analysisConvertedDataTypes.get( topic ).getConvertedObjects() ) 
            ).collect( Collectors.toList() );
        LOGGER.debug( "Done telegram" );
        analysisConvertedDataTypes.clear();
    }
    
    private CompletableFuture<SendResult<String, Object>> send( String topic, String id, Object data ) {
        String target = topicPrefix + topic + topicPostfix;
        LOGGER.trace( " -> {}", target );
        counterSendedObjects.increment();
        return kafkaTemplate.send( MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, target).setHeader(KafkaHeaders.KEY, id).build() );
    }
    
    private String topicName( String name ) {
        return TOPIC_PREFIX + name;
    }
    
    private boolean showNoIdMessages = true;

    public boolean isShowNoIdMessages() {
        return showNoIdMessages;
    }

    public void setShowNoIdMessages( boolean showNoIdMessages ) {
        this.showNoIdMessages = showNoIdMessages;
    }

    @Override
    /**
     * Convert analysed TLS data into target format protobuf using generated code
     * @param analysed a DataObject containing the inner representation of analysed TLS data
     */
    public void write( DataObjectIf analysed ) {
        Sample start = Timer.start( meterRegistry );
        
        String name = analysed.getName(); // i.e. datatype
        String topicName = topicName( name );
        
        if (topicName.startsWith( "-" )) { // there were problems to analyse the input. skip the rest
            LOGGER.trace( "Skipping " + topicName + ". Assuming earlier errors." );
            return;
        }
        
        if (analysed.isSubsequent()) {
            topicName += SubsequentTopicSuffix;
        }
        
        TlsReceiveDataConverterIf converter = convReg.getConverter( name  );
        if (null == converter) {
            converter = convReg.getConverter( name + "_16Bit"  );
        }
        
        try {
            if (null == converter) {
                throw new IllegalStateException( "Cannot find converter for " + name + "{_16Bit}" );
            }

//            if orb null != converter dot convert( analysed ) crb ocb // Stores converted objects in itself
//                printProblem( analysed, " \\- Problems during converion of ", "" )
//            ccb
//            analysisConvertedDataTypes.put( topicName, converter )
            String id = analysed.getId();
            // an object without id cannot be processed further. Otherwise it would result in an exception.
            if (null == id) {
                if (isShowNoIdMessages()) { 
                    printProblem( analysed, "During conversion of", "" );
                }
                return;
            }
            List<String> virtualTargets = osi7Cfg.getVirtualTargets( id );
            if ( null != virtualTargets ) {
                handleMultipliedData( analysed, topicName, converter, virtualTargets );
            } else {
                handleNonMultipiedData( analysed, topicName, converter );
            }
        } catch ( Exception e ) {
            counterFailedHard.increment();
            if (analysed instanceof DataObject) {
                String message = e.getMessage();
                printProblem( analysed, "Exception during conversion of", message );
            } else {
                String msg = "Could not convert unknown type of DataObject";
                LOGGER.error( msg );
                
                if (null != smm) {
                    smm.sendMessage( msg );
                }
            }
        }
        start.stop( writeTimer );
    }
    
//    private FileOutputStream fs = null;

    private void handleNonMultipiedData( DataObjectIf analysed, String topicName,
            TlsReceiveDataConverterIf converter ) {
        ConvertReturn ret = converter.convert2( analysed );
        LOGGER.debug( "  Data for {}", topicName );
        send( topicName, ret.getId(), ret.getRetList() );
        if ( null != ret.getErr() ) {
            counterFailedSoft.increment();
            
            String msg = ret.getErr();
            
            String pref = "";
            if ( analysed instanceof DataObject ) {
                DataObject dob = (DataObject) analysed; 
                pref = "When decoding fg/id/typ=" + dob.getEtel().getFg() + "/" + dob.getEtel().getTlsId() + "/" + dob.getDeMeta().getTyp() + ": ";
            }
            
            LOGGER.error(  "@local object " + analysed.getId() + ": " + pref + msg );
            
//            { // analysis HE only
//                if (null == fs) {
//                    try {
//                        fs = new FileOutputStream( "missingItems.txt" );
//                    }
//                    catch ( FileNotFoundException e ) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//                
//                try {
//                    fs.write( ("@local object " + analysed.getId() + ": " + pref + msg).getBytes( StandardCharsets.UTF_8 ) );
//                }
//                catch ( IOException e ) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
            
            if (null != smm) {
                smm.sendMessage( msg );
            }
        }
    }

    private void handleMultipliedData( DataObjectIf analysed, String topicName, TlsReceiveDataConverterIf converter,
            List<String> virtualTargets ) {
        ConvertReturn ret = converter.convert2( analysed );
        LOGGER.debug( "  Data for {}", topicName );
        send( topicName, ret.getId(), ret.getRetList() );
        if ( null != ret.getErr() ) {
            counterFailedSoft.increment();
            
            String msg = ret.getErr();
            
            LOGGER.error( msg );
            
            if (null != smm) {
                smm.sendMessage( msg );
            }
        }
        for (String virtTarget : virtualTargets) {
            DataObject dob = (DataObject) analysed;
            dob.setAddress( virtTarget );
            ret = converter.convert2( analysed );
            LOGGER.debug( "  Data for {}", topicName );
            send( topicName, ret.getId(), ret.getRetList() );
            if ( null != ret.getErr() ) {
                counterFailedSoft.increment();
                
                String msg = ret.getErr();
                
                LOGGER.error( msg );
                
                if (null != smm) {
                    smm.sendMessage( msg );
                }
            }
        }
    }

    private void printProblem( DataObjectIf analysed, String msg, String exMessage ) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String callingMethod = stack[1].getMethodName();
        int line = stack[1].getLineNumber();
        String classname = stack[1].getClassName();
        String calledFrom = "Called from " + classname + "." + callingMethod + "[" + line + "]";
        
        DataObject errObj = (DataObject) analysed;
        String txt = null == errObj.getAddress() ? "unknown location (node/de/fg)" : "other reason";
        if (null != exMessage && 0 < exMessage.length() ) {
            txt = exMessage;
        }
        String msgOut = String.format( "%s node %s fg %d id %d job %d de %d typ %d. Reason %s", 
                msg,
                lesbar( errObj.getTele().getLogAddress() ), // NOSONAR very simple call
                errObj.getEtel().getFg(),
                errObj.getEtel().getTlsId(),
                errObj.getEtel().getJob(),
                errObj.getDeMeta().getDeNr(),
                errObj.getDeMeta().getTyp(),
                txt
                );
        String msgOut2 = String.format( "ETel /%s/", hexdump( errObj.getEtel().getBytes() ) );
        LOGGER.error( msgOut );
        LOGGER.error( msgOut2 );
        
        if (null != smm) {
            StringBuilder sb = new StringBuilder();
            sb.append( calledFrom );
            sb.append( ":\n" );
            sb.append( msgOut );
            sb.append( "\n" );
            sb.append( msgOut2 );
            smm.sendMessage( sb.toString() );
        }
    }
    
    private String lesbar( int node ) {
        int loc = node / 256;
        int dist = node % 256;
        return String.format( "%d ~ %5d-%3d", node, loc, dist );
    }

//    private NewTopic createTopicDeclaration( final String topicName ) +
//        final Map<String, String> config = new LinkedHashMap<>()
//        String retention = Long.toString( this.kafkaDataRetentionTime.toMillis() )
//        config.put( RETENTION_MS, retention )
//        LOGGER.info( "Creating topic: {} with retention {}", topicName, retention )
//        return new NewTopic( topicName, 1, (short) 3 ).configs( config )
//    -

}
