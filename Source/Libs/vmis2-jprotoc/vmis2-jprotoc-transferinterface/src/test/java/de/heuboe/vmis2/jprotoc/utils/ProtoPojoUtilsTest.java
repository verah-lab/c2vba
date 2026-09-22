package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isJavaClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isJavaEnum;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isJavaPojo;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isProtoClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isProtoEnum;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isProtoMessage;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toDescriptor;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toEnumDescriptor;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toEnumTransfer;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toFullProtoName;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toJavaClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toMessageDescriptor;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toPojoTransfer;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toProtoClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toTransfer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Syntax;
import com.google.protobuf.Timestamp;

import de.heuboe.vmis2.jprotoc.test.TestEnum;
import de.heuboe.vmis2.jprotoc.test.TestMessage;
import de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestEnum;
import de.heuboe.vmis2.jprotoc.test.pojo.PTestMessage;

class ProtoPojoUtilsTest {

    // Proto
    private static final Class<TestMessage> PROTO_MESSAGE = TestMessage.class;
    private static final Class<InnerMessage> PROTO_INNER_MESSAGE = TestMessage.InnerMessage.class;
    private static final Class<TestEnum> PROTO_ENUM = TestEnum.class;
    private static final Class<Timestamp> PROTO_TIMESTAMP = Timestamp.class;
    private static final Class<Syntax> PROTO_SYNTAX = Syntax.class;
    // Java
    private static final Class<PTestMessage> JAVA_POJO = PTestMessage.class;
    private static final Class<PTestMessage.InnerMessage> JAVA_INNER_POJO = PTestMessage.InnerMessage.class;
    private static final Class<PTestEnum> JAVA_ENUM = PTestEnum.class;
    private static final Class<String> JAVA_STRING = String.class;
    // Transfer
    private static final PTestMessage.Transfer TRANSFER_MESSAGE = PTestMessage.transfer();
    private static final PTestMessage.InnerMessage.Transfer TRANSFER_INNER_MESSAGE =
            PTestMessage.InnerMessage.transfer();
    private static final PTestEnum.Transfer TRANSFER_ENUM = PTestEnum.transfer();
    // Descriptor
    private static final Descriptor DESCRIPTOR_MESSAGE = TestMessage.getDescriptor();
    private static final Descriptor DESCRIPTOR_INNER_MESSAGE = TestMessage.InnerMessage.getDescriptor();
    private static final EnumDescriptor DESCRIPTOR_ENUM = TestEnum.getDescriptor();
    private static final Descriptor DESCRIPTOR_TIMESTAMP = Timestamp.getDescriptor();
    private static final EnumDescriptor DESCRIPTOR_SYNTAX = Syntax.getDescriptor();
    // Name
    private static final String NAME_MESSAGE = DESCRIPTOR_MESSAGE.getFullName();
    private static final String NAME_INNER_MESSAGE = DESCRIPTOR_INNER_MESSAGE.getFullName();
    private static final String NAME_ENUM = DESCRIPTOR_ENUM.getFullName();
    private static final String NAME_TIMESTAMP = DESCRIPTOR_TIMESTAMP.getFullName();
    private static final String NAME_SYNTAX = DESCRIPTOR_SYNTAX.getFullName();

    @Test
    void testIsProtoClass() {
        assertTrue(isProtoClass(PROTO_MESSAGE));
        assertTrue(isProtoClass(PROTO_INNER_MESSAGE));
        assertTrue(isProtoClass(PROTO_ENUM));
        assertFalse(isProtoClass(JAVA_POJO));
        assertFalse(isProtoClass(JAVA_INNER_POJO));
        assertFalse(isProtoClass(JAVA_ENUM));
        assertTrue(isProtoClass(PROTO_TIMESTAMP));
        assertTrue(isProtoClass(PROTO_SYNTAX));
        assertFalse(isProtoClass(JAVA_STRING));
    }

    @Test
    void testIsProtoMessage() {
        assertTrue(isProtoMessage(PROTO_MESSAGE));
        assertTrue(isProtoMessage(PROTO_INNER_MESSAGE));
        assertFalse(isProtoMessage(PROTO_ENUM));
        assertFalse(isProtoMessage(JAVA_POJO));
        assertFalse(isProtoMessage(JAVA_INNER_POJO));
        assertFalse(isProtoMessage(JAVA_ENUM));
        assertTrue(isProtoMessage(PROTO_TIMESTAMP));
        assertFalse(isProtoMessage(PROTO_SYNTAX));
        assertFalse(isProtoMessage(JAVA_STRING));
    }

    @Test
    void testIsProtoEnum() {
        assertFalse(isProtoEnum(PROTO_MESSAGE));
        assertFalse(isProtoEnum(PROTO_INNER_MESSAGE));
        assertTrue(isProtoEnum(PROTO_ENUM));
        assertFalse(isProtoEnum(JAVA_POJO));
        assertFalse(isProtoEnum(JAVA_INNER_POJO));
        assertFalse(isProtoEnum(JAVA_ENUM));
        assertFalse(isProtoEnum(PROTO_TIMESTAMP));
        assertTrue(isProtoEnum(PROTO_SYNTAX));
        assertFalse(isProtoEnum(JAVA_STRING));
    }

    @Test
    void testIsJavaClass() {
        assertFalse(isJavaClass(PROTO_MESSAGE));
        assertFalse(isJavaClass(PROTO_INNER_MESSAGE));
        assertFalse(isJavaClass(PROTO_ENUM));
        assertTrue(isJavaClass(JAVA_POJO));
        assertTrue(isJavaClass(JAVA_INNER_POJO));
        assertTrue(isJavaClass(JAVA_ENUM));
        assertFalse(isJavaClass(PROTO_TIMESTAMP));
        assertFalse(isJavaClass(PROTO_SYNTAX));
        assertFalse(isJavaClass(JAVA_STRING));
    }

    @Test
    void testIsJavaPojo() {
        assertFalse(isJavaPojo(PROTO_MESSAGE));
        assertFalse(isJavaPojo(PROTO_INNER_MESSAGE));
        assertFalse(isJavaPojo(PROTO_ENUM));
        assertTrue(isJavaPojo(JAVA_POJO));
        assertTrue(isJavaPojo(JAVA_INNER_POJO));
        assertFalse(isJavaPojo(JAVA_ENUM));
        assertFalse(isJavaPojo(PROTO_TIMESTAMP));
        assertFalse(isJavaPojo(PROTO_SYNTAX));
        assertFalse(isJavaPojo(JAVA_STRING));
    }

    @Test
    void testIsJavaEnum() {
        assertFalse(isJavaEnum(PROTO_MESSAGE));
        assertFalse(isJavaEnum(PROTO_INNER_MESSAGE));
        assertFalse(isJavaEnum(PROTO_ENUM));
        assertFalse(isJavaEnum(JAVA_POJO));
        assertFalse(isJavaEnum(JAVA_INNER_POJO));
        assertTrue(isJavaEnum(JAVA_ENUM));
        assertFalse(isJavaEnum(PROTO_TIMESTAMP));
        assertFalse(isJavaEnum(PROTO_SYNTAX));
        assertFalse(isJavaEnum(JAVA_STRING));
    }

    @Test
    void testToPojoClass() throws ClassNotFoundException {
        assertEquals(JAVA_POJO, toJavaClass(PROTO_MESSAGE));
        assertEquals(JAVA_INNER_POJO, toJavaClass(PROTO_INNER_MESSAGE));
        assertEquals(JAVA_ENUM, toJavaClass(PROTO_ENUM));
        assertEquals(JAVA_POJO, toJavaClass(JAVA_POJO));
        assertEquals(JAVA_INNER_POJO, toJavaClass(JAVA_INNER_POJO));
        assertEquals(JAVA_ENUM, toJavaClass(JAVA_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toJavaClass(PROTO_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toJavaClass(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toJavaClass(JAVA_STRING));
    }

    @Test
    void testToPojoClassByFullName() throws ClassNotFoundException {
        assertEquals(JAVA_POJO, toJavaClass(NAME_MESSAGE));
        assertEquals(JAVA_INNER_POJO, toJavaClass(NAME_INNER_MESSAGE));
        assertEquals(JAVA_ENUM, toJavaClass(NAME_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toJavaClass(NAME_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toJavaClass(NAME_SYNTAX));
    }

    @Test
    void testToProtoClass() {
        assertEquals(PROTO_MESSAGE, toProtoClass(PROTO_MESSAGE));
        assertEquals(PROTO_INNER_MESSAGE, toProtoClass(PROTO_INNER_MESSAGE));
        assertEquals(PROTO_ENUM, toProtoClass(PROTO_ENUM));
        assertEquals(PROTO_MESSAGE, toProtoClass(JAVA_POJO));
        assertEquals(PROTO_INNER_MESSAGE, toProtoClass(JAVA_INNER_POJO));
        assertEquals(PROTO_ENUM, toProtoClass(JAVA_ENUM));
        assertEquals(PROTO_TIMESTAMP, toProtoClass(PROTO_TIMESTAMP));
        assertEquals(PROTO_SYNTAX, toProtoClass(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toProtoClass(JAVA_STRING));
    }

    @Test
    void testToProtoClassByFullName() throws ClassNotFoundException {
        assertEquals(PROTO_MESSAGE, toProtoClass(NAME_MESSAGE));
        assertEquals(PROTO_INNER_MESSAGE, toProtoClass(NAME_INNER_MESSAGE));
        assertEquals(PROTO_ENUM, toProtoClass(NAME_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toProtoClass(NAME_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toProtoClass(NAME_SYNTAX));
    }

    @Test
    void testToTransfer() {
        assertEquals(TRANSFER_MESSAGE, toTransfer(PROTO_MESSAGE));
        assertEquals(TRANSFER_INNER_MESSAGE, toTransfer(PROTO_INNER_MESSAGE));
        assertEquals(TRANSFER_ENUM, toTransfer(PROTO_ENUM));
        assertEquals(TRANSFER_MESSAGE, toTransfer(JAVA_POJO));
        assertEquals(TRANSFER_INNER_MESSAGE, toTransfer(JAVA_INNER_POJO));
        assertEquals(TRANSFER_ENUM, toTransfer(JAVA_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toTransfer(PROTO_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toTransfer(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toTransfer(JAVA_STRING));
    }

    @Test
    void testToPojoTransfer() {
        assertEquals(TRANSFER_MESSAGE, toPojoTransfer(PROTO_MESSAGE));
        assertEquals(TRANSFER_INNER_MESSAGE, toPojoTransfer(PROTO_INNER_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> toPojoTransfer(PROTO_ENUM));
        assertEquals(TRANSFER_MESSAGE, toPojoTransfer(JAVA_POJO));
        assertEquals(TRANSFER_INNER_MESSAGE, toPojoTransfer(JAVA_INNER_POJO));
        assertThrows(IllegalArgumentException.class, () -> toPojoTransfer(JAVA_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toPojoTransfer(PROTO_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toPojoTransfer(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toPojoTransfer(JAVA_STRING));
    }

    @Test
    void testToEmumTransfer() {
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(PROTO_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(PROTO_INNER_MESSAGE));
        assertEquals(TRANSFER_ENUM, toEnumTransfer(PROTO_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(JAVA_POJO));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(JAVA_INNER_POJO));
        assertEquals(TRANSFER_ENUM, toEnumTransfer(JAVA_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(PROTO_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toEnumTransfer(JAVA_STRING));
    }

    @Test
    void testToDescriptor() {
        assertEquals(DESCRIPTOR_MESSAGE, toDescriptor(PROTO_MESSAGE));
        assertEquals(DESCRIPTOR_INNER_MESSAGE, toDescriptor(PROTO_INNER_MESSAGE));
        assertEquals(DESCRIPTOR_ENUM, toDescriptor(PROTO_ENUM));
        assertEquals(DESCRIPTOR_MESSAGE, toDescriptor(JAVA_POJO));
        assertEquals(DESCRIPTOR_INNER_MESSAGE, toDescriptor(JAVA_INNER_POJO));
        assertEquals(DESCRIPTOR_ENUM, toDescriptor(JAVA_ENUM));
        assertEquals(DESCRIPTOR_TIMESTAMP, toDescriptor(PROTO_TIMESTAMP));
        assertEquals(DESCRIPTOR_SYNTAX, toDescriptor(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toDescriptor(JAVA_STRING));
    }

    @Test
    void testToMessageDescriptor() {
        assertEquals(DESCRIPTOR_MESSAGE, toMessageDescriptor(PROTO_MESSAGE));
        assertEquals(DESCRIPTOR_INNER_MESSAGE, toMessageDescriptor(PROTO_INNER_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> toMessageDescriptor(PROTO_ENUM));
        assertEquals(DESCRIPTOR_MESSAGE, toMessageDescriptor(JAVA_POJO));
        assertEquals(DESCRIPTOR_INNER_MESSAGE, toMessageDescriptor(JAVA_INNER_POJO));
        assertThrows(IllegalArgumentException.class, () -> toMessageDescriptor(JAVA_ENUM));
        assertEquals(DESCRIPTOR_TIMESTAMP, toMessageDescriptor(PROTO_TIMESTAMP));
        assertThrows(IllegalArgumentException.class, () -> toMessageDescriptor(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toMessageDescriptor(JAVA_STRING));
    }

    @Test
    void testToEnumDescriptor() {
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(PROTO_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(PROTO_INNER_MESSAGE));
        assertEquals(DESCRIPTOR_ENUM, toEnumDescriptor(PROTO_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(JAVA_POJO));
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(JAVA_INNER_POJO));
        assertEquals(DESCRIPTOR_ENUM, toEnumDescriptor(JAVA_ENUM));
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(PROTO_TIMESTAMP));
        assertEquals(DESCRIPTOR_SYNTAX, toEnumDescriptor(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toEnumDescriptor(JAVA_STRING));
    }

    @Test
    void testGetFullProtoName() {
        assertEquals(NAME_MESSAGE, toFullProtoName(PROTO_MESSAGE));
        assertEquals(NAME_INNER_MESSAGE, toFullProtoName(PROTO_INNER_MESSAGE));
        assertEquals(NAME_ENUM, toFullProtoName(PROTO_ENUM));
        assertEquals(NAME_MESSAGE, toFullProtoName(JAVA_POJO));
        assertEquals(NAME_INNER_MESSAGE, toFullProtoName(JAVA_INNER_POJO));
        assertEquals(NAME_ENUM, toFullProtoName(JAVA_ENUM));
        assertEquals(NAME_TIMESTAMP, toFullProtoName(PROTO_TIMESTAMP));
        assertEquals(NAME_SYNTAX, toFullProtoName(PROTO_SYNTAX));
        assertThrows(IllegalArgumentException.class, () -> toFullProtoName(JAVA_STRING));
    }

}
