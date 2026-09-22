/*
 * Copyright (c) 2018, HeuBoe
 */

package de.heuboe.vmis2.jprotoc.debug;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.stream.Stream;

import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;

/**
 * Generates some useful debug output that can be used in tests. This allows executing the plugin
 * without the protoc wrapper overhead. This class does not generate any actual code generator
 * response files instead it saves the code generator request to the disk.
 */
public class DebugOutputGenerator implements JProtocPlugin {

    private final File target;

    /**
     * Creates a new DebugOutputGenerator that will save the incoming request at the given target.
     *
     * @param target The target to save the request to.
     */
    public DebugOutputGenerator(final File target) {
        this.target = target.getAbsoluteFile();
    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> generate(final PluginProtos.CodeGeneratorRequest request) {
        final File parent = this.target.getParentFile();
        if (null != parent) {
            parent.mkdirs();
        }
        try (OutputStream fo = new FileOutputStream(this.target)) {
            request.writeTo(fo);
        } catch (final Exception e) {
            throw new GenerationException("Failed to write proto request to " + this.target.getPath(), e);
        }

        try (OutputStream fo = new FileOutputStream(new File(parent, this.target.getName() + ".json"));
                OutputStreamWriter writer = new OutputStreamWriter(fo, UTF_8)) {
            final Printer jsonPrinter = JsonFormat.printer();
            writer.write(jsonPrinter.print(request));
        } catch (final Exception e) {
            throw new GenerationException("Failed to write json request to " + this.target.getPath(), e);
        }

        return Stream.empty();
    }

}
