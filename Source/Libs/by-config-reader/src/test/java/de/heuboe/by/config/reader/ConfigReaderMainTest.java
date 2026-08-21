package de.heuboe.by.config.reader;

import de.heuboe.base.excel.controller.reader.ExcelFormat;
import de.heuboe.by.config.reader.tls.ConfigObject;
import de.heuboe.by.config.reader.tls.TlsWorld;
import de.heuboe.config.base.Types;
import eu.vmis_ehe.vmis2.configservice.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Slf4j
@ExtendWith(MockitoExtension.class)
@Disabled
class ConfigReaderMainTest {

    private static final String UZ_ID = "C2VBA";

    @Autowired
    private Properties properties;

    @Mock
    private ConfigServiceImporter importer;

    @Autowired
    @InjectMocks
    private ImportCommand command;

    @Autowired
    private TlsWorld config;

    @Autowired
    private ConfigReaderMain main;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    private String importDir = "C://Users//hannahn//eclipse-workspace//Bayern//Konfig//c2vba//aufbereitet";

    @Test
    public void testRun() {
        assertEquals(0, ConfigReaderMain.execute());
    }

    @Test
    public void testRunH() {
        assertEquals(0, ConfigReaderMain.execute("-h"));
    }

    @Test
    public void runIT() {
        Mockito.when(importer.importConfig(config)).thenCallRealMethod();
        Mockito.doCallRealMethod().when(importer).printConfig(any(), any());
        Mockito.when(importer.importConfig(any(), any())).thenReturn(0);
        Mockito.doNothing().when(importer).createEmptyManuals(any());
        Mockito.doNothing().when(importer).activateVersion(any());
        main.run("import", importDir);
        assertEquals(0, main.getExitCode());
    }

    private ActivateVersionRequest getActivateRequest() {
        return ActivateVersionRequest.newBuilder()
                .setVersion(VersionHandle.newBuilder()
                        .setKeys(VersionKeys.newBuilder()
                                .addAllKeys(createVersionKeys())
                                .build())
                        .build())
                .build();
    }

    private java.util.List<VersionKey> createVersionKeys() {
        return Arrays.asList(VersionKey.newBuilder().setUzId(UZ_ID)
                .setBaseVersionId(anyString())
                .setManualVersionId("0")
                .build());
    }

    @Test
    public void createLookup() {
        List<KriLookupReader.Record> kriLookup = null;
        try (Workbook wb = WorkbookFactory.create(new File(properties.getKriFile()))) {
            KriLookupReader kriLookupReader = new KriLookupReader(ExcelFormat.DEFAULT, wb);
            kriLookup = kriLookupReader.getTlsRecords("Sheet0");
        } catch (IOException e) {

        }
        config.addKriLookup(kriLookup);
        Map<Tuple2<Integer, Integer>, String> lookup = readeLookup("Lookup_AQ_Cpio.txt");
        File configFile = new File(importDir);
        if (configFile.isDirectory()) {
            List.of(configFile.listFiles()).filter(File::isFile).map(ImportCommand::readFile).forEach(r -> config.addPhysTable(r, "test"));
        } else {
            config.addPhysTable(ImportCommand.readFile(configFile), configFile.getName());
        }
        config.createLogConfig();
        
        Set<ConfigObject> items = config.get(Types.ConfigItemType.AQ);
        items.addAll(config.get(Types.ConfigItemType.WZG));

        java.util.List<List<String>> records = new ArrayList<>();
        for (ConfigObject item : items) {
            int knotenNr = item.getLoc() * 256 + item.getDist();
            String oldId = lookup.remove(Tuple.of(knotenNr, item.getDe()));
            records.add(List.of(item.getId(), oldId == null ? "" : oldId, item.getDe() + "", item.getLoc() + "", item.getDist() + "", knotenNr + ""));
        }
        lookup.values().forEach(l -> log.warn("Object not found: " + l));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("FG4-ID-Lookup.tsv"))) {
            bw.write("NewId\tOldId\tDE\tLOD\tDIST\tKontenNr\n");
            for (List<String> record : records) {
                bw.write(String.join("\t", record) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Tuple2<Integer, Integer>, String> readeLookup(String fileName) {
        Map<Tuple2<Integer, Integer>, String> lookupTable = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName)))) {
            String line = br.readLine();
            while (line != null) {
                if (line.contains("DE")) {
                    line = br.readLine();
                }
                List<String> record = List.of(line.split("\\t"));
                lookupTable.put(Tuple.of(Integer.parseInt(record.get(6)), Integer.parseInt(record.get(2))), record.get(1));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lookupTable;
    }
}