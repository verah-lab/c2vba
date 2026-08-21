package de.heuboe.by.config.reader.tls;

import de.heuboe.by.config.reader.PermanentId;
import de.heuboe.config.base.Types;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a entry of the configuration table
 */
@Getter
@Setter
@Slf4j
public class ConfigObject {

    private static final String ID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_-]*$";
    private static final String ROAD_PATTERN = "[AB]{1}[0-9]*";
    private static final HashMap<Integer, String> UFD_MAP = new HashMap<>();
    private static final String DOMAIN = "m.de";
    public static final Map<String, String> PERM_TO_CONF_ID_MAP = new HashMap<>();
    private static final Map<String, String> NAME_MAP = new HashMap<>();


    private final List<ConfigObject> children = new ArrayList<>();

    private Types.ConfigItemType type;
    private String name;
    private String id;
    private int port;
    private int fg;
    private int eak;
    private int loc;
    private int dist;
    private int de;
    private String tlsRef;
    private int slave;
    private String road;
    private String direction;
    private String km;
    private String lane;
    private String efh;
    private int tlsTyp;

    private int osi7Address;
    private String ipAddress;

    /**
     * creates a ConfigObject from a record
     *
     * @param record entry of the configuration table
     */
    public ConfigObject(TlsConfigReader.HBRecord record) throws IllegalArgumentException {
        this.type = Types.ConfigItemType.valueOf(record.get(TlsConfigReader.HBColumnType.ID_CLASS));
        this.name = record.get(TlsConfigReader.HBColumnType.ID_NAME);
        this.de = toInt(record.get(TlsConfigReader.HBColumnType.DE));
        this.fg = toInt(record.get(TlsConfigReader.HBColumnType.FG));
        this.port = toInt(record.get(TlsConfigReader.HBColumnType.PORT));
        this.eak = toInt(record.get(TlsConfigReader.HBColumnType.EAK));
        this.loc = toInt(record.get(TlsConfigReader.HBColumnType.LOC));
        this.dist = toInt(record.get(TlsConfigReader.HBColumnType.DIST));
        this.osi7Address = this.loc * 256 + this.dist;
        this.slave = toInt(record.get(TlsConfigReader.HBColumnType.SLAVE));
        this.ipAddress = record.get(TlsConfigReader.HBColumnType.IP_ADRESSE);
        this.lane = parseLane(record.get(TlsConfigReader.HBColumnType.LANE));
        this.efh = parseLane(record.get(TlsConfigReader.HBColumnType.EFH));
        parseRoad(record.get(TlsConfigReader.HBColumnType.ROAD));
        this.tlsTyp = toInt(record.get(TlsConfigReader.HBColumnType.TLS_TYPE));
        if (fg == 4) {
            this.tlsTyp = toInt(record.get(TlsConfigReader.HBColumnType.WZG_TYPE));
        }
        try {
            this.id = createPermanentId();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create PermanentId for sheet record:" +
                    " type='" + this.type.name() + "', name='" + this.name + "', de='" + this.de + "'", e);
        }
    }

    /**
     * Constructor for www chain objects
     * @param record www chain record
     */
    public ConfigObject(WwwChainReader.WWWChainRecord record) {
        this.name = record.get(WwwChainReader.WWWColumnType.NAME);
        this.id = "wwk." + record.get(WwwChainReader.WWWColumnType.ID).toLowerCase() + DOMAIN;
    }

    private void parseRoad(String value) {
        road = value.replace("_", "-").split("-")[0];
        if (!road.matches(ROAD_PATTERN)) {
            log.warn("Wrong road pattern: " + value);
        }
    }

    private String parseLane(String lane) {
        lane = lane.replace("FS", "F");
        if (lane.contains("/")) {
            String[] split = lane.split("/");
            lane = split[1] + "/" + split[0];
        }
        return lane;
    }

    /**
     * Constructor for devices
     * @param type Types.ConfigItemType
     * @param name String
     * @param port int
     * @param knotenNr int
     */
    public ConfigObject(Types.ConfigItemType type, String name, int port, int knotenNr) throws IllegalArgumentException {
        this.type = type;
        this.name = name;
        this.id = createPermanentId();
        this.port = port;
        this.osi7Address = knotenNr;
        this.loc = osi7Address / 256;
        this.dist = osi7Address % 256;
    }

    /**
     * Constructor for devices
     * @param type Types.ConfigItemType
     * @param id String
     * @param name String
     * @param port int
     * @param knotenNr int
     */
    public ConfigObject(Types.ConfigItemType type, String id, String name, int port, int knotenNr) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.port = port;
        this.osi7Address = knotenNr;
        this.loc = osi7Address / 256;
        this.dist = osi7Address % 256;
    }

    private int toInt(String temp) {
        return temp == null || temp.isEmpty() ? 0 : Integer.parseInt(temp.replace(".0", ""));
    }

    /**
     * copies a ConfigObject and set the given id to the new object
     *
     * @param type TlsType
     * @param old  ConfigObject
     * @param osi7Address int
     *
     */
    public ConfigObject(ConfigObject old, Types.ConfigItemType type, int osi7Address) throws IllegalArgumentException {
        this.type = type;
        this.name = old.getName();
        this.loc = old.getLoc();
        this.dist = old.getDist();
        this.eak = old.getEak();
        this.de = old.getDe();
        if (type.equals(Types.ConfigItemType.MQ)) {
            if (old.getDe() <= 192) {
                this.de = old.getDe();
                this.de |= 7;
            } else if (old.getDe() > 223) {
                this.de = old.getDe();
            } else {
                this.de = 7;
            }
        }
        this.fg = old.getFg();
        this.port = old.getPort();
        this.slave = old.getSlave();
        this.road = old.getRoad();
        this.km = old.getKm();
        this.direction = old.getDirection();
        this.id = createPermanentId();
        this.dist = old.getDist();
        this.children.addAll(old.getChildren());
        this.road = old.getRoad();
        this.osi7Address = osi7Address;
    }

    private String createPermanentId() throws IllegalArgumentException {
        return createPermId();
    }

    private String createPermId() {
        String confId = de != 0 ? name + "_" + de : name;
        return new PermanentId(type.name().toLowerCase(), toId(confId).toLowerCase(), DOMAIN).toString();
    }

    /**
     * creates a csv entry of the object
     *
     * @return csv entry
     */
    public String toCsvString() {
        return TlsWorld.toConfTabEntry(this);
    }

    public int getTlsTyp() {
        if (type == Types.ConfigItemType.WZG && tlsTyp == 0 && !isCl()) {
            return calcWzgTyp();
        }
        if (type == Types.ConfigItemType.AQ && tlsTyp == 0) {
            return Types.AqType.AQ.getNumber();
        }
        return tlsTyp;
    }

    private int calcWzgTyp() {
        if(this.name != null) {
            String nameUpper = this.name.toUpperCase();
            String[] parts = nameUpper.split("_");
            String last = parts[parts.length - 1];
            if (last.matches("[A-C][0-9]*")) {
                return Types.WzgType.valueOf("WZG_" + last.charAt(0)).getNumber();
            }
            if(nameUpper.endsWith("_A_R") || nameUpper.endsWith("_A_L") || nameUpper.contains("_A_SEITL")) {
                return Types.WzgType.WZG_A.getNumber();
            } else if(nameUpper.endsWith("_B_R") || nameUpper.endsWith("_B_L") || nameUpper.contains("_B_SEITL")) {
                return Types.WzgType.WZG_B.getNumber();
            } else if(nameUpper.endsWith("_C_R") || nameUpper.endsWith("_C_L") || nameUpper.contains("_C_SEITL")) {
                return Types.WzgType.WZG_C.getNumber();
            }
            if (nameUpper.contains("DLZ")) {
                return 19;
            }
            if (nameUpper.contains("LSA")) {
                return Types.WzgType.LSA.getNumber();
            }
            // if last part of name is contains PW or a single W with digits (e.g. "W1", "W2", "W3", "W13") it is a PW
            if (nameUpper.contains("PW") || last.matches("[W][0-9]*")) {
                return Types.WzgType.PW.getNumber();
            }
            if(nameUpper.contains("LCD") || nameUpper.contains("LED")) {
                return Types.WzgType.WTV.getNumber();
            }
            if (nameUpper.contains("BL")) {
                return Types.WzgType.BLINKER.getNumber();
            }
            if (nameUpper.contains("WTA")) {
                return Types.WzgType.WTV.getNumber();
            }
            if (nameUpper.contains("TEXT")) {
                return Types.WzgType.TEXT.getNumber();
            }
            if (nameUpper.contains("HS") || nameUpper.contains("SCH")) {
                return Types.WzgType.VHT.getNumber(); //Notlösung: setze VHT für Schranken / Halbschranken
            }

            if(parts.length >= 2) {
                String secondLast = parts[parts.length - 2];
                if (secondLast.matches("[A-C][0-9]")) {
                    return Types.WzgType.valueOf("WZG_" + secondLast.charAt(0)).getNumber();
                }
            }
        }
        return Types.WzgType.UNKNOWN.getNumber();
    }


    public String getLane() {
        if (lane == null || lane.isEmpty()) {
            return calcLane();
        }
        return lane;
    }

    private String calcLane() {
        if (de >= 193 && de < 224) {
            return "";
        }
        if (type == Types.ConfigItemType.VDE) {
            String laneType = getLaneType();
            if (laneType.isBlank()) {
                return laneType;
            }
            return laneType + getLaneNumber() + getLaneSite();
        }
        if (type == Types.ConfigItemType.WZG) {
            // default: guess lanelage from de-nr...
            // very very simple ... (1-6=HFS..UFS5, 9-14=HFSUFS1..UFS2UFS3, 17=AS, 18=IS)

            // WZG ist auf dem Fahrstreifen
            if (de < 64) {
                return "F" + (de % 8);
            }
            // WZG ist zwischen zwei Fahrstreifen
            if (de < 128) {
                return "F" + ((de % 8) + 1) + "/F" + (de % 8);
            }
            // WZG ist rechts oder links aussen
            return (de % 2 == 0) ? "IS" : "AS";
        }
        return "";
    }

    private String getLaneSite() {
        int mask = de & 0xD8;
        if (!isHfb(mask)) {
            switch (mask) {
                case 0x40://65, 66- 71 |  97, 98-103
                case 0x50://81, 82- 87 | 113,114-119
                case 0x80:// 129,130-135 | 161,162-167
                case 0x90://145,146-151 | 177,178-183
                    return "R";
                case 0x48://73, 74- 79 | 105,106-111
                case 0x88://137,138-143 | 169,170-175
                    return "L";
                default:
                    return "";
            }
        }
        return "";
    }

    private String getLaneNumber() {
        return de % 8 + "";
    }

    private String getLaneType() {
        int mask = de & 0xD8;
        if (isHfb(mask)) {
            return "F";
        }
        switch (mask) {
            case 0x40://65, 66- 71 |  97, 98-103
            case 0x48://73, 74- 79 | 105,106-111
            case 0x50://81, 82- 87 | 113,114-119
                return "A";
            case 0x80:// 129,130-135 | 161,162-167
            case 0x88://137,138-143 | 169,170-175
            case 0x90://145,146-151 | 177,178-183
                return "E";
            default:
                return "";
        }
    }

    private boolean isHfb(int mask) {
        return mask == 0x00 || mask == 0x08 || mask == 0x10 || mask == 0x18;
    }

    static String toId(String s) {
        String id = s;
        if (!id.matches(ID_PATTERN)) {
            id = id.trim().replace(' ', '_').replace('/', '_').replace('.', '_').replace(',', '_');
        }
        return id;
    }

    public boolean isCl() {
        return de >= 193 && de < 223;
    }

    @Override
    public String toString() {
        return "ConfigObject{" +
                ", id=" + id +
                ", fg=" + fg +
                ", de=" + de +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", slave=" + slave +
                ", eak=" + eak +
                ", loc=" + loc +
                ", osi7Address=" + osi7Address +
                ", dist=" + dist +
                ", tlsRef=" + tlsRef +
                "children=" + children +
                '}';
    }
}
