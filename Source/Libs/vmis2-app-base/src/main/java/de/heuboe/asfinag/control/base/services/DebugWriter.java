package de.heuboe.asfinag.control.base.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

/**
 * Writer for test data.
 */
public class DebugWriter {


    public static final String JSON_FILE = ".json";

    private DebugWriter() {

    }

    /**
     * Writes proto message to given output file.
     * 
     * @param outFile output file
     * @param protoMsg message to write
     */
    public static void proto2file(File outFile, MessageOrBuilder protoMsg) {

        JsonFormat.Printer jsonFormat = JsonFormat.printer();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write(jsonFormat.print(protoMsg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Writes proto message to a file of folder "src/test/resources/stubData".
     * 
     * @param fn filename
     * @param pb message to write
     */
    public static void writePb2Json(String fn, MessageOrBuilder pb) {
        writePb2Json(fn, pb, "src/test/resources/stubData");
    }

    /**
     * Writes proto message to a file of a given folder.
     *
     * @param fn filename
     * @param pb message to write
     * @param path path to the save the debug write
     */
    public static void writePb2Json(String fn, MessageOrBuilder pb, String path) {
        try {
            String asString = JsonFormat.printer().includingDefaultValueFields().print(pb);
            Path d = Path.of(path);
            d.toFile().mkdirs();
            Path p = d.resolve(fn);
            Files.writeString(p, asString, StandardOpenOption.CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes proto message to a file in subfolder, if write-mode is active.
     * @param isWriteMode true if write-mode is active
     * @param path path to folder
     * @param fn filename and name of subfolder
     * @param fnSuffix suffix for filename (null = no suffix)
     * @param pb message to write
     */
    public static void writePb2JsonWithSubfolder(boolean isWriteMode, String path, String fn, String fnSuffix, MessageOrBuilder pb) {
        if (isWriteMode) {
            File outDir = new File(path);
            File outDirMS = new File(outDir, fn);
            outDirMS.mkdirs();
            String name = fnSuffix == null ? fn : fn + fnSuffix;
            File outFile = new File(outDirMS, name + DebugWriter.JSON_FILE);

            DebugWriter.proto2file(outFile, pb);
        }
    }


}
