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
import de.heuboe.test.result.comparator.ResultComparator.CompareResult;
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
public class ReceiverFG1Tests {

    private static int              dstOffset = 0;
    private static ResultComparator trc       = null;

    private static AddressConverter    addressConverter;
    private static Transformer         transformer;
    private static TransformationRules transformationRules;

    @BeforeAll
    public static void setUp() throws Exception {

        if (new GregorianCalendar().get(Calendar.DST_OFFSET) != 0) {
            dstOffset = 128;
        }

        ResultComparator.createFolders(true);
        trc = ResultComparator.createComparator(
                System.getProperty("user.dir") + "/src/test/resources/vmis2CorrectResult/",
                System.getProperty("user.dir") + "/src/test/resources/vmis2GeneratedResult/", true);

        addressConverter = new TestAddressConverter();
        TransformationReader transformationReader = new TransformationReaderImpl();
        transformer = new TransformerImpl();
        String receiveScript = "src/test/resources/rcv-fg1.txt";

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
    public void test_1_1_1_DEFehlermeldung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 129); // ID: 1 + 128
        builder.addDeBlockHeader(33, 1)
                .addByte(0)
                .addByte(6);

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
            assertEquals("LVEDeFehler", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fehlercode":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_1_14_ErgaenzendeDEFehlermeldung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 129); // ID: 1 + 128
        builder.addDeBlockHeader(33, 14)
                .addByte(6)
                .addByteArrayWithSize(1, 2, 3, 4)
                .addByteArrayWithSize(11, 12);

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
            assertEquals("LVEErgDeFehler", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                case "tlsFehlerbytes":
                    long[] tlsFehlerbytes = item.getAsBlock();
                    assertEquals(4, tlsFehlerbytes.length);
                    assertEquals(1, tlsFehlerbytes[0]);
                    assertEquals(2, tlsFehlerbytes[1]);
                    assertEquals(3, tlsFehlerbytes[2]);
                    assertEquals(4, tlsFehlerbytes[3]);
                    break;
                case "herstellerFehlerbytes":
                    long[] herstellerFehlerbytes = item.getAsBlock();
                    assertEquals(2, herstellerFehlerbytes.length);
                    assertEquals(11, herstellerFehlerbytes[0]);
                    assertEquals(12, herstellerFehlerbytes[1]);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_2_16_NegativeQuittung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 130); // ID: 2 + 128
        builder.addDeBlockHeader(33, 16)
                .addByte(8)
                .addByte(6);

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
            assertEquals("LVENegativeQuittung", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fehlerursache":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_2_28_PositiveQuittung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 130); // ID: 2 + 128
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
            assertEquals("LVEPositiveQuittung", o.getName());
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
    public void test_1_2_29_Kanalsteuerung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 130); // ID: 2 + 128
        builder.addDeBlockHeader(33, 29)
                .addByte(0);

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
            assertEquals("LVEKanalsteuerung", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "kanalsteuerbyte":
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
    public void test_1_1_30_Zeitstempel() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 129); // ID: 1 + 128

        builder.addDeBlockHeader(255, 30)
                .addByte(17 + dstOffset)
                .addByte(12)
                .addByte(6);

        builder.addDeBlockHeader(33, 1) //Fg 1 Id 1 Typ 1 LVEDeFehler as second DE-Block    
                .addByte(200)
                .addByte(6);

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
            assertEquals("LVEDeFehler", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fehlercode":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                case "zeitstempel":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(6, timeIn.get(Calendar.SECOND));
                    assertEquals(12, timeIn.get(Calendar.MINUTE));
                    assertEquals(17, timeIn.get(Calendar.HOUR_OF_DAY));
                    assertEquals(now.get(Calendar.DAY_OF_MONTH), timeIn.get(Calendar.DAY_OF_MONTH)); // today
                    assertEquals(now.get(Calendar.MONTH), timeIn.get(Calendar.MONTH));
                    assertEquals(now.get(Calendar.YEAR), timeIn.get(Calendar.YEAR));
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_3_32_LVEBetriebsparameter() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 32)
                .addByte(5)
                .addByte(4)
                .addByte(15)
                .addByte(48)
                .addByte(127)
                .addByte(31)
                .addByte(44)
                .addByte(184);

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
            assertEquals("LVEBetriebsparameter", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "datenversionKurz":
                    assertEquals(5, item.getAsLong().longValue());
                    break;
                case "erfassungsintervalldauerKurz":
                    assertEquals(4, item.getAsLong().longValue());
                    break;
                case "datenversionLang":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "erfassungsintervalldauerLang":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "alpha1":
                    assertEquals(0.5, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "alpha2":
                    assertEquals(0.125, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "laengengrenzwert":
                    assertEquals(444, item.getAsLong().longValue());
                    break;
                case "artMittelwertbildung":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "startwertMittelwertbildung":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_3_33_EFZBetriebsparameter() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 33)
                .addByte(13)
                .addByte(23)
                .addByte(65)
                .addByte(69)
                .addByte(32)
                .addByte(1);

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
            assertEquals("EFZBetriebsparameter", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "maxPufferAnzahl":
                    assertEquals(13, item.getAsLong().longValue());
                    break;
                case "maxPufferDauer":
                    assertEquals(23, item.getAsLong().longValue());
                    break;
                case "vMin":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                case "vMax":
                    assertEquals(69, item.getAsLong().longValue());
                    break;
                case "fahrzeugklassencode":
                    assertEquals(32, item.getAsLong().longValue());
                    break;
                case "meldeoptionen":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_3_34_SVEBetriebsparameter() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 34)
                .addByte(0)
                .addByte(8);

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
            assertEquals("SVEBetriebsparameter", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "datenversion":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "erfassungsintervalldauer":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_3_36_GeographischeKenndaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 36)
                .addByte(5)
                .addByte(1)
                .addBCD(2, "5484")
                .addBCD(3, "139878")
                .addByte(3)
                .addByte(0);

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
            assertEquals("LVEGeoKenndaten", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "landeskennung":
                    assertEquals(5, item.getAsLong().longValue());
                    break;
                case "strassenart":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "strassennummer":
                    assertEquals(5484, item.getAsLong().longValue());
                    break;
                case "kilometrierung":
                    assertEquals(1398.78, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "fahrtrichtung":
                    assertEquals(3, item.getAsLong().longValue());
                    break;
                case "reservebyte":
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
    public void test_1_3_37_ParameterFürGeschwindigkeitsklassenBeiKurzzeitdaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 37)
                .addByte(35)
                .addByteArrayWithSize(20, 30, 40);

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
            assertEquals("LVEGeschwindigkeitsklassenKurz", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fahrzeugklasse":
                    assertEquals(35, item.getAsLong().longValue());
                    break;
                case "vGrenzen":
                    long[] vGrenzen = item.getAsBlock();
                    assertEquals(3, vGrenzen.length);
                    assertEquals(20, vGrenzen[0]);
                    assertEquals(30, vGrenzen[1]);
                    assertEquals(40, vGrenzen[2]);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_3_38_ParameterFürGeschwindigkeitsklassenBeiLangzeitdaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 131); // ID: 3 + 128
        builder.addDeBlockHeader(33, 38)
                .addByte(33)
                .addByteArrayWithSize(20, 30, 40);

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
            assertEquals("LVEGeschwindigkeitsklassenLang", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "fahrzeugklasse":
                    assertEquals(33, item.getAsLong().longValue());
                    break;
                case "vGrenzen":
                    long[] vGrenzen = item.getAsBlock();
                    assertEquals(3, vGrenzen.length);
                    assertEquals(20, vGrenzen[0]);
                    assertEquals(30, vGrenzen[1]);
                    assertEquals(40, vGrenzen[2]);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_48_IntervalldatenFürKurzzeitUndStreckenbezogeneDaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(255, 48)
                .addByte(17 + dstOffset)
                .addByte(12)
                .addByte(6)
                .addByte(1)
                .addByte(8);

        builder.addDeBlockHeader(33, 49)//Fg 1 Id 4 Typ 49 LVEErgebnisVersion0 as second DE-Block 
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165);

        builder.addDeBlockHeader(34, 49)//Fg 1 Id 4 Typ 49 LVEErgebnisVersion0 as third DE-Block 
                .addByte(55)
                .addByte(99)
                .addByte(57)
                .addByte(166);

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

            assertEquals(2, decoded.size());

            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEErgebnisVersion0", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "intervallbeginn":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(6, timeIn.get(java.util.Calendar.SECOND));
                    assertEquals(12, timeIn.get(java.util.Calendar.MINUTE));
                    assertEquals(17, timeIn.get(java.util.Calendar.HOUR_OF_DAY));
                    assertEquals(now.get(java.util.Calendar.DAY_OF_MONTH), timeIn.get(java.util.Calendar.DAY_OF_MONTH)); // today
                    assertEquals(now.get(java.util.Calendar.MONTH), timeIn.get(java.util.Calendar.MONTH));
                    assertEquals(now.get(java.util.Calendar.YEAR), timeIn.get(java.util.Calendar.YEAR));
                    break;
                case "intervallArt":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "intervalllaenge":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }

            DataObject o1 = decoded.get(1);
            assertEquals(false, o1.isSubsequent());
            assertEquals("LVEErgebnisVersion0", o1.getName());
            for (DataItem item : o1.getItems()) {
                switch (item.getName()) {
                case "intervallbeginn":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(6, timeIn.get(java.util.Calendar.SECOND));
                    assertEquals(12, timeIn.get(java.util.Calendar.MINUTE));
                    assertEquals(17, timeIn.get(java.util.Calendar.HOUR_OF_DAY));
                    assertEquals(now.get(java.util.Calendar.DAY_OF_MONTH), timeIn.get(java.util.Calendar.DAY_OF_MONTH)); // today
                    assertEquals(now.get(java.util.Calendar.MONTH), timeIn.get(java.util.Calendar.MONTH));
                    assertEquals(now.get(java.util.Calendar.YEAR), timeIn.get(java.util.Calendar.YEAR));
                    break;
                case "intervallArt":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "intervalllaenge":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "qKfz":
                    assertEquals(55, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(99, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(57, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(166, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_49_LVEErgebnismeldungVersion0_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 49)
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165);

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
            assertEquals("LVEErgebnisVersion0", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_113_LVEErgebnismeldungVersion0_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 113)
                .addInt16(6543)
                .addInt16(9873)
                .addByte(56)
                .addByte(165);

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
            assertEquals("LVEErgebnisVersion0", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(6543, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(9873, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_50_LVEErgebnismeldungVersion1_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 50)
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165)
                .addByte(153);

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
            assertEquals("LVEErgebnisVersion1", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_114_LVEErgebnismeldungVersion1_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 114)
                .addInt16(6543)
                .addInt16(9873)
                .addByte(56)
                .addByte(165)
                .addByte(153);

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
            assertEquals("LVEErgebnisVersion1", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(6543, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(9873, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_51_LVEErgebnismeldungVersion2_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 51)
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165)
                .addByte(153);

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
            assertEquals("LVEErgebnisVersion2", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "belegung":
                    assertEquals(153, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_115_LVEErgebnismeldungVersion2_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 115)
                .addInt16(6543)
                .addInt16(9873)
                .addByte(56)
                .addByte(165)
                .addByte(153);

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
            assertEquals("LVEErgebnisVersion2", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(6543, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(9873, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "belegung":
                    assertEquals(153, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_52_LVEErgebnismeldungVersion3_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 52)
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165)
                .addByte(153)
                .addByte(48)
                .addByte(215)
                .addByte(8);

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
            assertEquals("LVEErgebnisVersion3", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "sKfz":
                    assertEquals(215, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_116_LVEErgebnismeldungVersion3_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 116)
                .addInt16(6543)
                .addInt16(9873)
                .addByte(56)
                .addByte(165)
                .addByte(153)
                .addByte(48)
                .addByte(215)
                .addByte(8);

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
            assertEquals("LVEErgebnisVersion3", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(6543, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(9873, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "sKfz":
                    assertEquals(215, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_53_LVEErgebnismeldungVersion4_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 53)
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165)
                .addByte(153)
                .addByte(48)
                .addByte(215)
                .addByte(8)
                .addByteArrayWithSize(40, 60, 80)
                .addByteArrayWithSize(20, 30, 40);

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
            assertEquals("LVEErgebnisVersion4", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "sKfz":
                    assertEquals(215, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "vKlassenPkwAe":
                    long[] vKlassenPkwAe = item.getAsBlock();
                    assertEquals(3, vKlassenPkwAe.length);
                    assertEquals(40, vKlassenPkwAe[0]);
                    assertEquals(60, vKlassenPkwAe[1]);
                    assertEquals(80, vKlassenPkwAe[2]);
                    break;
                case "vKlassenLkwAe":
                    long[] vKlassenLkwAe = item.getAsBlock();
                    assertEquals(3, vKlassenLkwAe.length);
                    assertEquals(20, vKlassenLkwAe[0]);
                    assertEquals(30, vKlassenLkwAe[1]);
                    assertEquals(40, vKlassenLkwAe[2]);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_117_LVEErgebnismeldungVersion4_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 117)
                .addInt16(6543)
                .addInt16(9873)
                .addByte(56)
                .addByte(165)
                .addByte(153)
                .addByte(48)
                .addByte(215)
                .addByte(8)
                .addByte(3)
                .addInt16Array(400, 600, 800)
                .addByte(3)
                .addInt16Array(200, 300, 400);

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
            assertEquals("LVEErgebnisVersion4", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(6543, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(9873, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "sKfz":
                    assertEquals(215, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "vKlassenPkwAe": {
                    List<DataObject> pkws = item.getAsArray();
                    assertEquals(3, pkws.size());

                    List<DataItem> vKlasse0 = pkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(400, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(600, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(800, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkwAe": {
                    List<DataObject> lkws = item.getAsArray();
                    assertEquals(3, lkws.size());

                    List<DataItem> vKlasse0 = lkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(200, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(300, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(400, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_54_LVEErgebnismeldungVersion5_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 54)
                .addByte(89)
                .addByte(55)
                .addByte(82)
                .addByte(71)
                .addByte(43)
                .addByte(91)
                .addByte(103)
                .addByte(200)
                .addByte(39)
                .addByte(154)
                .addByte(25)
                .addByte(84)
                .addByte(165)
                .addByte(92)
                .addByte(75)
                .addByte(1)
                .addByte(153)
                .addByte(48)
                .addByte(8);

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
            assertEquals("LVEErgebnisVersion5", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(89, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(55, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(82, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(71, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(43, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "vPkwG":
                    assertEquals(103, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(39, item.getAsLong().longValue());
                    break;
                case "vLkwK":
                    assertEquals(154, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(25, item.getAsLong().longValue());
                    break;
                case "sPkwG":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "sLkwK":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_118_LVEErgebnismeldungVersion5_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 118)
                .addInt16(899)
                .addInt16(5558)
                .addInt16(8241)
                .addInt16(7168)
                .addInt16(4315)
                .addInt16(9174)
                .addByte(103)
                .addByte(200)
                .addByte(39)
                .addByte(154)
                .addByte(25)
                .addByte(84)
                .addByte(165)
                .addByte(92)
                .addByte(75)
                .addByte(1)
                .addByte(153)
                .addByte(48)
                .addByte(8);

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
            assertEquals("LVEErgebnisVersion5", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(899, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(5558, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(8241, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(7168, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(4315, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(9174, item.getAsLong().longValue());
                    break;
                case "vPkwG":
                    assertEquals(103, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(39, item.getAsLong().longValue());
                    break;
                case "vLkwK":
                    assertEquals(154, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(25, item.getAsLong().longValue());
                    break;
                case "sPkwG":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "sLkwK":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_55_LVEErgebnismeldungVersion6_8Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 55)
                .addByte(89)
                .addByte(55)
                .addByte(82)
                .addByte(71)
                .addByte(43)
                .addByte(91)
                .addByte(103)
                .addByte(200)
                .addByte(39)
                .addByte(154)
                .addByte(25)
                .addByte(84)
                .addByte(165)
                .addByte(92)
                .addByte(75)
                .addByte(1)
                .addByte(98)
                .addByte(15)
                .addByte(75)
                .addByte(12)
                .addByte(38)
                .addByte(74)
                .addByte(14)
                .addByte(58)
                .addByte(65)
                .addByte(153)
                .addByte(48)
                .addByte(8);
        ;

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
            assertEquals("LVEErgebnisVersion6", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(89, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(55, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(82, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(71, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(43, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(103, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(39, item.getAsLong().longValue());
                    break;
                case "vKrad":
                    assertEquals(154, item.getAsLong().longValue());
                    break;
                case "vPkw":
                    assertEquals(25, item.getAsLong().longValue());
                    break;
                case "vLfw":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "vLkwA":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "vSattelKfz":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "sKrad":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "sPkw":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "sLfw":
                    assertEquals(12, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(38, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(74, item.getAsLong().longValue());
                    break;
                case "sLkwA":
                    assertEquals(14, item.getAsLong().longValue());
                    break;
                case "sSattelKfz":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_119_LVEErgebnismeldungVersion6_16Bit() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 119)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addByte(154)
                .addByte(25)
                .addByte(84)
                .addByte(165)
                .addByte(92)
                .addByte(75)
                .addByte(1)
                .addByte(98)
                .addByte(15)
                .addByte(75)
                .addByte(12)
                .addByte(38)
                .addByte(74)
                .addByte(14)
                .addByte(58)
                .addByte(65)
                .addByte(153)
                .addByte(48)
                .addByte(8);

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
            assertEquals("LVEErgebnisVersion6", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vKrad":
                    assertEquals(154, item.getAsLong().longValue());
                    break;
                case "vPkw":
                    assertEquals(25, item.getAsLong().longValue());
                    break;
                case "vLfw":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "vLkwA":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "vSattelKfz":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "sKrad":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "sPkw":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "sLfw":
                    assertEquals(12, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(38, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(74, item.getAsLong().longValue());
                    break;
                case "sLkwA":
                    assertEquals(14, item.getAsLong().longValue());
                    break;
                case "sSattelKfz":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(15.3, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "belegung":
                    assertEquals(48, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_62_SammelmeldungKfzEinzeldaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 62)
                .addByte(3)
                .addByte(0) // first index
                .addByte(32)
                .addByte(50)
                .addInt16(25841)
                .addInt16(20022)
                .addByte(50)
                .addByte(0) // second index
                .addByte(33)
                .addByte(100)
                .addInt16(32468)
                .addInt16(30000)
                .addByte(20)
                .addByte(0) // third index
                .addByte(34)
                .addByte(150)
                .addInt16(54988)
                .addInt16(40000)
                .addByte(35);

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
            assertEquals("LVEKfzEinzeldatenSammelmeldung", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "einzeldaten":
                    List<DataObject> einzeldaten = item.getAsArray();
                    assertEquals(3, einzeldaten.size());

                    for (DataItem item0 : einzeldaten.get(0).getItems()) {
                        switch (item0.getName()) {
                        case "status":
                            assertEquals(0, item0.getAsLong().longValue());
                            break;
                        case "fahrzeugklasse":
                            assertEquals(32, item0.getAsLong().longValue());
                            break;
                        case "geschwindigkeit":
                            assertEquals(50, item0.getAsLong().longValue());
                            break;
                        case "belegtzeit":
                            assertEquals(25841, item0.getAsLong().longValue());
                            break;
                        case "zeitoffset":
                            assertEquals(200.22, item0.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                            break;
                        case "fahrzeuglaenge":
                            assertEquals(50, item0.getAsLong().longValue());
                            break;
                        default:
                            fail("Unexpected DataItem " + item0.getName());
                            break;
                        }
                    }

                    for (DataItem item1 : einzeldaten.get(1).getItems()) {
                        switch (item1.getName()) {
                        case "status":
                            assertEquals(0, item1.getAsLong().longValue());
                            break;
                        case "fahrzeugklasse":
                            assertEquals(33, item1.getAsLong().longValue());
                            break;
                        case "geschwindigkeit":
                            assertEquals(100, item1.getAsLong().longValue());
                            break;
                        case "belegtzeit":
                            assertEquals(32468, item1.getAsLong().longValue());
                            break;
                        case "zeitoffset":
                            assertEquals(300, item1.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                            break;
                        case "fahrzeuglaenge":
                            assertEquals(20, item1.getAsLong().longValue());
                            break;
                        default:
                            fail("Unexpected DataItem " + item1.getName());
                            break;
                        }
                    }

                    for (DataItem item2 : einzeldaten.get(2).getItems()) {
                        switch (item2.getName()) {
                        case "status":
                            assertEquals(0, item2.getAsLong().longValue());
                            break;
                        case "fahrzeugklasse":
                            assertEquals(34, item2.getAsLong().longValue());
                            break;
                        case "geschwindigkeit":
                            assertEquals(150, item2.getAsLong().longValue());
                            break;
                        case "belegtzeit":
                            assertEquals(54988, item2.getAsLong().longValue());
                            break;
                        case "zeitoffset":
                            assertEquals(400, item2.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                            break;
                        case "fahrzeuglaenge":
                            assertEquals(35, item2.getAsLong().longValue());
                            break;
                        default:
                            fail("Unexpected DataItem " + item2.getName());
                            break;
                        }
                    }

                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_63_KfzEinzelDaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 63)
                .addByte(0)
                .addByte(32)
                .addInt16(53487)
                .addInt16(35448)
                .addInt16(15343)
                .addByte(65);

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
            assertEquals("LVEKfzEinzeldaten", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "status":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "fahrzeugklassencode":
                    assertEquals(32, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(53487, item.getAsLong().longValue());
                    break;
                case "belegtzeit":
                    assertEquals(35448, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(153.43, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "fahrzeuglaenge":
                    assertEquals(65, item.getAsLong().longValue());
                    break;

                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_64_IntervalldatenFuerLangzeitdaten() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(255, 64)
                .addByte(18)
                .addByte(6 + 1)
                .addByte(19)
                .addByte(9 + 128)// +128 da am 19.06.2018 Sommerzeit war
                .addByte(8);

        builder.addDeBlockHeader(33, 49)//Fg 1 Id 4 Typ 49 LVEErgebnisVersion0 as second DE-Block 
                .addByte(54)
                .addByte(98)
                .addByte(56)
                .addByte(165);

        builder.addDeBlockHeader(34, 49)//Fg 1 Id 4 Typ 49 LVEErgebnisVersion0 as third DE-Block 
                .addByte(55)
                .addByte(99)
                .addByte(57)
                .addByte(166);

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

            assertEquals(2, decoded.size());

            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEErgebnisVersion0", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "zeitstempel":
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(0, timeIn.get(java.util.Calendar.SECOND));
                    assertEquals(0, timeIn.get(java.util.Calendar.MINUTE));
                    assertEquals(9, timeIn.get(java.util.Calendar.HOUR_OF_DAY));
                    assertEquals(19, timeIn.get(java.util.Calendar.DAY_OF_MONTH));
                    assertEquals(6, timeIn.get(java.util.Calendar.MONTH));
                    assertEquals(2018, timeIn.get(java.util.Calendar.YEAR));
                    break;
                case "intervalllaenge":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "qKfz":
                    assertEquals(54, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }

            DataObject o1 = decoded.get(1);
            assertEquals(false, o1.isSubsequent());
            assertEquals("LVEErgebnisVersion0", o1.getName());
            for (DataItem item : o1.getItems()) {
                switch (item.getName()) {
                case "zeitstempel":
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(0, timeIn.get(java.util.Calendar.SECOND));
                    assertEquals(0, timeIn.get(java.util.Calendar.MINUTE));
                    assertEquals(9, timeIn.get(java.util.Calendar.HOUR_OF_DAY));
                    assertEquals(19, timeIn.get(java.util.Calendar.DAY_OF_MONTH));
                    assertEquals(6, timeIn.get(java.util.Calendar.MONTH));
                    assertEquals(2018, timeIn.get(java.util.Calendar.YEAR));
                    break;
                case "intervalllaenge":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "qKfz":
                    assertEquals(55, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(99, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(57, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(166, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }

    }

    @Test
    public void test_1_4_65_LVEErgebinsmeldungVersion10() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 65)
                .addInt16(53125)
                .addInt16(12586);

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
            assertEquals("LVEErgebnisVersion10", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_66_LVEErgebinsmeldungVersion11() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 66)
                .addInt16(53125)
                .addInt16(12586)
                .addByte(132);

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
            assertEquals("LVEErgebnisVersion11", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(132, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_67_LVEErgebinsmeldungVersion12() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 67)
                .addInt16(53125)
                .addInt16(12586)
                .addByte(15)
                .addByte(85)
                .addByte(91)
                .addByte(72);

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
            assertEquals("LVEErgebnisVersion12", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(85, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(72, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_68_LVEErgebinsmeldungVersion13() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 68)
                .addInt16(53125)
                .addInt16(12586)
                .addByte(15)
                .addByte(85)
                .addByte(91)
                .addByte(72)
                .addByte(77);

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
            assertEquals("LVEErgebnisVersion13", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qKfz":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qLkwAe":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(85, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(72, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(77, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_69_LVEErgebinsmeldungVersion14() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 69)
                .addInt16(53125)
                .addInt16(12586)
                .addInt16(15874)
                .addInt16(65285)
                .addInt16(11111)
                .addInt16(58962);

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
            assertEquals("LVEErgebnisVersion14", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(15874, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(65285, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(11111, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(58962, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_70_LVEErgebinsmeldungVersion15() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 70)
                .addInt16(53125)
                .addInt16(12586)
                .addInt16(15874)
                .addInt16(65285)
                .addInt16(11111)
                .addInt16(58962)
                .addByte(15)
                .addByte(85)
                .addByte(91)
                .addByte(72);

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
            assertEquals("LVEErgebnisVersion15", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(15874, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(65285, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(11111, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(58962, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(85, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(72, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_71_LVEErgebinsmeldungVersion16() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 71)
                .addInt16(53125)
                .addInt16(12586)
                .addInt16(15874)
                .addInt16(65285)
                .addInt16(11111)
                .addInt16(58962)
                .addByte(15)
                .addByte(85)
                .addByte(91)
                .addByte(72)
                .addByte(98);

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
            assertEquals("LVEErgebnisVersion16", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(15874, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(65285, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(11111, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(58962, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(85, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(72, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_72_LVEErgebinsmeldungVersion17() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 72)
                .addInt16(53125)
                .addInt16(12586)
                .addInt16(15874)
                .addInt16(65285)
                .addInt16(11111)
                .addInt16(58962)
                .addByte(15)
                .addByte(85)
                .addByte(91)
                .addByte(72)
                .addByte(98)
                .addByte(3)
                .addInt16Array(5482, 6848, 1547)
                .addByte(3)
                .addInt16Array(1484, 8434, 45454);

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
            assertEquals("LVEErgebnisVersion17", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(15874, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(65285, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(11111, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(58962, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(85, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(91, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(72, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vKlassenPkwAe": {
                    List<DataObject> pkws = item.getAsArray();
                    assertEquals(3, pkws.size());

                    List<DataItem> vKlasse0 = pkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(5482, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(6848, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(1547, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkwAe": {
                    List<DataObject> lkws = item.getAsArray();
                    assertEquals(3, lkws.size());

                    List<DataItem> vKlasse0 = lkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(1484, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(8434, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(45454, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_73_LVEErgebinsmeldungVersion18() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 73)
                .addInt16(53125)
                .addInt16(12586)
                .addInt16(15874)
                .addInt16(65285)
                .addInt16(11111)
                .addInt16(58962)
                .addByte(1)
                .addByte(2)
                .addByte(3)
                .addByte(4)
                .addByte(5)
                .addByte(6)
                .addByte(7)
                .addByte(8)
                .addByte(9)
                .addByte(10)
                .addByte(98)
                .addByte(3)
                .addInt16Array(35485, 548, 41885)
                .addByte(3)
                .addInt16Array(5487, 4856, 15883)
                .addByte(3)
                .addInt16Array(21654, 1484, 7877)
                .addByte(3)
                .addInt16Array(1547, 48735, 1857)
                .addByte(3)
                .addInt16Array(52489, 484, 45784);

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
            assertEquals("LVEErgebnisVersion18", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qPkwG":
                    assertEquals(53125, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(12586, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(15874, item.getAsLong().longValue());
                    break;
                case "qLkwK":
                    assertEquals(65285, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(11111, item.getAsLong().longValue());
                    break;
                case "qnkKfz":
                    assertEquals(58962, item.getAsLong().longValue());
                    break;
                case "vPkwG":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(2, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(3, item.getAsLong().longValue());
                    break;
                case "vLkwK":
                    assertEquals(4, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(5, item.getAsLong().longValue());
                    break;
                case "sPkwG":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(7, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(8, item.getAsLong().longValue());
                    break;
                case "sLkwK":
                    assertEquals(9, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(10, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vKlassenPkwG": {
                    List<DataObject> pkwGs = item.getAsArray();
                    assertEquals(3, pkwGs.size());

                    List<DataItem> vKlasse0 = pkwGs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(35485, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkwGs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(548, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkwGs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(41885, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenPkwA": {
                    List<DataObject> pkwAs = item.getAsArray();
                    assertEquals(3, pkwAs.size());

                    List<DataItem> vKlasse0 = pkwAs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(5487, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkwAs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(4856, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkwAs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(15883, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkw": {
                    List<DataObject> lkws = item.getAsArray();
                    assertEquals(3, lkws.size());

                    List<DataItem> vKlasse0 = lkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(21654, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(1484, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(7877, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkwK": {
                    List<DataObject> lkwKs = item.getAsArray();
                    assertEquals(3, lkwKs.size());

                    List<DataItem> vKlasse0 = lkwKs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(1547, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkwKs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(48735, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkwKs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(1857, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenBus": {
                    List<DataObject> buss = item.getAsArray();
                    assertEquals(3, buss.size());

                    List<DataItem> vKlasse0 = buss.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(52489, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = buss.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(484, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = buss.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(45784, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_74_LVEErgebnismeldungVersion19() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 74)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945);

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
            assertEquals("LVEErgebnisVersion19", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_75_LVEErgebnismeldungVersion20() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 75)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addByte(15)
                .addByte(45)
                .addByte(56)
                .addByte(58);

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
            assertEquals("LVEErgebnisVersion20", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(45, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_76_LVEErgebnismeldungVersion21() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 76)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addByte(15)
                .addByte(45)
                .addByte(56)
                .addByte(58)
                .addByte(98);

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
            assertEquals("LVEErgebnisVersion21", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(45, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_77_LVEErgebnismeldungVersion22() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 77)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addByte(15)
                .addByte(45)
                .addByte(56)
                .addByte(58)
                .addByte(98)
                .addByte(3)
                .addInt16Array(5482, 6848, 1547)
                .addByte(3)
                .addInt16Array(1484, 8434, 45454);

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
            assertEquals("LVEErgebnisVersion22", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vPkwAe":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "vLkwAe":
                    assertEquals(45, item.getAsLong().longValue());
                    break;
                case "sPkwAe":
                    assertEquals(56, item.getAsLong().longValue());
                    break;
                case "sLkwAe":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "v85PkwAe":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "vKlassenPkwAe": {
                    List<DataObject> pkws = item.getAsArray();
                    assertEquals(3, pkws.size());

                    List<DataItem> vKlasse0 = pkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(5482, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(6848, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(1547, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkwAe": {
                    List<DataObject> lkws = item.getAsArray();
                    assertEquals(3, lkws.size());

                    List<DataItem> vKlasse0 = lkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(1484, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(8434, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(45454, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_78_LVEErgebnismeldungVersion23() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 78)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addByte(154)
                .addByte(25)
                .addByte(84)
                .addByte(165)
                .addByte(92)
                .addByte(75)
                .addByte(1)
                .addByte(98)
                .addByte(15)
                .addByte(75)
                .addByte(12)
                .addByte(38)
                .addByte(74)
                .addByte(14)
                .addByte(58)
                .addByte(65)
                .addByte(123)

                .addByte(3) // vKlassenKrad 000
                .addByte(15 + 128)
                .addByte(81 + 128)
                .addByte(1 + 128)

                .addByte(3) // vKlassenPkw  001
                .addByte(27 + 128)
                .addByte(66 + 128)
                .addInt16(4989) // 2557

                .addByte(3) // vKlassenLfw  010
                .addByte(42 + 128)
                .addInt16(1661) // 893
                .addByte(22 + 128)

                .addByte(3) // vKlassenPkwA 011
                .addByte(31 + 128)
                .addInt16(2429) // 1277
                .addInt16(32793) // 16409

                .addByte(3) // vKlassenLkw  100
                .addInt16(12317) // 6173
                .addByte(16 + 128)
                .addByte(51 + 128)

                .addByte(3) // vKlassenLkwA 101
                .addInt16(12345)// 6201
                .addByte(12 + 128)
                .addInt16(1106) // 594

                .addByte(3) // vKlassenSattelKfz  110
                .addInt16(6685) // 3357
                .addInt16(19775) // 9919
                .addByte(91 + 128)

                .addByte(3) // vKlassenBus  111
                .addInt16(32541) // 16285
                .addInt16(14621) // 7325
                .addInt16(1053); // 541

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
            assertEquals("LVEErgebnisVersion23", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vKrad":
                    assertEquals(154, item.getAsLong().longValue());
                    break;
                case "vPkw":
                    assertEquals(25, item.getAsLong().longValue());
                    break;
                case "vLfw":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "vPkwA":
                    assertEquals(165, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "vLkwA":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "vSattelKfz":
                    assertEquals(1, item.getAsLong().longValue());
                    break;
                case "vBus":
                    assertEquals(98, item.getAsLong().longValue());
                    break;
                case "sKrad":
                    assertEquals(15, item.getAsLong().longValue());
                    break;
                case "sPkw":
                    assertEquals(75, item.getAsLong().longValue());
                    break;
                case "sLfw":
                    assertEquals(12, item.getAsLong().longValue());
                    break;
                case "sPkwA":
                    assertEquals(38, item.getAsLong().longValue());
                    break;
                case "sLkw":
                    assertEquals(74, item.getAsLong().longValue());
                    break;
                case "sLkwA":
                    assertEquals(14, item.getAsLong().longValue());
                    break;
                case "sSattelKfz":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "sBus":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                case "v85Pkw":
                    assertEquals(123, item.getAsLong().longValue());
                    break;
                case "vKlassenKrad": {
                    List<DataObject> krads = item.getAsArray();
                    assertEquals(3, krads.size());

                    List<DataItem> vKlasse0 = krads.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(15, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = krads.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(81, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = krads.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(1, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenPkw": {
                    List<DataObject> pkws = item.getAsArray();
                    assertEquals(3, pkws.size());

                    List<DataItem> vKlasse0 = pkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(27, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(66, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(2557, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLfw": {
                    List<DataObject> lfws = item.getAsArray();
                    assertEquals(3, lfws.size());

                    List<DataItem> vKlasse0 = lfws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(42, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lfws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(893, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lfws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(22, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenPkwA": {
                    List<DataObject> pkwAs = item.getAsArray();
                    assertEquals(3, pkwAs.size());

                    List<DataItem> vKlasse0 = pkwAs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(31, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = pkwAs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(1277, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = pkwAs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(16409, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }

                case "vKlassenLkw": {
                    List<DataObject> lkws = item.getAsArray();
                    assertEquals(3, lkws.size());

                    List<DataItem> vKlasse0 = lkws.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(6173, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkws.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(16, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkws.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(51, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenLkwA": {
                    List<DataObject> lkwAs = item.getAsArray();
                    assertEquals(3, lkwAs.size());

                    List<DataItem> vKlasse0 = lkwAs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(6201, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lkwAs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(12, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lkwAs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(594, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenSattelKfz": {
                    List<DataObject> krads = item.getAsArray();
                    assertEquals(3, krads.size());

                    List<DataItem> vKlasse0 = krads.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(3357, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = krads.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(9919, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = krads.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(91, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenBus": {
                    List<DataObject> buss = item.getAsArray();
                    assertEquals(3, buss.size());

                    List<DataItem> vKlasse0 = buss.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(16285, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = buss.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(7325, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = buss.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(541, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_79_LVEErgebnismeldungVersion24() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 79)
                .addInt16(8945)
                .addInt16(5545)
                .addInt16(8245)
                .addInt16(7145)
                .addInt16(4345)
                .addInt16(9145)
                .addInt16(10453)
                .addInt16(20450)
                .addInt16(3945)
                .addInt16(5862)
                .addInt16(12345)
                .addInt16(4587)
                .addInt16(7897)
                .addInt16(3577)
                .addInt16(9878)
                .addByte(28)
                .addByte(52)
                .addByte(123)
                .addByte(200)
                .addByte(74)
                .addByte(70)
                .addByte(3)
                .addInt16Array(54987, 8432, 4897)
                .addByte(3)
                .addInt16Array(5458, 486, 48765)
                .addByte(3)
                .addInt16Array(55425, 2784, 12345);

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
            assertEquals("LVEErgebnisVersion24", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "qnkKfz":
                    assertEquals(8945, item.getAsLong().longValue());
                    break;
                case "qKrad":
                    assertEquals(5545, item.getAsLong().longValue());
                    break;
                case "qPkw":
                    assertEquals(8245, item.getAsLong().longValue());
                    break;
                case "qLfw":
                    assertEquals(7145, item.getAsLong().longValue());
                    break;
                case "qPkwA":
                    assertEquals(4345, item.getAsLong().longValue());
                    break;
                case "qLkw":
                    assertEquals(9145, item.getAsLong().longValue());
                    break;
                case "qLkwA":
                    assertEquals(10453, item.getAsLong().longValue());
                    break;
                case "qSattelKfz":
                    assertEquals(20450, item.getAsLong().longValue());
                    break;
                case "qBus":
                    assertEquals(3945, item.getAsLong().longValue());
                    break;
                case "vLVo":
                    assertEquals(586.2, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "vSGV":
                    assertEquals(1234.5, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "vBPA":
                    assertEquals(458.7, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "sLVo":
                    assertEquals(789.7, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "sSGV":
                    assertEquals(357.7, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "sBPA":
                    assertEquals(987.8, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "v85LVo":
                    assertEquals(28, item.getAsLong().longValue());
                    break;
                case "v85SGV":
                    assertEquals(52, item.getAsLong().longValue());
                    break;
                case "v85BPA":
                    assertEquals(123, item.getAsLong().longValue());
                    break;
                case "v15LVo":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "v15SGV":
                    assertEquals(74, item.getAsLong().longValue());
                    break;
                case "v15BPA":
                    assertEquals(70, item.getAsLong().longValue());
                    break;
                case "vKlassenLVo": {
                    List<DataObject> lvos = item.getAsArray();
                    assertEquals(3, lvos.size());

                    List<DataItem> vKlasse0 = lvos.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(54987, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = lvos.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(8432, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = lvos.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(4897, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenSGV": {
                    List<DataObject> sgvs = item.getAsArray();
                    assertEquals(3, sgvs.size());

                    List<DataItem> vKlasse0 = sgvs.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(5458, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = sgvs.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(486, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = sgvs.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(48765, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }
                case "vKlassenBPA": {
                    List<DataObject> bpas = item.getAsArray();
                    assertEquals(3, bpas.size());

                    List<DataItem> vKlasse0 = bpas.get(0).getItems();
                    assertEquals(1, vKlasse0.size());
                    assertEquals(55425, vKlasse0.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse1 = bpas.get(1).getItems();
                    assertEquals(1, vKlasse1.size());
                    assertEquals(2784, vKlasse1.get(0).getAsLong().longValue());

                    List<DataItem> vKlasse2 = bpas.get(2).getItems();
                    assertEquals(1, vKlasse2.size());
                    assertEquals(12345, vKlasse2.get(0).getAsLong().longValue());
                    break;
                }

                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_96_SVEErgebnismeldungVersion0() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 96)
                .addInt16(1578)
                .addByte(2);

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
            assertEquals("SVEErgebnisVersion0", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "kKfz":
                    assertEquals(1578, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(2, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_4_97_SVEErgebnismeldungVersion1() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // ID: 4 + 128
        builder.addDeBlockHeader(33, 97)
                .addInt16(1578)
                .addByte(2)
                .addInt16(2588)
                .addInt16(1234)
                .addByte(84)
                .addByte(222);

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
            assertEquals("SVEErgebnisVersion1", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "kKfz":
                    assertEquals(1578, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(2, item.getAsLong().longValue());
                    break;
                case "kPkw":
                    assertEquals(2588, item.getAsLong().longValue());
                    break;
                case "kLkw":
                    assertEquals(1234, item.getAsLong().longValue());
                    break;
                case "vPkw":
                    assertEquals(84, item.getAsLong().longValue());
                    break;
                case "vLkw":
                    assertEquals(222, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    @Test
    public void test_1_1_251_UnixZeitstempel() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 129); // ID: 1 + 128

        builder.addDeBlockHeader(255, 251) // deTyp 251 = Zeitstempel (alle FGs) (Unix Zeitstempel)
                .addByteArray((byte) (0x5b), (byte) (0x6e), (byte) (0xe3), (byte) (0xc8)); // => 11.8.2018 15:25:28 MESZ

        builder.addDeBlockHeader(33, 1)
                .addByte(0)
                .addByte(6);

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

            CompareResult cr2 = null;
            try {
                cr2 = trc.checkResult(decoded, "Zst251-LveDeFehler");
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                fail();
            }
            if (cr2.hasDifference()) {
                System.out.println("Difference for Zst251-LveDeFehler");
                System.out.println(cr2.getDifference());
                fail();
            }
        }

    }

}
