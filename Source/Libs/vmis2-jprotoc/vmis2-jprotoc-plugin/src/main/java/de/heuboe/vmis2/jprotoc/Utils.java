package de.heuboe.vmis2.jprotoc;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;

import de.heuboe.protobuf.InterfaceVersionProto;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;

/**
 * A helper class with utility/shared methods for the {@link JProtocPlugin}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class Utils {

    /**
     * A constant that contains the pojo sub-package name.
     */
    public static final String POJO_SUBPACKAGE = "pojo";

    /**
     * The suffix used to disambiguate outer class names from a similarly named message, enum or
     * service.
     */
    public static final String OUTER_CLASS_SUFFIX = "OuterClass";

    /**
     * A pattern containing the word separator characters from proto case.
     */
    private static final Pattern PROTO2CAMEL_CASE_PATTERN = Pattern.compile("(?>^|[0-9\\._-])([a-z])");
    /**
     * A pattern containing characters that needs to be stripped.
     */
    private static final Pattern PROTO2CAMEL_STRIP_PATTERN = Pattern.compile("[\\._-]");

    /**
     * Converts the given proto package name to a pojo package name.
     *
     * @param packageName The proto package name.
     * @return The pojo package name.
     */
    public static String protoToPojoPackage(final String packageName) {
        if (isBlank(packageName)) {
            return POJO_SUBPACKAGE;
        } else {
            return packageName + '.' + POJO_SUBPACKAGE;
        }
    }

    /**
     * Extracts the outer class name from the given proto file.
     *
     * @param protoFile The proto file to get the outer name for.
     * @return The outer name for the given proto file.
     */
    public static String extractOuterClassname(final DescriptorProtos.FileDescriptorProto protoFile) {
        final FileOptions options = protoFile.getOptions();
        if (options != null) {
            final String outerName = options.getJavaOuterClassname();
            if (!isBlank(outerName)) {
                return outerName;
            }
        }

        String outerClassname = removeEndIgnoreCase(protoFile.getName(), ".proto"); // Strip file extension
        outerClassname = outerClassname.replaceAll(".*/", ""); // Strip parent folders/path
        outerClassname = replaceAll(PROTO2CAMEL_CASE_PATTERN, outerClassname,
                m -> m.group().toUpperCase()); // Capitalize separated Parts ( foo_bAr -> FooBAr )
        outerClassname = replaceAll(PROTO2CAMEL_STRIP_PATTERN, outerClassname,
                m -> ""); // Strip illegal characters ( foo_BAr -> FooBAr )

        if (Stream.of(
                protoFile.getMessageTypeList().stream().map(DescriptorProto::getName),
                protoFile.getEnumTypeList().stream().map(EnumDescriptorProto::getName),
                protoFile.getServiceList().stream().map(ServiceDescriptorProto::getName))
                .flatMap(UnaryOperator.identity())
                .anyMatch(outerClassname::equals)) {
            return outerClassname + OUTER_CLASS_SUFFIX;
        } else {
            return outerClassname;
        }
    }

    /**
     * Converts the given outer class name to the associated catalog name.
     *
     * @param outerClassName The outer class name to start from.
     * @return The catalog name for the given outer class name.
     */
    public static String toCatalogName(final String outerClassName) {
        return "P" + removeEndIgnoreCase(removeEnd(outerClassName, OUTER_CLASS_SUFFIX), "Proto")
                + "ProtoTransferCatalog";
    }

    /**
     * Extracts the interface version from the given proto file.
     *
     * @param protoFile The proto file to get the interface version for.
     * @return The interface version for the given proto file or null, if not specified.
     */
    public static String extractInterfaceVersion(final DescriptorProtos.FileDescriptorProto protoFile) {
        final FileOptions options = protoFile.getOptions();
        if (options == null) {
            return null;
        } else {
            final String version = options.getExtension(InterfaceVersionProto.interfaceVersion);
            if (version != null && !version.isEmpty()) {
                return version;
            }
            // This fixes the current assumed shortcomings form the JProtocHelper that cannot load extensions.
            // https://github.com/protocolbuffers/protobuf/issues/6373
            final List<ByteString> lengthDelimitedList = options.getUnknownFields()
                    .getField(InterfaceVersionProto.interfaceVersion.getNumber()).getLengthDelimitedList();
            if (lengthDelimitedList.isEmpty()) {
                return null;
            }
            return lengthDelimitedList.get(0).toStringUtf8();
        }
    }

    /**
     * Replaces all matches in the given input using the given function.
     *
     * @param regex The pattern used to identify the matches.
     * @param input The input string to search and replace the matching elements in.
     * @param fn The function used to determine the replacement.
     * @return The string with all matches replaced.
     */
    private static String replaceAll(final Pattern regex, final String input, final Function<Matcher, String> fn) {
        final Matcher m = regex.matcher(input);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, fn.apply(m));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Extracts the java package name for the proto classes from the given proto file.
     *
     * @param protoFile The proto file to get the java package for.
     * @return The package name for the proto classes.
     */
    public static String extractPackageName(final DescriptorProtos.FileDescriptorProto protoFile) {
        final DescriptorProtos.FileOptions options = protoFile.getOptions();
        if (options != null) {
            final String javaPackage = options.getJavaPackage();
            if (!isBlank(javaPackage)) {
                return javaPackage;
            }
        }
        return nullToEmpty(protoFile.getPackage());
    }

    /**
     * Converts the given package and class name to a file path.
     *
     * @param packageName The java package name.
     * @param className The java class name.
     * @return The file path for the given java class.
     */
    public static String toPath(final String packageName, final String className) {
        if (isBlank(packageName)) {
            return className + ".java";
        } else {
            return packageName.replace('.', '/') + '/' + className + ".java";
        }
    }

    /**
     * Creates a new function that converts the given value in a tuple with the index of the element.
     *
     * <p>
     * <b>Usage:</b>
     * </p>
     *
     * <pre>
     * {@code
     * stream.map(Utils.newZipWithIndexMapper())
     *       .forEach(e -> map.put(e.getKey(), e.getValue()));
     * }
     * </pre>
     *
     * @param <T> The type of the values.
     * @return A new function that zips the element with its index.
     */
    public static <T> Function<T, Entry<Integer, T>> newZipWithIndexMapper() {
        final AtomicInteger counter = new AtomicInteger(0);
        return v -> Pair.of(counter.getAndIncrement(), v);
    }

    private Utils() {}

}
