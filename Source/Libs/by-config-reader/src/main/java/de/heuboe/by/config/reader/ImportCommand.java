package de.heuboe.by.config.reader;


import de.heuboe.base.excel.controller.reader.ExcelFormat;
import de.heuboe.by.config.reader.tls.TlsConfigReader;
import de.heuboe.by.config.reader.tls.TlsWorld;
import de.heuboe.by.config.reader.tls.WwwChainReader;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Import command class
 */
@CommandLine.Command(name = "import", description = "Imports a tls-configuration to the configservice")
@Component
@Slf4j
public class ImportCommand implements Callable<Integer> {

//    @CommandLine.Parameters(index = "0")
//    private File configFile;

    @Autowired
    private Properties properties;

    @Autowired
    private ConfigServiceImporter importer;

    @Autowired
    private TlsWorld config;

    /**
     * to call the import command
     * @return 0 if successful
     */
    @Override
    public Integer call() {
        return doImport();
    }

    public Integer doImport() {
        List<KriLookupReader.Record> kriLookup = null;
        try (Workbook wb = WorkbookFactory.create(new File(properties.getKriFile()))) {
            KriLookupReader kriLookupReader = new KriLookupReader(ExcelFormat.DEFAULT, wb);
            kriLookup = kriLookupReader.getTlsRecords("Sheet0");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        config.addKriLookup(kriLookup);
        log.info("Start import");
        File configFile = new File(properties.getAufbereitetDir());
        if (configFile.isDirectory()) {
            Map<String, List<TlsConfigReader.HBRecord>> recsOfFiles = List.of(configFile.listFiles())
                    .filter(File::isFile)
                    .collect(Collectors.toMap(file -> file.getName(), ImportCommand::readFile));

            for(Map.Entry<String, List<TlsConfigReader.HBRecord>> recsOfFile : recsOfFiles.entrySet()) {
                log.info("Create TLSWorld for {}...", recsOfFile.getKey());
                try {
                    config.addPhysTable(recsOfFile.getValue(), recsOfFile.getKey());
                } catch (Exception e) {
                    log.error("Error processing file '{}'", recsOfFile.getKey(), e);
                    throw new RuntimeException(e);
                }
            }
        } else {
            config.addPhysTable(readFile(configFile), configFile.getName());
        }
        config.createLogConfig();
        config.addIbs();
        try {
            config.addWwwChains(WwwChainReader.read(new File(properties.getWwwDir())));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            importer.importConfig(config);
        } catch (Exception e) {
            log.error("Failed to import config", e);
            return 2;
        }
        return 0;
    }

    static List<TlsConfigReader.HBRecord> readFile(File file) {
        log.info("\t-> " + file.getName());
        try {
            TlsConfigReader reader = new TlsConfigReader(file);
            return reader.getTlsRecords();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
