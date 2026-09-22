package de.heuboe.vmis2.jprotoc.utils;

import java.lang.reflect.InvocationTargetException;

import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Timestamp;

import de.heuboe.protobuf.InterfaceVersionProto;
import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.EnumTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufJavaBase;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufJavaEnum;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;
import de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer;

/**
 * Helper class that offers useful methods related to the conversion between java (pojo and enum)
 * and proto classes. For hot code, caching the results can improve the performance.
 *
 * <p>
 * <b>Note:</b> This class uses the following naming convention:
 * </p>
 *
 * <ul>
 * <li><b>proto class:</b> Either a protobuf {@link Message} or a {@link ProtocolMessageEnum}.</li>
 * <li><b>proto message:</b> A protobuf {@link Message}.</li>
 * <li><b>proto enum:</b> A protobuf {@link ProtocolMessageEnum} that is also an {@link Enum}.</li>
 * <li><b>java class:</b> A java {@link HbProtoBufJavaBase} (pojo or enum).</li>
 * <li><b>java pojo:</b> A java {@link HbProtoBufPojo}.</li>
 * <li><b>java enum:</b> A java {@link HbProtoBufJavaEnum} that is also an {@link Enum}.</li>
 * </ul>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class ProtoPojoUtils {

    static {
        // Trigger init, if that one fails this one should fail as well.
        ProtoTransferRegistry.getCatalogs();
    }

    /**
     * A regex that matches a dot, that is followed by an uppercase character. The pattern uses a
     * lookahead and thus the uppercase characters aren't consumed.
     */
    private static final String DOT_FOLLOWED_BY_UPPER = "\\.(?=[A-Z])";
    /**
     * The default infix for pojo class names.
     */
    private static final String POJO_INFIX = ".pojo.P";

    /**
     * Checks whether the given class is a protobuf {@link Message} or {@link ProtocolMessageEnum}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code true}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code false}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a protobuf message or enum. False, otherwise.
     */
    public static boolean isProtoClass(final Class<?> clazz) {
        return isProtoMessage(clazz) || isProtoEnum(clazz);
    }

    /**
     * Checks whether the given class is a protobuf {@link Message}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code true}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code false}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a protobuf message. False, otherwise.
     */
    public static boolean isProtoMessage(final Class<?> clazz) {
        return Message.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether the given class is a protobuf {@link ProtocolMessageEnum}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code false}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code false}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a protobuf enum. False, otherwise.
     */
    public static boolean isProtoEnum(final Class<?> clazz) {
        return ProtocolMessageEnum.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether the given class is a {@link HbProtoBufJavaBase java pojo or enum}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code false}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code true}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a java pojo or enum. False, otherwise.
     */
    public static boolean isJavaClass(final Class<?> clazz) {
        return HbProtoBufJavaBase.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether the given class is a {@link HbProtoBufPojo java pojo}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code false}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code false}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a java pojo. False, otherwise.
     */
    public static boolean isJavaPojo(final Class<?> clazz) {
        return HbProtoBufPojo.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether the given class is a {@link HbProtoBufJavaEnum java enum}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code false}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt; {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code true}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code false}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code true}</li>
     * <li>{@link java.lang.String} -&gt; {@code false}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code false}</li>
     * </ul>
     *
     * @param clazz The class to check.
     * @return True, if the given class is a java enum. False, otherwise.
     */
    public static boolean isJavaEnum(final Class<?> clazz) {
        return HbProtoBufJavaEnum.class.isAssignableFrom(clazz);
    }

    /**
     * Searches for the java class for the given proto or pojo class. This method supports
     * {@link Message protobuf messages}, {@link ProtocolMessageEnum protobuf enums} and
     * {@link HbProtoBufJavaBase java pojos and enums}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceStatus}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceStatus}</li>
     * <li>{@link java.lang.String} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code IllegalArgumentException}</li>
     * </ul>
     *
     * @param clazz The proto or pojo class that you want to convert.
     * @return The pojo class associated with the given class.
     * @throws IllegalArgumentException If the associated java class couldn't be found.
     */
    public static Class<? extends HbProtoBufJavaBase> toJavaClass(final Class<?> clazz) {
        if (isJavaClass(clazz)) {
            return clazz.asSubclass(HbProtoBufJavaBase.class);
        }
        if (!isProtoClass(clazz)) {
            throw new IllegalArgumentException(clazz.getName() + " is not a Message or ProtocolMessageEnum class!");
        }
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(clazz);
        if (transfer != null) {
            return transfer.pojoClass();
        }
        final String protoClassName = clazz.getName();
        final String pojoClassName = protoClassName.replaceFirst(DOT_FOLLOWED_BY_UPPER, POJO_INFIX);
        try {
            return Class.forName(pojoClassName, false, clazz.getClassLoader()).asSubclass(HbProtoBufJavaBase.class);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException("Could not find the java class for: " + clazz.getName(), e);
        }
    }

    /**
     * Searches for the java class that belongs to the given fully qualified proto name.
     *
     * <p>
     * <b>Note:</b> This method will fail for basic/native protobuf types such as {@link Timestamp} and
     * messages that use a {@code java_package} that does not match their {@code package} specification.
     * </p>
     *
     * <p>
     * This method will use the {@link Thread#getContextClassLoader() Thread's ContextClassLoader}, if
     * set. Otherwise it will fallback to this class's classloader.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code google.protobuf.Timestamp} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code google.protobuf.Field.Kind} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name to search the class for.
     * @return The pojo class for the given fully qualified proto name.
     * @throws IllegalArgumentException If the associated java class couldn't be found.
     * @see #toJavaClass(String, ClassLoader)
     */
    public static Class<? extends HbProtoBufJavaBase> toJavaClass(final String fullProtoName) {
        return toJavaClass(fullProtoName, getCurrentClassLoader());
    }

    /**
     * Searches for the java class that belongs to the given fully qualified proto name.
     *
     * <p>
     * <b>Note:</b> This method will fail for basic/native protobuf types such as {@link Timestamp} and
     * messages that use a {@code java_package} that does not match their {@code package} specification.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code google.protobuf.Timestamp} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code google.protobuf.Field.Kind} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name to search the class for.
     * @param classLoader The classLoader to use.
     * @return The pojo class for the given fully qualified proto name.
     * @throws IllegalArgumentException If the associated java class couldn't be found.
     */
    public static Class<? extends HbProtoBufJavaBase> toJavaClass(final String fullProtoName,
            final ClassLoader classLoader) {
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(fullProtoName);
        if (transfer != null) {
            return transfer.pojoClass();
        }
        try {
            return Class.forName(toJavaClassName(fullProtoName), false, classLoader)
                    .asSubclass(HbProtoBufJavaBase.class);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find the java class for: " + fullProtoName, e);
        }
    }

    /**
     * Converts the given fully qualified proto name to the <b>assumed</b> java class name.
     *
     * <p>
     * <b>Note:</b> This method might return wrong results for basic/native protobuf types such as
     * {@link Timestamp} and messages that use a {@code java_package} that does not match their
     * {@code package} specification.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.pojo.PServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name to search the class name for.
     * @return The assumed class name for the java class.
     */
    private static String toJavaClassName(final String fullProtoName) {
        return fullProtoName
                .replaceAll(DOT_FOLLOWED_BY_UPPER, "\\$") // replace all dots that are followed by an uppercase character
                .replaceFirst("\\$", POJO_INFIX); // undo the first and insert the pojo infix.
    }

    /**
     * Searches for the proto class for the given proto or pojo class. This method supports
     * {@link Message protobuf messages}, {@link ProtocolMessageEnum protobuf enums} and
     * {@link HbProtoBufJavaBase java pojos and enums}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@link com.google.protobuf.Timestamp} -&gt; {@code com.google.protobuf.Timestamp}</li>
     * <li>{@link com.google.protobuf.Field.Kind} -&gt; {@code com.google.protobuf.Field.Kind}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion} (generated pojo) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceVersion$Nested} (generated nested pojo) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * <li>{@code de.heuboe.vmis2.interface.pojo.PServiceStatus} (generated java enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * <li>{@link java.lang.String} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@link java.time.DayOfWeek} -&gt; {@code IllegalArgumentException}</li>
     * </ul>
     *
     * @param clazz The proto or pojo class that you want to convert.
     * @return The proto class associated with the given class.
     * @throws IllegalArgumentException If the given class is neither a proto nor a java class.
     */
    public static Class<?> toProtoClass(final Class<?> clazz) {
        if (isProtoClass(clazz)) {
            return clazz;
        }
        if (!isJavaClass(clazz)) {
            throw new IllegalArgumentException(clazz.getName() + " is not a HbProtoBufJavaBase class!");
        }
        return javaToTransfer(clazz.asSubclass(HbProtoBufJavaBase.class)).protoClass();
    }

    /**
     * Searches for the proto class that belongs to the given fully qualified proto name.
     *
     * <p>
     * <b>Note:</b> This method will fail for basic/native protobuf types such as {@link Timestamp} and
     * messages that use a {@code java_package} that does not match their {@code package} specification.
     * </p>
     *
     * <p>
     * This method will use the {@link Thread#getContextClassLoader() Thread's ContextClassLoader}, if
     * set. Otherwise it will fallback to this class's classloader.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code google.protobuf.Timestamp} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code google.protobuf.Field.Kind} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name.
     * @return The associated proto class.
     * @throws IllegalArgumentException If the associated proto class could not be resolved.
     * @see #toProtoClass(String, ClassLoader)
     */
    public static Class<?> toProtoClass(final String fullProtoName) {
        return toProtoClass(fullProtoName, getCurrentClassLoader());
    }

    /**
     * Searches for the proto class that belongs to the given fully qualified proto name.
     *
     * <p>
     * <b>Note:</b> This method will fail for basic/native protobuf types such as {@link Timestamp} and
     * messages that use a {@code java_package} that does not match their {@code package} specification.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code google.protobuf.Timestamp} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code google.protobuf.Field.Kind} -&gt; {@code IllegalArgumentException}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name.
     * @param classLoader The classLoader to use.
     * @return The associated proto class.
     * @throws IllegalArgumentException If the associated proto class could not be resolved.
     */
    public static Class<?> toProtoClass(final String fullProtoName, final ClassLoader classLoader) {
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(fullProtoName);
        if (transfer != null) {
            return transfer.protoClass();
        }
        try {
            return Class.forName(toProtoClassName(fullProtoName), false, classLoader);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find the proto class for: " + fullProtoName, e);
        }
    }

    /**
     * Converts the given fully qualified proto name to the <b>assumed</b> proto class name.
     *
     * <p>
     * <b>Note:</b> This method might return wrong results for basic/native protobuf types such as
     * {@link Timestamp} and messages that use a {@code java_package} that does not match their
     * {@code package} specification.
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion.Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion$Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * </ul>
     *
     * @param fullProtoName The fully qualified proto name.
     * @return The assumed proto class name.
     */
    private static String toProtoClassName(final String fullProtoName) {
        return fullProtoName
                .replaceAll(DOT_FOLLOWED_BY_UPPER, "\\$") // replace all dots that are followed by an uppercase character
                .replaceFirst("\\$", "."); // undo the first
    }

    /**
     * Searches for the transfer definition for the given proto or java class. This method supports
     * {@link Message protobuf messages}, {@link ProtocolMessageEnum protobuf enums} and
     * {@link HbProtoBufJavaBase java pojos and enums}.
     *
     * @param clazz The class used to search for the transfer definition.
     * @return The transfer definition for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufJavaBase nor a
     *         protobuf class with an associated HbProtoBufJavaBase class.
     */
    public static BaseTransfer<?, ?> toTransfer(final Class<?> clazz) {
        if (isProtoClass(clazz)) {
            return toTransfer(toJavaClass(clazz));
        }
        if (!isJavaClass(clazz)) {
            throw new IllegalArgumentException(
                    "Could not find transfer definition for " + clazz.getName() + " (Not a HbProtoBufJavaBase class)");
        }
        return javaToTransfer(clazz.asSubclass(HbProtoBufJavaBase.class));
    }

    /**
     * Searches for the transfer definition belonging to the given fully qualified proto name.
     *
     * <p>
     * This method will use the {@link Thread#getContextClassLoader() Thread's ContextClassLoader}, if
     * set. Otherwise it will fallback to this class's classloader.
     * </p>
     *
     * @param fullProtoName The fully qualified proto name.
     * @return The transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found.
     */
    public static BaseTransfer<?, ?> toTransfer(final String fullProtoName) {
        return toTransfer(fullProtoName, getCurrentClassLoader());
    }

    /**
     * Searches for the transfer definition belonging to the given fully qualified proto name.
     *
     * @param fullProtoName The fully qualified proto name.
     * @param classLoader The classLoader to use.
     * @return The transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found.
     */
    public static BaseTransfer<?, ?> toTransfer(final String fullProtoName, final ClassLoader classLoader) {
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(fullProtoName);
        if (transfer != null) {
            return transfer;
        }
        return javaToTransfer(toJavaClass(fullProtoName, classLoader));
    }

    /**
     * Searches for the transfer definition for the given proto or java class. This method supports
     * {@link Message protobuf messages} and {@link HbProtoBufPojo java pojos}.
     *
     * @param clazz The class used to search for the transfer definition.
     * @return The transfer definition for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufPojo nor a protobuf
     *         message with an associated HbProtoBufPojo class.
     */
    public static PojoTransfer<?, ?> toPojoTransfer(final Class<?> clazz) {
        if (isProtoMessage(clazz)) {
            return toPojoTransfer(toJavaClass(clazz));
        }
        if (!isJavaPojo(clazz)) {
            throw new IllegalArgumentException(
                    "Could not find pojo transfer definition for " + clazz.getName() + " (Not a HbProtoBufPojo class)");
        }
        return (PojoTransfer<?, ?>) javaToTransfer(clazz.asSubclass(HbProtoBufPojo.class));
    }

    /**
     * Searches for the pojo transfer definition belonging to the given fully qualified proto name.
     *
     * <p>
     * This method will use the {@link Thread#getContextClassLoader() Thread's ContextClassLoader}, if
     * set. Otherwise it will fallback to this class's classloader.
     * </p>
     *
     * @param fullProtoName The fully qualified proto name.
     * @return The pojo transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found or the
     *         name does not refer to a message.
     */
    public static PojoTransfer<?, ?> toPojoTransfer(final String fullProtoName) {
        return toPojoTransfer(fullProtoName, getCurrentClassLoader());
    }

    /**
     * Searches for the pojo transfer definition belonging to the given fully qualified proto name.
     *
     * @param fullProtoName The fully qualified proto name.
     * @param classLoader The classLoader to use.
     * @return The pojo transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found or the
     *         name does not refer to a message.
     */
    public static PojoTransfer<?, ?> toPojoTransfer(final String fullProtoName, final ClassLoader classLoader) {
        try {
            return (PojoTransfer<?, ?>) toTransfer(fullProtoName, classLoader);
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException("The fully qualified proto name does not describe a message.", e);
        }
    }

    /**
     * Searches for the transfer definition for the given proto or java class. This method supports
     * {@link ProtocolMessageEnum protobuf enums} and {@link HbProtoBufJavaEnum java enums}.
     *
     * @param clazz The class used to search for the transfer definition.
     * @return The transfer definition for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufJavaEnum nor a
     *         protobuf enum with an associated HbProtoBufJavaEnum class.
     */
    public static EnumTransfer<?, ?> toEnumTransfer(final Class<?> clazz) {
        if (isProtoEnum(clazz)) {
            return toEnumTransfer(toJavaClass(clazz));
        }
        if (!isJavaEnum(clazz)) {
            throw new IllegalArgumentException(
                    "Could not find enum transfer definition for " + clazz.getName()
                            + " (Not a HbProtoBufJavaEnum class)");
        }
        try {
            return (EnumTransfer<?, ?>) javaToTransfer(clazz.asSubclass(HbProtoBufJavaEnum.class));
        } catch (final ClassCastException e) {
            // This can be removed in future versions (need some time to upgrade the interface-projects)
            throw new UnsupportedOperationException(
                    "Please update/upgrade the library that contains: " + clazz.getName(), e);
        }
    }

    /**
     * Searches for the enum transfer definition belonging to the given fully qualified proto name.
     *
     * <p>
     * This method will use the {@link Thread#getContextClassLoader() Thread's ContextClassLoader}, if
     * set. Otherwise it will fallback to this class's classloader.
     * </p>
     *
     * @param fullProtoName The fully qualified proto name.
     * @return The enum transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found or the
     *         name does not refer to an enum.
     */
    public static EnumTransfer<?, ?> toEnumTransfer(final String fullProtoName) {
        return toEnumTransfer(fullProtoName, getCurrentClassLoader());
    }

    /**
     * Searches for the enum transfer definition belonging to the given fully qualified proto name.
     *
     * @param fullProtoName The fully qualified proto name.
     * @param classLoader The classLoader to use.
     * @return The enum transfer definition for the given full proto name.
     * @throws IllegalArgumentException If the associated java/transfer class couldn't be found or the
     *         name does not refer to an enum.
     */
    public static EnumTransfer<?, ?> toEnumTransfer(final String fullProtoName, final ClassLoader classLoader) {
        try {
            return (EnumTransfer<?, ?>) toTransfer(fullProtoName, classLoader);
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException("The fully qualified proto name does not describe a enum.", e);
        }
    }

    /**
     * Helper class that encapsulates the reflection calls. Retrieves the {@link BaseTransfer} instance
     * by calling the static {@link HbProtoBufJavaBase HbProtoBufJavaBase#transfer()} method on the
     * given class.
     *
     * @param <T> The type of the java class.
     * @param clazz The pojo class to invoke the transfer method on.
     * @return The transfer definition for the given class.
     */
    @SuppressWarnings("unchecked")
    private static <T extends HbProtoBufJavaBase> BaseTransfer<?, T> javaToTransfer(final Class<T> clazz) {
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(clazz);
        if (transfer != null) {
            return (BaseTransfer<?, T>) transfer;
        }
        try {
            return (BaseTransfer<?, T>) clazz.getDeclaredMethod("transfer").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException
                | SecurityException e) {
            // Should never happen
            throw new IllegalStateException(
                    "The pojo class " + clazz.getName() + " does not meet the specifications!", e);
        }
    }

    /**
     * Searches for the generic descriptor for the given proto or java class. This method supports
     * {@link Message protobuf messages}, {@link ProtocolMessageEnum protobuf enums} and
     * {@link HbProtoBufJavaBase java pojos and enums}.
     *
     * @param clazz The class used to search for the descriptor.
     * @return The descriptor for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufJavaBase nor a
     *         protobuf class.
     */
    public static GenericDescriptor toDescriptor(final Class<?> clazz) {
        if (isJavaClass(clazz)) {
            return toTransfer(clazz).getDescriptor();
        }
        if (!isProtoClass(clazz)) {
            throw new IllegalArgumentException(
                    "Could not find descriptor for " + clazz.getName() + " (Not a protobuf class)");
        }
        return protoToDescriptor(clazz);
    }

    /**
     * Searches for the descriptor for the given proto or java pojo. This method supports {@link Message
     * protobuf messages} and {@link HbProtoBufPojo java pojos}.
     *
     * @param clazz The class used to search for the descriptor.
     * @return The descriptor for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufPojo nor a protobuf
     *         pojo.
     */
    public static Descriptor toMessageDescriptor(final Class<?> clazz) {
        if (isJavaPojo(clazz)) {
            return toPojoTransfer(clazz).getDescriptor();
        }
        if (!isProtoMessage(clazz)) {
            throw new IllegalArgumentException("Not a message class: " + clazz.getName());
        }
        return protoToDescriptor(clazz);
    }

    /**
     * Searches for the enum descriptor for the given proto or java enum. This method supports
     * {@link ProtocolMessageEnum protobuf enums} and {@link HbProtoBufJavaEnum java enums}.
     *
     * @param clazz The class used to search for the descriptor.
     * @return The descriptor for the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufJavaEnum nor a
     *         protobuf enum.
     */
    public static EnumDescriptor toEnumDescriptor(final Class<?> clazz) {
        if (isJavaEnum(clazz)) {
            try {
                return toEnumTransfer(clazz).getDescriptor();
            } catch (final UnsupportedOperationException e) {
                // This can be removed in future versions (need some time to upgrade the interface-projects)
                return toEnumDescriptor(toProtoClass(clazz));
            }
        }
        if (!isProtoEnum(clazz)) {
            throw new IllegalArgumentException("Not a enum class: " + clazz.getName());
        }
        return protoToDescriptor(clazz);
    }

    /**
     * Helper class that encapsulates the reflection calls. Retrieves the {@link GenericDescriptor}
     * instance by calling the static {@code protobuf getDescriptor()} method on the given class.
     *
     * @param <T> The type of descriptor to return.
     * @param clazz The proto class to invoke the getDescriptor method on.
     * @return The descriptor for the given class.
     */
    @SuppressWarnings("unchecked")
    private static <T extends GenericDescriptor> T protoToDescriptor(final Class<?> clazz) {
        final BaseTransfer<?, ?> transfer = ProtoTransferRegistry.searchFor(clazz);
        if (transfer != null) {
            return (T) transfer.getDescriptor();
        }
        try {
            return (T) clazz.getDeclaredMethod("getDescriptor").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException
                | SecurityException e) {
            // Should never happen
            throw new UnsupportedOperationException(
                    "The proto class " + clazz.getName() + " does not expose its descriptor as expected! "
                            + "This is likely a bug in this library.",
                    e);
        }
    }

    /**
     * Get the type's fully-qualified name, within the proto language's namespace. This differs from the
     * Java name. For example, given this {@code .proto}:
     *
     * <pre>
     *   package foo.bar;
     *   option java_package = "com.example.protos"
     *   message Baz {}
     * </pre>
     *
     * {@code Baz}'s full name is {@code "foo.bar.Baz"}.
     *
     * <p>
     * <b>Examples:</b>
     * </p>
     * <ul>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion} (generated message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceVersion$Nested} (generated nested message) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceVersion.Nested}</li>
     * <li>{@code de.heuboe.vmis2.interface.ServiceStatus} (generated proto enum) -&gt;
     * {@code de.heuboe.vmis2.interface.ServiceStatus}</li>
     * </ul>
     *
     * @param clazz The class to the full proto name for.
     * @return The full proto name of the given class.
     * @throws IllegalArgumentException If the given class is neither a HbProtoBufJavaBase nor a
     *         protobuf class.
     *
     * @see GenericDescriptor#getFullName()
     * @see Constants#HEADER_X_PROTOBUF_TYPE
     */
    public static String toFullProtoName(final Class<?> clazz) {
        return toDescriptor(clazz).getFullName();
    }

    /**
     * Gets the interface version of the proto file that specified the given proto or java class. This
     * version is usually the same as the project version, but this is not guaranteed.
     *
     * @param clazz The class to get the interface version for.
     * @return The interface version of the proto file that defines the given class.
     *
     * @see InterfaceVersionProto#interfaceVersion
     * @see Constants#HEADER_X_PROTOBUF_INTERFACEVERSION
     */
    public static String toInterfaceVersion(final Class<?> clazz) {
        final FileOptions options = toDescriptor(clazz).getFile().getOptions();
        if (options == null) {
            return null;
        } else {
            return options.getExtension(InterfaceVersionProto.interfaceVersion);
        }
    }

    /**
     * Gets the {@link ClassLoader} for the current {@link Thread}.
     *
     * @return The current class loader.
     * @see Thread#getContextClassLoader()
     * @see Class#getClassLoader()
     */
    private static ClassLoader getCurrentClassLoader() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            return ProtoPojoUtils.class.getClassLoader();
        } else {
            return classLoader;
        }
    }

    private ProtoPojoUtils() {}

}
