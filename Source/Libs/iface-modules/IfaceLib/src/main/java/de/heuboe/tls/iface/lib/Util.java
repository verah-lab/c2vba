package de.heuboe.tls.iface.lib;

/**
 * Static utility methods.
 * @author ralfz
 *
 */
public class Util {

	private Util() {
	}
	
	
	/**
	 * Convert a byte to string of hex digits
	 * @param b A byte to convert
	 * @return A string with hex digits (pairs)
	 */
	public static String toHex(byte b) {
		int i = Byte.toUnsignedInt(b);
		String hex = Integer.toHexString(i);
		if (hex.length() == 1) {
			hex = "0" + hex;
		}
		return hex;
	}
	
	/**
	 * Convert byte array to string of hex digits
	 * @param bArray Array of bytes to convert
	 * @return A string with hex digits (pairs)
	 */
	public static String toHex(byte[] bArray) {
		return toHex(bArray, 0, bArray.length);
	}
	
	/**
	 * Convert byte array to string of hex digits
	 * @param bArray Array of bytes to convert
	 * @param offset Offset where conversion shall start
	 * @param size length of area to be converted
	 * @return A string with hex digits (pairs)
	 */
	public static String toHex(byte[] bArray, int offset, int size) {
		StringBuilder buf = new StringBuilder(3*bArray.length);
		for(int i=offset; i<offset+size && i<bArray.length; ++i) {
			byte b = bArray[i];
			String hex = toHex(b);
			buf.append(hex);
			buf.append(' ');
		}
		return buf.toString().trim();
	}
	
	/**
	 * A string with the unsigned interpretation of a byte (i.e. add 256 if negative)
	 * @param b The byte value to be interpreted
	 * @return The corresponding unsigned value as string
	 */
	public static String toUnsignedString(byte b) {
		if (b<0) {
			return Integer.toString((int)b+256); // NOSONAR make it obvious
		}
		return Integer.toString(b);
	}
	
	/**
	 * Convert byte value to unsigned int (i.e. add 256 if negative)
	 * @param b the byte value to convert
	 * @return the unsigned int value of the byte
	 */
	public static int toUnsignedInt(byte b) {
		if (b<0) {
			return (int)(b+256); // NOSONAR make it obvious
		}
		return b;
	}
	
	/**
	 * Convert byte tu unsigned short
	 * @param b The byte to convert
	 * @return The unsigned short value of the byte as short
	 */
	public static short toUnsignedShort(byte b) {
		return (short) toUnsignedInt(b);
	}
	
	/**
	 * Construct a key consisting of osi2 ports
	 * @param osi2Port The devices port number outgoing
	 * @param osi2Partner The partner devices port number incoming
	 * @return The conbination as string: osi2Port:osi2Partner
	 */
	public static String getKey(short osi2Port, short osi2Partner) {
		return osi2Port + ":" + osi2Partner;
	}
	
	/**
	 * Convert a decimal osi7 node number to RDS notation
	 * @param node the given node number of a TLS device in one decimal
	 * @return node number in RDS notation
	 */
	public static String nodeToString(int node) {
		return node / 256 + "-" + node % 256;
	}
}
