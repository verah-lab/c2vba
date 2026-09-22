package de.heuboe.vmis2.jprotoc.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.GenericDescriptor;

import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.ProtoTransferCatalog;

/**
 * A registry for all {@link BaseTransfer} instances. This class relies on
 * {@link ProtoTransferCatalog}s that are loaded via java's {@link ServiceLoader}.
 *
 * <p>
 * For generic lookups its recommended to use the {@link ProtoPojoUtils} instead.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class ProtoTransferRegistry {

    /**
     * Property name for disabling the initialization.
     */
    public static final String DISABLE_INITIALIZATION_PROPERTY =
            "proto.transfer.registry.initialization.disabled";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoTransferRegistry.class);

    private static final Set<ProtoTransferCatalog> catalogs = new LinkedHashSet<>();
    private static final Set<BaseTransfer<?, ?>> transfers = new LinkedHashSet<>();
    private static final Map<String, BaseTransfer<?, ?>> transferByFullProtoName = new LinkedHashMap<>();
    private static final Map<Class<?>, BaseTransfer<?, ?>> transferByClass = new LinkedHashMap<>();

    static {
        if(!Boolean.parseBoolean(System.getProperty(DISABLE_INITIALIZATION_PROPERTY))) {
            // Initialize registry with all known catalogs
            try {
                scan(ClassLoader.getSystemClassLoader());
            } catch (final RuntimeException e) {
                throw new IllegalStateException(
                        "Failed to initialize ProtoTransferRegistry using the SystemClassLoader", e);
            }
        }
    }

    /**
     * Find all {@link ProtoTransferCatalog}s via java's {@link ServiceLoader}.
     *
     * @param classLoader The classloader used to load the catalog.
     * @return An iterable with the found catalogs.
     */
    private static Iterable<ProtoTransferCatalog> findCatalog(final ClassLoader classLoader) {
        return ServiceLoader.load(ProtoTransferCatalog.class, classLoader);
    }

    /**
     * Adds the given catalog type to the registry. This method should only be called during early
     * application startup.
     *
     * @param catalog The catalog to add.
     */
    public static synchronized void add(final ProtoTransferCatalog catalog) {
        if (catalogs.add(catalog)) {
            process(catalog);
        }
    }

    /**
     * Scans the given classloader for {@link ProtoTransferCatalog} using java's {@link ServiceLoader}.
     *
     * @param classLoader The classloader to use for the scan.
     * @throws IllegalStateException If the processing failed for some reason. Usually incompatible
     *         dependencies.
     */
    public static synchronized void scan(final ClassLoader classLoader) {
        IllegalStateException exception = null;
        for (final ProtoTransferCatalog registration : findCatalog(classLoader)) {
            try {
                add(registration);
            } catch (final RuntimeException e) {
                if (exception == null) {
                    exception = new IllegalStateException(
                            "Failed to scan classloader " + classLoader + " for ProtoTransfer instances.\n"
                                    + "Please check the cause, your pom and the failing ProtoTransferCatalog for hints.",
                            e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Processes the given {@link ProtoTransferCatalog} and adds it transfer definition to this
     * registry.
     *
     * @param catalog The catalog to process.
     * @throws IllegalStateException If the processing failed for some reason. Usually incompatible
     *         dependencies.
     */
    private static void process(final ProtoTransferCatalog catalog) {
        try {
            for (final BaseTransfer<?, ?> transfer : catalog.getTransfers()) {
                transfers.add(transfer);
                if (transferByFullProtoName.put(transfer.getDescriptor().getFullName(), transfer) != null) {
                    // I hope this never happens...
                    LOGGER.warn(
                            "Duplicate Transfer-Registration (Name: {}, Proto: {}, Pojo: {}, Catalog: {})"
                                    + " - This might indicate that the proto name is not unique (present in two artifacts/jars).",
                            transfer.getDescriptor().getFullName(), transfer.protoClass(), transfer.pojoClass(),
                            catalog);
                }
                transferByClass.put(transfer.getClass(), transfer);
                transferByClass.put(transfer.protoClass(), transfer);
                transferByClass.put(transfer.pojoClass(), transfer);
            }
        } catch (final NoClassDefFoundError | ExceptionInInitializerError e) {
            throw new IllegalStateException("Failed to process " + catalog + "\n"
                    + "You are either missing a dependency or there is a dependency conflict!", e);
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to process " + catalog, e);
        }
    }

    /**
     * Gets all registered catalogs.
     *
     * @return An unmodifiable set with the registered catalogs.
     */
    public static Set<ProtoTransferCatalog> getCatalogs() {
        return Collections.unmodifiableSet(catalogs);
    }

    /**
     * Gets all registered {@link BaseTransfer transfer definitions}.
     *
     * @return An unmodifiable set with the registered transfer definitions.
     */
    public static Set<BaseTransfer<?, ?>> getTransferDefintions() {
        return Collections.unmodifiableSet(transfers);
    }

    /**
     * Searches the {@link BaseTransfer transfer definitions} using the full proto name.
     *
     * <p>
     * Calling this method is equivalent to calling {@code searchFor(descriptor.getFullName())}.
     * </p>
     *
     * @param descriptor The descriptor to search for.
     * @return The transfer instance for the given descriptor, or null if no matching transfer
     *         definition was found.
     * @see #searchFor(String)
     */
    public static BaseTransfer<?, ?> searchFor(final GenericDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        return transferByFullProtoName.get(descriptor.getFullName());
    }

    /**
     * Searches the {@link BaseTransfer transfer definitions} using the full proto name.
     *
     * @param fullProtoName The full proto name to search for.
     * @return The transfer instance for the given name, or null if no matching transfer definition was
     *         found.
     */
    public static BaseTransfer<?, ?> searchFor(final String fullProtoName) {
        return transferByFullProtoName.get(fullProtoName);
    }

    /**
     * Searches the {@link BaseTransfer transfer definitions} using the given class. Supports the proto,
     * java (pojo) and transfer definition classes.
     *
     * @param clazz The class to search for.
     * @return The transfer instance for the given class, or null if no matching transfer definition was
     *         found.
     */
    public static BaseTransfer<?, ?> searchFor(final Class<?> clazz) {
        return transferByClass.get(clazz);
    }

    private ProtoTransferRegistry() {}

}
