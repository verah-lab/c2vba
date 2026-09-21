package de.heuboe.vmis2.kafka.converter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Lazy}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class LazyTest {

    @Test
    void test() {
        final AtomicBoolean resolved = new AtomicBoolean(false);

        final Lazy<Boolean> lazy = new Lazy<>(() -> {
            final boolean value = !resolved.get();
            resolved.set(value);
            return value;
        });

        assertFalse(resolved.get(), "resolved too early");

        assertTrue(lazy.get(), "invalid lazy get");

        assertTrue(resolved.get(), "not resolved");

        assertTrue(lazy.get(), "duplicate lazy get");

        assertTrue(resolved.get(), "resolved twice");
    }

}
