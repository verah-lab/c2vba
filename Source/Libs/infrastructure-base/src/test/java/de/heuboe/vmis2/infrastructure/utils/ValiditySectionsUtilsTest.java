package de.heuboe.vmis2.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import eu.vmis_ehe.vmis2.geomanager.validitySection.pojo.PSection;
import org.junit.jupiter.api.Test;
import de.heuboe.asfinag.vmis2.infrastructure.utils.ValiditySectionsUtils;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;

public class ValiditySectionsUtilsTest {
    private final double EPSILON = 0.0000001;
    private final String roadId = "S01_1";
    
    @Test
    public void PValiditySectionContainsKm_kmAtBegin_true() {
        double fromKm = 11.7;
        PValiditySection section =  PValiditySection.builder().logKmFrom(fromKm).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build();
        assertTrue(ValiditySectionsUtils.sectionContainsKm(section, roadId, fromKm));   
    }
    
    @Test
    public void PValiditySectionContainsKm_kmAtEnd_false() {
        double endKm = 12.3;
        PValiditySection section =  PValiditySection.builder().logKmFrom(11.7).logKmTo(endKm).roadId(roadId).section(PSection.builder().build()).build();
        assertFalse(ValiditySectionsUtils.sectionContainsKm(section, roadId, endKm));       
    }
    
    @Test
    public void PValiditySectionContainsKm_kmBetween_true() {
        double endKm = 12.3;
        PValiditySection section =  PValiditySection.builder().logKmFrom(11.7).logKmTo(endKm).roadId(roadId).section(PSection.builder().build()).build();
        assertTrue(ValiditySectionsUtils.sectionContainsKm(section, roadId, endKm - EPSILON));  
    }

    @Test
    public void PValiditySectionContainsKm_kmBeforeBegin_false() {
        double fromKm = 11.7;
        PValiditySection section =  PValiditySection.builder().logKmFrom(fromKm).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build();
        assertFalse(ValiditySectionsUtils.sectionContainsKm(section, roadId, fromKm - EPSILON));       
    }

    @Test
    public void comparePValiditySection_roadId1GtRoadId2_1() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(10).logKmTo(12.3).roadId("S01_2").section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(21).logKmTo(23.3).roadId("S01_1").section(PSection.builder().build()).build();
        assertEquals(1, ValiditySectionsUtils.compareValiditySection(section1, section2));    
    }

    @Test
    public void comparePValiditySection_roadId2GtRoadId1_n1() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(21).logKmTo(23.3).roadId("S01_1").section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(12.3).roadId("S01_2").section(PSection.builder().build()).build();
        assertEquals(-1, ValiditySectionsUtils.compareValiditySection(section1, section2));    
    }   
    
    @Test
    public void comparePValiditySection_kmFrom1GtKmFrom2_1() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(10+EPSILON).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build();
        assertEquals(1, ValiditySectionsUtils.compareValiditySection(section1, section2));    
    }   
    
    @Test
    public void comparePValiditySection_kmFrom2GtKmFrom1_n1() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(10).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10+EPSILON).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        assertEquals(-1, ValiditySectionsUtils.compareValiditySection(section1, section2));    
    }  
    
    @Test
    public void comparePValiditySection_section1EqualsSection2_0() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(10).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        assertEquals(0, ValiditySectionsUtils.compareValiditySection(section1, section2));    
    }  
    
    @Test
    public void findSectionWithKm_kmOnSection1_section1() {  
        double startKmSection1 = 10;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKmSection1).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(12).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        Optional<PValiditySection> optional = ValiditySectionsUtils.findSectionWithKm(sections, roadId, startKmSection1);
        assertTrue(optional.isPresent());
        assertEquals(section1, optional.get());  
    }  
    
    @Test
    public void findSectionWithKm_kmOnSection2_section2() {  
        double startKmSection = 10;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKmSection).logKmTo(11.3).roadId("S01_2").section(PSection.builder().build()).build(); //different road
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(startKmSection).logKmTo(11.3).roadId("S01_1").section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);
        
        Optional<PValiditySection> optional = ValiditySectionsUtils.findSectionWithKm(sections, roadId, startKmSection);
        assertTrue(optional.isPresent());
        assertEquals(section2, optional.get());    
    }  
    
    @Test
    public void findSectionWithKm_kmOutsideAllSections_empty() {  
        double kmOutside = 10;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(kmOutside+EPSILON).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build(); //after kmOutside
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(9).logKmTo(kmOutside).roadId(roadId).section(PSection.builder().build()).build(); //before kmOutside
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);

        Optional<PValiditySection> optional = ValiditySectionsUtils.findSectionWithKm(sections, roadId, kmOutside);  
        assertFalse(optional.isPresent());  
    }  
    
    @Test
    public void findSectionWithKm_kmOnAllSections_section1() {  
        double startKm = 10;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKm).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(startKm).logKmTo(12.3).roadId(roadId).section(PSection.builder().build()).build(); //contains also startKm
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        // only return first one
        Optional<PValiditySection> optional = ValiditySectionsUtils.findSectionWithKm(sections, roadId, startKm);
        assertTrue(optional.isPresent());
        assertEquals(section1, optional.get());  
    }  
    
    @Test
    public void findFirstSectionBetween_noneBetween_empty() {  
        double startKm = 10;
        double endKm = 15;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKm - EPSILON).logKmTo(11.3).roadId(roadId).section(PSection.builder().build()).build(); // begin outside
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(endKm).logKmTo(endKm + EPSILON).roadId(roadId).section(PSection.builder().build()).build(); // begin after section
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        Optional<PValiditySection> optional = ValiditySectionsUtils.findFirstSectionBetween(sections, roadId, startKm, endKm);
        assertFalse(optional.isPresent());  
    }  

    @Test
    public void findFirstSectionBetween_differentRoad_empty() {  
        double startKm = 10;
        double endKm = 15;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKm).logKmTo(endKm).roadId("S01_2").section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        Optional<PValiditySection> optional = ValiditySectionsUtils.findFirstSectionBetween(sections, "S01_1", startKm, endKm);
        assertFalse(optional.isPresent());  
    }  
    
    
    @Test
    public void findFirstSectionBetween_beginBetween_newSection() {  
        double startKm = 10;
        double endKm = 15;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(endKm-EPSILON).logKmTo(endKm+EPSILON).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        // only return first one
        Optional<PValiditySection> optional = ValiditySectionsUtils.findFirstSectionBetween(sections, roadId, startKm, endKm);
        assertTrue(optional.isPresent());
        assertEquals(section1.getLogKmFrom(), optional.get().getLogKmFrom()); 
        assertEquals(endKm, optional.get().getLogKmTo()); 
    }  
    
    @Test
    public void findFirstSectionBetween_bothSectionsBetween_section1() {  
        double startKm = 10;
        double endKm = 15;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKm).logKmTo(endKm).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(startKm + EPSILON).logKmTo(endKm).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        // only return first one
        Optional<PValiditySection> optional = ValiditySectionsUtils.findFirstSectionBetween(sections, roadId, startKm, endKm);
        assertTrue(optional.isPresent());
        assertEquals(section1, optional.get()); 
    }   

    @Test
    public void findFirstSectionBetween_section2FirstBetween_section2() {  
        double startKm = 10;
        double endKm = 15;
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(startKm - EPSILON).logKmTo(endKm).roadId(roadId).section(PSection.builder().build()).build(); // begin outside
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(startKm + EPSILON).logKmTo(endKm - EPSILON).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        Optional<PValiditySection> optional = ValiditySectionsUtils.findFirstSectionBetween(sections, roadId, startKm, endKm);
        assertTrue(optional.isPresent());
        assertEquals(section2, optional.get()); 
    }  

    @Test
    public void filterAllowedSections_allValid_listUnchanged() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  
        

        PValiditySection allowed1 =  PValiditySection.builder().logKmFrom(4).logKmTo(7).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection allowed2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowed1);
        allowed.add(allowed2); 

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, roadId);
        assertEquals(sections, list); 
    }  
    
    @Test
    public void filterAllowedSections_partValid_validSection() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        PValiditySection allowed1 =  PValiditySection.builder().logKmFrom(4).logKmTo(5).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection allowed2 =  PValiditySection.builder().logKmFrom(7).logKmTo(8).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection allowed3 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowed1);
        allowed.add(allowed2);
        allowed.add(allowed3);

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, roadId);
        assertEquals(1, list.size()); 
        assertEquals(7, list.get(0).getLogKmFrom()); 
        assertEquals(8, list.get(0).getLogKmTo()); 
    }


    @Test
    public void filterAllowedSections_sectionsAsPointLoc() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(305.828).logKmTo(305.828).roadId("A02_1").section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        PValiditySection allowed1 =  PValiditySection.builder().logKmFrom(0.35807311843847).logKmTo(24.8935741825758).roadId("A10_1").section(PSection.builder().build()).build();
        PValiditySection allowed2 =  PValiditySection.builder().logKmFrom(305.4203412845205).logKmTo(308.1371933125415).roadId("A02_1").section(PSection.builder().build()).build();
        PValiditySection allowed3 =  PValiditySection.builder().logKmFrom(0.049780274334672164).logKmTo(90.48186263724953).roadId("A12_1").section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowed1);
        allowed.add(allowed2);
        allowed.add(allowed3);

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, "A02_1");
        assertEquals(1, list.size());
        assertEquals(305.828, list.get(0).getLogKmFrom());
        assertEquals(305.828, list.get(0).getLogKmTo());
        assertEquals("A02_1", list.get(0).getRoadId());
    }

    @Test
    public void filterAllowedSections_allowedAsPointLoc() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(305.4203412845205).logKmTo(308.1371933125415).roadId("A02_1").section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        PValiditySection allowedSec =  PValiditySection.builder().logKmFrom(305.828).logKmTo(305.828).roadId("A02_1").section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowedSec);

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, "A02_1");
        assertEquals(1, list.size());
        assertEquals(305.828, list.get(0).getLogKmFrom());
        assertEquals(305.828, list.get(0).getLogKmTo());
        assertEquals("A02_1", list.get(0).getRoadId());
    }

    @Test
    public void filterAllowedSections_partsValid_cutSections() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  
        

        PValiditySection allowed1 =  PValiditySection.builder().logKmFrom(4).logKmTo(5.5).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection allowed2 =  PValiditySection.builder().logKmFrom(10.5).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowed1);
        allowed.add(allowed2); 

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, roadId);
        assertEquals(sections.size(), list.size()); 
        assertEquals(5, list.get(0).getLogKmFrom()); 
        assertEquals(5.5, list.get(0).getLogKmTo()); 
        assertEquals(10.5, list.get(1).getLogKmFrom()); 
        assertEquals(11, list.get(1).getLogKmTo()); 
    }  

    @Test
    public void filterAllowedSections_noneAllowed_empty() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  
        

        PValiditySection allowed1 =  PValiditySection.builder().logKmFrom(4).logKmTo(5).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection allowed2 =  PValiditySection.builder().logKmFrom(11).logKmTo(12).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> allowed = new LinkedList<>();
        allowed.add(allowed1);
        allowed.add(allowed2); 

        List<PValiditySection> list = ValiditySectionsUtils.filterAllowedSections(sections, allowed, roadId);
        assertTrue(list.isEmpty()); 
    }  

    @Test
    public void removeForbiddenAreas_noneForbidden_listUnchanged() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(4).logKmTo(5).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection forbidden2 =  PValiditySection.builder().logKmFrom(6).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection forbidden3 =  PValiditySection.builder().logKmFrom(11).logKmTo(12).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);
        forbidden.add(forbidden2); 
        forbidden.add(forbidden3); 

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(sections, list);
    }   

    @Test
    public void removeForbiddenAreas_allForbidden_empty() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(5).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertTrue(list.isEmpty()); 
    }  
    
    @Test
    public void removeForbiddenAreas_partsForbidden_cutSections() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(10).logKmTo(11).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);  

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(5.5).logKmTo(10.5).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(sections.size(), list.size()); 
        assertEquals(5, list.get(0).getLogKmFrom()); 
        assertEquals(5.5, list.get(0).getLogKmTo()); 
        assertEquals(10.5, list.get(1).getLogKmFrom()); 
        assertEquals(11, list.get(1).getLogKmTo()); 
    }


    @Test
    public void removeForbiddenAreas_forbiddenAsPointLoc() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(5.5).logKmTo(5.5).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);

        // forbidden point locs always interpreted as 1 m

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(2, list.size());
        assertEquals(5, list.get(0).getLogKmFrom());
        assertEquals(5.5, list.get(0).getLogKmTo());
        assertEquals(5.501, list.get(1).getLogKmFrom());
        assertEquals(6, list.get(1).getLogKmTo());
    }

    @Test
    public void removeForbiddenAreas_sectionAsPointLoc() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(5).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(7).logKmTo(8).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(1, list.size());
        assertEquals(5, list.get(0).getLogKmFrom());
        assertEquals(5, list.get(0).getLogKmTo());

        section1 =  PValiditySection.builder().logKmFrom(7.5).logKmTo(7.5).roadId(roadId).section(PSection.builder().build()).build();
        sections = new LinkedList<>();
        sections.add(section1);

        list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(0, list.size());
    }

    @Test
    public void removeForbiddenAreas_partForbidden_cutSection() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);        

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(7).logKmTo(8).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(2, list.size()); 
        assertEquals(5, list.get(0).getLogKmFrom()); 
        assertEquals(7, list.get(0).getLogKmTo()); 
        assertEquals(8, list.get(1).getLogKmFrom()); 
        assertEquals(10, list.get(1).getLogKmTo()); 
    }  
    
    @Test
    public void removeForbiddenAreas_equalBoundaries_empty() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(6).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1); 
        

        PValiditySection forbidden1 =  PValiditySection.builder().logKmFrom(4).logKmTo(5).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection forbidden2 =  PValiditySection.builder().logKmFrom(6).logKmTo(7).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> forbidden = new LinkedList<>();
        forbidden.add(forbidden1);
        forbidden.add(forbidden2);

        List<PValiditySection> list = ValiditySectionsUtils.removeForbiddenAreas(sections, forbidden, roadId);
        assertEquals(1, list.size()); 
        assertEquals(5, list.get(0).getLogKmFrom()); 
        assertEquals(6, list.get(0).getLogKmTo()); 
    }

    @Test
    public void sectionListContainsKm_true() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);

        assertTrue(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 10-EPSILON));
        assertTrue(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 5));
        assertTrue(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 11));
        assertTrue(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 15-EPSILON));
    }

    @Test
    public void sectionListContainsKm_false() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> sections = new LinkedList<>();
        sections.add(section1);
        sections.add(section2);

        assertFalse(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 10));
        assertFalse(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 5-EPSILON));
        assertFalse(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 11-EPSILON));
        assertFalse(ValiditySectionsUtils.sectionListContainsKm(sections, roadId, 15));
        assertFalse(ValiditySectionsUtils.sectionListContainsKm(sections, roadId+".1", 7));
    }

    @Test
    public void sectionListContainsKm_emptyList_false() {
        assertFalse(ValiditySectionsUtils.sectionListContainsKm(Collections.emptyList(), roadId, 10));
    }

    @Test
    public void isValid_emptyLists_true() {
        assertTrue(ValiditySectionsUtils.isValid(roadId, 10, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void isValid_emptyBlackList_true() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> whiteList = new LinkedList<>();
        whiteList.add(section1);
        whiteList.add(section2);

        assertTrue(ValiditySectionsUtils.isValid(roadId, 10-EPSILON, whiteList, Collections.emptyList()));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 5, whiteList, Collections.emptyList()));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 11, whiteList, Collections.emptyList()));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 15-EPSILON, whiteList, Collections.emptyList()));
    }

    @Test
    public void isValid_emptyBlackList_false() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> whiteList = new LinkedList<>();
        whiteList.add(section1);
        whiteList.add(section2);

        assertFalse(ValiditySectionsUtils.isValid(roadId, 10, whiteList, Collections.emptyList()));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 5-EPSILON, whiteList, Collections.emptyList()));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 11-EPSILON, whiteList, Collections.emptyList()));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 15, whiteList, Collections.emptyList()));
        assertFalse(ValiditySectionsUtils.isValid(roadId+".1", 7, whiteList, Collections.emptyList()));
    }

    @Test
    public void isValid_emptyWhiteList_false() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> blackList = new LinkedList<>();
        blackList.add(section1);
        blackList.add(section2);

        assertFalse(ValiditySectionsUtils.isValid(roadId, 10-EPSILON, Collections.emptyList(), blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 5, Collections.emptyList(), blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 11, Collections.emptyList(), blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 15-EPSILON, Collections.emptyList(), blackList));
    }

    @Test
    public void isValid_emptyWhiteList_true() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> blackList = new LinkedList<>();
        blackList.add(section1);
        blackList.add(section2);

        assertTrue(ValiditySectionsUtils.isValid(roadId, 10, Collections.emptyList(), blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 5-EPSILON, Collections.emptyList(), blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 11-EPSILON, Collections.emptyList(), blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 15, Collections.emptyList(), blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId+".1", 7, Collections.emptyList(), blackList));
    }

    @Test
    public void isValid_bothListsFilled_true() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> whiteList = new LinkedList<>();
        whiteList.add(section1);
        whiteList.add(section2);

        PValiditySection section1b =  PValiditySection.builder().logKmFrom(6).logKmTo(7).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2b =  PValiditySection.builder().logKmFrom(13).logKmTo(20).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> blackList = new LinkedList<>();
        blackList.add(section1b);
        blackList.add(section2b);

        assertTrue(ValiditySectionsUtils.isValid(roadId, 5, whiteList, blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 6-EPSILON, whiteList, blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 7, whiteList, blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 10-EPSILON, whiteList, blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 11, whiteList, blackList));
        assertTrue(ValiditySectionsUtils.isValid(roadId, 13-EPSILON, whiteList, blackList));
    }

    @Test
    public void isValid_bothListsFilled_false() {
        PValiditySection section1 =  PValiditySection.builder().logKmFrom(5).logKmTo(10).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2 =  PValiditySection.builder().logKmFrom(11).logKmTo(15).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> whiteList = new LinkedList<>();
        whiteList.add(section1);
        whiteList.add(section2);

        PValiditySection section1b =  PValiditySection.builder().logKmFrom(6).logKmTo(7).roadId(roadId).section(PSection.builder().build()).build();
        PValiditySection section2b =  PValiditySection.builder().logKmFrom(13).logKmTo(20).roadId(roadId).section(PSection.builder().build()).build();
        List<PValiditySection> blackList = new LinkedList<>();
        blackList.add(section1b);
        blackList.add(section2b);

        assertFalse(ValiditySectionsUtils.isValid(roadId, 5-EPSILON, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 6, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 7-EPSILON, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 10, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 11-EPSILON, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId, 13, whiteList, blackList));
        assertFalse(ValiditySectionsUtils.isValid(roadId+".1", 5, whiteList, blackList));
    }
}
