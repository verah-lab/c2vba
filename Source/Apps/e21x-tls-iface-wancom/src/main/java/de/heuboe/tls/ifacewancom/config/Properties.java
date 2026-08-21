package de.heuboe.tls.ifacewancom.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import lombok.Getter;
import lombok.Setter;

/**
 * Class carrying the properties for iface processes
 * @author ronald
 *
 */
@Validated
@ConfigurationProperties(prefix="de.heuboe.tls.ifacewancom.config")
@Getter
@Setter
public class Properties {

    @NotNull
    private String uzId;                // Id of UZ

    @Positive
    private int ifaceKey;               // key to group of devices handled

    @NotNull
    private String rcvTopic;            // kafka topics where telegrams received are written to

    @NotNull
    private String sndTopic;            // kafka topics where telegrams to send are written to

    @NotNull
    private String commStateTopic;      // kafka topics where communications status received are written to

    @NotNull
    private String configFileWANCom;    // path of file containing configuration parameters for WANCom

    // job number restrictions
    // zero is always considered as spontaneous
    // numbers outside [minJobNr,maxJobNr] are mapped to spontaneous, since the are considered to intiated outside this application context
    // to use the whole range, use [1,255]
    @Positive
    private int minJobNr;               // minimal job number considered to be used in this application context

    @Positive
    private int maxJobNr;               // maximal job number considered to be used in this application context

    @NotNull
    private boolean checkLimits;         // if true job numbers are checked against min and max

    // control generation of time sync
    @NotNull
    private String timezone4Sync;       // e.g. Europe/Berlin (default) | GMT+01:00 | UTC | ...

    @NotNull
    private boolean useDstBit;          // use bit 7 for tls hour when dst is in effect

    // insert new root device. the comma seperated list itemizes name, id, cable name, ifaceKey, osi2 parent port, osi2 child port
    private String specialDevRoot = "";
}
