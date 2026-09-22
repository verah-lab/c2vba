package de.heuboe.asfinag.vmis2.infrastructure.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper functions to access PValiditySection.
 */
@Slf4j
public class ValiditySectionsUtils {

    private static final double EPSILON = 0.0000001d;
    private static final double METER = 0.001;
    
    /**
     * Private constructor.
     */
    private ValiditySectionsUtils() {       
    }
    
    /**
     * Selects all allowed areas (allowedSections) from given sections (sectionsToFilter).
     * @param sectionsToFilter sections where areas which are not part of allowedSections has to be removed
     * @param allowedSections allowed areas
     * @param roadId    road id
     * @return List of PValiditySections containing given sectionsToFilter without areas which are not part of allowedSections
     */
    public static List<PValiditySection> filterAllowedSections(List<PValiditySection> sectionsToFilter,
            List<PValiditySection> allowedSections, String roadId) {
        List<PValiditySection> validAreasOfUse = new LinkedList<>();
        List<PValiditySection> sortedPValiditySections = allowedSections.stream().filter(s -> s.getRoadId().equals(roadId))
                .sorted(ValiditySectionsUtils::compareValiditySection).collect(Collectors.toList());
        for (PValiditySection s : sectionsToFilter) {
            validAreasOfUse.addAll(filterAllowedSections(s, sortedPValiditySections));
        }
        return validAreasOfUse;
    }

    private static List<PValiditySection> filterAllowedSections(PValiditySection section,
            List<PValiditySection> allowedSections) {
        log.trace("call filterAllowedSections: section={}, allowed={}", section, allowedSections);
        List<PValiditySection> validSectionAreas = new LinkedList<>();

        Optional<PValiditySection> vs = findSectionWithKm(allowedSections, section.getRoadId(), section.getLogKmFrom());
        if (vs.isPresent()) {
            // begin of section is allowed
            PValiditySection roadSection = vs.get();
            if (sectionContainsKm(roadSection, section.getRoadId(), section.getLogKmTo())) {
                // complete section is allowed
                validSectionAreas.add(section);
            } else {
                // begin of section is allowed
                validSectionAreas.add(PValiditySection.builder().logKmFrom(section.getLogKmFrom())
                        .logKmTo(roadSection.getLogKmTo())
                        .roadId(roadSection.getRoadId())
                        .section(roadSection.getSection())
                        .build());
                // check end of section
                validSectionAreas.addAll(filterAllowedSections(PValiditySection.builder()
                        .logKmFrom(roadSection.getLogKmTo())
                        .logKmTo(section.getLogKmTo())
                        .roadId(section.getRoadId())
                        .section(section.getSection())
                        .build(), allowedSections));
            }
        } else {
            vs = findFirstSectionBetween(allowedSections, section.getRoadId(), section.getLogKmFrom(), section.getLogKmTo());
            if (vs.isPresent()) {
                // begin of section is forbidden but section contains allowed areas
                PValiditySection validSection = vs.get();
                validSectionAreas.add(validSection);
                if (!isPointLoc(validSection) && (validSection.getLogKmTo() < section.getLogKmTo())) {
                    // check end of section
                    validSectionAreas.addAll(filterAllowedSections(PValiditySection.builder()
                            .logKmFrom(validSection.getLogKmTo())
                            .logKmTo(section.getLogKmTo())
                            .roadId(section.getRoadId())
                            .section(validSection.getSection())
                            .build(), allowedSections));

                }
            }
        }
        return validSectionAreas;
    }

    /**
     * Removes all forbidden areas (areasOfDontUse) from given sections (sectionsToCheck).
     * 
     * @param sectionsToCheck   sections where areasOfDontUse has to be removed
     * @param areasOfDontUse    forbidden areas
     * @param roadId            road id
     * @return List of PValiditySections containing given sectionsToCheck without areasOfDontUse
     */
    public static List<PValiditySection> removeForbiddenAreas(List<PValiditySection> sectionsToCheck,
            List<PValiditySection> areasOfDontUse, String roadId) {
        List<PValiditySection> validAreasOfUse = new LinkedList<>();
        // areas consist of one point will be extended to 1 m
        List<PValiditySection> extendPointLocs = areasOfDontUse.stream().map(a -> {
            if (isPointLoc(a)){
                return PValiditySection.builder()
                        .logKmFrom(a.getLogKmFrom())
                        .logKmTo(a.getLogKmFrom() + METER)
                        .section(a.getSection())
                        .roadId(a.getRoadId())
                        .build();
            } else {
                return a;
            }
        }).collect(Collectors.toList());
        List<PValiditySection> sortedAreasOfDontUse = extendPointLocs.stream()
                .filter(s -> s.getRoadId().equals(roadId))
                .sorted(ValiditySectionsUtils::compareValiditySection)
                .collect(Collectors.toList());
        for (PValiditySection section : sectionsToCheck) {
            validAreasOfUse.addAll(removeForbiddenAreas(section, sortedAreasOfDontUse));
        }
        return validAreasOfUse;
    }

    private static List<PValiditySection> removeForbiddenAreas(PValiditySection section,
            List<PValiditySection> sortedAreasOfDontUse) {
        List<PValiditySection> validSectionAreas = new LinkedList<>();
        Optional<PValiditySection> opt = findSectionWithKm(sortedAreasOfDontUse, section.getRoadId(), section.getLogKmFrom());
        if (opt.isPresent()) {
            PValiditySection invalidSection = opt.get();
            // begin of section is forbidden
            if (invalidSection.getLogKmTo() < section.getLogKmTo()) {
                // end of section is allowed
                // check end of section
                validSectionAreas.addAll(removeForbiddenAreas(PValiditySection.builder()
                        .logKmFrom(invalidSection.getLogKmTo())
                        .logKmTo(section.getLogKmTo())
                        .roadId(section.getRoadId())
                        .section(section.getSection())
                        .build(), sortedAreasOfDontUse));
            }// else complete section is invalid
        } else {
            // begin of section is allowed
            opt = findFirstSectionBetween(sortedAreasOfDontUse, section.getRoadId(), section.getLogKmFrom(), section.getLogKmTo());
            if (opt.isPresent()) {
                // section contains forbidden areas
                PValiditySection invalidSection = opt.get();
                validSectionAreas.add(PValiditySection.builder()
                        .logKmFrom(section.getLogKmFrom())
                        .logKmTo(invalidSection.getLogKmFrom())
                        .roadId(section.getRoadId())
                        .section(section.getSection())
                        .build());
                if ((invalidSection.getLogKmTo() < section.getLogKmTo())) {
                    // check end of section
                    validSectionAreas.addAll(removeForbiddenAreas(PValiditySection.builder()
                            .logKmFrom(invalidSection.getLogKmTo())
                            .logKmTo(section.getLogKmTo())
                            .roadId(section.getRoadId())
                            .section(section.getSection())
                            .build(), sortedAreasOfDontUse));
                }
            } else {
                // complete section allowed
                validSectionAreas.add(section);
            }
        }
        return validSectionAreas;
    }


    /**
     * Returns first PValiditySection, which begin is located on given section of road. If the end of the PValiditySection is outside
     * the given section, the section end will be used as end of the returned PValiditySection.
     * The begin of the PValiditySection has to greater or equals to the startKm but smaller than endKm.
     * 
     * @param sectionsSorted sorted list of PValiditySections
     * @param roadId        road id
     * @param startKm       start km
     * @param endKm         end km
     * @return first PValiditySection found on given section of road or null if none of given PValiditySections is located within given section of road
     */
    public static Optional<PValiditySection> findFirstSectionBetween(List<PValiditySection> sectionsSorted, String roadId, double startKm,
            double endKm) {
        for (PValiditySection s : sectionsSorted) {
            if (startKm <= s.getLogKmFrom() && s.getLogKmFrom() < endKm && s.getRoadId().equals(roadId)) {
                double endKmOfValidSection = s.getLogKmTo() < endKm ? s.getLogKmTo() : endKm;

                return Optional.ofNullable(
                         PValiditySection.builder()
                                 .logKmFrom(s.getLogKmFrom())
                                 .logKmTo(endKmOfValidSection)
                                 .roadId(s.getRoadId())
                                 .section(s.getSection())
                                 .build());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns first PValiditySection containing given position.
     * Contains means that km is greater or equals the begin of a section but smaller than the end of the section (because end of the section
     * does not belong to the section).
     * 
     * @param sectionList list of PValiditySections
     * @param roadId    road id
     * @param km        km to find
     * @return first PValiditySection containing given km on given road id
     */
    public static Optional<PValiditySection> findSectionWithKm(List<PValiditySection> sectionList, String roadId, double km) {
        for (PValiditySection s : sectionList) {
            if (sectionContainsKm(s, roadId, km)) {
                return Optional.ofNullable(s);
            }
        }
        return Optional.ofNullable(null);
    }

    /**
     * Checks whether a given PValiditySection contains given km. 
     * Contains means that km is greater or equals the begin of the section but smaller than the end of the section (because end of the section
     * does not belong to the section).
     * 
     * @param section   PValiditySection
     * @param roadId    road id
     * @param km        km to find
     * @return true if given km is between LogKmFrom and LogKmTo.
     */
    public static boolean sectionContainsKm(PValiditySection section, String roadId, double km) {
        if (!section.getRoadId().equals(roadId)) {
            return false;
        }
        return section.getLogKmFrom() <= km && km < section.getLogKmTo();
    }

    private static boolean sameLoc(double km1, double km2) {
        return Math.abs(km1-km2) < EPSILON;
    }

    private static boolean isPointLoc(PValiditySection section) {
        return sameLoc(section.getLogKmTo(), section.getLogKmFrom());
    }

    /**
     * Checks whether a given List of PValiditySections contains given km.
     * Contains means that km is greater or equals the begin of the section but smaller than the end of the section (because end of the section
     * does not belong to the section).
     *
     * @param sectionList   list of PValiditySections
     * @param roadId    road id
     * @param km        logical km to find
     * @return true if given km is between LogKmFrom and LogKmTo.
     */
    public static boolean sectionListContainsKm(List<PValiditySection> sectionList, String roadId, double km) {
        for (PValiditySection sec : sectionList) {
            if (sectionContainsKm(sec, roadId, km)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a given position (road + km) is located on a valid place. If both lists are empty,
     * every given position is valid. If whiteList is set and blackList is empty, only positions on given
     * whiteList's validitySections are valid. If blackList is set and whiteList is empty, every position which
     * is not placed on given blackList's validitySections is valid. If both lists are filled, a position is valid
     * as long as it is located on a ValiditySection from the whiteList but not on a ValiditySection from the
     * blackList.<br>
     * A position is on a validitySection if km is greater or equals the begin of the section but smaller than the
     * end of the section (because end of the section does not belong to the section).
     *
     * @param roadId    road id of given km
     * @param km        logical km to check
     * @param whiteList list containing all validity sections that are valid
     * @param blackList list containing all validity sections that are invalid
     * @return true if given km is between LogKmFrom and LogKmTo.
     */
    public static boolean isValid(String roadId, double km, List<PValiditySection> whiteList, List<PValiditySection> blackList) {
        if (isEmpty(whiteList) && isEmpty(blackList)) {
            // always valid
            return true;
        } else if (isEmpty(blackList)) {
            // valid if on whiteList
            return sectionListContainsKm(whiteList, roadId, km);
        } else if (isEmpty(whiteList)) {
            // valid if not on blackList
            return !sectionListContainsKm(blackList, roadId, km);
        } else {
            // both lists are filled
            // valid if on whiteList and NOT on blackList
            return sectionListContainsKm(whiteList, roadId, km) && !sectionListContainsKm(blackList, roadId, km);
        }
    }

    private static boolean isEmpty(List<PValiditySection> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Compares two PValiditySection objects. Therefore roadId and logKmFrom will be compared.
     * 
     * @param o1 PValiditySection to compare with o2
     * @param o2 PValiditySection to compare with o1
     * @return 1 if roadId or LogKmFrom (same roadId) of o1 is greater than o2, -1 if roadId or LogKmFrom (same roadId) of o2 is greater than o1, 0 if
     *         both are equal
     */
    public static int compareValiditySection(PValiditySection o1, PValiditySection o2) {
        double o1FromKm = o1.getLogKmFrom();
        double o2FromKm = o2.getLogKmFrom();
        if (!o1.getRoadId().equals(o2.getRoadId())){
            return o1.getRoadId().compareTo(o2.getRoadId());
        }
        if (o1FromKm < o2FromKm) {
            return -1;
        } else if (o1FromKm > o2FromKm) {
            return 1;
        } else {
            return 0;
        }
    }
}
