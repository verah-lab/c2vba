package de.heuboe.protobuf;

import static de.heuboe.protobuf.DocumentationProto.compositeTypeRef;
import static de.heuboe.protobuf.DocumentationProto.extRef;
import static de.heuboe.vmis2.jprotoc.test.RefMessage.COMPOSITEREF_FIELD_NUMBER;
import static de.heuboe.vmis2.jprotoc.test.RefMessage.REF_FIELD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import de.heuboe.vmis2.jprotoc.test.RefMessage;
import de.heuboe.vmis2.jprotoc.test.RefMessage.Ref;

public class ExternalReferenceTest {

    private static final Descriptor TYPE = RefMessage.getDescriptor();
    private static final FieldDescriptor REF_FIELD = TYPE.findFieldByNumber(REF_FIELD_NUMBER);
    private static final FieldDescriptor COMPOSITE_REF_FIELD = TYPE.findFieldByNumber(COMPOSITEREF_FIELD_NUMBER);
    private static final Descriptor COMPOSITE_REF_TYPE = COMPOSITE_REF_FIELD.getMessageType();
    private static final FieldDescriptor COMPOSITE_REF_TYPE_REF_FIELD =
            COMPOSITE_REF_TYPE.findFieldByNumber(Ref.REF_FIELD_NUMBER);

    @Test
    void testSimple() {
        final ExternalReference extRefValue = REF_FIELD.getOptions().getExtension(extRef);
        assertNotNull(extRefValue);
        assertEquals(TYPE.getFullName(), extRefValue.getType());
        assertEquals("value", extRefValue.getField());
    }

    @Test
    void testComposite() {
        final String typeRefValue = COMPOSITE_REF_TYPE.getOptions().getExtension(compositeTypeRef);
        assertEquals(TYPE.getFullName(), typeRefValue);
        final ExternalReference extRefValue = COMPOSITE_REF_TYPE_REF_FIELD.getOptions().getExtension(extRef);
        assertNotNull(extRefValue);
        assertEquals("value", extRefValue.getField());
    }

}
