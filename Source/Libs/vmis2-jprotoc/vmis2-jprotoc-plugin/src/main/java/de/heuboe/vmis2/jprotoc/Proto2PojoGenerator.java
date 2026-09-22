/*
 * Copyright (c) 2018, HeuBoe
 */

package de.heuboe.vmis2.jprotoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

import de.heuboe.vmis2.jprotoc.api.JProtocHelper;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;
import de.heuboe.vmis2.jprotoc.catalog.ProtoTransferCatalogGenerator;
import de.heuboe.vmis2.jprotoc.catalogtest.ProtoTransferCatalogAsserter;
import de.heuboe.vmis2.jprotoc.debug.DebugOutputGenerator;
import de.heuboe.vmis2.jprotoc.documentation.ProtoExternalReferenceAsserter;
import de.heuboe.vmis2.jprotoc.documentation.ProtoStereotypeAsserter;
import de.heuboe.vmis2.jprotoc.protopojo.ProtoPojoGenerator;

/**
 * The main class for the Proto2Pojo module. Referenced in poms, do NOT rename.
 */
public class Proto2PojoGenerator {

    /**
     * The (optional) expected version for the interfaceVersion in each proto-file.
     */
    private static final String ASSERT_VERSION_OPTION = "--assertVersion=";

    /**
     * A flag that changes the version assertion to ignore absent version specifications.
     */
    private static final String IGNORE_ABSENT_VERSION_FLAG = "--ignoreAbsentVersion";

    /**
     * A flag that changes the version assertion to ignore absent version specifications.
     */
    private static final String IGNORE_ABSENT_STEREOTYPE_FLAG = "--ignoreAbsentStereotype";

    /**
     * The (optional) path to save the {@link CodeGeneratorRequest} to. Useful to prepare tests.
     */
    private static final String SAVE_REQUEST_OPTION = "--saveRequest=";

    /**
     * The main method called from maven-protobuf-plugin.
     *
     * @param args The args specified in the pom.
     */
    public static void main(final String... args) {
        try {
            final List<JProtocPlugin> plugins = new ArrayList<>();
            final Optional<String> saveRequestPath = readStringOption(args, SAVE_REQUEST_OPTION);
            if (saveRequestPath.isPresent()) {
                plugins.add(new DebugOutputGenerator(new File(saveRequestPath.get())));
            }

            final Optional<String> version = readStringOption(args, ASSERT_VERSION_OPTION);
            if (version.isPresent()) {
                final boolean ignoreAbsentVersion = hasFlag(args, IGNORE_ABSENT_VERSION_FLAG);
                plugins.add(new ProtoTransferCatalogAsserter(version.get(), ignoreAbsentVersion));
            }

            plugins.add(new ProtoPojoGenerator());
            plugins.add(new ProtoTransferCatalogGenerator());
            plugins.add(new ProtoExternalReferenceAsserter());
            if (!hasFlagOrBooleanOption(args, IGNORE_ABSENT_STEREOTYPE_FLAG)) {
                plugins.add(new ProtoStereotypeAsserter());
            }

            JProtocHelper.generate(plugins);
        } catch (final Throwable e) { // NOSONAR
            // This ugly exception handling is required otherwise protoc will ignore the exception.
            e.printStackTrace(); // NOSONAR
            System.exit(1);
        }
    }

    /**
     * Reads the value for the given string option.
     *
     * @param args The arguments to search in.
     * @param parameter The prefix to search for. This should usually start with one or to dashes
     *        {@code -} and end with an equals sign {@code =}.
     * @return An optional with the value for the option if present.
     */
    private static Optional<String> readStringOption(final String[] args, final String parameter) {
        return Arrays.stream(args)
                .filter(s -> s.startsWith(parameter))
                .findFirst()
                .map(s -> s.substring(parameter.length()));
    }

    /**
     * Checks whether the given flag is present. Does not support flags with values.
     *
     * @param args The arguments to search in.
     * @param parameter The flag to search for. This should usually start with one or two dashes
     *        {@code -}.
     * @return True, if the flag is present. False otherwise.
     */
    private static boolean hasFlag(final String[] args, final String parameter) {
        return Arrays.stream(args)
                .anyMatch(parameter::equals);
    }

    /**
     * Checks whether the given flag is present as is or with an appended {@code =true} (case
     * insensitive).
     *
     * @param args The arguments to search in.
     * @param parameter The flag to search for. This should usually start with one or two dashes
     *        {@code -}.
     * @return True, if the flag is present as is or with a true suffix. False otherwise.
     */
    @VisibleForTesting
    static boolean hasFlagOrBooleanOption(final String[] args, final String parameter) {
        return hasFlag(args, parameter) || readStringOption(args, parameter + '=')
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

}
