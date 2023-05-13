package gr.aueb.delorean.util.Encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class UShortEncoder {
    public static void write(int number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8 * 2) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned short");
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put((byte) (number & 0xff));
        buffer.put((byte) ((number >> 8) & 0xff));
        outputStream.write(buffer.array());
    }

    public static int read(ByteArrayInputStream inputStream) throws IOException {
        byte[] byteArray = new byte[Short.BYTES];
        int k = inputStream.read(byteArray);
        if (k != Short.BYTES)
            throw new IOException();
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(byteArray);
        buffer.flip();

        int number = (short) (buffer.get() & 0xff);
        number |= (buffer.get() & 0xff) << 8;
        return number;
    }

    public static void writeWithFlag(int number, ByteArrayOutputStream outputStream) throws IOException {
        if (number < Math.pow(2, 8) - 1) {
            UByteEncoder.write((short) number, outputStream);
        } else {
            UByteEncoder.write((short) (Math.pow(2, 8) - 1), outputStream);
            write(number, outputStream);
        }
    }

    public static int readWithFlag(ByteArrayInputStream inputStream) throws IOException {
        int number = UByteEncoder.read(inputStream);
        if (number == Math.pow(2, 8) - 1)
            number = read(inputStream);

        return number;
    }
}
