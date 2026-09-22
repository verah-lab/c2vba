package de.heuboe.vmis2.jprotoc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void test() {
        final Instant now = Instant.now();
        final Instant converted = DateUtils.toInstant(DateUtils.fromInstantUtc(now));
        assertEquals(now, converted);

        assertNull(DateUtils.fromInstantUtc(null));
        assertNull(DateUtils.toInstant(null));
    }

}
