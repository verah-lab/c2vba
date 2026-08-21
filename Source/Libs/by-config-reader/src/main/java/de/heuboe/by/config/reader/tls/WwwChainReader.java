package de.heuboe.by.config.reader.tls;

import de.heuboe.base.config.reader.CsvConfigReader;
import de.heuboe.base.config.reader.data.ConfigFile;
import de.heuboe.config.base.Types;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * class to read the www chains
 */
@Slf4j
public class WwwChainReader extends CsvConfigReader {

    public static final String WWW_CHAIN_CHARSET = "ISO-8859-1";

    private static int rowNr = 0;

    public WwwChainReader(ConfigFile file) {
        super(file);
    }

    enum WWWColumnType {
        TYPE, ID, NAME, SRID, X, Y, HELP1, HELP2
        ;

        private boolean isId;

        WWWColumnType() {
            isId = false;
        }

        public boolean isId() {
            return isId;
        }
    }

    /**
     * record for one www chain
     */
    public static class WWWChainRecord {
        private final List<String> values;

        WWWChainRecord(List<String> values) {
            this.values = values;
        }

        private String get(int nr) {
            if (values.size() <= nr) {
                return "";
            }
            return values.get(nr);
        }

        /**
         * get the value of the cel with the given column
         * @param type WWWColumnType
         * @return value of the cel
         */
        public String get(WWWColumnType type) {
            return get(type.ordinal());
        }
    }

    /**
     * reads the www chains
     * @param file File
     * @return List of WWWChainRecord
     * @throws IOException -  if the file does not exist
     */
    public static List<WWWChainRecord> read(File file) throws IOException {
        log.info("Reading: {}", file.getName());
        java.util.List<WWWChainRecord> records = readFile(new FileReader(file, Charset.forName(WWW_CHAIN_CHARSET)));
        rowNr = 0;
        return List.ofAll(records);
    }

    private static java.util.List<WWWChainRecord> readFile(Reader reader) throws IOException {
        try (BufferedReader data = new BufferedReader(reader)) {
            java.util.List<WWWChainRecord> records = new ArrayList<>();
            while (readeNextRecord(data, records)) ; // NOSONAR (stupid)
            data.close();
            reader.close();
            return records;
        } catch (IOException e) {
            throw new IOException("Failed to read input ", e);
        }
    }

    private static boolean readeNextRecord(BufferedReader reader, java.util.List<WWWChainRecord> records) {
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
                    continue;
                }
                List<String> tokens = List.of(line.split("\\t"));
                tokens = tokens.map(s -> s.replace("\"", ""));
                if (isDataLine(tokens) && !isHeaderLine(tokens)) {
                    WWWChainRecord e = new WWWChainRecord(tokens);
                    records.add(e);
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to parse row {} | {}", rowNr, line, e);
                return true;
            }
        }
    }

    private static boolean isHeaderLine(List<String> tokens) {
        return tokens.contains(WWWColumnType.ID.name());
    }

    private static boolean isDataLine(List<String> tokens) {
        try {
            String t = columnValue(tokens, WWWColumnType.TYPE.ordinal());
            if (t == null || t.contains("#")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String columnValue(List<String> values, int colId) {
        return values.get(colId);
    }
}
