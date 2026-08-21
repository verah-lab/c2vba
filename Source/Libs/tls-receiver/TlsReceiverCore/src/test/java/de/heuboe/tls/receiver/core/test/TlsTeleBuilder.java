package de.heuboe.tls.receiver.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.TlsTele.Direction;

public class TlsTeleBuilder {

    public enum EndianType {
        LITTLE,
        BIG;
    }

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private List<Integer> deBlockLengthIndizes = new ArrayList<>();  // stores the start positions of each DeBlock
    
    
    /**
     * Die Anzahl der DE-Blöcke wird automatisch hinzugefügt
     * @param fg               Funktionsgruppe
     * @param id               Richtung/Anwendungsidentifier
     */
    public TlsTeleBuilder(int fg, int id) {
        stream.write(1); // Knotennummer
        stream.write(2); // Knotennummer
        stream.write(3); // Knotennummer
        stream.write(1); // Anzahl Einzeltelegramme
        stream.write(0); // Länge Einzeltelegramme
        addByte(fg); // Funktionsgruppe
        addByte(id); // Richtung/Anwendungsidentifier (ID)
        stream.write(0); // Jobnummer
        stream.write(-1); // Anzahl DE-Blöcke (will be overwritten at the end)
    }

    public byte[] getAsByteArray() {
        byte[] bytes = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Stream closed failed", e);
        }
        
        bytes[8] = (byte) deBlockLengthIndizes.size(); // Anzahl DE-Blöcke
        
        deBlockLengthIndizes.add(bytes.length);
        
        for(int i = 0; i< deBlockLengthIndizes.size()-1;i++) {
            int index = deBlockLengthIndizes.get(i);
            bytes[index] = (byte) (deBlockLengthIndizes.get(i+1)-index-1); // Länge der einzelnen DE-Blöcke
        }
        
        return bytes;
    }

    public TlsTele getAsTlsTele() {
        final int etelLengthPosition = 4;
        byte[] bytes = getAsByteArray();
        int osi7Len = bytes.length;
        TlsTele tel = null;
        bytes[etelLengthPosition] = (byte) (bytes.length - 5);
        try {
            tel = new TlsTele(new Date(), Direction.RECEIVE, 1001 * 256 + 1, bytes, 0, osi7Len);
        } catch (TlsBadTele e) {
            e.printStackTrace();
        }
        return tel;
    }

    /**
     * Die Länge des DE-Blocks wird automatisch hinzugefügt
     * @param deNumber
     * @param deTyp
     * @return
     */
    public TlsTeleBuilder addDeBlockHeader(int deNumber, int deTyp) {
        deBlockLengthIndizes.add(stream.size());
        addByte(0); // Länge des DE-Blocks (will be overwritten at the end)
        addByte(deNumber);
        addByte(deTyp);
        return this;
    }

    public TlsTeleBuilder addByteArray(byte... values) {
        try {
            stream.write(values);
        } catch (IOException e) {
            throw new UncheckedIOException("byte array couldn't be added to stream", e);
        }
        return this;
    }

    public TlsTeleBuilder addByte(int value) {
        checkInt8(value);
        stream.write(value);
        return this;
    }

    public TlsTeleBuilder addByteArrayWithSize(int... values) {
        
        addByte(values.length);
        for (int value : values) {
            addByte(value);
        }
        return this;
    }

    public TlsTeleBuilder addInt16(int value) {
        return addInt16(EndianType.LITTLE, value);
    }

    public TlsTeleBuilder addInt16(EndianType endian, int value) {
        checkInt16(value);
        byte[] bytes = new byte[2];
        if (endian == EndianType.LITTLE) {
            bytes[0] = (byte) (value & 0xff);
            bytes[1] = (byte) ((value >> 8) & 0xff);
        } else {
            bytes[0] = (byte) ((value >> 8) & 0xff);
            bytes[1] = (byte) (value & 0xff);
        }
        addByteArray(bytes);

        return this;
    }

    public TlsTeleBuilder addInt16Array(int... values) {
        for (int value : values) {
            addInt16(value);
        }
        return this;
    }

    public TlsTeleBuilder addInt16Array(EndianType endian, int... values) {
        for (int value : values) {
            addInt16(endian, value);
        }
        return this;
    }

    public TlsTeleBuilder addInt24(int value) {
        return addInt24(EndianType.LITTLE, value);
    }

    public TlsTeleBuilder addInt24(EndianType endian, int value) {
        checkInt24(value);
        byte[] bytes = new byte[3];
        if (endian == EndianType.LITTLE) {
            bytes[0] = (byte) (value & 0xff);
            bytes[1] = (byte) ((value >> 8) & 0xff);
            bytes[2] = (byte) ((value >> 16) & 0xff);
        } else {
            bytes[0] = (byte) ((value >> 16) & 0xff);
            bytes[1] = (byte) ((value >> 8) & 0xff);
            bytes[2] = (byte) (value & 0xff);
        }
        addByteArray(bytes);
        return this;
    }

    public TlsTeleBuilder addInt24Array(int... values) {
        for (int value : values) {
            addInt24(value);
        }
        return this;
    }

    public TlsTeleBuilder addInt24Array(EndianType endian, int... values) {
        for (int value : values) {
            addInt24(endian, value);
        }
        return this;
    }

    public TlsTeleBuilder addInt32(long value) {

        return addInt32(EndianType.LITTLE, value);
    }

    public TlsTeleBuilder addInt32(EndianType endian, long value) {
        checkInt32(value);
        byte[] bytes = new byte[4];
        if (endian == EndianType.LITTLE) {
            bytes[0] = (byte) (value & 0xff);
            bytes[1] = (byte) ((value >> 8) & 0xff);
            bytes[2] = (byte) ((value >> 16) & 0xff);
            bytes[3] = (byte) ((value >> 24) & 0xff);
        } else {
            bytes[0] = (byte) ((value >> 24) & 0xff);
            bytes[1] = (byte) ((value >> 16) & 0xff);
            bytes[2] = (byte) ((value >> 8) & 0xff);
            bytes[3] = (byte) (value & 0xff);
        }
        addByteArray(bytes);
        return this;
    }

    public TlsTeleBuilder addInt32Array(int... values) {
        for (int value : values) {
            addInt32(value);
        }
        return this;
    }

    public TlsTeleBuilder addInt32Array(EndianType endian, int... values) {
        for (int value : values) {
            addInt32(endian, value);
        }
        return this;
    }

    public TlsTeleBuilder addBCD(int numberOfBytes, String value) {

        if (value.length() != numberOfBytes * 2) {
            throw new IllegalArgumentException(
                    "The String \"" + value + "\" does not match the the number of bytes (" + numberOfBytes + ")");
        }
        char[] chars = value.toCharArray();

        for (int i = 0; i < chars.length; i += 2) {
            int digitHigh = Character.getNumericValue(chars[i]);
            int digitLow = Character.getNumericValue(chars[i + 1]);
            addBCD(digitHigh, digitLow);
        }

        return this;
    }

    private TlsTeleBuilder addBCD(int digitHigh, int digitLow) {
        if (digitHigh < 0 || digitHigh > 9 || digitLow < 0 || digitLow > 9) {
            throw new IllegalArgumentException("The values are not single digit numbers");
        }
        byte value = (byte) ((digitHigh << 4) + digitLow);
        stream.write(value);
        return this;
    }

    public TlsTeleBuilder addFloat(float value) {
        byte[] bytes = ByteBuffer.allocate(4).putFloat(value).array();
        addByte(bytes[3]);
        addByte(bytes[2]);
        addByte(bytes[1]);
        addByte(bytes[0]);
        return this;
    }

    private void checkInt8(int value) {
        if (value < Byte.MIN_VALUE || value > 255) {
            throw new IllegalArgumentException(value + " is outside the scope of 8 Bit");
        }
    }

    private void checkInt16(int value) {
        if (value < Short.MIN_VALUE || value > 65535) {
            throw new IllegalArgumentException(value + " is outside the scope of 16 Bit");
        }
    }

    private void checkInt24(int value) {
        if (value < -8388608 || value > 16777215) {
            throw new IllegalArgumentException(value + " is outside the scope of 24 Bit");
        }
    }

    private void checkInt32(long value) {
        if (value < Integer.MIN_VALUE || value > 4294967295L) {
            throw new IllegalArgumentException(value + " is outside the scope of 32 Bit");
        }
    }

}
