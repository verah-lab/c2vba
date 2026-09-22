package de.heuboe.vmis2.jprotoc.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import de.heuboe.vmis2.jprotoc.helpers.PAny.PAnyException;
import de.heuboe.vmis2.jprotoc.test.TestMessage;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestMessage;

class PAnyTest {

    @Test
    void testPack() throws InvalidProtocolBufferException {
        assertNull(PAny.pack(null));

        final PTestMessage expected = new PTestMessage("test");
        final Any any = PAny.pack(expected);
        final TestMessage proto = any.unpack(TestMessage.class);
        final PTestMessage pojo = PTestMessage.from(proto);
        assertEquals(expected, pojo);
    }

    @Test
    void testUnpack() {
        assertNull(PAny.unpack(null));

        final TestMessage expected = TestMessage.getDefaultInstance();
        final Any any = Any.pack(expected);
        final PTestMessage pojo = (PTestMessage) PAny.unpack(any);
        final TestMessage proto = PTestMessage.to(pojo);
        assertEquals(expected, proto);

        final Timestamp timestamp = Timestamp.getDefaultInstance();
        final Any timestampAny = Any.pack(timestamp);
        assertThrows(PAnyException.class, () -> PAny.unpack(timestampAny));

        assertThrows(PAnyException.class, () -> PAny.unpack(Any.getDefaultInstance()));

        assertEquals(new PTestMessage(""), PAny.unpack(Any.newBuilder()
                .setTypeUrl("type.googleapis.com/de.heuboe.vmis2.jprotoc.test.TestMessage")
                .build()));
        assertThrows(PAnyException.class, () -> PAny.unpack(Any.newBuilder()
                .setValue(TestMessage.getDefaultInstance().toByteString())
                .build()));
    }

    @Test
    void testIs() {
        assertFalse(PAny.is(null, PTestMessage.class));
        assertThrows(NullPointerException.class, () -> PAny.is(Any.getDefaultInstance(), null));

        final TestMessage expected = TestMessage.getDefaultInstance();
        final Any any = Any.pack(expected);
        assertTrue(PAny.is(any, PTestMessage.class));
        assertFalse(PAny.is(any, PTestMessage.InnerMessage.class));
    }

}
