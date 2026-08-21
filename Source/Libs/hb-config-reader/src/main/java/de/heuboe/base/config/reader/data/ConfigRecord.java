package de.heuboe.base.config.reader.data;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.Value;

@Value
public class ConfigRecord {

    String id;
    Map<String, Integer> header;
    List<String> values;

    public ConfigRecord(List<String> values, Map<String, Integer> header, String idHeader) {
        this.values = values;
        this.header = header;
        this.id = getValue(idHeader);
    }

    public String getValue(String name) {
        Option<Integer> colNr = header.get(name);
        if (colNr.isEmpty()) {
            throw new IllegalArgumentException("Unknown header: " + name);
        }
        if (colNr.get() > values.size() - 1) {
            return "";
        }
        String value = values.get(colNr.get());
        return value == null ? "" : value;
    }

    public boolean hasColumn(String name) {
        return this.header.containsKey(name);
    }
}
