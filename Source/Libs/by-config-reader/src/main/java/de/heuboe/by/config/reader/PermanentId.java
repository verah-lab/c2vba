package de.heuboe.by.config.reader;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Eine permanent ID besteht aus einem Typcode, einem Bezeichner und einer Domäne. Die Bestandteile
 * einer permanenten ID müssen den folgenden Vorgaben genügen:
 *
 * <ul>
 * <li>Der Typcode muss aus einer vordefinierten Liste von Typcodes stammen. Der Typcode muss mit
 * einem Buchstaben oder einem Unterstrich beginnen und darf nur Buchstaben, Ziffern, Unterstriche
 * und Minuszeichen enthalten.</li>
 * <li>Der Bezeichner muss mit einem Buchstaben, einer Ziffer oder einem Unterstrich beginnen und
 * darf nur Buchstaben, Ziffern, Unterstriche und Minuszeichen enthalten.</li>
 * <li>Die Domäne besteht aus einer oder mehreren Subdomänen, die durch Punkte voneinander getrennt
 * sind. Eine Subdomäne muss mit einem Buchstaben beginnen und darf nur Buchstaben, Ziffern und
 * Minuszeichen enthalten.</li>
 * </ul>
 *
 * @author Werner
 */
@Slf4j
public class PermanentId {
    private static final String ERROR_MESSAGE = "Illegal character '";
    private static final int TYPE_CODE_INDEX = 0;
    private static final int IDENTIFIER_INDEX = 1;
    private static final int DOMAIN_INDEX = 2;
    private static final Set<String> VALID_TYPES;
    private static final Map<String, String> TYPE_LOOKUP;
    private static final Map<String, String> SPECIAL_IDS;
    private final String typeCode;
    private final String identifier;
    private final String domain;

    /**
     * Erzeugt eine permanente ID aus den angegebenen Parametern. Wenn die Parameter nicht den
     * syntaktischen und/oder semantischen Vorgaben für permanente IDs entsprechen, wird eine
     * IllegalArgumentException geworfen.
     *
     * @param typeCode   Typcode der permanenten ID
     * @param identifier Bezeichner der permanenten ID
     * @param domain     Domäne der permanenten ID
     */
    public PermanentId(String typeCode, String identifier, String domain) throws IllegalArgumentException {
        log.debug("create new PermanentId for typeCode='{}', identifier='{}', domain='{}'",
                typeCode, identifier, domain);
        assertValidTypeCode(typeCode.toLowerCase());
        assertValidIdentifier(specialIds(identifier));
        assertValidDomain(domain);
        this.identifier = specialIds(identifier);
        this.typeCode = typeCode;
        this.domain = domain;
    }

    /**
     * Erzeugt eine permanente ID aus der String-Repräsentation der permanenten ID. Die Sytax der
     * permanenten ID ist: Typcode.Bezeichner.Domäne. Wenn diese Bestandteile nicht den syntaktischen
     * und/oder semantischen Vorgaben für permanente IDs entsprechen, wird eine IllegalArgumentException
     * geworfen.
     *
     * @param permanentId String-Repräsentation der permanenten ID
     */
    public PermanentId(String permanentId) throws IllegalArgumentException {
        log.debug("create new PermanentId for String '{}'", permanentId);
        String[] parts = permanentId.split("\\.");
        if (parts.length <= DOMAIN_INDEX) {
            throw new IllegalArgumentException("Illegal permanent Id: " + permanentId);
        }
        assertValidTypeCode(parts[TYPE_CODE_INDEX].toLowerCase());
        assertValidIdentifier(parts[IDENTIFIER_INDEX]);
        StringBuilder domainTemp = new StringBuilder();
        for (int i = DOMAIN_INDEX; i < parts.length; ++i) {
            if (i > DOMAIN_INDEX) {
                domainTemp.append(".");
            }
            domainTemp.append(parts[i]);
        }
        assertValidDomain(domainTemp.toString());
        this.typeCode = parts[TYPE_CODE_INDEX];
        this.identifier = parts[IDENTIFIER_INDEX];
        this.domain = domainTemp.toString();
    }

    /**
     * Gibt den Typcode der permanenten ID zurück.
     *
     * @return Typcode der permanenten ID
     */
    public String getTypeCode() {
        return typeCode;
    }

    /**
     * Gibt den Bezeichner der permanenten ID zurück.
     *
     * @return Bezeichner der permanenten ID
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gibt die Domäne der permanenten ID zurück.
     *
     * @return Domäne der permanenten ID
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gibt eine Kopie der vordefinierten Typcodes zurück.
     *
     * @return Kopie der vordefinierten Typcodes
     */
    public Set<String> getTypeCodes() {
        return new TreeSet<>(VALID_TYPES);
    }


    /**
     * Extrahiert den Typcode aus einer permanenten ID des Bestandssystems der VZH.
     *
     * @param vzhPermanentId permanente ID des Bestandssystems der VZH
     * @return extrahierter Typcode
     */
    public static String getVzhTypeCode(String vzhPermanentId) {
        vzhPermanentId = specialIds(vzhPermanentId);
        String[] parts = splitVzhPermanentId(vzhPermanentId);
        return parts[TYPE_CODE_INDEX].toUpperCase();
    }

    /**
     * Ersetzt spezielle permanente IDs des Bestandssystems der VZH durch syntaxkonforme permanente IDs.
     * Falls keine Ersetzung vorgenommen wird, wird die ursprüngliche permanente ID zurückgegeben.
     *
     * @param vzhPermanentId permanente ID des Bestandssystems der VZH
     * @return ersetzte permanente ID
     */
    private static String specialIds(String vzhPermanentId) {
        return SPECIAL_IDS.getOrDefault(vzhPermanentId, vzhPermanentId);
    }


    /**
     * Überprüft, ob ein Typcode gültig ist und wirft eine IllegalArgumentException, wenn das nicht der
     * Fall ist. Ein Typcode muss aus einer vordefinierten Liste von Typcodes stammen. Der Typcode muss
     * mit einem Buchstaben oder einem Unterstrich beginnen und darf nur Buchstaben, Ziffern,
     * Unterstriche und Minuszeichen enthalten.
     *
     * @param typeCode zu überprüfender Typcode
     */
    private static void assertValidTypeCode(String typeCode) throws IllegalArgumentException{
        typeCode = TYPE_LOOKUP.getOrDefault(typeCode, typeCode);
        if (typeCode == null || !VALID_TYPES.contains(typeCode)) {
            throw new IllegalArgumentException("Illegal type code: " + typeCode);
        }
        for (int i = 0; i < typeCode.length(); ++i) {
            char ch = typeCode.charAt(i);
            if (i == 0) {
                if (!Character.isLetter(ch) && ch != '_') {
                    throw new IllegalArgumentException(
                            ERROR_MESSAGE + ch + "' in type code part of permanent id: " + typeCode);
                }
            } else {
                if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_' && ch != '-') {
                    throw new IllegalArgumentException(
                            ERROR_MESSAGE + ch + "' in type code part of permanent id: " + typeCode);
                }
            }
        }
    }

    /**
     * Überprüft, ob ein Bezeichner gültig ist und wirft eine IllegalArgumentException, wenn das nicht
     * der Fall ist. Ein Bezeichner muss mit einem Buchstaben, einer Ziffer oder einem Unterstrich
     * beginnen und darf nur Buchstaben, Ziffern, Unterstriche und Minuszeichen enthalten.
     *
     * @param identifier zu überprüfender Bezeichner
     */
    private static void assertValidIdentifier(String identifier) throws IllegalArgumentException {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier part of permanent id is empty.");
        }
        for (int i = 0; i < identifier.length(); ++i) {
            char ch = identifier.charAt(i);
            if (i == 0) {
                if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_') {
                    throw new IllegalArgumentException(
                            ERROR_MESSAGE + ch + "' in identifier part of permanent id: " + identifier);
                }
            } else {
                if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_' && ch != '-') {
                    throw new IllegalArgumentException(
                            ERROR_MESSAGE + ch + "' in identifier part of permanent id: " + identifier);
                }
            }
        }
    }

    /**
     * Überprüft, ob eine Domäne gültig ist und wirft eine IllegalArgumentException, wenn das nicht der
     * Fall ist. Eine Domäne besteht aus einer oder mehreren Subdomänen, die durch Punkte voneinander
     * getrennt sind. Eine Subdomäne muss mit einem Buchstaben beginnen und darf nur Buchstaben, Ziffern
     * und Minuszeichen enthalten.
     *
     * @param domain zu überprüfende Domäne
     */
    private static void assertValidDomain(String domain) {
        String[] subDomains = domain.split("\\.");
        for (String subDomain : subDomains) {
            if (subDomain.isEmpty()) {
                throw new IllegalArgumentException("Domain part of permanent id contains empty subdomain: " + domain);
            }
            for (int i = 0; i < subDomain.length(); ++i) {
                char ch = subDomain.charAt(i);
                if (i == 0) {
                    if (!Character.isLetter(ch)) {
                        throw new IllegalArgumentException(
                                ERROR_MESSAGE + ch + "' in domain part of permanent id: " + domain);
                    }
                } else {
                    if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '-') {
                        throw new IllegalArgumentException(
                                ERROR_MESSAGE + ch + "' in domain part of permanent id: " + domain);
                    }
                }
            }
        }
    }

    /**
     * Zerlegt eine permanente ID des Bestandssystems der VZH in die drei Bestandteile Typcode,
     * Bezeichner und Domäne
     *
     * @param vzhPermanentId permanente ID des Bestandssystems der VZH
     * @return Array mit den drei Bestandteilen einer permanenten ID des Bestandssystems der VZH.
     */
    private static String[] splitVzhPermanentId(String vzhPermanentId) {
        String[] parts = new String[DOMAIN_INDEX + 1];
        if (!vzhPermanentId.toLowerCase().startsWith("he-")) {
            parts[DOMAIN_INDEX] = "";
            parts[IDENTIFIER_INDEX] = vzhPermanentId;
            parts[TYPE_CODE_INDEX] = "";
        } else {
            parts[DOMAIN_INDEX] = vzhPermanentId.substring(0, 2);
            vzhPermanentId = vzhPermanentId.substring(3);
            int minusIndex = vzhPermanentId.indexOf('-');
            int underscoreIndex = vzhPermanentId.indexOf('_');
            int index = -1;
            if (minusIndex < 0) {
                if (underscoreIndex >= 0) {
                    index = underscoreIndex;
                }
            } else if (underscoreIndex < 0) {
                index = minusIndex;
            } else {
                index = Math.min(minusIndex, underscoreIndex);
            }
            if (index < 0) {
                parts[IDENTIFIER_INDEX] = vzhPermanentId;
                parts[TYPE_CODE_INDEX] = "";
            } else {
                parts[IDENTIFIER_INDEX] = vzhPermanentId.substring(index + 1);
                parts[TYPE_CODE_INDEX] = vzhPermanentId.substring(0, index);
            }
        }
        return parts;
    }

    @Override
    public String toString() {
        return typeCode + "." + identifier + "." + domain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof PermanentId) {
            return this.toString().equals(obj.toString());
        } else if (obj instanceof String) {
            return this.toString().equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    static {
        // Liste der gültigen Typcodes
        VALID_TYPES = new TreeSet<>();
        VALID_TYPES.add("aq");
        VALID_TYPES.add("axfs");
        VALID_TYPES.add("axq");
        VALID_TYPES.add("eq");
        VALID_TYPES.add("fs");
        VALID_TYPES.add("gma");
        VALID_TYPES.add("info");
        VALID_TYPES.add("kam");
        VALID_TYPES.add("kri");
        VALID_TYPES.add("lkws");
        VALID_TYPES.add("lsa");
        VALID_TYPES.add("prw");
        VALID_TYPES.add("ssf");
        VALID_TYPES.add("sst");
        VALID_TYPES.add("ufd");
        VALID_TYPES.add("ufm");
        VALID_TYPES.add("uz");
        VALID_TYPES.add("vi-akt");
        VALID_TYPES.add("vi-avl");
        VALID_TYPES.add("vi-bs");
        VALID_TYPES.add("vi-fbm");
        VALID_TYPES.add("vi-gen");
        VALID_TYPES.add("vi-gm");
        VALID_TYPES.add("vi-hind");
        VALID_TYPES.add("vi-uml");
        VALID_TYPES.add("vi-unf");
        VALID_TYPES.add("vi-vb");
        VALID_TYPES.add("vi-wd");
        VALID_TYPES.add("vi-zus");
        VALID_TYPES.add("vlt");
        VALID_TYPES.add("vrz");
        VALID_TYPES.add("wta");
        VALID_TYPES.add("wtv");
        VALID_TYPES.add("www");
        VALID_TYPES.add("wzg");
        VALID_TYPES.add("ib");

        // Lookup-Tabelle, die den Typcodes des Bestandssystems der VZH neue Typcodes zuordnet.
        TYPE_LOOKUP = new TreeMap<>();
        TYPE_LOOKUP.put("vde", "fs");
        TYPE_LOOKUP.put("mq", "eq");
        TYPE_LOOKUP.put("ude", "ufd");

        // Lookup-Tabelle für spezielle permanente IDs des Bestandssystems der VZH
        SPECIAL_IDS = new TreeMap<>();
        SPECIAL_IDS.put("Taunusblick", "HE-LKWS-Taunusblick");
        SPECIAL_IDS.put("Langen-Bergheim FR Nord", "HE-LKWS-Langen-Bergheim_FR Nord");
        SPECIAL_IDS.put("Langen-Bergheim FR Sued", "HE-LKWS-Langen-Bergheim FR Sued");
        SPECIAL_IDS.put("He-WWW-HK101", "He-WWWB-BHK111");
        SPECIAL_IDS.put("He-WWW-HK123", "He-WWWB-BHK112");
        SPECIAL_IDS.put("He-WWW-HK102", "He-WWWB-BHK113");
        SPECIAL_IDS.put("He-WWW-HK103", "He-WWWB-BHK114");
        SPECIAL_IDS.put("He-WWW-HK104", "He-WWWB-BHK115");
        SPECIAL_IDS.put("He-WWW-HK128", "He-WWWB-BHK116");
        SPECIAL_IDS.put("HK105", "bhk117");
        SPECIAL_IDS.put("HK101", "bhk111");
        SPECIAL_IDS.put("HK123", "bhk112");
        SPECIAL_IDS.put("HK102", "bhk113");
        SPECIAL_IDS.put("HK103", "bhk114");
        SPECIAL_IDS.put("HK104", "bhk115");
        SPECIAL_IDS.put("HK128", "bhk116");
        SPECIAL_IDS.put("HK105", "bhk117");
    }

    /**
     * check if the given id is a special id
     * @param id to check
     * @return true if it is a special id
     */
    public static boolean isSpecialId(String id) {
        return SPECIAL_IDS.containsKey(id);
    }
}
