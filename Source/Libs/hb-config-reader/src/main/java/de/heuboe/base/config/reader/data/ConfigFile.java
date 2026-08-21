package de.heuboe.base.config.reader.data;

import de.heuboe.base.config.reader.ConfigReader;
import lombok.Value;

import java.io.File;

@Value
public class ConfigFile extends File {

    ConfigReader.FileType type;

    public ConfigFile(File file, ConfigReader.FileType type) {
        super(file.getAbsolutePath());
        this.type = type;
    }
}
