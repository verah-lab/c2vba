package de.heuboe.vmis2.kafka.converter;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

/**
 * Container for lazy deserialized kafka data.
 *
 * @param <T> The type of the deserialized payload.
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class Lazy<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private T value;

    /**
     * Creates a Lazy instance.
     *
     * @param supplier The supplier used to deserialize the actual value on demand.
     */
    Lazy(final Supplier<T> supplier) {
        this.supplier = requireNonNull(supplier, "supplier");
    }

    /**
     * Gets the deserialized kafka data. The data are deserialized on first call.
     *
     * @return The deserialized data.
     */
    @Override
    public T get() {
        if (this.supplier != null) {
            this.value = this.supplier.get();
            this.supplier = null;
        }
        return this.value;
    }

}
