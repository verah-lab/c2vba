package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.vmis2.jprotoc.utils.DocumentationUtils.getCompositeTypeRef;
import static de.heuboe.vmis2.jprotoc.utils.DocumentationUtils.getExternalReference;
import static de.heuboe.vmis2.jprotoc.utils.DocumentationUtils.getStereotype;
import static de.heuboe.vmis2.jprotoc.utils.DocumentationUtils.isRequired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import de.heuboe.protobuf.ExternalReference;
import de.heuboe.protobuf.Stereotype;
import de.heuboe.vmis2.jprotoc.test.CardinalityMessage;
import de.heuboe.vmis2.jprotoc.test.ContainerMessage;
import de.heuboe.vmis2.jprotoc.test.DataTypeMessage;
import de.heuboe.vmis2.jprotoc.test.EntityMessage;
import de.heuboe.vmis2.jprotoc.test.RefMessage;

/**
 * Tests for {@link DocumentationUtils}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class DocumentationUtilsTest {

    // RefMessage
    private static final Descriptor REF_MESSAGE = RefMessage.getDescriptor();
    private static final FieldDescriptor REF_MESSAGE_VALUE =
            getField(REF_MESSAGE, RefMessage.VALUE_FIELD_NUMBER);
    private static final FieldDescriptor REF_MESSAGE_REF =
            getField(REF_MESSAGE, RefMessage.REF_FIELD_NUMBER);
    private static final FieldDescriptor REF_MESSAGE_COMPOSITEREF =
            getField(REF_MESSAGE, RefMessage.COMPOSITEREF_FIELD_NUMBER);

    // Ref
    private static final Descriptor REF = RefMessage.Ref.getDescriptor();
    private static final FieldDescriptor REF_REF =
            getField(REF, RefMessage.Ref.REF_FIELD_NUMBER);

    // CardinalityMessage
    private static final Descriptor CARDINALITY_MESSAGE = CardinalityMessage.getDescriptor();
    private static final FieldDescriptor CARDINALITY_MESSAGE_CHILDNAME =
            getField(CARDINALITY_MESSAGE, CardinalityMessage.CHILDNAME_FIELD_NUMBER);
    private static final FieldDescriptor CARDINALITY_MESSAGE_WEIGHT =
            getField(CARDINALITY_MESSAGE, CardinalityMessage.WEIGHT_FIELD_NUMBER);
    private static final FieldDescriptor CARDINALITY_MESSAGE_HINT =
            getField(CARDINALITY_MESSAGE, CardinalityMessage.HINT_FIELD_NUMBER);

    /**
     * Test for {@link DocumentationUtils#getCompositeTypeRef(Descriptor)}.
     */
    @Test
    void testGetCompositeTypeRef() {
        assertNull(getCompositeTypeRef(REF_MESSAGE));
        assertEquals(REF_MESSAGE.getFullName(), getCompositeTypeRef(REF));
    }

    /**
     * Test for {@link DocumentationUtils#getExternalReference(FieldDescriptor)}.
     */
    @Test
    void testGetExternalReference() {
        assertNull(getExternalReference(REF_MESSAGE_VALUE));
        assertEquals(ExternalReference.newBuilder()
                .setType(REF_MESSAGE.getFullName())
                .setField(REF_MESSAGE_VALUE.getName())
                .build(),
                getExternalReference(REF_MESSAGE_REF));
        assertNull(getExternalReference(REF_MESSAGE_COMPOSITEREF));

        assertEquals(ExternalReference.newBuilder()
                // .setType(REF_MESSAGE.getFullName())
                .setField(REF_MESSAGE_VALUE.getName())
                .build(),
                getExternalReference(REF_REF));
    }

    @Test
    void testGetStereotype() {
        assertEquals(Stereotype.UNKNOWN, getStereotype(REF_MESSAGE));
        assertEquals(Stereotype.UNKNOWN, getStereotype(REF));
        assertEquals(Stereotype.CONTAINER, getStereotype(ContainerMessage.getDescriptor()));
        assertEquals(Stereotype.ENTITY, getStereotype(EntityMessage.getDescriptor()));
        assertEquals(Stereotype.DATATYPE, getStereotype(DataTypeMessage.getDescriptor()));
    }

    @Test
    void testIsRequired() {
        assertEquals(true, isRequired(CARDINALITY_MESSAGE_CHILDNAME));
        assertEquals(false, isRequired(CARDINALITY_MESSAGE_WEIGHT));
        assertEquals(true, isRequired(CARDINALITY_MESSAGE_HINT));
    }

    private static FieldDescriptor getField(final Descriptor descriptor, final int fieldNumber) {
        return descriptor.getFields().stream()
                .filter(f -> f.getNumber() == fieldNumber)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find field " + fieldNumber + " in " + descriptor.getFullName()));
    }

}
