package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.vmis2.jprotoc.utils.ProtoTransferRegistry.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Timestamp;

import de.heuboe.protobuf.pojo.PInterfaceVersionProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.test.TestEnum;
import de.heuboe.vmis2.jprotoc.test.TestMessage;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestEnum;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestMessage;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.ProtoTransferCatalog;

/**
 * Tests for {@link ProtoTransferRegistry}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoTransferRegistryTest {

    /**
     * Simple class used to test the disabling of the registry initialization.
     */
    public class TestClassLoader extends ClassLoader {

        @Override
        public Class findClass(String name) {
            byte[] b = loadClassFromFile(name);
            return defineClass(name, b, 0, b.length);
        }

        private byte[] loadClassFromFile(String fileName)  {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                    fileName.replace('.', File.separatorChar) + ".class");
            byte[] buffer;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int nextValue = 0;
            try {
                while ( (nextValue = inputStream.read()) != -1 ) {
                    byteStream.write(nextValue);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer = byteStream.toByteArray();
            return buffer;
        }
    }

    /**
     * Tests the disabling of the initialization.
     */
    @Test
    void testDeactivationOfInitialization()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.setProperty(DISABLE_INITIALIZATION_PROPERTY, "true");

        TestClassLoader testClassLoader = new TestClassLoader();

        Class<?> protoTransferRegistryClass  = testClassLoader.findClass("de.heuboe.vmis2.jprotoc.utils.ProtoTransferRegistry");

        assertTrue(((Collection) protoTransferRegistryClass.getMethod("getCatalogs").invoke(null)).isEmpty());
        assertTrue(((Collection) protoTransferRegistryClass.getMethod("getTransferDefintions").invoke(null)).isEmpty());
        assertTrue(((Collection) protoTransferRegistryClass.getMethod("getCatalogs").invoke(null)).isEmpty());

        System.setProperty(DISABLE_INITIALIZATION_PROPERTY, "false");
    }

    /**
     * Tests {@link ProtoTransferRegistry#getCatalogs()}.
     */
    @Test
    void testGetCatalogs() {
        final Set<ProtoTransferCatalog> catalogs = getCatalogs();
        assertTrue(catalogs.size() == 2);
        assertThat(catalogs)
                .containsExactlyInAnyOrder(
                        new PTestProtoTransferCatalog(),
                        new PInterfaceVersionProtoTransferCatalog());
    }

    /**
     * Tests {@link ProtoTransferRegistry#getTransferDefintions()}.
     */
    @Test
    void testGetTransferDefintions() {
        final Set<BaseTransfer<?, ?>> transfers = getTransferDefintions();
        assertTrue(transfers.size() == 3);
        assertThat(transfers)
                .containsExactlyInAnyOrder(
                        PTestEnum.transfer(),
                        PTestMessage.transfer(),
                        PTestMessage.InnerMessage.transfer());
    }

    /**
     * Tests {@link ProtoTransferRegistry#searchFor(GenericDescriptor)}.
     */
    @Test
    void testSearchForDescriptor() {
        assertNull(searchFor((GenericDescriptor) null));

        assertEquals(PTestEnum.transfer(), searchFor(TestEnum.getDescriptor()));
        assertEquals(PTestEnum.transfer(), searchFor(PTestEnum.transfer().getDescriptor()));
        assertEquals(PTestMessage.transfer(), searchFor(TestMessage.getDescriptor()));
        assertEquals(PTestMessage.transfer(), searchFor(PTestMessage.transfer().getDescriptor()));
        assertEquals(PTestMessage.InnerMessage.transfer(), searchFor(TestMessage.InnerMessage.getDescriptor()));
        assertEquals(PTestMessage.InnerMessage.transfer(),
                searchFor(PTestMessage.InnerMessage.transfer().getDescriptor()));

        for (final BaseTransfer<?, ?> transfer : getTransferDefintions()) {
            assertSame(transfer, searchFor(transfer.getDescriptor()));
        }

        assertNull(searchFor(Timestamp.getDescriptor()));
    }

    /**
     * Tests {@link ProtoTransferRegistry#searchFor(String)}.
     */
    @Test
    void testSearchForName() {
        assertNull(searchFor((String) null));

        assertEquals(PTestEnum.transfer(), searchFor(TestEnum.getDescriptor().getFullName()));
        assertEquals(PTestEnum.transfer(), searchFor(PTestEnum.transfer().getFullProtoName()));
        assertEquals(PTestMessage.transfer(), searchFor(PTestMessage.transfer().getFullProtoName()));
        assertEquals(PTestMessage.transfer(), searchFor(TestMessage.getDescriptor().getFullName()));
        assertEquals(PTestMessage.InnerMessage.transfer(),
                searchFor(TestMessage.InnerMessage.getDescriptor().getFullName()));
        assertEquals(PTestMessage.InnerMessage.transfer(),
                searchFor(PTestMessage.InnerMessage.transfer().getFullProtoName()));

        for (final BaseTransfer<?, ?> transfer : getTransferDefintions()) {
            assertSame(transfer, searchFor(transfer.getFullProtoName()));
        }

        assertNull(searchFor(Timestamp.getDescriptor().getFullName()));
    }

    /**
     * Tests {@link ProtoTransferRegistry#searchFor(Class)}.
     */
    @Test
    void testSearchForClass() {
        assertNull(searchFor((Class<?>) null));

        assertEquals(PTestEnum.transfer(), searchFor(TestEnum.class));
        assertEquals(PTestEnum.transfer(), searchFor(PTestEnum.class));
        assertEquals(PTestMessage.transfer(), searchFor(TestMessage.class));
        assertEquals(PTestMessage.transfer(), searchFor(PTestMessage.class));
        assertEquals(PTestMessage.InnerMessage.transfer(), searchFor(TestMessage.InnerMessage.class));
        assertEquals(PTestMessage.InnerMessage.transfer(), searchFor(PTestMessage.InnerMessage.class));

        for (final BaseTransfer<?, ?> transfer : getTransferDefintions()) {
            assertSame(transfer, searchFor(transfer.getClass()));
            assertSame(transfer, searchFor(transfer.protoClass()));
            assertSame(transfer, searchFor(transfer.pojoClass()));
        }

        assertNull(searchFor(Timestamp.class));
    }

}
