package de.heuboe.asfinag.vmis2.synchronize.vd.data;

import java.time.Instant;
import com.google.protobuf.ByteString;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurz;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerung;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUE;

public class TestData {
    private static boolean first = true;
    private static int TRAFFIC_CATEGORY_PKW = 32;
    private static int TRAFFIC_CATEGORY_LKW = 33;
    private static final byte[] DEFAULT_TRAFFIC_CATEGORIES_1 = {(byte) 10, (byte) 50, (byte) 120, (byte) 165};
    private static final byte[] DEFAULT_TRAFFIC_CATEGORIES_2 = {(byte) 10, (byte) 55, (byte) 80, (byte) 120};
    private static final ByteString  DEFAULT_TRAFFIC_CATEGORIES__BYTESTRING_1  = ByteString.copyFrom(DEFAULT_TRAFFIC_CATEGORIES_1);
    private static final ByteString  DEFAULT_TRAFFIC_CATEGORIES__BYTESTRING_2  = ByteString.copyFrom(DEFAULT_TRAFFIC_CATEGORIES_2);

    
    private TestData() {
        // hide public
    }

    public static PLVEBetriebsparameter getBetriebsparam(String id, int version) {
        return PLVEBetriebsparameter.builder().id(id).alpha1(0.30078125).alpha2(0.30078125)
            .artMittelwertbildung(1).datenversionKurz(version).datenversionLang(13)
            .erfassungsintervalldauerKurz(4).erfassungsintervalldauerLang(129).jobnummer(0)
            .laengengrenzwert(525).processTime(Instant.now()).startwertMittelwertbildung(60)
            .tlsTime(Instant.now()).build();
    }

    public static PLVEDeFehler getDeFehler(String id) {
        return PLVEDeFehler.builder().id(id).fehlercode(1).hersteller(10).jobnummer(1)
                .tlsTime(Instant.now()).processTime(Instant.now()).build();
    }

    public static PSYSFehlerDUE getSysFehlerDue(String id) {
        return PSYSFehlerDUE.builder().id(id).fehlercode(1).hersteller(10).jobnummer(1)
                .tlsTime(Instant.now()).processTime(Instant.now()).build();
    }

    public static PLVEKanalsteuerung getKanalsteuerung(String id) {
        return PLVEKanalsteuerung.builder().id(id).tlsTime(Instant.now()).processTime(Instant.now())
                .jobnummer(0).kanalsteuerbyte(1).build();
    }

    public static PLVEGeschwindigkeitsklassenKurz getGeschwkl(String id, int fahrzeugklasse) {
        if (fahrzeugklasse == TRAFFIC_CATEGORY_PKW) {
             return PLVEGeschwindigkeitsklassenKurz.builder().id(id)
                    .vGrenzen(DEFAULT_TRAFFIC_CATEGORIES__BYTESTRING_1)
                    .fahrzeugklasse(fahrzeugklasse)
                    .processTime(Instant.now())
                    .tlsTime(Instant.now()).build();
        } else {
            return PLVEGeschwindigkeitsklassenKurz.builder().id(id)
                    .vGrenzen(DEFAULT_TRAFFIC_CATEGORIES__BYTESTRING_2)
                    .fahrzeugklasse(fahrzeugklasse)
                    .processTime(Instant.now())
                    .tlsTime(Instant.now()).build();           
        }
    }
}
