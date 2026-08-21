package de.heuboe.by.config.reader;

import de.heuboe.base.excel.controller.reader.ExcelFormat;
import de.heuboe.base.excel.controller.reader.ExcelReader;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Reader class for the extra file with the KRI info
 */
public class KriLookupReader extends ExcelReader {


    /**
     * KRI file record
     */
    public static class Record {
        private final io.vavr.collection.List<String> values;
        private final io.vavr.collection.Map<String, Integer> header;

        Record(List<String> values, Map<String, Integer> header) {
            this.values = values;
            this.header = header;
        }

        protected String getValue(String name) {
            int col = header.get(name).getOrElseThrow(() -> new IllegalArgumentException("Unknown header: " + name));
            return col <= values.size() - 1 ? values.get(col) : null;
        }

        protected String getValue(int nr) {
            int col = header.get(nr + ".0").get();
            return col <= values.size() - 1 ? values.get(col) : null;
        }

        /**
         * gets the entry of the given column
         *
         * @param type ColumnType
         * @return String
         */
        protected String get(ColumnType type) {
            return this.getValue(type.name());
        }

        public String getKriId() {
            return getValue(ColumnType.KRI.name());
        }

        public Integer getKnotenNr() {
            return Integer.parseInt(getValue(ColumnType.KNOTENNUMMER.name()));
        }

        public String getDE() {
            return getValue(ColumnType.DE_KANAL.name());
        }

        public String getFG() {
            return getValue(ColumnType.FG.name());
        }
    }

    /**
     * KRI file columns
     */
    public enum ColumnType {
        UNTERZENTRALE,
        KRI,
        STRECKENSTATION,
        QUERSCHNITT,
        BEZEICHNER,
        DE_KANAL,
        KNOTENNUMMER,
        BREITE_WGS,
        LAENGE,
        BETRIEBS_KM,
        BETRIEBSSTATUS,
        FG,
        FAHRTRICHTUNG;
    }

    /**
     * Customized Excel reader using the given ExcelFormat
     * <p>
     * If you do not read all records from the given reader, you should call close() on the parser, unless you close the workbook.
     *
     * @param format   the {@link ExcelFormat} used for Excel parsing.
     * @param workbook a {@link Workbook} containing excel input. Must not be null.
     */
    protected KriLookupReader(ExcelFormat format, Workbook workbook) {
        super(format, workbook);
    }

    /**
     * reads all records
     *
     * @param dataSheetName name of the KRI sheet
     * @return a list of all records
     */
    public List<Record> getTlsRecords(String dataSheetName) {
        List<List<String>> records = this.getRecords(dataSheetName);
        Map<String, Integer> header = records.filter(r -> r.contains("KRI")).get().zipWithIndex().toMap(r -> r._1(), r -> r._2()).mapKeys(k-> k.toUpperCase());
        return records.filter(r -> !r.contains("KRI"))
                .map(r -> new Record(r, header)).toList();
    }
}
