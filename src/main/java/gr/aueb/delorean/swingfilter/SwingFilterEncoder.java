package gr.aueb.delorean.swingfilter;

import gr.aueb.delorean.util.VariableEncoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SwingFilterEncoder {
    public static byte[] getBinary(List<SwingFilterSegment> segments) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            VariableEncoding.writeUIntToStream(segments.size(), outputStream);
            for (SwingFilterSegment segment : segments){
                VariableEncoding.writeUIntToStream(segment.getInitialTimestamp(), outputStream);
                VariableEncoding.writeFloatToStream((float) segment.getInitialValue(), outputStream);
            }

            VariableEncoding.writeUIntToStream(segments.get(segments.size() - 1).getLastTimestamp(), outputStream);
            VariableEncoding.writeFloatToStream((float) segments.get(segments.size() - 1).getLastValue(), outputStream);
        } catch (Exception e){
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public static List<SwingFilterSegment> readBinary(byte[] binary) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);
        List<SwingFilterSegment> segments = new ArrayList<>();

        try {
            long totalSegments = VariableEncoding.readUIntFromStream(inputStream);
            if (totalSegments == 0)
                return segments;

            long initialTimestamp = VariableEncoding.readUIntFromStream(inputStream);
            float initialValue = VariableEncoding.readFloatFromStream(inputStream);
            for (int i = 1; i < totalSegments; i++) {
                long lastTimestamp = VariableEncoding.readUIntFromStream(inputStream);
                float lastValue = VariableEncoding.readFloatFromStream(inputStream);
                segments.add(new SwingFilterSegment(initialTimestamp, initialValue, lastTimestamp, lastValue));
                initialTimestamp = lastTimestamp;
                initialValue = lastValue;
            }
            long lastTimestamp = VariableEncoding.readUIntFromStream(inputStream);
            float lastValue = VariableEncoding.readFloatFromStream(inputStream);
            segments.add(new SwingFilterSegment(initialTimestamp, initialValue, lastTimestamp, lastValue));
        } catch (Exception e){
            e.printStackTrace();
        }

        return segments.stream().sorted(Comparator.comparingDouble(SwingFilterSegment::getInitialTimestamp)).toList();
    }
}
