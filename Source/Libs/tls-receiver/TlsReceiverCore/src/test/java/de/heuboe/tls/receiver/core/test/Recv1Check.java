package de.heuboe.tls.receiver.core.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.heuboe.log.Logger;
import de.heuboe.test.result.comparator.ResultComparator;
import de.heuboe.test.result.comparator.ResultComparator.CompareResult;
import de.heuboe.tls.receiver.app.TlsReceiver;
import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.impl.TransformationReaderImpl;
import de.heuboe.tls.receiver.impl.TransformerImpl;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import de.heuboe.tls.receiver.interfaces.TeleReceiver;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.TlsTele.Direction;

public class Recv1Check {
        static final int etelLenPos = 4;

        static public class TestReceiver implements TeleReceiver {

                public List<TlsTele> receive() {
                        byte[] stel = { 1, 2, 3, // log address
                                        1, // num etel
                                        0, // !! etel len
                                        1, // fg
                                        (byte) ( 1 + 128 ), // id
                                        0, // job
                                        2, // numDE

                                        // Zeitstempel Typ 30
                                        5, // deLength
                                        (byte) 255, // deNumber
                                        30, // deTyp ==>> fg/id/typ = 1/1/30 = zeitstempel
                                        (byte) (17), //( 17 + 128 ), // stunde
                                        12, // minute
                                        6, // sekunde

                                        4, // deLength
                                        33, // deNumber
                                        1, // deTyp ==>> fg/id/typ = 1/1/1 = lve defehler
                                        0, // fehlercode
                                        6, // hersteller hb
                        };
                        int osi7Len = stel.length;
                        TlsTele tel = null;
                        stel[etelLenPos] = (byte) ( stel.length - 5 );
                        try {
                                tel = new TlsTele( new Date(), Direction.RECEIVE, 1001 * 256 + 1, stel, 0, osi7Len );
                        }
                        catch ( TlsBadTele e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        List<TlsTele> res = new LinkedList<>();
                        res.add( tel );

                        byte[] stel2 = { 4, 4, 4, // log address
                                        1, // num etel
                                        0, // !! etel len
                                        1, // fg
                                        (byte) ( 1 + 128 ), // id
                                        0, // job
                                        2, // numDE

                                        // Zeitstempel Typ 251 ~ Unix-Zeitstempel
                                        6, // deLength
                                        (byte) 255, // deNumber
                                        (byte) 251, // deTyp ==>> fg/id/typ = 1/1/30 = zeitstempel
                                        (byte) ( 0x5b ), (byte) ( 0x6e ), (byte) ( 0xe3 ), (byte) ( 0xc8 ), // => 11.8.2018 15:25:28 MESZ

                                        4, // deLength
                                        33, // deNumber
                                        1, // deTyp ==>> fg/id/typ = 1/1/1 = lve defehler
                                        0, // fehlercode
                                        6, // hersteller hb
                        };
                        osi7Len = stel2.length;
                        tel = null;
                        stel2[etelLenPos] = (byte) ( stel2.length - 5 );
                        try {
                                tel = new TlsTele( new Date(), Direction.RECEIVE, 1001 * 256 + 1, stel2, 0, osi7Len );
                        }
                        catch ( TlsBadTele e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        res.add( tel );

                        return res;
                }

                public TlsTele getTel( String key ) {
                        return null;
                }

                /**
                 * in case a receiver has an (infinite) loop we want to break for a shutdown for instance
                 */
                public void stopReceive() {
                }
        }

        static public class AddressConverterStolpe implements AddressConverter {

                @Override
                public String convert( int node, int fg, int de ) {
                        int id = de + 256 * node;
                        return Integer.toString( id );
                }
        }

        static public class DataWriterImplTest implements DataWriter {
                private static final Logger LOGGER = Logger.getLogger( DataWriterImplTest.class );

                @Override
                public void write( DataObject obj ) {
                }

                @Override
                public void beginEtel() {
                }

                @Override
                public void endEtel() {
                }

                // public boolean isNoMissingHistComplain() {
                // return noMissingHistComplain;
                // }
                //
                // public void setNoMissingHistComplain( boolean noMissingHistComplain ) {
                // this.noMissingHistComplain = noMissingHistComplain;
                // }

        }

        static TlsReceiver                  tlsReceiver;
        private static ResultComparator     trc = null;

        static private AddressConverter     addressConverter;
        // static private DataWriter dataWriter;
        static private TeleReceiver         teleReceiver;
        static private TransformationReader transformationReader;
        static private Transformer          transformer;
        static private File                 specFile;
        static TransformationRules          transformationRules;

        @BeforeAll
        public static void setUp() throws Exception {
                ResultComparator.createFolders( true );
                trc = ResultComparator.createComparator( System.getProperty( "user.dir" ) + "/src/test/resources/checkResult",
                                System.getProperty( "user.dir" ) + "/src/test/resources/result",true );
                // }
                //
                // @BeforeClass
                // public static void init1() throws IOException {
                // SpringApplication app = new SpringApplication( Config.class );
                // ConfigurableApplicationContext ctx = app.run( "" );
                // Connection con = (Connection) ctx.getBean( "Connection" );

                addressConverter = new AddressConverterStolpe();
                // DataWriter dataWriter = new DataWriterImplTest();
                // TeleReceiver teleReceiver = new TestReceiver();
                TransformationReader transformationReader = new TransformationReaderImpl();
                transformer = new TransformerImpl();
                String receiveScript = "src/test/resources/rcv-stolpe-ab.txt";

                try {
                        transformationRules = transformationReader.createTransformationRules( new File( receiveScript ) );
                }
                catch ( Throwable e ) {
                        e.printStackTrace();
                        System.err.println( "Exception during reading of script: " + e.getMessage() );
                        fail();
                }

                transformer.setAddressConverter( addressConverter );
                transformer.setTransformationRules( transformationRules );
                transformer.init();
        }

        @Test
        @Disabled
        public void test1() throws Exception {
                TeleReceiver teleReceiver = new TestReceiver();
                List<TlsTele> recvd = null;
                try {
                        recvd = teleReceiver.receive();
                }
                catch ( Exception e1 ) {
                        e1.printStackTrace();
                        System.out.println( e1.getMessage() );
                        fail();
                }

                List<DataObject> decoded = null;
                {
                        try {
                                decoded = transformer.transform( recvd.get( 0 ) );
                        }
                        catch ( Exception e1 ) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                        }
                        for ( DataObject dob : decoded ) {
                                dob.setTele( null );
                                dob.setEtel( null );
                        }
                        
                        // expecting 1 DataObject:
                        //  name LVEFehlerDE
                        //  subsequent false
                        //   dataitem Fehlercode = 0
                        //   dataitem Hersteller = 6
                        //   dataitem #Puffer = 0/false
                        //   dataitem Zeitstempel ~ today, 17:12:06 [!always today (or yesterday ;))]

                        assertEquals( 1, decoded.size() );
                        DataObject o = decoded.get( 0 );
                        assertEquals( false, o.isSubsequent() );
                        assertEquals( "LVEFehlerDE", o.getName() );
                        for ( DataItem item : o.getItems() ) {
                                switch ( item.getName() ) {
                                case "Fehlercode":
                                        assertEquals(  0, item.getAsLong().longValue() );
                                        break;
                                case "Hersteller":
                                        assertEquals(  6, item.getAsLong().longValue() );
                                        break;
                                case "#Puffer":
                                        assertEquals(  0, item.getAsLong().longValue() );
                                        break;
                                case "Zeitstempel":
                                        GregorianCalendar now = new GregorianCalendar();
                                        GregorianCalendar timeIn = new GregorianCalendar();
                                        timeIn.setTimeInMillis( item.getAsLong().longValue() );
                                        assertEquals(  6, timeIn.get( java.util.Calendar.SECOND ) );
                                        assertEquals( 12, timeIn.get( java.util.Calendar.MINUTE ) );
                                        assertEquals( 17, timeIn.get( java.util.Calendar.HOUR_OF_DAY ) );
                                        assertEquals( now.get( java.util.Calendar.DAY_OF_MONTH ), timeIn.get( java.util.Calendar.DAY_OF_MONTH ) ); // today
                                        assertEquals( now.get( java.util.Calendar.MONTH ), timeIn.get( java.util.Calendar.MONTH ) );
                                        assertEquals( now.get( java.util.Calendar.YEAR ), timeIn.get( java.util.Calendar.YEAR ) );
                                        break;
                                default:
                                        fail( "Unexpected DataItem " + item.getName() );
                                        break;
                                }
                        }

                        // String result = JavaObject2JSON.j2String( newRl );

//                        CompareResult cr = null;
//                        String checkFileName = "Zst30-LveDeFehlerDe";
//                        try {
//                                cr = trc.checkResult( decoded, checkFileName );
//                        }
//                        catch ( Throwable e ) {
//                                e.printStackTrace();
//                                System.out.println( e.getMessage() );
//                                fail();
//                        }
//                        if ( cr.hasDifference() ) {
//                                System.out.println( "Difference for " + checkFileName );
//                                System.out.println( cr.getDifference() );
//                                fail();
//                        }
                }                

                decoded = null;
                {
                        try {
                                decoded = transformer.transform( recvd.get( 1 ) );
                        }
                        catch ( Exception e1 ) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                        }
                        for ( DataObject dob : decoded ) {
                                dob.setTele( null );
                                dob.setEtel( null );
                        }

                        // String result = JavaObject2JSON.j2String( newRl );

                        CompareResult cr2 = null;
                        try {
                                cr2 = trc.checkResult( decoded, "Zst251-LveDeFehlerDe" );
                        }
                        catch ( Throwable e ) {
                                e.printStackTrace();
                                System.out.println( e.getMessage() );
                                fail();
                        }
                        if ( cr2.hasDifference() ) {
                                System.out.println( "Difference for Zst251-LveDeFehlerDe" );
                                System.out.println( cr2.getDifference() );
                                fail();
                        }
                }
                int i = 0; i = i + 1;
        }
        @Test
        public void test_2_3_31_AXLZeitstempel() {
     
            TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
            builder.addDeBlockHeader(255, 31)
                    .addInt16(1983) // jahr (zwei Byte von 0-9999)
                    .addByte(3) // monat
                    .addByte(5) // tag
                    .addByte(22 + 0/*dstOffset*/) // stunde (bit 8 = Sommerzeit flag)
                    .addByte(7) // minute
                    .addByte(49) // sekunde
                    .addByte(96) // hundertstelsekunden
                    .addInt16(12372) // laufendeNr
                    .addInt16(51381); // gesamtzahl
     
            builder.addDeBlockHeader(33, 32)
                    .addByte(0) // skip
                    .addByte(0) // skip
                    .addByte(41) // datenversion
                    .addByte(136) // intervalldauer
                    .addByte(0) // skip
                    .addByte(109) // datenversionEinzel
                    .addByte(59); // speicherung
     
            TlsTele telegram = builder.getAsTlsTele();
     
            List<DataObject> decoded = null;
            {
                try {
                    decoded = transformer.transform(telegram);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    fail();
                }
                for (DataObject dob : decoded) {
                    dob.setTele(null);
                    dob.setEtel(null);
                }
     
                assertEquals(1, decoded.size());
                DataObject o = decoded.get(0);
                assertEquals(false, o.isSubsequent());
                assertEquals("AXLBetriebsparameter", o.getName());
     
                for (DataItem item : o.getItems()) {
                    switch (item.getName()) {
                    case "zeitstempel":
                        GregorianCalendar timeIn = new GregorianCalendar();
                        timeIn.setTimeInMillis(item.getAsLong().longValue());
                        assertEquals(1983, timeIn.get(Calendar.YEAR));
                        assertEquals(2, timeIn.get(Calendar.MONTH));
                        assertEquals(5, timeIn.get(Calendar.DAY_OF_MONTH));
                        assertEquals(22, timeIn.get(Calendar.HOUR_OF_DAY));
                        assertEquals(7, timeIn.get(Calendar.MINUTE));
                        assertEquals(49, timeIn.get(Calendar.SECOND));
                        break;
                    case "hundertstelsekunden":
                        assertEquals(96, item.getAsLong().longValue());
                        break;
                    case "laufendeNr":
                        assertEquals(12372, item.getAsLong().longValue());
                        break;
                    case "gesamtzahl":
                        assertEquals(51381, item.getAsLong().longValue());
                        break;
                    case "datenversion": 
                        assertEquals(41, item.getAsLong().longValue());
                        break;
                    case "intervalldauer": 
                        assertEquals(136, item.getAsLong().longValue());
                        break;
                    case "datenversionEinzel": 
                        assertEquals(109, item.getAsLong().longValue());
                        break;
                    case "speicherung": 
                        assertEquals(59, item.getAsLong().longValue());
                        break;
                    case "SKIPITEM":
                        break;
                    default:
                        fail("Unexpected DataItem " + item.getName());
                        break;
                    }
                }
            }
        }        
/*
/////////////////////////
// Locations
/////////////////////////

LOCATIONS TwoByteV78_2000Draft 6233-19, 7345-19, 4711-215

LOCATIONS OnePointFiveByteV78_2002 6233-21, 7645-19, 4712-115

Datatype SYSKnotennummer                                                                Fg 254 Id 131 Typ 37 @ TwoByteV78_2000Draft
NODE Knotennummer

Datatype SYSKnotennummer                                                                Fg 254 Id 131 Typ 37 -@ TwoByteV78_2000Draft //OnePointFiveByteV78_2002
NODE $Knotennummer
SET Knotennummer := $Knotennummer + 1

 */
        @Test
        @Disabled
        public void test2() {
                TlsTeleBuilder builder = new TlsTeleBuilder(254, 3 + 128);
                builder.addDeBlockHeader(33, 1)
                        .addByte(0)
                        .addByte(6);

                TlsTele telegram = builder.getAsTlsTele();
                byte[] ba = builder.getAsByteArray();

        }

        @AfterAll
        public static void leave1() {

        }
}
