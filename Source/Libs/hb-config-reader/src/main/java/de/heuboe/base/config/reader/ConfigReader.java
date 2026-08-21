package de.heuboe.base.config.reader;

import de.heuboe.base.config.reader.data.ConfigFile;
import de.heuboe.base.config.reader.data.ConfigRecord;
import de.heuboe.base.excel.controller.reader.ExcelFormat;
import io.vavr.collection.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;

public interface ConfigReader {

    enum FileType {EXCEL, TSV, CSV}

    List<ConfigRecord> read(Function<ConfigRecord, Boolean> filter, String idHeader);

    static ConfigReader create(final File file, final FileType type) throws IOException {
        switch (type) {
            case CSV:
            case TSV:
                return new CsvConfigReader(new ConfigFile(file, type));
            case EXCEL:
                try {
                    return new ExcelConfigReader(new ConfigFile(file, FileType.EXCEL));
                } catch (IOException e) {
                    throw new IOException(e);
                }
            default:
                throw new IllegalArgumentException("Unknown file type!");
        }
    }

    static ConfigReader create(final File file, final ExcelFormat format) throws IOException {
        try {
            return new ExcelConfigReader(new ConfigFile(file, FileType.EXCEL), format);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    static ConfigReader create(final File file) throws IOException {
        final String name = file.getName();
        final String ending = name.substring(name.lastIndexOf("\\."));
        if (ending.startsWith("xls")) {
            return new ExcelConfigReader(new ConfigFile(file, FileType.EXCEL));
        } else if ("txt".equals(ending) || "csv".equals(ending) || "tsv".equals(ending)) {
            return new CsvConfigReader(new ConfigFile(file, getFileType(file)));
        } else {
            throw new IllegalArgumentException("Unknown file type!");
        }
    }

    private static FileType getFileType(final File file) throws IOException {
        CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);
        List<CSVRecord> records = List.ofAll(parser.getRecords());
        if (hasMultipleValues(records)) {
            return FileType.CSV;
        }

        parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withDelimiter('\t'));
        records = List.ofAll(parser.getRecords());
        if (hasMultipleValues(records)) {
            return FileType.TSV;
        }

        throw new IllegalArgumentException("Unknown file type!");
    }

    private static boolean hasMultipleValues(List<CSVRecord> records) {
        if (records.size() >= 4) {
            return records.get(0).size() > 1 || records.get(1).size() > 1 || records.get(2).size() > 1 || records.get(3).size() > 1;
        }
        if (records.size() >= 3) {
            return records.get(0).size() > 1 || records.get(1).size() > 1 || records.get(2).size() > 1;
        }
        if (records.size() >= 2) {
            return records.get(0).size() > 1 || records.get(1).size() > 1;
        }
        return false;
    }
}
