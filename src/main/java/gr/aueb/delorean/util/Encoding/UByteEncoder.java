package gr.aueb.delorean.util.Encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class UByteEncoder {
    public static void write(short number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned short");
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put((byte) (number & 0xff));
        outputStream.write(buffer.array());
    }

    public static short read(ByteArrayInputStream inputStream) throws IOException {
        byte[] byteArray = new byte[Byte.BYTES];
        int k = inputStream.read(byteArray);
        if (k != Byte.BYTES)
            throw new IOException();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put(byteArray);
        buffer.flip();

        return (short) (buffer.get() & 0xff);
    }
}
