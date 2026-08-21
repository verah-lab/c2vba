package de.heuboe.tls.receiver.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.heuboe.test.result.comparator.ResultComparator;
import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.impl.TransformationReaderImpl;
import de.heuboe.tls.receiver.impl.TransformerImpl;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.tlstele.TlsTele;

/**
 * <u>Objective:</u> Überprüfen ob der Tls-Receiver alle Verkehrsdaten
 * Telegramme (FG 1) empfangen kann<br>
 * <u>Description:</u> Ein Test für jeden Telegramm-Typen der FG 1<br>
 * <u>Requirements:</u> <br>
 * VMIS2-S1ANF-8 (Übernahme von Kurzzeit-Verkehrsdaten)<br>
 * VMIS2-S1ANF-10 (Übernahme von Einzelfahrzeugdaten)<br>
 * VMIS2-S1ANF-11 (Übernahme gepufferter Langzeit-Verkehrsdaten)<br>
 */
public class ReceiverFG2Tests {

    private static int              dstOffset = 0;

    private static AddressConverter    addressConverter;
    private static Transformer         transformer;
    private static TransformationRules transformationRules;

    @BeforeAll
    public static void setUp() throws Exception {

        if (new GregorianCalendar().get(Calendar.DST_OFFSET) != 0) {
            dstOffset = 128;
        }

        ResultComparator.createFolders(true);

        addressConverter = new TestAddressConverter();
        TransformationReader transformationReader = new TransformationReaderImpl();
        transformer = new TransformerImpl();
        String receiveScript = "src/test/resources/rcv-fg2.txt";

        try {
            transformationRules = transformationReader.createTransformationRules(new File(receiveScript));
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Exception during reading of script: " + e.getMessage());
            fail();
        }

        transformer.setAddressConverter(addressConverter);
        transformer.setTransformationRules(transformationRules);
        transformer.init();
    }

    @Test
    public void test_2_1_1_AXLDeFehler() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 129); // id = 1 + 128
        builder.addDeBlockHeader(33, 1)
                .addByte(163) // fehlercode
                .addByte(32); // hersteller

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
            assertEquals("AXLDeFehler", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fehlercode":
                    assertEquals(163, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(32, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_1_14_AXLErgDeFehler() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 129); // id = 1 + 128
        builder.addDeBlockHeader(33, 14)
                .addByte(248) // hersteller
                .addByteArrayWithSize(182, 80, 32, 35, 111) // tlsFehlerbytes
                .addByteArrayWithSize(156, 38, 120, 128); // herstellerFehlerbytes

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
            assertEquals("AXLErgDeFehler", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "hersteller":
                    assertEquals(248, item.getAsLong().longValue());
                    break;
                case "tlsFehlerbytes":
                    long[] tlsFehlerbytes = item.getAsBlock();
                    assertEquals(5, tlsFehlerbytes.length);

                    assertEquals(182, tlsFehlerbytes[0]);
                    assertEquals(80, tlsFehlerbytes[1]);
                    assertEquals(32, tlsFehlerbytes[2]);
                    assertEquals(35, tlsFehlerbytes[3]);
                    assertEquals(111, tlsFehlerbytes[4]);
                    break;
                case "herstellerFehlerbytes":
                    long[] herstellerFehlerbytes = item.getAsBlock();
                    assertEquals(4, herstellerFehlerbytes.length);
                    assertEquals(156, herstellerFehlerbytes[0]);
                    assertEquals(38, herstellerFehlerbytes[1]);
                    assertEquals(120, herstellerFehlerbytes[2]);
                    assertEquals(128, herstellerFehlerbytes[3]);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_2_16_AXLNegativeQuittung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 130); // id = 2 + 128
        builder.addDeBlockHeader(33, 16)
                .addByte(67) // fehlerursache
                .addByte(143); // hersteller

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
            assertEquals("AXLNegativeQuittung", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fehlerursache":
                    assertEquals(67, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(143, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_2_28_AXLPositiveQuittung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 130); // id = 2 + 128
        builder.addDeBlockHeader(33, 28);

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
            assertEquals("AXLPositiveQuittung", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "quittung":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_2_29_AXLKanalsteuerung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 130); // id = 2 + 128
        builder.addDeBlockHeader(33, 29)
                .addByte(214); // kanalsteuerbyte

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
            assertEquals("AXLKanalsteuerung", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "kanalsteuerbyte":
                    assertEquals(214, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_1_30_Zeitstempel() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 129); // id = 1 + 128
        builder.addDeBlockHeader(255, 30)
                .addByte(1 + dstOffset) // stunde (bit 8 = Sommerzeit flag)
                .addByte(15) // minute
                .addByte(13); // sekunde

        builder.addDeBlockHeader(33, 1)
                .addByte(64) // fehlercode
                .addByte(159); // hersteller

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
            assertEquals("AXLDeFehler", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {

                case "zeitstempel":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(now.get(Calendar.YEAR), timeIn.get(Calendar.YEAR));
                    assertEquals(now.get(Calendar.MONTH), timeIn.get(Calendar.MONTH));
                    assertEquals(now.get(Calendar.DAY_OF_MONTH), timeIn.get(Calendar.DAY_OF_MONTH));
                    assertEquals(1, timeIn.get(Calendar.HOUR_OF_DAY));
                    assertEquals(15, timeIn.get(Calendar.MINUTE));
                    assertEquals(13, timeIn.get(Calendar.SECOND));
                    break;
                case "fehlercode":
                    assertEquals(64, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(159, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_3_31_AXLZeitstempel() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(255, 31)
                .addInt16(1983) // jahr (zwei Byte von 0-9999)
                .addByte(3) // monat
                .addByte(5) // tag
                .addByte(22 + dstOffset) // stunde (bit 8 = Sommerzeit flag)
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

    @Test
    public void test_2_3_32_AXLBetriebsparameter() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(33, 32)
                .addByte(0) // skip
                .addByte(0) // skip
                .addByte(4) // datenversion
                .addByte(59) // intervalldauer
                .addByte(0) // skip
                .addByte(151) // datenversionEinzel
                .addByte(221); // speicherung

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
                case "datenversion":
                    assertEquals(4, item.getAsLong().longValue());
                    break;
                case "intervalldauer":
                    assertEquals(59, item.getAsLong().longValue());
                    break;
                case "datenversionEinzel":
                    assertEquals(151, item.getAsLong().longValue());
                    break;
                case "speicherung":
                    assertEquals(221, item.getAsLong().longValue());
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

    @Test
    public void test_2_3_36_AXLGeoKenndaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(33, 36)
                .addByte(89) // landeskennung
                .addByte(138) // strassenart
                .addBCD(2, "2586") // strassennummer
                .addBCD(3, "256997") // $kilometrierung
                .addByte(224) // fahrtrichtung
                .addByte(208); // reservebyte

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
            assertEquals("AXLGeoKenndaten", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "landeskennung":
                    assertEquals(89, item.getAsLong().longValue());
                    break;
                case "strassenart":
                    assertEquals(138, item.getAsLong().longValue());
                    break;
                case "strassennummer":
                    assertEquals(2586, item.getAsLong().longValue());
                    break;
                case "kilometrierung":
                    assertEquals(2569.97, item.getAsDouble().doubleValue());
                    break;
                case "fahrtrichtung":
                    assertEquals(224, item.getAsLong().longValue());
                    break;
                case "reservebyte":
                    assertEquals(208, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_3_37_AXLParameterAchslastGrenzwerte() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(33, 37)
                .addInt16(11767) // maxEinzelachslast1
                .addInt16(8436) // maxEinzelachslast
                .addByte(145) // maxAbstandDoppelachse1
                .addInt16(37335) // maxDoppelachslast1
                .addByte(243) // maxAbstandDoppelachse2
                .addInt16(54449) // maxDoppelachslast2
                .addByte(58) // maxAbstandDoppelachse3
                .addInt16(61513) // maxDoppelachslast3
                .addByte(233) // maxAbstandDreifachachse1
                .addInt16(51779) // maxDreifachachslast1
                .addByte(248) // maxAbstandDreifachachse2
                .addInt16(35786) // maxDreifachachslast2
                // Array fahrzeugtypen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 6)
                .addByte(221) // fahrzeugtypCode[0]
                .addInt16(7896) // maxGesamtgewicht[0]
                .addByte(159) // fahrzeugtypCode[1]
                .addInt16(58463); // maxGesamtgewicht[1]
        // Array fahrzeugtypen End

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
            assertEquals("AXLParameterAchslastGrenzwerte", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "maxEinzelachslast1":
                    assertEquals(11767, item.getAsLong().longValue());
                    break;
                case "maxEinzelachslast":
                    assertEquals(8436, item.getAsLong().longValue());
                    break;
                case "maxAbstandDoppelachse1":
                    assertEquals(145, item.getAsLong().longValue());
                    break;
                case "maxDoppelachslast1":
                    assertEquals(37335, item.getAsLong().longValue());
                    break;
                case "maxAbstandDoppelachse2":
                    assertEquals(243, item.getAsLong().longValue());
                    break;
                case "maxDoppelachslast2":
                    assertEquals(54449, item.getAsLong().longValue());
                    break;
                case "maxAbstandDoppelachse3":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "maxDoppelachslast3":
                    assertEquals(61513, item.getAsLong().longValue());
                    break;
                case "maxAbstandDreifachachse1":
                    assertEquals(233, item.getAsLong().longValue());
                    break;
                case "maxDreifachachslast1":
                    assertEquals(51779, item.getAsLong().longValue());
                    break;
                case "maxAbstandDreifachachse2":
                    assertEquals(248, item.getAsLong().longValue());
                    break;
                case "maxDreifachachslast2":
                    assertEquals(35786, item.getAsLong().longValue());
                    break;
                case "fahrzeugtypen":
                    List<DataObject> fahrzeugtypen = item.getAsArray();
                    assertEquals(2, fahrzeugtypen.size());

                    List<DataItem> fahrzeugtypen0 = fahrzeugtypen.get(0).getItems();
                    assertEquals(2, fahrzeugtypen0.size());
                    assertEquals(221, fahrzeugtypen0.get(0).getAsLong().longValue());
                    assertEquals(7896, fahrzeugtypen0.get(1).getAsLong().longValue());

                    List<DataItem> fahrzeugtypen1 = fahrzeugtypen.get(1).getItems();
                    assertEquals(2, fahrzeugtypen1.size());
                    assertEquals(159, fahrzeugtypen1.get(0).getAsLong().longValue());
                    assertEquals(58463, fahrzeugtypen1.get(1).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_3_38_AXLParameterAchslastklassen5p1() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(33, 38)
                // Array einzelachslastklasseGrenzen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(45901) // grenze[0]
                .addInt16(546) // grenze[1]
                .addInt16(9093) // grenze[2]
                .addInt16(53624) // grenze[3]
                .addInt16(28345) // grenze[4]
                // Array einzelachslastklasseGrenzen End
                // Array doppelachslastklasseGrenzen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(16844) // grenze[0]
                .addInt16(32276) // grenze[1]
                // Array doppelachslastklasseGrenzen End
                // Array dreifachachslastklasseGrenzen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(16119) // grenze[0]
                .addInt16(37038) // grenze[1]
                // Array dreifachachslastklasseGrenzen End
                // Array fahrzeugklasse3Grenzen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(49950) // grenze[0]
                .addInt16(45122) // grenze[1]
                .addInt16(26568) // grenze[2]
                .addInt16(57621) // grenze[3]
                .addInt16(46775) // grenze[4]
                // Array fahrzeugklasse3Grenzen End
                // Array fahrzeugklasse4Grenzen Begin
                .addByte(4) // number of ArrayElements (Number of Bytes: 8)
                .addInt16(5920) // grenze[0]
                .addInt16(65208) // grenze[1]
                .addInt16(29024) // grenze[2]
                .addInt16(8794) // grenze[3]
                // Array fahrzeugklasse4Grenzen End
                // Array fahrzeugklasse5Grenzen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(61649) // grenze[0]
                .addInt16(45927) // grenze[1]
                .addInt16(30370) // grenze[2]
                .addInt16(36057) // grenze[3]
                .addInt16(13262); // grenze[4]
        // Array fahrzeugklasse5Grenzen End

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
            assertEquals("AXLParameterAchslastklassen5p1", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "einzelachslastklasseGrenzen":
                    List<DataObject> einzelachslastklasseGrenzen = item.getAsArray();
                    assertEquals(5, einzelachslastklasseGrenzen.size());

                    List<DataItem> einzelachslastklasseGrenzen0 = einzelachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen0.size());
                    assertEquals(45901, einzelachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen1 = einzelachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen1.size());
                    assertEquals(546, einzelachslastklasseGrenzen1.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen2 = einzelachslastklasseGrenzen.get(2).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen2.size());
                    assertEquals(9093, einzelachslastklasseGrenzen2.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen3 = einzelachslastklasseGrenzen.get(3).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen3.size());
                    assertEquals(53624, einzelachslastklasseGrenzen3.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen4 = einzelachslastklasseGrenzen.get(4).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen4.size());
                    assertEquals(28345, einzelachslastklasseGrenzen4.get(0).getAsLong().longValue());
                    break;
                case "doppelachslastklasseGrenzen":
                    List<DataObject> doppelachslastklasseGrenzen = item.getAsArray();
                    assertEquals(2, doppelachslastklasseGrenzen.size());

                    List<DataItem> doppelachslastklasseGrenzen0 = doppelachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, doppelachslastklasseGrenzen0.size());
                    assertEquals(16844, doppelachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> doppelachslastklasseGrenzen1 = doppelachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, doppelachslastklasseGrenzen1.size());
                    assertEquals(32276, doppelachslastklasseGrenzen1.get(0).getAsLong().longValue());
                    break;
                case "dreifachachslastklasseGrenzen":
                    List<DataObject> dreifachachslastklasseGrenzen = item.getAsArray();
                    assertEquals(2, dreifachachslastklasseGrenzen.size());

                    List<DataItem> dreifachachslastklasseGrenzen0 = dreifachachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, dreifachachslastklasseGrenzen0.size());
                    assertEquals(16119, dreifachachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklasseGrenzen1 = dreifachachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, dreifachachslastklasseGrenzen1.size());
                    assertEquals(37038, dreifachachslastklasseGrenzen1.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse3Grenzen":
                    List<DataObject> fahrzeugklasse3Grenzen = item.getAsArray();
                    assertEquals(5, fahrzeugklasse3Grenzen.size());

                    List<DataItem> fahrzeugklasse3Grenzen0 = fahrzeugklasse3Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen0.size());
                    assertEquals(49950, fahrzeugklasse3Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen1 = fahrzeugklasse3Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen1.size());
                    assertEquals(45122, fahrzeugklasse3Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen2 = fahrzeugklasse3Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen2.size());
                    assertEquals(26568, fahrzeugklasse3Grenzen2.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen3 = fahrzeugklasse3Grenzen.get(3).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen3.size());
                    assertEquals(57621, fahrzeugklasse3Grenzen3.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen4 = fahrzeugklasse3Grenzen.get(4).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen4.size());
                    assertEquals(46775, fahrzeugklasse3Grenzen4.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse4Grenzen":
                    List<DataObject> fahrzeugklasse4Grenzen = item.getAsArray();
                    assertEquals(4, fahrzeugklasse4Grenzen.size());

                    List<DataItem> fahrzeugklasse4Grenzen0 = fahrzeugklasse4Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse4Grenzen0.size());
                    assertEquals(5920, fahrzeugklasse4Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse4Grenzen1 = fahrzeugklasse4Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse4Grenzen1.size());
                    assertEquals(65208, fahrzeugklasse4Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse4Grenzen2 = fahrzeugklasse4Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse4Grenzen2.size());
                    assertEquals(29024, fahrzeugklasse4Grenzen2.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse4Grenzen3 = fahrzeugklasse4Grenzen.get(3).getItems();
                    assertEquals(1, fahrzeugklasse4Grenzen3.size());
                    assertEquals(8794, fahrzeugklasse4Grenzen3.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse5Grenzen":
                    List<DataObject> fahrzeugklasse5Grenzen = item.getAsArray();
                    assertEquals(5, fahrzeugklasse5Grenzen.size());

                    List<DataItem> fahrzeugklasse5Grenzen0 = fahrzeugklasse5Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen0.size());
                    assertEquals(61649, fahrzeugklasse5Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen1 = fahrzeugklasse5Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen1.size());
                    assertEquals(45927, fahrzeugklasse5Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen2 = fahrzeugklasse5Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen2.size());
                    assertEquals(30370, fahrzeugklasse5Grenzen2.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen3 = fahrzeugklasse5Grenzen.get(3).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen3.size());
                    assertEquals(36057, fahrzeugklasse5Grenzen3.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen4 = fahrzeugklasse5Grenzen.get(4).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen4.size());
                    assertEquals(13262, fahrzeugklasse5Grenzen4.get(0).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_3_39_AXLParameterAchslastklassen8p1() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 131); // id = 3 + 128
        builder.addDeBlockHeader(33, 39)
                // Array einzelachslastklasseGrenzen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(38301) // grenze[0]
                .addInt16(35732) // grenze[1]
                .addInt16(32573) // grenze[2]
                // Array einzelachslastklasseGrenzen End
                // Array doppelachslastklasseGrenzen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(46593) // grenze[0]
                .addInt16(56756) // grenze[1]
                // Array doppelachslastklasseGrenzen End
                // Array dreifachachslastklasseGrenzen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(47605) // grenze[0]
                .addInt16(25206) // grenze[1]
                // Array dreifachachslastklasseGrenzen End
                // Array fahrzeugklasse3Grenzen Begin
                .addByte(4) // number of ArrayElements (Number of Bytes: 8)
                .addInt16(54035) // grenze[0]
                .addInt16(35829) // grenze[1]
                .addInt16(57492) // grenze[2]
                .addInt16(57618) // grenze[3]
                // Array fahrzeugklasse3Grenzen End
                // Array fahrzeugklasse5Grenzen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(23766) // grenze[0]
                .addInt16(10349) // grenze[1]
                .addInt16(61494) // grenze[2]
                // Array fahrzeugklasse5Grenzen End
                // Array fahrzeugklasse8Grenzen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(63751) // grenze[0]
                .addInt16(55101) // grenze[1]
                // Array fahrzeugklasse8Grenzen End
                // Array fahrzeugklasse9Grenzen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(7587) // grenze[0]
                .addInt16(65524) // grenze[1]
                .addInt16(53093) // grenze[2]
                .addInt16(22446) // grenze[3]
                .addInt16(36262); // grenze[4]
        // Array fahrzeugklasse9Grenzen End

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
            assertEquals("AXLParameterAchslastklassen8p1", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "einzelachslastklasseGrenzen":
                    List<DataObject> einzelachslastklasseGrenzen = item.getAsArray();
                    assertEquals(3, einzelachslastklasseGrenzen.size());

                    List<DataItem> einzelachslastklasseGrenzen0 = einzelachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen0.size());
                    assertEquals(38301, einzelachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen1 = einzelachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen1.size());
                    assertEquals(35732, einzelachslastklasseGrenzen1.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklasseGrenzen2 = einzelachslastklasseGrenzen.get(2).getItems();
                    assertEquals(1, einzelachslastklasseGrenzen2.size());
                    assertEquals(32573, einzelachslastklasseGrenzen2.get(0).getAsLong().longValue());
                    break;
                case "doppelachslastklasseGrenzen":
                    List<DataObject> doppelachslastklasseGrenzen = item.getAsArray();
                    assertEquals(2, doppelachslastklasseGrenzen.size());

                    List<DataItem> doppelachslastklasseGrenzen0 = doppelachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, doppelachslastklasseGrenzen0.size());
                    assertEquals(46593, doppelachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> doppelachslastklasseGrenzen1 = doppelachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, doppelachslastklasseGrenzen1.size());
                    assertEquals(56756, doppelachslastklasseGrenzen1.get(0).getAsLong().longValue());
                    break;
                case "dreifachachslastklasseGrenzen":
                    List<DataObject> dreifachachslastklasseGrenzen = item.getAsArray();
                    assertEquals(2, dreifachachslastklasseGrenzen.size());

                    List<DataItem> dreifachachslastklasseGrenzen0 = dreifachachslastklasseGrenzen.get(0).getItems();
                    assertEquals(1, dreifachachslastklasseGrenzen0.size());
                    assertEquals(47605, dreifachachslastklasseGrenzen0.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklasseGrenzen1 = dreifachachslastklasseGrenzen.get(1).getItems();
                    assertEquals(1, dreifachachslastklasseGrenzen1.size());
                    assertEquals(25206, dreifachachslastklasseGrenzen1.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse3Grenzen":
                    List<DataObject> fahrzeugklasse3Grenzen = item.getAsArray();
                    assertEquals(4, fahrzeugklasse3Grenzen.size());

                    List<DataItem> fahrzeugklasse3Grenzen0 = fahrzeugklasse3Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen0.size());
                    assertEquals(54035, fahrzeugklasse3Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen1 = fahrzeugklasse3Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen1.size());
                    assertEquals(35829, fahrzeugklasse3Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen2 = fahrzeugklasse3Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen2.size());
                    assertEquals(57492, fahrzeugklasse3Grenzen2.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse3Grenzen3 = fahrzeugklasse3Grenzen.get(3).getItems();
                    assertEquals(1, fahrzeugklasse3Grenzen3.size());
                    assertEquals(57618, fahrzeugklasse3Grenzen3.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse5Grenzen":
                    List<DataObject> fahrzeugklasse5Grenzen = item.getAsArray();
                    assertEquals(3, fahrzeugklasse5Grenzen.size());

                    List<DataItem> fahrzeugklasse5Grenzen0 = fahrzeugklasse5Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen0.size());
                    assertEquals(23766, fahrzeugklasse5Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen1 = fahrzeugklasse5Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen1.size());
                    assertEquals(10349, fahrzeugklasse5Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse5Grenzen2 = fahrzeugklasse5Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse5Grenzen2.size());
                    assertEquals(61494, fahrzeugklasse5Grenzen2.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse8Grenzen":
                    List<DataObject> fahrzeugklasse8Grenzen = item.getAsArray();
                    assertEquals(2, fahrzeugklasse8Grenzen.size());

                    List<DataItem> fahrzeugklasse8Grenzen0 = fahrzeugklasse8Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse8Grenzen0.size());
                    assertEquals(63751, fahrzeugklasse8Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse8Grenzen1 = fahrzeugklasse8Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse8Grenzen1.size());
                    assertEquals(55101, fahrzeugklasse8Grenzen1.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklasse9Grenzen":
                    List<DataObject> fahrzeugklasse9Grenzen = item.getAsArray();
                    assertEquals(5, fahrzeugklasse9Grenzen.size());

                    List<DataItem> fahrzeugklasse9Grenzen0 = fahrzeugklasse9Grenzen.get(0).getItems();
                    assertEquals(1, fahrzeugklasse9Grenzen0.size());
                    assertEquals(7587, fahrzeugklasse9Grenzen0.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse9Grenzen1 = fahrzeugklasse9Grenzen.get(1).getItems();
                    assertEquals(1, fahrzeugklasse9Grenzen1.size());
                    assertEquals(65524, fahrzeugklasse9Grenzen1.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse9Grenzen2 = fahrzeugklasse9Grenzen.get(2).getItems();
                    assertEquals(1, fahrzeugklasse9Grenzen2.size());
                    assertEquals(53093, fahrzeugklasse9Grenzen2.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse9Grenzen3 = fahrzeugklasse9Grenzen.get(3).getItems();
                    assertEquals(1, fahrzeugklasse9Grenzen3.size());
                    assertEquals(22446, fahrzeugklasse9Grenzen3.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklasse9Grenzen4 = fahrzeugklasse9Grenzen.get(4).getItems();
                    assertEquals(1, fahrzeugklasse9Grenzen4.size());
                    assertEquals(36262, fahrzeugklasse9Grenzen4.get(0).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_60_AXLEinzelergebnisVersion1() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 60)
                .addByte(250) // fahrzeugtyp
                .addInt16(1042) // fahrzeuglaenge
                .addByte(91) // geschwindigkeit
                .addInt16(20214) // fahrzeugabstand
                .addInt16(53038) // gesamtgewicht
                .addByte(90) // ueberladung
                // Array achslastdatengruppen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 25)
                .addByte(235) // achsart[0]
                .addInt16(15974) // achslast[0]
                .addInt16(3927) // achsabstand[0]
                .addByte(152) // achsart[1]
                .addInt16(58888) // achslast[1]
                .addInt16(20135) // achsabstand[1]
                .addByte(196) // achsart[2]
                .addInt16(63580) // achslast[2]
                .addInt16(8398) // achsabstand[2]
                .addByte(105) // achsart[3]
                .addInt16(30354) // achslast[3]
                .addInt16(57034) // achsabstand[3]
                .addByte(106) // achsart[4]
                .addInt16(62626) // achslast[4]
                .addInt16(36204); // achsabstand[4]
        // Array achslastdatengruppen End

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
            assertEquals("AXLEinzelergebnisVersion1", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fahrzeugtyp":
                    assertEquals(250, item.getAsLong().longValue());
                    break;
                case "fahrzeuglaenge":
                    assertEquals(1042, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "fahrzeugabstand":
                    assertEquals(20214, item.getAsLong().longValue());
                    break;
                case "gesamtgewicht":
                    assertEquals(53038, item.getAsLong().longValue());
                    break;
                case "ueberladung":
                    assertEquals(90, item.getAsLong().longValue());
                    break;
                case "achslastdatengruppen":
                    List<DataObject> achslastdatengruppen = item.getAsArray();
                    assertEquals(5, achslastdatengruppen.size());

                    List<DataItem> achslastdatengruppen0 = achslastdatengruppen.get(0).getItems();
                    assertEquals(3, achslastdatengruppen0.size());
                    assertEquals(235, achslastdatengruppen0.get(0).getAsLong().longValue());
                    assertEquals(15974, achslastdatengruppen0.get(1).getAsLong().longValue());
                    assertEquals(3927, achslastdatengruppen0.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen1 = achslastdatengruppen.get(1).getItems();
                    assertEquals(3, achslastdatengruppen1.size());
                    assertEquals(152, achslastdatengruppen1.get(0).getAsLong().longValue());
                    assertEquals(58888, achslastdatengruppen1.get(1).getAsLong().longValue());
                    assertEquals(20135, achslastdatengruppen1.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen2 = achslastdatengruppen.get(2).getItems();
                    assertEquals(3, achslastdatengruppen2.size());
                    assertEquals(196, achslastdatengruppen2.get(0).getAsLong().longValue());
                    assertEquals(63580, achslastdatengruppen2.get(1).getAsLong().longValue());
                    assertEquals(8398, achslastdatengruppen2.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen3 = achslastdatengruppen.get(3).getItems();
                    assertEquals(3, achslastdatengruppen3.size());
                    assertEquals(105, achslastdatengruppen3.get(0).getAsLong().longValue());
                    assertEquals(30354, achslastdatengruppen3.get(1).getAsLong().longValue());
                    assertEquals(57034, achslastdatengruppen3.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen4 = achslastdatengruppen.get(4).getItems();
                    assertEquals(3, achslastdatengruppen4.size());
                    assertEquals(106, achslastdatengruppen4.get(0).getAsLong().longValue());
                    assertEquals(62626, achslastdatengruppen4.get(1).getAsLong().longValue());
                    assertEquals(36204, achslastdatengruppen4.get(2).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_61_AXLEinzelergebnisVersion2() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 61)
                .addByte(75) // fahrzeugtyp
                .addInt16(40725) // fahrzeuglaenge
                .addByte(164) // geschwindigkeit
                .addInt16(41699) // fahrzeugabstand
                .addInt16(57808) // gesamtgewicht
                .addByte(124) // ueberladung
                // Array achslastdatengruppen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 15)
                .addByte(6) // achsart[0]
                .addByte(231) // radlastLinks[0]
                .addByte(177) // radlastRechts[0]
                .addInt16(43933) // achsabstand[0]
                .addByte(120) // achsart[1]
                .addByte(197) // radlastLinks[1]
                .addByte(11) // radlastRechts[1]
                .addInt16(22277) // achsabstand[1]
                .addByte(247) // achsart[2]
                .addByte(101) // radlastLinks[2]
                .addByte(191) // radlastRechts[2]
                .addInt16(53748); // achsabstand[2]
        // Array achslastdatengruppen End

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
            assertEquals("AXLEinzelergebnisVersion2", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fahrzeugtyp":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "fahrzeuglaenge":
                    assertEquals(40725, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(164, item.getAsLong().longValue());
                    break;
                case "fahrzeugabstand":
                    assertEquals(41699, item.getAsLong().longValue());
                    break;
                case "gesamtgewicht":
                    assertEquals(57808, item.getAsLong().longValue());
                    break;
                case "ueberladung":
                    assertEquals(124, item.getAsLong().longValue());
                    break;
                case "achslastdatengruppen":
                    List<DataObject> achslastdatengruppen = item.getAsArray();
                    assertEquals(3, achslastdatengruppen.size());

                    List<DataItem> achslastdatengruppen0 = achslastdatengruppen.get(0).getItems();
                    assertEquals(4, achslastdatengruppen0.size());
                    assertEquals(6, achslastdatengruppen0.get(0).getAsLong().longValue());
                    assertEquals(23.1, achslastdatengruppen0.get(1).getAsDouble().doubleValue());
                    assertEquals(17.7, achslastdatengruppen0.get(2).getAsDouble().doubleValue());
                    assertEquals(43933, achslastdatengruppen0.get(3).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen1 = achslastdatengruppen.get(1).getItems();
                    assertEquals(4, achslastdatengruppen1.size());
                    assertEquals(120, achslastdatengruppen1.get(0).getAsLong().longValue());
                    assertEquals(19.7, achslastdatengruppen1.get(1).getAsDouble().doubleValue());
                    assertEquals(1.1, achslastdatengruppen1.get(2).getAsDouble().doubleValue());
                    assertEquals(22277, achslastdatengruppen1.get(3).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen2 = achslastdatengruppen.get(2).getItems();
                    assertEquals(4, achslastdatengruppen2.size());
                    assertEquals(247, achslastdatengruppen2.get(0).getAsLong().longValue());
                    assertEquals(10.1, achslastdatengruppen2.get(1).getAsDouble().doubleValue());
                    assertEquals(19.1, achslastdatengruppen2.get(2).getAsDouble().doubleValue());
                    assertEquals(53748, achslastdatengruppen2.get(3).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_62_AXLEinzelergebnisVersion3() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 62)
                .addByte(65) // fahrzeugtyp
                .addInt16(18435) // fahrzeuglaenge
                .addByte(212) // geschwindigkeit
                .addInt16(2439) // fahrzeugabstand
                .addInt24(461303) // gesamtgewicht
                .addByte(46) // ueberladung
                // Array achslastdatengruppen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 10)
                .addByte(24) // achsart[0]
                .addInt16(20812) // achslast[0]
                .addInt16(45868) // achsabstand[0]
                .addByte(156) // achsart[1]
                .addInt16(51466) // achslast[1]
                .addInt16(38171); // achsabstand[1]
        // Array achslastdatengruppen End

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
            assertEquals("AXLEinzelergebnisVersion3", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fahrzeugtyp":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                case "fahrzeuglaenge":
                    assertEquals(18435, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(212, item.getAsLong().longValue());
                    break;
                case "fahrzeugabstand":
                    assertEquals(2439, item.getAsLong().longValue());
                    break;
                case "gesamtgewicht":
                    assertEquals(461303, item.getAsLong().longValue());
                    break;
                case "ueberladung":
                    assertEquals(46, item.getAsLong().longValue());
                    break;
                case "achslastdatengruppen":
                    List<DataObject> achslastdatengruppen = item.getAsArray();
                    assertEquals(2, achslastdatengruppen.size());

                    List<DataItem> achslastdatengruppen0 = achslastdatengruppen.get(0).getItems();
                    assertEquals(3, achslastdatengruppen0.size());
                    assertEquals(24, achslastdatengruppen0.get(0).getAsLong().longValue());
                    assertEquals(20812, achslastdatengruppen0.get(1).getAsLong().longValue());
                    assertEquals(45868, achslastdatengruppen0.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen1 = achslastdatengruppen.get(1).getItems();
                    assertEquals(3, achslastdatengruppen1.size());
                    assertEquals(156, achslastdatengruppen1.get(0).getAsLong().longValue());
                    assertEquals(51466, achslastdatengruppen1.get(1).getAsLong().longValue());
                    assertEquals(38171, achslastdatengruppen1.get(2).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_64_AXLIntervallzeitstempel() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(255, 64)
                .addByte(16) // jahr (ein Byte von 0-99)
                .addByte(11) // monat
                .addByte(13) // tag
                .addByte(19 + dstOffset) // stunde (bit 8 = Sommerzeit flag)
                .addByte(32) // minute
                .addByte(89); // intervalllaenge

        builder.addDeBlockHeader(33, 60)
                .addByte(43) // fahrzeugtyp
                .addInt16(39284) // fahrzeuglaenge
                .addByte(240) // geschwindigkeit
                .addInt16(43146) // fahrzeugabstand
                .addInt16(30192) // gesamtgewicht
                .addByte(7) // ueberladung
                // Array achslastdatengruppen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 10)
                .addByte(54) // achsart[0]
                .addInt16(48618) // achslast[0]
                .addInt16(14267) // achsabstand[0]
                .addByte(111) // achsart[1]
                .addInt16(16583) // achslast[1]
                .addInt16(28661); // achsabstand[1]
        // Array achslastdatengruppen End

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
            assertEquals("AXLEinzelergebnisVersion1", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "zeitstempel":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(2016, timeIn.get(Calendar.YEAR));
                    assertEquals(10, timeIn.get(Calendar.MONTH));
                    assertEquals(13, timeIn.get(Calendar.DAY_OF_MONTH));
                    assertEquals(19, timeIn.get(Calendar.HOUR_OF_DAY));
                    assertEquals(32, timeIn.get(Calendar.MINUTE));
                    assertEquals(0, timeIn.get(Calendar.SECOND));
                    break;
                case "intervalllaenge":
                    assertEquals(89, item.getAsLong().longValue());
                    break;
                case "fahrzeugtyp":
                    assertEquals(43, item.getAsLong().longValue());
                    break;
                case "fahrzeuglaenge":
                    assertEquals(39284, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(240, item.getAsLong().longValue());
                    break;
                case "fahrzeugabstand":
                    assertEquals(43146, item.getAsLong().longValue());
                    break;
                case "gesamtgewicht":
                    assertEquals(30192, item.getAsLong().longValue());
                    break;
                case "ueberladung":
                    assertEquals(7, item.getAsLong().longValue());
                    break;
                case "achslastdatengruppen":
                    List<DataObject> achslastdatengruppen = item.getAsArray();
                    assertEquals(2, achslastdatengruppen.size());

                    List<DataItem> achslastdatengruppen0 = achslastdatengruppen.get(0).getItems();
                    assertEquals(3, achslastdatengruppen0.size());
                    assertEquals(54, achslastdatengruppen0.get(0).getAsLong().longValue());
                    assertEquals(48618, achslastdatengruppen0.get(1).getAsLong().longValue());
                    assertEquals(14267, achslastdatengruppen0.get(2).getAsLong().longValue());

                    List<DataItem> achslastdatengruppen1 = achslastdatengruppen.get(1).getItems();
                    assertEquals(3, achslastdatengruppen1.size());
                    assertEquals(111, achslastdatengruppen1.get(0).getAsLong().longValue());
                    assertEquals(16583, achslastdatengruppen1.get(1).getAsLong().longValue());
                    assertEquals(28661, achslastdatengruppen1.get(2).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_65_AXLErgebnisVersion10() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 65)
                // Array einzelachslastklassen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(9491) // klasse[0]
                .addInt16(26281) // klasse[1]
                .addInt16(36905) // klasse[2]
                // Array einzelachslastklassen End
                // Array doppelachslastklassen Begin
                .addByte(2) // number of ArrayElements (Number of Bytes: 4)
                .addInt16(57039) // klasse[0]
                .addInt16(42176) // klasse[1]
                // Array doppelachslastklassen End
                // Array dreifachachslastklassen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(52128) // klasse[0]
                .addInt16(54728) // klasse[1]
                .addInt16(36605) // klasse[2]
                // Array dreifachachslastklassen End
                .addByte(203) // ueberladungenKlasse3
                .addByte(132) // ueberladungenKlasse4
                .addByte(4) // ueberladungenKlasse5
                // Array fahrzeugklassen3 Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(53452) // klasse[0]
                .addInt16(17224) // klasse[1]
                .addInt16(31755) // klasse[2]
                .addInt16(33447) // klasse[3]
                .addInt16(60700) // klasse[4]
                // Array fahrzeugklassen3 End
                // Array fahrzeugklassen4 Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(31310) // klasse[0]
                .addInt16(34598) // klasse[1]
                .addInt16(56953) // klasse[2]
                .addInt16(41623) // klasse[3]
                .addInt16(10586) // klasse[4]
                // Array fahrzeugklassen4 End
                // Array fahrzeugklassen5 Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(27360) // klasse[0]
                .addInt16(60066) // klasse[1]
                .addInt16(10933) // klasse[2]
                .addInt16(27144) // klasse[3]
                .addInt16(711); // klasse[4]
        // Array fahrzeugklassen5 End

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
            assertEquals("AXLErgebnisVersion10", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "einzelachslastklassen":
                    List<DataObject> einzelachslastklassen = item.getAsArray();
                    assertEquals(3, einzelachslastklassen.size());

                    List<DataItem> einzelachslastklassen0 = einzelachslastklassen.get(0).getItems();
                    assertEquals(1, einzelachslastklassen0.size());
                    assertEquals(9491, einzelachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen1 = einzelachslastklassen.get(1).getItems();
                    assertEquals(1, einzelachslastklassen1.size());
                    assertEquals(26281, einzelachslastklassen1.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen2 = einzelachslastklassen.get(2).getItems();
                    assertEquals(1, einzelachslastklassen2.size());
                    assertEquals(36905, einzelachslastklassen2.get(0).getAsLong().longValue());
                    break;
                case "doppelachslastklassen":
                    List<DataObject> doppelachslastklassen = item.getAsArray();
                    assertEquals(2, doppelachslastklassen.size());

                    List<DataItem> doppelachslastklassen0 = doppelachslastklassen.get(0).getItems();
                    assertEquals(1, doppelachslastklassen0.size());
                    assertEquals(57039, doppelachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> doppelachslastklassen1 = doppelachslastklassen.get(1).getItems();
                    assertEquals(1, doppelachslastklassen1.size());
                    assertEquals(42176, doppelachslastklassen1.get(0).getAsLong().longValue());
                    break;
                case "dreifachachslastklassen":
                    List<DataObject> dreifachachslastklassen = item.getAsArray();
                    assertEquals(3, dreifachachslastklassen.size());

                    List<DataItem> dreifachachslastklassen0 = dreifachachslastklassen.get(0).getItems();
                    assertEquals(1, dreifachachslastklassen0.size());
                    assertEquals(52128, dreifachachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklassen1 = dreifachachslastklassen.get(1).getItems();
                    assertEquals(1, dreifachachslastklassen1.size());
                    assertEquals(54728, dreifachachslastklassen1.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklassen2 = dreifachachslastklassen.get(2).getItems();
                    assertEquals(1, dreifachachslastklassen2.size());
                    assertEquals(36605, dreifachachslastklassen2.get(0).getAsLong().longValue());
                    break;
                case "ueberladungenKlasse3":
                    assertEquals(203, item.getAsLong().longValue());
                    break;
                case "ueberladungenKlasse4":
                    assertEquals(132, item.getAsLong().longValue());
                    break;
                case "ueberladungenKlasse5":
                    assertEquals(4, item.getAsLong().longValue());
                    break;
                case "fahrzeugklassen3":
                    List<DataObject> fahrzeugklassen3 = item.getAsArray();
                    assertEquals(5, fahrzeugklassen3.size());

                    List<DataItem> fahrzeugklassen30 = fahrzeugklassen3.get(0).getItems();
                    assertEquals(1, fahrzeugklassen30.size());
                    assertEquals(53452, fahrzeugklassen30.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen31 = fahrzeugklassen3.get(1).getItems();
                    assertEquals(1, fahrzeugklassen31.size());
                    assertEquals(17224, fahrzeugklassen31.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen32 = fahrzeugklassen3.get(2).getItems();
                    assertEquals(1, fahrzeugklassen32.size());
                    assertEquals(31755, fahrzeugklassen32.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen33 = fahrzeugklassen3.get(3).getItems();
                    assertEquals(1, fahrzeugklassen33.size());
                    assertEquals(33447, fahrzeugklassen33.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen34 = fahrzeugklassen3.get(4).getItems();
                    assertEquals(1, fahrzeugklassen34.size());
                    assertEquals(60700, fahrzeugklassen34.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklassen4":
                    List<DataObject> fahrzeugklassen4 = item.getAsArray();
                    assertEquals(5, fahrzeugklassen4.size());

                    List<DataItem> fahrzeugklassen40 = fahrzeugklassen4.get(0).getItems();
                    assertEquals(1, fahrzeugklassen40.size());
                    assertEquals(31310, fahrzeugklassen40.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen41 = fahrzeugklassen4.get(1).getItems();
                    assertEquals(1, fahrzeugklassen41.size());
                    assertEquals(34598, fahrzeugklassen41.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen42 = fahrzeugklassen4.get(2).getItems();
                    assertEquals(1, fahrzeugklassen42.size());
                    assertEquals(56953, fahrzeugklassen42.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen43 = fahrzeugklassen4.get(3).getItems();
                    assertEquals(1, fahrzeugklassen43.size());
                    assertEquals(41623, fahrzeugklassen43.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen44 = fahrzeugklassen4.get(4).getItems();
                    assertEquals(1, fahrzeugklassen44.size());
                    assertEquals(10586, fahrzeugklassen44.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklassen5":
                    List<DataObject> fahrzeugklassen5 = item.getAsArray();
                    assertEquals(5, fahrzeugklassen5.size());

                    List<DataItem> fahrzeugklassen50 = fahrzeugklassen5.get(0).getItems();
                    assertEquals(1, fahrzeugklassen50.size());
                    assertEquals(27360, fahrzeugklassen50.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen51 = fahrzeugklassen5.get(1).getItems();
                    assertEquals(1, fahrzeugklassen51.size());
                    assertEquals(60066, fahrzeugklassen51.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen52 = fahrzeugklassen5.get(2).getItems();
                    assertEquals(1, fahrzeugklassen52.size());
                    assertEquals(10933, fahrzeugklassen52.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen53 = fahrzeugklassen5.get(3).getItems();
                    assertEquals(1, fahrzeugklassen53.size());
                    assertEquals(27144, fahrzeugklassen53.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen54 = fahrzeugklassen5.get(4).getItems();
                    assertEquals(1, fahrzeugklassen54.size());
                    assertEquals(711, fahrzeugklassen54.get(0).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_2_4_66_AXLErgebnisVersion11() {

        TlsTeleBuilder builder = new TlsTeleBuilder(2, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 66)
                // Array einzelachslastklassen Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(7029) // klasse[0]
                .addInt16(8062) // klasse[1]
                .addInt16(1009) // klasse[2]
                .addInt16(8153) // klasse[3]
                .addInt16(50357) // klasse[4]
                // Array einzelachslastklassen End
                // Array doppelachslastklassen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(18204) // klasse[0]
                .addInt16(7737) // klasse[1]
                .addInt16(25917) // klasse[2]
                // Array doppelachslastklassen End
                // Array dreifachachslastklassen Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(16647) // klasse[0]
                .addInt16(6517) // klasse[1]
                .addInt16(36726) // klasse[2]
                // Array dreifachachslastklassen End
                .addByte(254) // ueberladungenKlasse3
                .addByte(165) // ueberladungenKlasse5
                .addByte(233) // ueberladungenKlasse8
                .addByte(0) // ueberladungenKlasse9
                // Array fahrzeugklassen3 Begin
                .addByte(3) // number of ArrayElements (Number of Bytes: 6)
                .addInt16(51717) // klasse[0]
                .addInt16(40810) // klasse[1]
                .addInt16(57451) // klasse[2]
                // Array fahrzeugklassen3 End
                // Array fahrzeugklassen5 Begin
                .addByte(4) // number of ArrayElements (Number of Bytes: 8)
                .addInt16(28120) // klasse[0]
                .addInt16(57425) // klasse[1]
                .addInt16(9118) // klasse[2]
                .addInt16(11034) // klasse[3]
                // Array fahrzeugklassen5 End
                // Array fahrzeugklassen8 Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(28660) // klasse[0]
                .addInt16(63302) // klasse[1]
                .addInt16(61396) // klasse[2]
                .addInt16(56039) // klasse[3]
                .addInt16(11554) // klasse[4]
                // Array fahrzeugklassen8 End
                // Array fahrzeugklassen9 Begin
                .addByte(5) // number of ArrayElements (Number of Bytes: 10)
                .addInt16(15799) // klasse[0]
                .addInt16(40193) // klasse[1]
                .addInt16(44667) // klasse[2]
                .addInt16(59341) // klasse[3]
                .addInt16(49451); // klasse[4]
        // Array fahrzeugklassen9 End

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
            assertEquals("AXLErgebnisVersion11", o.getName());

            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "einzelachslastklassen":
                    List<DataObject> einzelachslastklassen = item.getAsArray();
                    assertEquals(5, einzelachslastklassen.size());

                    List<DataItem> einzelachslastklassen0 = einzelachslastklassen.get(0).getItems();
                    assertEquals(1, einzelachslastklassen0.size());
                    assertEquals(7029, einzelachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen1 = einzelachslastklassen.get(1).getItems();
                    assertEquals(1, einzelachslastklassen1.size());
                    assertEquals(8062, einzelachslastklassen1.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen2 = einzelachslastklassen.get(2).getItems();
                    assertEquals(1, einzelachslastklassen2.size());
                    assertEquals(1009, einzelachslastklassen2.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen3 = einzelachslastklassen.get(3).getItems();
                    assertEquals(1, einzelachslastklassen3.size());
                    assertEquals(8153, einzelachslastklassen3.get(0).getAsLong().longValue());

                    List<DataItem> einzelachslastklassen4 = einzelachslastklassen.get(4).getItems();
                    assertEquals(1, einzelachslastklassen4.size());
                    assertEquals(50357, einzelachslastklassen4.get(0).getAsLong().longValue());
                    break;
                case "doppelachslastklassen":
                    List<DataObject> doppelachslastklassen = item.getAsArray();
                    assertEquals(3, doppelachslastklassen.size());

                    List<DataItem> doppelachslastklassen0 = doppelachslastklassen.get(0).getItems();
                    assertEquals(1, doppelachslastklassen0.size());
                    assertEquals(18204, doppelachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> doppelachslastklassen1 = doppelachslastklassen.get(1).getItems();
                    assertEquals(1, doppelachslastklassen1.size());
                    assertEquals(7737, doppelachslastklassen1.get(0).getAsLong().longValue());

                    List<DataItem> doppelachslastklassen2 = doppelachslastklassen.get(2).getItems();
                    assertEquals(1, doppelachslastklassen2.size());
                    assertEquals(25917, doppelachslastklassen2.get(0).getAsLong().longValue());
                    break;
                case "dreifachachslastklassen":
                    List<DataObject> dreifachachslastklassen = item.getAsArray();
                    assertEquals(3, dreifachachslastklassen.size());

                    List<DataItem> dreifachachslastklassen0 = dreifachachslastklassen.get(0).getItems();
                    assertEquals(1, dreifachachslastklassen0.size());
                    assertEquals(16647, dreifachachslastklassen0.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklassen1 = dreifachachslastklassen.get(1).getItems();
                    assertEquals(1, dreifachachslastklassen1.size());
                    assertEquals(6517, dreifachachslastklassen1.get(0).getAsLong().longValue());

                    List<DataItem> dreifachachslastklassen2 = dreifachachslastklassen.get(2).getItems();
                    assertEquals(1, dreifachachslastklassen2.size());
                    assertEquals(36726, dreifachachslastklassen2.get(0).getAsLong().longValue());
                    break;
                case "ueberladungenKlasse3":
                    assertEquals(254, item.getAsLong().longValue());
                    break;
                case "ueberladungenKlasse5":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "ueberladungenKlasse8":
                    assertEquals(233, item.getAsLong().longValue());
                    break;
                case "ueberladungenKlasse9":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "fahrzeugklassen3":
                    List<DataObject> fahrzeugklassen3 = item.getAsArray();
                    assertEquals(3, fahrzeugklassen3.size());

                    List<DataItem> fahrzeugklassen30 = fahrzeugklassen3.get(0).getItems();
                    assertEquals(1, fahrzeugklassen30.size());
                    assertEquals(51717, fahrzeugklassen30.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen31 = fahrzeugklassen3.get(1).getItems();
                    assertEquals(1, fahrzeugklassen31.size());
                    assertEquals(40810, fahrzeugklassen31.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen32 = fahrzeugklassen3.get(2).getItems();
                    assertEquals(1, fahrzeugklassen32.size());
                    assertEquals(57451, fahrzeugklassen32.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklassen5":
                    List<DataObject> fahrzeugklassen5 = item.getAsArray();
                    assertEquals(4, fahrzeugklassen5.size());

                    List<DataItem> fahrzeugklassen50 = fahrzeugklassen5.get(0).getItems();
                    assertEquals(1, fahrzeugklassen50.size());
                    assertEquals(28120, fahrzeugklassen50.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen51 = fahrzeugklassen5.get(1).getItems();
                    assertEquals(1, fahrzeugklassen51.size());
                    assertEquals(57425, fahrzeugklassen51.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen52 = fahrzeugklassen5.get(2).getItems();
                    assertEquals(1, fahrzeugklassen52.size());
                    assertEquals(9118, fahrzeugklassen52.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen53 = fahrzeugklassen5.get(3).getItems();
                    assertEquals(1, fahrzeugklassen53.size());
                    assertEquals(11034, fahrzeugklassen53.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklassen8":
                    List<DataObject> fahrzeugklassen8 = item.getAsArray();
                    assertEquals(5, fahrzeugklassen8.size());

                    List<DataItem> fahrzeugklassen80 = fahrzeugklassen8.get(0).getItems();
                    assertEquals(1, fahrzeugklassen80.size());
                    assertEquals(28660, fahrzeugklassen80.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen81 = fahrzeugklassen8.get(1).getItems();
                    assertEquals(1, fahrzeugklassen81.size());
                    assertEquals(63302, fahrzeugklassen81.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen82 = fahrzeugklassen8.get(2).getItems();
                    assertEquals(1, fahrzeugklassen82.size());
                    assertEquals(61396, fahrzeugklassen82.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen83 = fahrzeugklassen8.get(3).getItems();
                    assertEquals(1, fahrzeugklassen83.size());
                    assertEquals(56039, fahrzeugklassen83.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen84 = fahrzeugklassen8.get(4).getItems();
                    assertEquals(1, fahrzeugklassen84.size());
                    assertEquals(11554, fahrzeugklassen84.get(0).getAsLong().longValue());
                    break;
                case "fahrzeugklassen9":
                    List<DataObject> fahrzeugklassen9 = item.getAsArray();
                    assertEquals(5, fahrzeugklassen9.size());

                    List<DataItem> fahrzeugklassen90 = fahrzeugklassen9.get(0).getItems();
                    assertEquals(1, fahrzeugklassen90.size());
                    assertEquals(15799, fahrzeugklassen90.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen91 = fahrzeugklassen9.get(1).getItems();
                    assertEquals(1, fahrzeugklassen91.size());
                    assertEquals(40193, fahrzeugklassen91.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen92 = fahrzeugklassen9.get(2).getItems();
                    assertEquals(1, fahrzeugklassen92.size());
                    assertEquals(44667, fahrzeugklassen92.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen93 = fahrzeugklassen9.get(3).getItems();
                    assertEquals(1, fahrzeugklassen93.size());
                    assertEquals(59341, fahrzeugklassen93.get(0).getAsLong().longValue());

                    List<DataItem> fahrzeugklassen94 = fahrzeugklassen9.get(4).getItems();
                    assertEquals(1, fahrzeugklassen94.size());
                    assertEquals(49451, fahrzeugklassen94.get(0).getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

}
