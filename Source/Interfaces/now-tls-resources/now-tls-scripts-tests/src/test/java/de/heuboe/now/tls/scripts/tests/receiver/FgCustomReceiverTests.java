package de.heuboe.now.tls.scripts.tests.receiver;

import de.heuboe.now.tls.scripts.tests.NowTestBase;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.resources.scripts.tests.TlsTeleBuilder;
import de.heuboe.tls.tlstele.TlsTele;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FgCustomReceiverTests extends NowTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FgCustomReceiverTests.class);

    @Test
    public void test_1_4_163_LVEGeisterfahrer() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 163)
                .addByte(103) // hersteller
                .addByte(194) // status
                .addByte(233) // fahrzeugklasse
                .addInt16(4319) // vKfz
                .addInt16(38144) // belegung
                .addInt16(12019) // nettozeitluecke
                .addByte(244); // fahrzeuglaenge

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/1_4_163_LVEGeisterfahrer.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_1_4_163_LVEGeisterfahrer");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEGeisterfahrer", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(103, item.getAsLong().longValue());
                    break;
                case "status":
                    assertEquals(194, item.getAsLong().longValue());
                    break;
                case "fahrzeugklasse":
                    assertEquals(233, item.getAsLong().longValue());
                    break;
                case "vKfz":
                    assertEquals(4319, item.getAsLong().longValue());
                    break;
                case "belegung":
                    assertEquals(38144, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(12019, item.getAsLong().longValue());
                    break;
                case "fahrzeuglaenge":
                    assertEquals(244, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "status", "fahrzeugklasse", "vKfz", "belegung", "nettozeitluecke", "fahrzeuglaenge");
        }
    }

    @Test
    public void test_1_4_182_FVEUnbekannterTyp() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 182)
                .addByteArray(120, 5, 56, 16, 44); // -SKIP-

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/1_4_182_FVEUnbekannterTyp.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_1_4_182_FVEUnbekannterTyp");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("FVEUnbekannterTyp", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "-SKIP-":
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "-SKIP-");
        }
    }

    @Test
    public void test_1_4_190_LVEErgebnisAbstandswarnung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 190)
                .addByte(36) // fahrzeugtyp
                .addByte(242) // geschwindigkeit
                .addByte(65); // nettozeitluecke

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/1_4_190_LVEErgebnisAbstandswarnung.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_1_4_190_LVEErgebnisAbstandswarnung");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEErgebnisAbstandswarnung", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "fahrzeugtyp":
                    assertEquals(36, item.getAsLong().longValue());
                    break;
                case "geschwindigkeit":
                    assertEquals(242, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(65, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "fahrzeugtyp", "geschwindigkeit", "nettozeitluecke");
        }
    }

    @Test
    public void test_1_4_191_LVEBetriebsparameterAbstandswarnung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 191)
                .addByte(122) // aktiv
                .addByte(16); // nettozeitluecke

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/1_4_191_LVEBetriebsparameterAbstandswarnung.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_1_4_191_LVEBetriebsparameterAbstandswarnung");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("LVEBetriebsparameterAbstandswarnung", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "aktiv":
                    assertEquals(122, item.getAsLong().longValue());
                    break;
                case "nettozeitluecke":
                    assertEquals(16, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "aktiv", "nettozeitluecke");
        }
    }

    @Test
    public void test_1_4_194_FVEUnbekannterTyp() {

        TlsTeleBuilder builder = new TlsTeleBuilder(1, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 194)
                .addByteArrayWithSize(71, 177, 139, 44, 27, 120, 236, 142, 233); // -SKIP-

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/1_4_194_FVEUnbekannterTyp.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_1_4_194_FVEUnbekannterTyp");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("FVEUnbekannterTyp", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "-SKIP-":
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "-SKIP-");
        }
    }

    @Test
    public void test_3_4_203_UFDLaerm() {

        TlsTeleBuilder builder = new TlsTeleBuilder(3, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 203)
                .addFloat(0.5456294417381287f); // messwert

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/3_4_203_UFDLaerm.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_3_4_203_UFDLaerm");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("UFDLaerm", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "messwert":
                    assertEquals(0.5456294417381287, item.getAsDouble().doubleValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "messwert");
        }
    }

    @Test
    public void test_6_4_182_VLTBatterieLadezustand() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 182)
                .addByte(223) // autorencode
                .addByte(140); // ladezustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File("TlsTelegramme/6_4_182_VLTBatterieLadezustand.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_182_VLTBatterieLadezustand");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTBatterieLadezustand", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                    case "autorencode":
                        assertEquals(223, item.getAsLong().longValue());
                        break;
                    case "ladezustand":
                        assertEquals(140, item.getAsLong().longValue());
                        break;
                    case "jobnummer":
                        assertEquals(0, item.getAsLong());
                        break;
                    default:
                        fail("Unexpected DataItem " + itemName);
                        break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("autorencode", "ladezustand", "jobnummer");
        }
    }

    @Test
    public void test_6_4_183_VLTBatterieDefekt() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 183)
                .addByte(195) // autorencode
                .addByte(20); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File("TlsTelegramme/6_4_183_VLTBatterieDefekt.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_183_VLTBatterieDefekt");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTBatterieDefekt", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                    case "autorencode":
                        assertEquals(195, item.getAsLong().longValue());
                        break;
                    case "zustand":
                        assertEquals(20, item.getAsLong().longValue());
                        break;
                    case "jobnummer":
                        assertEquals(0, item.getAsLong());
                        break;
                    default:
                        fail("Unexpected DataItem " + itemName);
                        break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("autorencode", "zustand", "jobnummer");
        }
    }

    @Test
    public void test_6_4_184_VLTSolarzellenDefekt() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 184)
                .addByte(185) // autorencode
                .addByte(30); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File("TlsTelegramme/6_4_184_VLTSolarzellenDefekt.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_184_VLTSolarzellenDefekt");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTSolarzellenDefekt", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                    case "autorencode":
                        assertEquals(185, item.getAsLong().longValue());
                        break;
                    case "zustand":
                        assertEquals(30, item.getAsLong().longValue());
                        break;
                    case "jobnummer":
                        assertEquals(0, item.getAsLong());
                        break;
                    default:
                        fail("Unexpected DataItem " + itemName);
                        break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("autorencode", "zustand", "jobnummer");
        }
    }

    @Test
    public void test_6_4_128_VLTVideoEncoder() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 128)
                .addByte(58) // hersteller
                .addByte(0) // SKIPPER
                .addByte(156); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_128_VLTVideoEncoder.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_128_VLTVideoEncoder");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTVideoEncoder", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(58, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(156, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_129_VLTLeistungsmodem() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 129)
                .addByte(209) // hersteller
                .addByte(0) // SKIPPER
                .addByte(88); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_129_VLTLeistungsmodem.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_129_VLTLeistungsmodem");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTLeistungsmodem", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(209, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(88, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_130_VLTNTVideoUSV() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 130)
                .addByte(61) // hersteller
                .addByte(0) // SKIPPER
                .addByte(70); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_130_VLTNTVideoUSV.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_130_VLTNTVideoUSV");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTNTVideoUSV", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(61, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(70, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_131_VLTVideoEncoderErdschluss() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 131)
                .addByte(189) // hersteller
                .addByte(0) // SKIPPER
                .addByte(222); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_131_VLTErdschluss.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_131_VLTErdschluss");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTErdschluss", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(189, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(222, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_132_VLTHoehenkontrolle() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 132)
                .addByte(141) // hersteller
                .addByte(0) // SKIPPER
                .addByte(249); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_132_VLTHoehenkontrolle.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_132_VLTHoehenkontrolle");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTHoehenkontrolle", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(141, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(249, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_133_VLTBrandmeldung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 133)
                .addByte(92) // hersteller
                .addByte(0) // SKIPPER
                .addByte(122); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_133_VLTBrandmeldung.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_133_VLTBrandmeldung");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTBrandmeldung", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(92, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(122, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_134_VLTCO2WertUeberschreitung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 134)
                .addByte(126) // hersteller
                .addByte(0) // SKIPPER
                .addByte(43); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_134_VLTCO2WertUeberschreitung.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_134_VLTCO2WertUeberschreitung");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTCO2WertUeberschreitung", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(126, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(43, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_135_VLTSichttruebung() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 135)
                .addByte(0) // hersteller
                .addByte(0) // SKIPPER
                .addByte(38); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_135_VLTSichttruebung.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_135_VLTSichttruebung");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTSichttruebung", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(38, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_136_VLTNotfall() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 136)
                .addByte(190) // hersteller
                .addByte(0) // SKIPPER
                .addByte(145); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_136_VLTNotfall.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_136_VLTNotfall");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTNotfall", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(190, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(145, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_137_VLTZuVieleFahrzeuge() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 137)
                .addByte(159) // hersteller
                .addByte(0) // SKIPPER
                .addByte(150); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_137_VLTZuVieleFahrzeuge.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_137_VLTZuVieleFahrzeuge");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTZuVieleFahrzeuge", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(159, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(150, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_138_VLTRichtungsbetriebsart() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 138)
                .addByte(246) // hersteller
                .addByte(0) // SKIPPER
                .addByte(171); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_138_VLTRichtungsbetriebsart.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_138_VLTRichtungsbetriebsart");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTRichtungsbetriebsart", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(246, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(171, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_139_VLTUeBetrieb() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 139)
                .addByte(45) // hersteller
                .addByte(0) // SKIPPER
                .addByte(103); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_139_VLTUeBetrieb.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_139_VLTUeBetrieb");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTUeBetrieb", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(45, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(103, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_220_VLTVideoEncoder() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 220)
                .addByte(74) // hersteller
                .addByte(0) // SKIPPER
                .addByte(9); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_220_VLTVideoEncoder.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_220_VLTVideoEncoder");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTVideoEncoder", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(74, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(9, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_221_VLTErdschluss() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 221)
                .addByte(133) // hersteller
                .addByte(0) // SKIPPER
                .addByte(139); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_221_VLTErdschluss.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_221_VLTErdschluss");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTErdschluss", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(133, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(139, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_222_VLTSchluesselschalter() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 222)
                .addByte(135) // hersteller
                .addByte(0) // SKIPPER
                .addByte(237); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_222_VLTSchluesselschalter.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_222_VLTSchluesselschalter");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTSchluesselschalter", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(135, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(237, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_6_4_223_VLTBrandmelder() {

        TlsTeleBuilder builder = new TlsTeleBuilder(6, 132); // id = 4 + 128
        builder.addDeBlockHeader(33, 223)
                .addByte(247) // hersteller
                .addByte(0) // SKIPPER
                .addByte(132); // zustand

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/6_4_223_VLTBrandmelder.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_6_4_223_VLTBrandmelder");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("VLTBrandmelder", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(247, item.getAsLong().longValue());
                    break;
                case "SKIPPER":
                    break;
                case "zustand":
                    assertEquals(132, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "hersteller", "SKIPPER", "zustand");
        }
    }

    @Test
    public void test_254_1_131_DaSYSFehlerDUE() {

        TlsTeleBuilder builder = new TlsTeleBuilder(254, 129); // id = 1 + 128
        builder.addDeBlockHeader(33, 131)
                .addByte(236) // fehlercode
                .addByte(219); // hersteller

        TlsTele telegram = builder.getAsTlsTele();

        try {
            telegram.saveJs(new File(TELE_PATH + "/254_1_131_DaSYSFehlerDUE.json"));
        } catch (IOException e) {
            LOGGER.warn("Could not save TlsTele for test_254_1_131_DaSYSFehlerDUE");
        }

        List<DataObjectIf> decoded = null;
        {
            try {
                decoded = transformer.transform(telegram);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            assertEquals(1, decoded.size());
            DataObjectIf o = decoded.get(0);
            assertEquals(false, o.isSubsequent());
            assertEquals("SYSFehlerDUE", o.getName());

            Set<String> itemNames = new HashSet<>();

            for (DataItem item : o.getItems()) {
                String itemName = item.getName();
                assertTrue(itemNames.add(itemName) || itemName.equals("SKIPPER"), "Duplicate DataItem " + itemName);

                switch (itemName) {
                case "jobnummer":
                    assertEquals(0, item.getAsLong().longValue());
                    break;
                case "fehlercode":
                    assertEquals(236, item.getAsLong().longValue());
                    break;
                case "hersteller":
                    assertEquals(219, item.getAsLong().longValue());
                    break;
                default:
                    fail("Unexpected DataItem " + itemName);
                    break;
                }
            }
            Assertions.assertThat(itemNames).containsExactlyInAnyOrder("jobnummer", "fehlercode", "hersteller");
        }
    }
}
