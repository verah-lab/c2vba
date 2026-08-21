package de.heuboe.tls.iface.prot.tlsoip;

/**
 * a helper class the capsulates the byte array of a telegram. This enables the
 * use of telegrams in container classes.
 * 
 * @author ralfz
 *
 */
class Telegram {

    byte[] telegram; // NOSONAR fear legacy use

    Telegram( byte[] telegram ) {
        super();
        this.telegram = telegram;
    }

    byte[] getTelegram() {
        return telegram;
    }
}
