package gr.aueb.delorean.util.Encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ShortEncoder {
    public static void write(short number, ByteArrayOutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(number);
        outputStream.write(buffer.array());
    }

    public static short read(ByteArrayInputStream inputStream) throws IOException {
        byte[] byteArray = new byte[Short.BYTES];
        int k = inputStream.read(byteArray);
        if (k != Short.BYTES)
            throw new IOException();
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(byteArray);
        buffer.flip();

        return buffer.getShort();
    }
}
