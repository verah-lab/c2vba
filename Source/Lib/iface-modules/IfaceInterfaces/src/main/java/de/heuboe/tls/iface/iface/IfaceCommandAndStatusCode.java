package de.heuboe.tls.iface.iface;

/**
 * Thes codes are use to interact with iface processes directly
 * @author ronald
 *
 */
public enum IfaceCommandAndStatusCode {
    STARTCOMM( (byte) 0 ), STOPCOMM( (byte) 1 ), WERLEBT( (byte) 2 ), COMMSTAT( (byte) 3 ), TIMESYNC( (byte) 4 ), COMMSTATDETAIL( (byte) 5 );

    private byte val;

    private IfaceCommandAndStatusCode( byte val ) {
        this.val = val;
    }

    /**
     * @return The byte value of the enum value.
     */
    public byte getVal() {
        return val;
    }
}
