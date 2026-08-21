package de.heuboe.by.config.reader.tls;

import de.heuboe.by.config.reader.KriLookupReader;
import de.heuboe.by.config.reader.Properties;
import de.heuboe.config.base.Types;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores TLS config data
 */
@Getter
@Slf4j
@Component
public class TlsWorld {

    public static final String SEPARATOR_COL = "\t";

    private String version;
    private ConfigObject vrz;

    private Map<String, List<KriLookupReader.Record>> kriLookup;
    private Map<Integer, ConfigObject> kris;

    private java.util.Map<String, ConfigObject> wwwChains = new HashMap<>();

    private java.util.Map<String, String> idToFiles = new HashMap<>();

    /**
     * Constructor
     *
     * @param properties Properties

     */
    public TlsWorld(Properties properties) {
        this.version = Instant.now().toString();
        readProperties(properties);
    }

    /**
     * add KRI lookup
     *  @param kriLookup list of KriLookupReader.Record
     */
    public void addKriLookup(List<KriLookupReader.Record> kriLookup) {
        this.kriLookup = kriLookup.groupBy(r -> r.getKriId()).toMap(t -> t._1(), t -> t._2());
        this.kris = this.kriLookup.mapValues(v -> createKri(v.get(0).getKriId(), v.get(0).getKnotenNr())).toMap(t -> t._2().getOsi7Address(), t -> t._2());
    }


    /**
     * creates the logical config (AQs)
     */
    public void createLogConfig() {
        Set<ConfigObject> ssts = get(Types.ConfigItemType.SST);
        ssts.forEach(TlsLogConfig::linkAqs);
    }

    /**
     * Reads the records and creates the config tree
     * @param records list of TlsConfigReader.HBRecord
     */
    public void addPhysTable(List<TlsConfigReader.HBRecord> records, String fileName) throws IllegalArgumentException{
        createUz(records, fileName);
    }

    private void readProperties(Properties properties) {
        vrz = new ConfigObject(Types.ConfigItemType.VRZ, properties.getVrzId(), 0, properties.getVrzKnotenNr());
        ConfigObject uz = new ConfigObject(Types.ConfigItemType.UZ, properties.getUzId(), properties.getUzId(), 200, properties.getUzKnotenNr());
        vrz.getChildren().add(uz);
        uz.setTlsRef(vrz.getId());
    }

    private void createUz(List<TlsConfigReader.HBRecord> records, String fileName) throws IllegalArgumentException{
        ConfigObject sst = null;
        ConfigObject mq = null;
        ConfigObject kri = null;
        for (TlsConfigReader.HBRecord record : records) {
            ConfigObject obj = new ConfigObject(record);
            if(this.idToFiles.containsKey(obj.getId())) {
                log.warn("Duplicate: item id '{}' exists in '{}' and '{}'",
                        obj.getId(), fileName, this.idToFiles.get(obj.getId()));
            } else {
                this.idToFiles.put(obj.getId(), fileName);
            }
            if (kri == null) {
                kri = kris.get(obj.getOsi7Address()).getOrElse(() -> createKri(obj.getOsi7Address()));
            }
            if (Types.ConfigItemType.SST == obj.getType()) {
                linkSst(kri, obj);
                sst = obj;
                continue;
            }
            linkEa(sst, obj);
            if (obj.getType() == Types.ConfigItemType.MQ) {
                mq = obj;
                obj.setTlsRef("");
                continue;
            }
            if (obj.getType() == Types.ConfigItemType.VDE && mq != null) {
                setTlsRef(mq, obj);
            }
        }
    }

    private static void setTlsRef(ConfigObject q, ConfigObject obj) {
        if (obj.isCl()) {
            q.setTlsRef(obj.getId());
        } else {
            obj.setTlsRef(q.getId());
        }
    }


    private ConfigObject createKri(String id, int knotenNr) {
        ConfigObject uz = vrz.getChildren().get(0);
        int kriCount = uz.getChildren().size();
        ConfigObject kri = new ConfigObject(Types.ConfigItemType.KRI, id, 200 + kriCount, knotenNr);

        uz.getChildren().add(kri);
        kri.setTlsRef(uz.getId());
        return kri;
    }

    private ConfigObject createKri(int knotenNr) {
        log.warn("Missing KRI in Lookup! KRI with knotennummer='" + knotenNr + "' will be created by this importer...");
        ConfigObject uz = vrz.getChildren().get(0);
        int kriCount = uz.getChildren().size();
        ConfigObject kri = new ConfigObject(Types.ConfigItemType.KRI, "KRI" + kriCount, 200 + kriCount, knotenNr);

        uz.getChildren().add(kri);
        kri.setTlsRef(uz.getId());
        return kri;
    }

    private void linkEa(ConfigObject parent, ConfigObject obj) {
        log.debug("  -> EA: {}", obj.getId());
        if (parent != null) {
            if (!obj.isCl()) {
                obj.setTlsRef(parent.getId());
            }
            parent.getChildren().add(obj);
        }
    }

    private void linkSst(ConfigObject kri, ConfigObject obj) {
        log.debug("  -> SSt: {}", obj.getId());
        kri.getChildren().add(obj);
        obj.setTlsRef(kri.getId());
    }

    /**
     * adds the IBs to the config
     */
    public void addIbs() {
        java.util.Set<ConfigObject> uzs = this.get(Types.ConfigItemType.UZ);

        for (ConfigObject uz : uzs) {
            java.util.List<ConfigObject> ibs = new ArrayList<>();
            for (ConfigObject child : uz.getChildren()) {
                if (!isPhysical(child)) {
                    continue;
                }
                if (child.getType() == Types.ConfigItemType.SST) {
                    ibs.addAll(createIBs(uz).toJavaList());
                    break;
                } else if (child.getType() == Types.ConfigItemType.KRI) {
                    ibs = createIBs(child).toJavaList();
                    child.getChildren().clear();
                    child.getChildren().addAll(ibs);
                    ibs.clear();
                }
            }
            if (!ibs.isEmpty()) {
                java.util.List<ConfigObject> qs = uz.getChildren().stream().filter(c -> !isPhysical(c)).collect(Collectors.toList());
                uz.getChildren().clear();
                uz.getChildren().addAll(ibs);
                uz.getChildren().addAll(qs);
            }
        }
    }

    private List<ConfigObject> createIBs(ConfigObject kri) {
        Map<Integer, List<ConfigObject>> ibMap = List.ofAll(kri.getChildren()).filter(TlsWorld::isPhysical).groupBy(sst -> sst.getPort());
        return ibMap.values().map(ssts -> createID(kri, ssts)).toList();
    }

    private ConfigObject createID(ConfigObject kri, List<ConfigObject> ssts) {
        ConfigObject ib = new ConfigObject(ssts.get(0), Types.ConfigItemType.IB, kri.getOsi7Address());
        ib.setTlsRef(kri.getId());
        ib.setLoc(kri.getLoc());
        ib.setDist(kri.getDist());
        ib.getChildren().clear();
        ib.getChildren().addAll(ssts.toJavaList());
        ssts.forEach(i -> i.setTlsRef(ib.getId()));
        return ib;
    }

    private static boolean isPhysical(ConfigObject obj) {
        return obj.getType() != Types.ConfigItemType.MQ && obj.getType() != Types.ConfigItemType.AQ;
    }

    /**
     * gets all objects with the requested type
     *
     * @param type type of requested objects
     * @return list of objects with the requested type
     */
    public java.util.Set<ConfigObject> get(Types.ConfigItemType type) {
        if (vrz.getType() == type) {
            return Collections.singleton(vrz);
        }
        return get(type, vrz.getChildren());
    }

    private static java.util.Set<ConfigObject> get(Types.ConfigItemType type, Collection<ConfigObject> children) {
        java.util.Set<ConfigObject> items = new java.util.HashSet<>();
        children.forEach(i -> {
            if (i.getType() == type) {
                items.add(i);
            } else {
                items.addAll(get(type, i.getChildren()));
            }
        });
        return items;
    }

    /**
     * creates the header for a csv configTab
     *
     * @return the header for a csv configTab
     */
    public static String getHeaderString() {
        return io.vavr.collection.List.of(ConfigColumn.values()).map(ConfigColumn::name).mkString(SEPARATOR_COL);
    }

    /**
     * turns the given obj to a csv configTab entry
     *
     * @param obj obj to write
     * @return the object as csv configTab entry
     */
    public static String toConfTabEntry(ConfigObject obj) {
        java.util.List<String> record = new java.util.ArrayList<>();
        for (int i = 0; i < 27; i++) {
            record.add("");
        }
        record.add(ConfigColumn.ID_CLASS.ordinal(), getTypeName(obj));
        record.add(ConfigColumn.ID_PERM.ordinal(), obj.getId());
        record.add(ConfigColumn.ID_NAME.ordinal(), obj.getName() == null ? "" : obj.getName());
        record.add(ConfigColumn.TLS_KNOTENNUMMER.ordinal(), obj.getOsi7Address() + "");
        record.add(ConfigColumn.TLS_LOC.ordinal(), obj.getLoc() + "");
        record.add(ConfigColumn.TLS_DIST.ordinal(), obj.getDist() + "");
        record.add(ConfigColumn.TLS_FG.ordinal(), obj.getFg() + "");
        record.add(ConfigColumn.TLS_DE.ordinal(), obj.getDe() + "");
        record.add(ConfigColumn.TLS_PORT.ordinal(), obj.getPort() == -1 ? "" : obj.getPort() + "");
        record.add(ConfigColumn.TLS_SLAVE.ordinal(), obj.getSlave() + "");
        record.add(ConfigColumn.TLS_EAK.ordinal(), obj.getEak() + "");
        record.add(ConfigColumn.TLS_ANZEIGEPRINZIP.ordinal(), "");
        record.add(ConfigColumn.TLS_TYP.ordinal(), obj.getTlsTyp() + "");
        record.add(ConfigColumn.TLS_REF.ordinal(), obj.getTlsRef() == null ? "" : obj.getTlsRef());
        record.add(ConfigColumn.LOC_LANE.ordinal(), obj.getLane() == null ? "" : obj.getLane());
        record.add(ConfigColumn.LOC_ID_ASFiNAG.ordinal(), obj.getRoad() == null ? "" : obj.getRoad());
        record.add(ConfigColumn.EFH.ordinal(), obj.getEfh() == null ? "" : obj.getEfh());

        return String.join(SEPARATOR_COL, record);
    }

    private static String getTypeName(ConfigObject obj) {
        if (obj.getType() == null) {
            return "GRP";
        }
        switch (obj.getType()) {
            case MQ:
                return "EQ";
            case VDE:
                return "MQ";
            case WZG:
                return "WVZ";
            case UDE:
                return "UFD";
            default:
                return obj.getType().name();
        }
    }

    /**
     * adds the www chains to the config
     * @param wwwChains list of WwwChainReader.WWWChainRecord
     */
    public void addWwwChains(List<WwwChainReader.WWWChainRecord> wwwChains) {
        java.util.Map<String, ConfigObject> aqs = this.get(Types.ConfigItemType.AQ).stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c));
//        Set<ConfigObject> aqSet = this.get(Types.ConfigItemType.AQ);
//        java.util.Map<String, ConfigObject> aqs = new HashMap<>();
//        for(ConfigObject obj : aqSet) {
//            aqs.put(obj.getId(), obj);
//        }
        ConfigObject chainObject = null;
        for (WwwChainReader.WWWChainRecord chain : wwwChains) {
            String type = chain.get(WwwChainReader.WWWColumnType.TYPE);
            switch (type) {
                case "WWWSET":
                    chainObject = new ConfigObject(chain);
                    this.wwwChains.put(chainObject.getId(), chainObject);
                    break;
                case "WWWPANEL":
                    String id = chain.get(WwwChainReader.WWWColumnType.ID);
                    ConfigObject aq = aqs.get(id);
                    if (aq != null) {
                        assert chainObject != null;
                        aq.setTlsRef(aq.getTlsRef() + "|" + chainObject.getId());
                        aq.setTlsTyp(Types.AqType.WWW.getNumber());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Type: " +type);
            }
        }
    }

    private enum ConfigColumn {
        ID_CLASS, ID_PERM, ID_NAME,

        TLS_KNOTENNUMMER, TLS_LOC, TLS_DIST, TLS_FG, TLS_DE, FD, TLS_PORT, TLS_SLAVE, TLS_EAK, TLS_EA,

        TLS_WZGTYP, TLS_ZEICHENSATZ, TLS_ANZEIGEPRINZIP,

        TLS_HERSTELLER, TLS_TYP,

        TLS_REF,

        TLS_FAHRTR, LOC_LANE,

        LOC_WGS84_COOR_LAT, LOC_WGS84_COOR_LONG, LOC_ID_ASFiNAG,// NOSONAR
        EFH,

        TLS_IFACEKEY
    }

}
