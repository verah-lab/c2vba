package de.heuboe.tls.receiver.impl;

import de.heuboe.tls.receiver.interfaces.AddressConverter;

public class AddressConverterGenericImpl implements AddressConverter {

	@Override
	public String convert(int node, int fg, int de) {
		return Integer.toString(node/256) + "-" + node%256 + "-" + de + "-" + fg; // rn changed order 2108-05-02
	}
}
