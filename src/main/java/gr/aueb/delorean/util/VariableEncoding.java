package gr.aueb.delorean.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VariableEncoding {
    public static void writeFloatToStream(float number, ByteArrayOutputStream outputStream) throws IOException {
        int intBits = Float.floatToIntBits(number);
        writeIntToStream(intBits, outputStream);
    }

    public static float readFloatFromStream(ByteArrayInputStream inputStream) throws IOException {
        int number = readIntFromStream(inputStream);
        return Float.intBitsToFloat(number);
    }

    public static void writeIntToStream(int number, ByteArrayOutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(number);
        outputStream.write(buffer.array());
    }

    public static int readIntFromStream(ByteArrayInputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(inputStream.readNBytes(Integer.BYTES));
        buffer.flip();

        return buffer.getInt();
    }

    public static void writeUIntToStream(long number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8 * 4) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned Int");
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt((int) (number & 0xffffffffL));
        outputStream.write(buffer.array());
    }

    public static long readUIntFromStream(ByteArrayInputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(inputStream.readNBytes(Integer.BYTES));
        buffer.flip();

        return buffer.getInt() & 0xffffffffL;
    }

    public static void writeShortToStream(short number, ByteArrayOutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(number);
        outputStream.write(buffer.array());
    }

    public static short readShortFromStream(ByteArrayInputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(inputStream.readNBytes(Short.BYTES));
        buffer.flip();

        return buffer.getShort();
    }

    public static void writeUShortToStream(int number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8 * 2) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned Int");
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put((byte) (number & 0xff));
        buffer.put((byte) ((number >> 8) & 0xff));
        outputStream.write(buffer.array());
    }

    public static int readUShortFromStream(ByteArrayInputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(inputStream.readNBytes(Short.BYTES));
        buffer.flip();

        int number = (short) (buffer.get() & 0xff);
        number |= (buffer.get() & 0xff) << 8;
        return number;
    }


    public static void writeUByteToStream(short number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned Short");
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put((byte) (number & 0xff));
        outputStream.write(buffer.array());
    }

    public static short readUByteFromStream(ByteArrayInputStream inputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put(inputStream.readNBytes(Byte.BYTES));
        buffer.flip();

        return (short) (buffer.get() & 0xff);
    }

    public static void writeUShortWithFlagToStream(short number, ByteArrayOutputStream outputStream) throws IOException {
        if (number < Math.pow(2, 8) - 1) {
            writeUByteToStream(number, outputStream);
        } else {
            writeUByteToStream((short) (Math.pow(2, 8) - 1), outputStream);
            writeUShortToStream(number, outputStream);
        }
    }

    public static short readUShortWithFlagFromStream(ByteArrayInputStream inputStream) throws IOException {
        int number = readUByteFromStream(inputStream);
        if (number == Math.pow(2, 8) - 1)
            number = readUShortFromStream(inputStream);

        return (short) number;
    }

    public static void writeUIntWithFlagToStream(int number, ByteArrayOutputStream outputStream) throws IOException {
        if (number < Math.pow(2, 8) - 1) {
            writeUByteToStream((short) number, outputStream);
        } else {
            writeUByteToStream((short) (Math.pow(2, 8) - 1), outputStream);
            writeUIntToStream(number, outputStream);
        }
    }

    public static int readUIntWithFlagFromStream(ByteArrayInputStream inputStream) throws IOException {
        int number = readUByteFromStream(inputStream);
        if (number == Math.pow(2, 8) - 1)
            number = readIntFromStream(inputStream);

        return number;
    }
}
