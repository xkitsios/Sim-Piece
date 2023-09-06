package gr.aueb.delorean.simpiece;

import com.github.luben.zstd.Zstd;
import gr.aueb.delorean.util.Point;
import gr.aueb.delorean.util.Encoding.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class SimPiece {
    private List<SimPieceSegment> segments;
    private long lastTimeStamp;
    private double epsilon;
    public SimPiece(List<Point> points, double epsilon){
        this.segments = new ArrayList<>();
        this.epsilon = epsilon;
        this.lastTimeStamp = points.get(points.size() - 1).getTimestamp();
        compress(points, epsilon);
        mergeSegments();
    }

    public SimPiece(byte[] bytes, boolean variableByte, boolean zstd) {
        this.segments = new ArrayList<>();
        readByteArray(bytes, variableByte, zstd);
    }

    private double quantization(double value) {
        return Math.round(value / epsilon) * epsilon;
    }

    private void compress(List<Point> points, double epsilon) {
        if (points.isEmpty())
            return;

        double aMax = Double.MAX_VALUE;
        double aMin = Double.MIN_VALUE;

        long segmentSize = 1;
        long segmentInitTimestamp = points.get(0).getTimestamp();
        double segmentInitValue = points.get(0).getValue();
        double b = quantization(segmentInitValue);

        for (Point point : points) {
            if (point == points.get(0))
                continue;
            segmentSize++;
            double upValue = point.getValue() + epsilon;
            double downValue = point.getValue() - epsilon;


            if (segmentSize > 2) {
                double upLim = aMax * (point.getTimestamp() - segmentInitTimestamp) + b;
                double downLim = aMin * (point.getTimestamp() - segmentInitTimestamp) + b;
                if (downValue > upLim || upValue < downLim) {
                    segments.add(new SimPieceSegment(segmentInitTimestamp, aMin, aMax, b));
                    segmentSize = 1;
                    segmentInitTimestamp = point.getTimestamp();
                    segmentInitValue = point.getValue();
                    b = quantization(segmentInitValue);
                    continue;
                }
            }

            double aMaxTemp = (upValue - quantization(segmentInitValue)) /
                    (point.getTimestamp() - segmentInitTimestamp);
            double aMinTemp = (downValue - quantization(segmentInitValue)) /
                    (point.getTimestamp() - segmentInitTimestamp);

            if (segmentSize == 2) {
                aMax = aMaxTemp;
                aMin = aMinTemp;
            } else {
                double upLim = aMax * (point.getTimestamp() - segmentInitTimestamp) + b;
                double downLim = aMin * (point.getTimestamp() - segmentInitTimestamp) + b;
                if (upValue < upLim)
                    aMax = Math.max(aMaxTemp, aMin);
                if (downValue > downLim)
                    aMin = Math.min(aMinTemp, aMax);
            }
        }
        if (segmentSize != 0)
            segments.add(new SimPieceSegment(segmentInitTimestamp, aMin, aMax, b));
    }


    private void mergeSegments() {
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<SimPieceSegment> segmentsMerged = new ArrayList<>();
        SimPieceSegment mergedSeg;

        TreeMap<Double, List<SimPieceSegment>> segmentsPerB = new TreeMap<>();
        for (SimPieceSegment segment : segments) {
            List<SimPieceSegment> temp = segmentsPerB.getOrDefault(segment.getB(), new ArrayList<>());
            temp.add(segment);
            segmentsPerB.put(segment.getB(), temp);
        }
        for (Map.Entry<Double, List<SimPieceSegment>> bSegments : segmentsPerB.entrySet()) {
            bSegments.getValue().sort(Comparator.comparingDouble(SimPieceSegment::getAMin));
            Iterator<SimPieceSegment> it = bSegments.getValue().iterator();
            SimPieceSegment currentSeg = it.next();
            mergedSeg = new SimPieceSegment(-1, currentSeg.aMin, currentSeg.aMax, currentSeg.b);
            timestamps.add(currentSeg.getInitTimestamp());
            while (it.hasNext()) {
                currentSeg = it.next();
                if (currentSeg.aMin <= mergedSeg.aMax && currentSeg.aMax >= mergedSeg.aMin) {
                    timestamps.add(currentSeg.getInitTimestamp());
                    mergedSeg.aMin = Math.max(mergedSeg.aMin, currentSeg.aMin);
                    mergedSeg.aMax = Math.min(mergedSeg.aMax, currentSeg.aMax);
                } else {
                    for (long timestamp : timestamps)
                        segmentsMerged.add(new SimPieceSegment(timestamp, mergedSeg.getAMin(), mergedSeg.getAMax(), mergedSeg.getB()));
                    timestamps.clear();
                    mergedSeg = new SimPieceSegment(-1, currentSeg.aMin, currentSeg.aMax, currentSeg.b);
                    timestamps.add(currentSeg.getInitTimestamp());
                }
            }
            if (timestamps.size() > 0){
                for (long timestamp : timestamps)
                    segmentsMerged.add(new SimPieceSegment(timestamp, mergedSeg.getAMin(), mergedSeg.getAMax(), mergedSeg.getB()));
                timestamps.clear();
            }
        }

        segmentsMerged.sort(Comparator.comparingLong(SimPieceSegment::getInitTimestamp));
        segments = segmentsMerged;
    }

    public List<Point> decompress() {
        List<Point> points = new ArrayList<>();
        long currentTimeStamp = 0;

        for (int i = 0; i < segments.size() - 1; i++){
            while (currentTimeStamp < segments.get(i + 1).getInitTimestamp()) {
                points.add(new Point(currentTimeStamp, segments.get(i).getA() * (currentTimeStamp - segments.get(i).getInitTimestamp()) + segments.get(i).getB()));
                currentTimeStamp++;
            }
        }

        while (currentTimeStamp <= lastTimeStamp) {
            points.add(new Point(currentTimeStamp, segments.get(segments.size() - 1).getA() * (currentTimeStamp - segments.get(segments.size() - 1).getInitTimestamp()) + segments.get(segments.size() - 1).getB()));
            currentTimeStamp++;
        }

        return points;
    }

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

    public byte[] toByteArray(boolean variableByte, boolean zstd) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TreeMap<Integer, HashMap<Double, ArrayList<Long>>> segmentsPerB = reshapeSegments(epsilon, segments);
        int numB = segmentsPerB.size();
        int firstB = segmentsPerB.keySet().iterator().next();
        byte[] bytes = null;

        try {
            FloatEncoder.write((float) epsilon, outputStream);
            VariableByteEncoder.write(numB, outputStream);
            IntEncoder.write(firstB, outputStream);
            int previousBQnt = firstB;
            for (Map.Entry<Integer, HashMap<Double, ArrayList<Long>>> segmentPerB : segmentsPerB.entrySet()) {
                int bQnt = segmentPerB.getKey();
                UIntEncoder.writeWithFlag(bQnt - previousBQnt, outputStream);
                previousBQnt = bQnt;
                VariableByteEncoder.write(segmentPerB.getValue().size(), outputStream);
                for (Map.Entry<Double, ArrayList<Long>> aPerB : segmentPerB.getValue().entrySet()) {
                    FloatEncoder.write(aPerB.getKey().floatValue(), outputStream);
                    VariableByteEncoder.write(aPerB.getValue().size(), outputStream);
                    long previousTS = 0;
                    for (Long timestamp : aPerB.getValue()) {
                        if (variableByte)
                            VariableByteEncoder.write((int) (timestamp - previousTS), outputStream);
                        else
                            UIntEncoder.write(timestamp, outputStream);
                        previousTS = timestamp;
                    }
                }
            }
            if (variableByte)
                VariableByteEncoder.write((int) lastTimeStamp, outputStream);
            else
                UIntEncoder.write(lastTimeStamp, outputStream);
            if (zstd)
                bytes = Zstd.compress(outputStream.toByteArray());
            else
                bytes = outputStream.toByteArray();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return bytes;
    }

    private void readByteArray(byte[] input, boolean variableByte, boolean zstd) {
        byte [] binary;
        if (zstd)
            binary = Zstd.decompress(input, input.length * 2); //TODO: How to know apriori original size?
        else
            binary = input;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);

        try{
            float epsilon = FloatEncoder.read(inputStream);
            long numB = VariableByteEncoder.read(inputStream);
            long previousBQnt = IntEncoder.read(inputStream);
            for (int i = 0; i < numB; i++) {
                long bDiff = UIntEncoder.readWithFlag(inputStream);
                float b = (bDiff + previousBQnt) * epsilon;
                previousBQnt = bDiff + previousBQnt;
                int numA = VariableByteEncoder.read(inputStream);
                for (int j = 0; j < numA; j++) {
                    float a = FloatEncoder.read(inputStream);
                    int numTimestamps = VariableByteEncoder.read(inputStream);
                    long timestamp = 0;
                    for (int k = 0; k < numTimestamps; k++) {
                        if (variableByte)
                            timestamp += VariableByteEncoder.read(inputStream);
                        else
                            timestamp = UIntEncoder.read(inputStream);
                        segments.add(new SimPieceSegment(timestamp, a, b));
                    }
                }
            }
            if (variableByte)
                lastTimeStamp = VariableByteEncoder.read(inputStream);
            else
                lastTimeStamp = UIntEncoder.read(inputStream);
            inputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        segments.sort(Comparator.comparingDouble(SimPieceSegment::getInitTimestamp));
    }
}
