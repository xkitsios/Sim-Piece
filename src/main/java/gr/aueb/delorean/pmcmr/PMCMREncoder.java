package gr.aueb.delorean.pmcmr;

import gr.aueb.delorean.util.VariableEncoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PMCMREncoder {
    public static byte[] getBinary(List<PMCMRSegment> segments) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            VariableEncoding.writeUIntToStream(segments.size(), outputStream);
            for (PMCMRSegment segment : segments) {
                VariableEncoding.writeUIntToStream(segment.getInitialTimestamp(), outputStream);
                VariableEncoding.writeUIntToStream(segment.getFinalTimestamp(), outputStream);
                VariableEncoding.writeFloatToStream((float) segment.getValue(), outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public static List<PMCMRSegment> readBinary(byte[] binary) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);
        List<PMCMRSegment> segments = new ArrayList<>();

        try {
            long totalSegments = VariableEncoding.readUIntFromStream(inputStream);
            for (int i = 0; i < totalSegments; i++) {
                PMCMRSegment segment = new PMCMRSegment();
                segment.setInitialTimestamp(VariableEncoding.readUIntFromStream(inputStream));
                segment.setFinalTimestamp(VariableEncoding.readUIntFromStream(inputStream));
                segment.setValue(VariableEncoding.readFloatFromStream(inputStream));
                segments.add(segment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return segments.stream().sorted(Comparator.comparingDouble(PMCMRSegment::getInitialTimestamp)).toList();
    }
}
