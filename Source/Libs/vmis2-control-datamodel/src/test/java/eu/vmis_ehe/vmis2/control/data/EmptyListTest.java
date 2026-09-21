package eu.vmis_ehe.vmis2.control.data;

import eu.vmis_ehe.vmis2.control.data.pojo.PConcreteControlSign;
import eu.vmis_ehe.vmis2.control.data.pojo.PSection;
import eu.vmis_ehe.vmis2.control.data.pojo.PSituationClass;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class EmptyListTest {

    @Test
    void emptyListTest() {
        PSituationClass sc = PSituationClass.builder().build();
        assertTrue(sc.getSubClassesList().isEmpty());
    }

    @Test
    void equalsTest() {
        PSituationClass sc = PSituationClass.builder().build();
        SituationClass situationClass = PSituationClass.to(sc);
        assertFalse(situationClass.hasTwoPartId());
        PSituationClass sc2 = PSituationClass.from(situationClass);
//        assertEquals(sc, sc2);
    }

  /*  @Test
    void testOneOf() {
        //PConcreteControlSign
        List<PValiditySection> pValiditySections = new ArrayList<>();
        pValiditySections.add(PValiditySection.builder().logKmTo(1).logKmFrom(2).roadId("A23_1").build());
        PConcreteControlSign pCCS =
                PConcreteControlSign.builder().sections(PSection.builder().sectionsList(pValiditySections).build()).
                        aqId("AQ_A23_1_075_F1.A").build();
        assertEquals("AQ_A23_1_075_F1.A", pCCS.getAqId());
        assertEquals(pValiditySections.get(0), pCCS.getSections().getSectionsList().get(0));

        //ConcreteControlSign
        ValiditySection section = ValiditySection.newBuilder().setLogKmFrom(1).setLogKmTo(2).setRoadId("A23_1").build();
        ConcreteControlSign ccs = ConcreteControlSign.newBuilder().setAqId("AQ_A23_1_075_F1.A")
                .setSections(Section.newBuilder().addSections(section).build()).build();

        assertEquals("", ccs.getAqId());
        assertEquals(section, ccs.getSections().getSectionsList().get(0));

    }*/
}
