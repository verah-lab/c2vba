package de.heuboe.by.config.reader.tls;

import de.heuboe.base.config.reader.CsvConfigReader;
import de.heuboe.base.config.reader.data.ConfigFile;
import de.heuboe.config.base.Types;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;


/**
 * TLS reader class
 */
@Slf4j
public class TlsConfigReader extends CsvConfigReader {

    public static final String TLS_DATA_CHARSET = "UTF8";
    private static final java.util.Map<String, String> TYPE_NAMES = new java.util.HashMap<>();
    private static final java.util.List<String> IGNORE_TYPE = new ArrayList<>();

    private int rowNr = 0;

    /**
     * Customized Excel reader using the given ExcelFormat
     * <p>
     * If you do not read all records from the given reader, you should call close() on the parser, unless you close the workbook.
     * @param file File
     */
    public TlsConfigReader(File file) {
        super(new ConfigFile(file, FileType.TSV));
    }

    static {
        TYPE_NAMES.put("SM", "SST");
        IGNORE_TYPE.add("MU");
    }

    enum HBColumnType {
        ID_CLASS, ID_PERM, ID_NAME, ROAD, LOC, DIST, FG, DE, EA, EAK, PORT, SLAVE, LANE, EFH, TLS_TYPE,
        WZG_TYPE, ANZEIGEPRINZIP, STEUERPRINZIP, ZEICHENSATZ, IP_ADRESSE;

        private boolean isId;

        HBColumnType() {
            isId = false;
        }

        public boolean isId() {
            return isId;
        }
    }


    /**
     * tls record
     */
    public static class HBRecord {
        private final List<String> values;

        HBRecord(List<String> values) {
            this.values = values;
        }

        private String get(int nr) {
            if (values.size() <= nr) {
                return "";
            }
            return TYPE_NAMES.getOrDefault(values.get(nr), values.get(nr));
        }

        /**
         * get the value of the cel with the given column
         *
         * @param type HBColumnType
         * @return value of the cell
         */
        public String get(HBColumnType type) {
            return get(type.ordinal());
        }
    }

    public List<HBRecord> getTlsRecords() throws IOException {
        return read();
    }

    private List<HBRecord> read() throws IOException {
        log.info("Reading: {}", file);
        java.util.List<HBRecord> records = this.readFile(new FileReader(file, Charset.forName(TLS_DATA_CHARSET)));
        rowNr = 0;
        return List.ofAll(records);
    }

    private java.util.List<HBRecord> readFile(Reader reader) throws IOException {
        try (BufferedReader data = new BufferedReader(reader)) {
            java.util.List<HBRecord> records = new ArrayList<>();
            while (this.readeNextRecord(data, records)) ; // NOSONAR (stupid)
            data.close();
            reader.close();
            return records;
        } catch (IOException e) {
            throw new IOException("Failed to read input ", e);
        }
    }

    private boolean readeNextRecord(BufferedReader reader, java.util.List<HBRecord> records) {
        while (true) {
            ++rowNr;
            String line;
            try {
                line = reader.readLine();
                if (line == null) {
                    return false;
                }
            } catch (Exception e) {
                log.error("Failed to read row {}", rowNr, e);
                return false;
            }
            try {
                if (line.replace("\t", "").isBlank()) {
                    return true;
                }
                List<String> tokens = List.of(line.split("\\t"));
                tokens = tokens.map(s -> s.replace("\"", ""));
                if (isDataLine(tokens) && !isHeaderLine(tokens)) {
                    HBRecord e = new HBRecord(tokens);
                    if (!IGNORE_TYPE.contains(e.get(HBColumnType.ID_CLASS))) {
                        records.add(e);
                    }
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to parse row {} | {}", rowNr, line, e);
                return true;
            }
        }
    }


    private boolean isHeaderLine(List<String> tokens) {
        return tokens.contains(HBColumnType.ID_CLASS.name());
    }

    private boolean isDataLine(List<String> tokens) {
        try {
            String t = columnValue(tokens, HBColumnType.ID_CLASS.ordinal());
            Types.ConfigItemType.valueOf(TYPE_NAMES.getOrDefault(t, t));
            if (t == null || t.contains("#")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String columnValue(List<String> values, int colId) {
        return values.get(colId);
    }
}
