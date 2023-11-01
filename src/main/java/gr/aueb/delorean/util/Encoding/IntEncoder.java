package gr.aueb.delorean.util.Encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IntEncoder {
    public static void write(int number, ByteArrayOutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(number);
        outputStream.write(buffer.array());
    }

    public static int read(ByteArrayInputStream inputStream) throws IOException {
        byte[] byteArray = new byte[Integer.BYTES];
        int k = inputStream.read(byteArray);
        if (k != Integer.BYTES) throw new IOException();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(byteArray);
        buffer.flip();

        return buffer.getInt();
    }
}
