package gr.aueb.delorean.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/*
 * Source code by:
 * https://github.com/lemire/JavaFastPFOR/blob/master/src/main/java/me/lemire/integercompression/VariableByte.java
 */
public class VariableByte {

    private static byte extract7bits(int i, long val) {
        return (byte) ((val >> (7 * i)) & ((1 << 7) - 1));
    }

    private static byte extract7bitsmaskless(int i, long val) {
        return (byte) ((val >> (7 * i)));
    }

    public static void write(int number, ByteArrayOutputStream outputStream) {
        final long val = number & 0xFFFFFFFFL;

        if (val < (1 << 7)) {
            outputStream.write((byte) (val | (1 << 7)));
        } else if (val < (1 << 14)) {
            outputStream.write((byte) extract7bits(0, val));
            outputStream.write((byte) (extract7bitsmaskless(1, (val)) | (1 << 7)));
        } else if (val < (1 << 21)) {
            outputStream.write((byte) extract7bits(0, val));
            outputStream.write((byte) extract7bits(1, val));
            outputStream.write((byte) (extract7bitsmaskless(2, (val)) | (1 << 7)));
        } else if (val < (1 << 28)) {
            outputStream.write((byte) extract7bits(0, val));
            outputStream.write((byte) extract7bits(1, val));
            outputStream.write((byte) extract7bits(2, val));
            outputStream.write((byte) (extract7bitsmaskless(3, (val)) | (1 << 7)));
        } else {
            outputStream.write((byte) extract7bits(0, val));
            outputStream.write((byte) extract7bits(1, val));
            outputStream.write((byte) extract7bits(2, val));
            outputStream.write((byte) extract7bits(3, val));
            outputStream.write((byte) (extract7bitsmaskless(4, (val)) | (1 << 7)));
        }
    }

    public static int read(ByteArrayInputStream inputStream) {
        byte in;
        int v;

        in = (byte) inputStream.read();
        v = in & 0x7F;
        if (in < 0)
            return v;

        in = (byte) inputStream.read();
        v = ((in & 0x7F) << 7) | v;
        if (in < 0)
            return v;

        in = (byte) inputStream.read();
        v = ((in & 0x7F) << 14) | v;
        if (in < 0 )
            return v;

        in = (byte) inputStream.read();
        v = ((in & 0x7F) << 21) | v;
        if (in < 0)
            return v;

        v = (((byte) inputStream.read() & 0x7F) << 28) | v;

        return v;
    }
}
