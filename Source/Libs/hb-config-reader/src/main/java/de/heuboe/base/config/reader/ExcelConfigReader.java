package de.heuboe.base.config.reader;

import de.heuboe.base.config.reader.data.ConfigFile;
import de.heuboe.base.config.reader.data.ConfigRecord;
import de.heuboe.base.excel.controller.reader.ExcelFormat;
import de.heuboe.base.excel.controller.reader.ExcelReader;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExcelConfigReader extends ExcelReader implements ConfigReader {

    private static final ExcelFormat FORMAT = ExcelFormat.FORMULA_CASHED_VALUE.withIgnoreEmptyRows(true) //NOSONAR
            .withParseNumbersToInt(false).withBlankValue("").withErrorCells(false);

    private String sheetName;
    private Option<Integer> startRow;

    ExcelConfigReader(final ConfigFile file) throws IOException {
        super(FORMAT, WorkbookFactory.create(file));
    }

    ExcelConfigReader(final ConfigFile file, ExcelFormat format) throws IOException {
        super(format, WorkbookFactory.create(file));
    }

    @Override
    public List<ConfigRecord> read(Function<ConfigRecord, Boolean> filter, String idHeader) {
        if (sheetName == null || sheetName.isBlank()) {
            throw new IllegalArgumentException("No sheet name set!");
        }
        List<List<String>> records = this.getRecords(this.sheetName);
        List<String> header = records.filter(r -> r.contains(idHeader))
                .getOrElseThrow(() -> new IllegalArgumentException("No Header found!"));
        Map<String, Integer> headerMap = createHeaderMap(header);
        if (startRow.isDefined()) {
            records = records.subSequence(startRow.get(), records.size() - 1);
        }
        return records.filter(r -> !r.contains(idHeader))
                .map(r -> new ConfigRecord(r, headerMap, idHeader))
                .filter(filter::apply);
    }

    private Map<String, Integer> createHeaderMap(List<String> header) {
        return io.vavr.collection.HashMap.ofAll(header.collect(Collectors.toMap(h -> h, h -> header.indexOf(h))));
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setStartRow(int startRow) {
        this.startRow = Option.of(startRow);
    }
}
