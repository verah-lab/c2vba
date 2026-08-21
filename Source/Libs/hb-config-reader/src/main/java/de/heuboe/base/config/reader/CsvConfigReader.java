package de.heuboe.base.config.reader;


import de.heuboe.base.config.reader.data.ConfigFile;
import de.heuboe.base.config.reader.data.ConfigRecord;
import io.vavr.collection.List;

import java.util.function.Function;

public class CsvConfigReader implements ConfigReader {

    protected final ConfigFile file;

    public CsvConfigReader (final ConfigFile file) {
        this.file = file;
    }

    @Override
    public List<ConfigRecord> read(Function<ConfigRecord, Boolean> filter, String idHeader) {
        return null;
    }
}
