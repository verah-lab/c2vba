package de.heuboe.tls.receiver.core.test;

import de.heuboe.tls.receiver.interfaces.AddressConverter;

public class TestAddressConverter implements AddressConverter {

    @Override
    public String convert(int node, int fg, int de) {
        int id = de + 256 * node;
      return Integer.toString(id);
    }

}
