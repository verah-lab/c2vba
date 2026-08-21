package de.heuboe.tls.receiver.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.core.test.TlsTeleBuilder.EndianType;
import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.impl.TransformationReaderImpl;
import de.heuboe.tls.receiver.impl.TransformerImpl;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.tlstele.TlsTele;

public class ReceiverTests {

    private static AddressConverter    addressConverter;
    private static Transformer         transformer;
    private static TransformationRules transformationRules;

    @BeforeAll
    public static void setUp() throws Exception {
        addressConverter = new TestAddressConverter();
        TransformationReader transformationReader = new TransformationReaderImpl();
        transformer = new TransformerImpl();
        String receiveScript = "src/test/resources/rcv-test-datatypes.txt";

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

    /**
     * Test for "Zeitstempel (Typ 30)"
     */
    @Test
    public void timeTestTyp30() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 129); // fg/id = 1/129

        builder.addDeBlockHeader(255, 30) // deTyp 30 = Zeitstempel (alle FGs)
                .addByte(17) // Stunde + Sommerzeit
                .addByte(12) // Minute
                .addByte(6); // Sekunde

        builder.addDeBlockHeader(33, 1) // deTyp 1 = LVEFehlerDE        
                .addByte(200) // fehlercode
                .addByte(6);// hersteller 

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEFehlerDE", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "Fehlercode":
                    assertEquals(200, item.getAsLong().longValue());
                    break;
                case "Hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                case "#Puffer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "Zeitstempel":
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
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    /**
     * Test for "Zeitstempel (Typ 31)"
     */
    @Test
    public void timeTestTyp31() {

        TlsTeleBuilder builder = new TlsTeleBuilder(4, 129); // fg/id = 4/129

        builder.addDeBlockHeader(255, 31) // deTyp 31 = Zeitstempel (nur FG 4)
                .addByte(17) // Stunde + Sommerzeit
                .addByte(12) // Minute
                .addByte(6) // Sekunde
                .addByte(22) // Tag
                .addInt16(5);

        builder.addDeBlockHeader(33, 1) // deTyp 1 = LVEFehlerDE        
                .addByte(0) // fehlercode
                .addByte(6);// hersteller 

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;

        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("WVZFehlerDEIst", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "Fehlercode":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "Hersteller":
                    assertEquals(6, item.getAsLong().longValue());
                    break;
                case "#Puffer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "Zeitstempel":
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar timeIn = new GregorianCalendar();
                    timeIn.setTimeInMillis(item.getAsLong().longValue());
                    assertEquals(6, timeIn.get(java.util.Calendar.SECOND));
                    assertEquals(12, timeIn.get(java.util.Calendar.MINUTE));
                    assertEquals(17, timeIn.get(java.util.Calendar.HOUR_OF_DAY));
                    assertEquals(22, timeIn.get(java.util.Calendar.DAY_OF_MONTH));
                    assertEquals(now.get(java.util.Calendar.MONTH), timeIn.get(java.util.Calendar.MONTH));
                    assertEquals(now.get(java.util.Calendar.YEAR), timeIn.get(java.util.Calendar.YEAR));
                    break;
                case "folgenummer":
                    assertEquals(5, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    /**
     * Test for "BYTE (1 Byte)"
     */
    @Test
    public void byteTest() {

        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 128) // deTyp 128 = ByteTest
                .addByte(7) // unsigned byte 
                .addByte(128)
                .addByte(255)
                .addByte(Byte.MIN_VALUE) // signed byte
                .addByte(-3)
                .addByte(Byte.MAX_VALUE);

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("ByteTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "low":
                    assertEquals(7, item.getAsLong().longValue());
                    break;
                case "medium":
                    assertEquals(128, item.getAsLong().longValue());
                    break;
                case "high":
                    assertEquals(255, item.getAsLong().longValue());
                    break;
                case "sLow":
                    assertEquals(Byte.MIN_VALUE, item.getAsLong().longValue());
                    break;
                case "sMedium":
                    assertEquals(-3, item.getAsLong().longValue());
                    break;
                case "sHigh":
                    assertEquals(Byte.MAX_VALUE, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    /**
     * Test for "SHORT (2 Byte)"
     */
    @Test
    public void shortTest() {

        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 129) // deTyp 128 = ShortTest
                .addInt16(Short.MIN_VALUE) // signed little endian short
                .addInt16(-3)
                .addInt16(Short.MAX_VALUE)
                .addInt16(9) // unsigned little endian short
                .addInt16(32768)
                .addInt16(65535)
                .addInt16(EndianType.BIG, Short.MIN_VALUE) // signed big endian short
                .addInt16(EndianType.BIG, -13)
                .addInt16(EndianType.BIG, Short.MAX_VALUE)
                .addInt16(EndianType.BIG, 4) // unsigned big endian short
                .addInt16(EndianType.BIG, 32768)
                .addInt16(EndianType.BIG, 65535);

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("ShortTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "low":
                    assertEquals(Short.MIN_VALUE, item.getAsLong().longValue());
                    break;
                case "medium":
                    assertEquals(-3, item.getAsLong().longValue());
                    break;
                case "high":
                    assertEquals(Short.MAX_VALUE, item.getAsLong().longValue());
                    break;
                case "uLow":
                    assertEquals(9, item.getAsLong().longValue());
                    break;
                case "uMedium":
                    assertEquals(32768, item.getAsLong().longValue());
                    break;
                case "uHigh":
                    assertEquals(65535, item.getAsLong().longValue());
                    break;
                case "beLow":
                    assertEquals(Short.MIN_VALUE, item.getAsLong().longValue());
                    break;
                case "beMedium":
                    assertEquals(-13, item.getAsLong().longValue());
                    break;
                case "beHigh":
                    assertEquals(Short.MAX_VALUE, item.getAsLong().longValue());
                    break;
                case "beuLow":
                    assertEquals(4, item.getAsLong().longValue());
                    break;
                case "beuMedium":
                    assertEquals(32768, item.getAsLong().longValue());
                    break;
                case "beuHigh":
                    assertEquals(65535, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }

    }

    /**
     * Test for "NODE (3 Byte)"
     */
    @Test
    public void nodeTest() {

        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 130) // deTyp 130 = NodeTest
                .addInt24(17) // unsigned little endian Node (3 Byte)
                .addInt24(8388608)
                .addInt24(16777215);
        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("NodeTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "low":
                    assertEquals(17, item.getAsLong().longValue());
                    break;
                case "medium":
                    assertEquals(8388608, item.getAsLong().longValue());
                    break;
                case "high":
                    assertEquals(16777215, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    /**
     * Test for "LONG (4 Byte)"
     */
    @Test
    public void longTest() {

        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 131) // deTyp 128 = LongTest
                .addInt32(Integer.MIN_VALUE) // signed little endian long
                .addInt32(-5)
                .addInt32(Integer.MAX_VALUE)
                .addInt32(2) // unsigned little endian long
                .addInt32(214748368)
                .addInt32(4294967295L)
                .addInt32(EndianType.BIG, Integer.MIN_VALUE) // signed big endian long
                .addInt32(EndianType.BIG, -11)
                .addInt32(EndianType.BIG, Integer.MAX_VALUE)
                .addInt32(EndianType.BIG, 7) // unsigned big endian long
                .addInt32(EndianType.BIG, 214748368)
                .addInt32(EndianType.BIG, 4294967295L);

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LongTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "low":
                    assertEquals(Integer.MIN_VALUE, item.getAsLong().longValue());
                    break;
                case "medium":
                    assertEquals(-5, item.getAsLong().longValue());
                    break;
                case "high":
                    assertEquals(Integer.MAX_VALUE, item.getAsLong().longValue());
                    break;
                case "uLow":
                    assertEquals(2, item.getAsLong().longValue());
                    break;
                case "uMedium":
                    assertEquals(214748368, item.getAsLong().longValue());
                    break;
                case "uHigh":
                    assertEquals(4294967295L, item.getAsLong().longValue());
                    break;
                case "beLow":
                    assertEquals(Integer.MIN_VALUE, item.getAsLong().longValue());
                    break;
                case "beMedium":
                    assertEquals(-11, item.getAsLong().longValue());
                    break;
                case "beHigh":
                    assertEquals(Integer.MAX_VALUE, item.getAsLong().longValue());
                    break;
                case "beuLow":
                    assertEquals(7, item.getAsLong().longValue());
                    break;
                case "beuMedium":
                    assertEquals(214748368, item.getAsLong().longValue());
                    break;
                case "beuHigh":
                    assertEquals(4294967295L, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }

    }

    /**
     * Test for "BCD"
     */
    @Test
    public void bcdTest() {
        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 132) // deTyp 132 = BcdTest
                .addBCD(1, "34")
                .addBCD(2, "7632")
                .addBCD(3, "157684")
                .addBCD(4, "18348645");

        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("BcdTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "one":
                    assertEquals(34, item.getAsLong().longValue());
                    break;
                case "two":
                    assertEquals(7632, item.getAsLong().longValue());
                    break;
                case "three":
                    assertEquals(157684, item.getAsLong().longValue());
                    break;
                case "four":
                    assertEquals(18348645, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

    /**
     * Test for "FLOAT"
     */
    @Test
    public void floatTest() {

        TlsTeleBuilder builder = new TlsTeleBuilder(128, 192); // fg/id = 128/192
        builder.addDeBlockHeader(33, 133) // deTyp 133 = FloatTest
                .addFloat(Float.MIN_VALUE)
                .addFloat(11.3f)
                .addFloat(Float.MAX_VALUE);
        TlsTele telegram = builder.getAsTlsTele();

        List<DataObject> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (DataObject dob : decoded) {
                dob.setTele(null);
                dob.setEtel(null);
            }

            assertEquals(1, decoded.size());
            DataObject o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("FloatTest", o.getName());
            for (DataItem item : o.getItems()) {
                switch (item.getName()) {
                case "low":
                    assertEquals(Float.MIN_VALUE, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "medium":
                    assertEquals(11.3f, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                case "high":
                    assertEquals(Float.MAX_VALUE, item.getAsDouble().doubleValue(), Double.MIN_NORMAL);
                    break;
                default:
                    fail("Unexpected DataItem " + item.getName());
                    break;
                }
            }
        }
    }

}
