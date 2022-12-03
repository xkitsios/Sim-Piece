package gr.aueb.delorean.simpiece;

import gr.aueb.delorean.util.VariableEncoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class SimPieceEncoder {
    private static TreeMap<Integer, HashMap<Double, ArrayList<Long>>> reshapeSegments(double epsilon, List<SimPieceSegment> segments) {
        TreeMap<Integer, HashMap<Double, ArrayList<Long>>> segmentsPerB = new TreeMap<>();
        for (SimPieceSegment segment : segments) {
            int b = (int) Math.round(segment.getB() / epsilon);
            double a = segment.getA();
            if (!segmentsPerB.containsKey(b)) {
                segmentsPerB.put(b, new HashMap<>());
            }
            if (!segmentsPerB.get(b).containsKey(a)) {
                segmentsPerB.get(b).put(a, new ArrayList<>());
            }
            segmentsPerB.get(b).get(a).add(segment.getInitTimestamp());
        }

        return segmentsPerB;
    }

    public static byte[] getBinary(double epsilon, List<SimPieceSegment> segments) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TreeMap<Integer, HashMap<Double, ArrayList<Long>>> segmentsPerB = reshapeSegments(epsilon, segments);
        int numB = segmentsPerB.size();
        int firstB = segmentsPerB.keySet().iterator().next();

        try {
            VariableEncoding.writeFloatToStream((float) epsilon, outputStream);
            VariableEncoding.writeUIntToStream(numB, outputStream);
            VariableEncoding.writeIntToStream(firstB, outputStream);
            int previousBQnt = firstB;
            for (Map.Entry<Integer, HashMap<Double, ArrayList<Long>>> segmentPerB : segmentsPerB.entrySet()) {
                int bQnt = segmentPerB.getKey();
                VariableEncoding.writeUIntWithFlagToStream(bQnt - previousBQnt, outputStream);
                previousBQnt = bQnt;
                VariableEncoding.writeUShortWithFlagToStream((short) segmentPerB.getValue().size(), outputStream);
                for (Map.Entry<Double, ArrayList<Long>> aPerB : segmentPerB.getValue().entrySet()) {
                    VariableEncoding.writeFloatToStream(aPerB.getKey().floatValue(), outputStream);
                    VariableEncoding.writeUShortWithFlagToStream((short) aPerB.getValue().size(), outputStream);
                    for (Long timestamp : aPerB.getValue())
                        VariableEncoding.writeUIntToStream(timestamp.intValue(), outputStream);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public static List<SimPieceSegment> readBinary(byte[] binary) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);
        List<SimPieceSegment> segments = new ArrayList<>();

        try{
            float epsilon = VariableEncoding.readFloatFromStream(inputStream);
            long numB = VariableEncoding.readUIntFromStream(inputStream);
            int previousBQnt = VariableEncoding.readIntFromStream(inputStream);
            for (int i = 0; i < numB; i++) {
                int bDiff = VariableEncoding.readUIntWithFlagFromStream(inputStream);
                float b = (bDiff + previousBQnt) * epsilon;
                previousBQnt = bDiff + previousBQnt;
                int numA = VariableEncoding.readUShortWithFlagFromStream(inputStream);
                for (int j = 0; j < numA; j++) {
                    float a = VariableEncoding.readFloatFromStream(inputStream);
                    int numTimestamps = VariableEncoding.readUShortWithFlagFromStream(inputStream);
                    for (int k = 0; k < numTimestamps; k++) {
                        long timestamp = VariableEncoding.readUIntFromStream(inputStream);
                        segments.add(new SimPieceSegment(timestamp, a, b));
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return segments.stream().sorted(Comparator.comparingDouble(SimPieceSegment::getInitTimestamp)).toList();
    }
}
