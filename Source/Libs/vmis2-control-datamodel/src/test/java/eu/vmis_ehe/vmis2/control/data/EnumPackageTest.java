package eu.vmis_ehe.vmis2.control.data;

import eu.vmis_ehe.vmis2.control.data.enum1.pojo.PEnum1;
import eu.vmis_ehe.vmis2.control.data.enum2.pojo.PEnum2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumPackageTest {


    @Test
    void enumInDifferentPackagesTest() {
        PEnum1 enum1 = PEnum1.ERROR;
        PEnum2 enum2 = PEnum2.OK;
        PEnum1 enum11 = PEnum1.OK;
        PEnum2 enum21 = PEnum2.ERROR;

        PEnum1 pEnum1 = PEnum1.from(PEnum1.to(enum1));
        PEnum1 pEnum11 = PEnum1.from(PEnum1.to(enum11));
        PEnum2 pEnum2 = PEnum2.from(PEnum2.to(enum2));
        PEnum2 pEnum21 = PEnum2.from(PEnum2.to(enum21));

        assertEquals(enum1, pEnum1);
        assertEquals(enum11, pEnum11);
        assertEquals(enum2, pEnum2);
        assertEquals(enum21, pEnum21);
    }
}
