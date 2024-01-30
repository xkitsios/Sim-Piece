package gr.aueb.delorean.simpiece;

import com.github.luben.zstd.Zstd;
import gr.aueb.delorean.util.Encoding.FloatEncoder;
import gr.aueb.delorean.util.Encoding.UIntEncoder;
import gr.aueb.delorean.util.Encoding.VariableByteEncoder;
import gr.aueb.delorean.util.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class SimPiece {
    private ArrayList<SimPieceSegment> segments;

    private ArrayList<SimPieceSegment> bestSegments;

    private int bestSize = Integer.MAX_VALUE;
    private double epsilon;
    private long lastTimeStamp;

    private int previousNoOfGroups = 0;

    public SimPiece(List<Point> points, double epsilon) throws IOException {
        if (points.isEmpty()) throw new IOException();

        this.epsilon = epsilon;
        this.lastTimeStamp = points.get(points.size() - 1).getTimestamp();
        this.segments = compress(points);
    }

    public SimPiece(byte[] bytes, boolean variableByte, boolean zstd) throws IOException {
        readByteArray(bytes, variableByte, zstd);
    }

    private double quantization(double value) {
        return Math.round(value / epsilon) * epsilon;
    }

    private List<SimPieceSegment> createSegmentsFromStartIdx(int startIdx, List<Point> points) {
        List<SimPieceSegment> segments = new LinkedList<>();

        long initTimestamp = points.get(startIdx).getTimestamp();
        double b = quantization(points.get(startIdx).getValue());
        if (startIdx + 1 == points.size()) {
            segments.add(new SimPieceSegment(initTimestamp, -Double.MAX_VALUE, Double.MAX_VALUE, b));
            return segments;
        }
        double aMax = ((points.get(startIdx + 1).getValue() + epsilon) - b) / (points.get(startIdx + 1).getTimestamp() - initTimestamp);
        double aMin = ((points.get(startIdx + 1).getValue() - epsilon) - b) / (points.get(startIdx + 1).getTimestamp() - initTimestamp);
        double diff = aMax-aMin;
        if (startIdx + 2 == points.size()) {
            segments.add(new SimPieceSegment(initTimestamp, aMin, aMax, b));
            return segments;
        }

        for (int idx = startIdx + 2; idx < points.size(); idx++) {
            double upValue = points.get(idx).getValue() + epsilon;
            double downValue = points.get(idx).getValue() - epsilon;

            double upLim = aMax * (points.get(idx).getTimestamp() - initTimestamp) + b;
            double downLim = aMin * (points.get(idx).getTimestamp() - initTimestamp) + b;
            segments.add(new SimPieceSegment(initTimestamp, aMin, aMax, b));
            if ((downValue > upLim || upValue < downLim)) {
                return segments;
            }
            if (upValue < upLim)
                aMax = Math.max((upValue - b) / (points.get(idx).getTimestamp() - initTimestamp), aMin);
            if (downValue > downLim)
                aMin = Math.min((downValue - b) / (points.get(idx).getTimestamp() - initTimestamp), aMax);
        }
        segments.add(new SimPieceSegment(initTimestamp, aMin, aMax, b));

        return segments;
    }


    private State binarySearch(List<State> states, int previousNoOfGroups, long initTimestamp, double b, ArrayList<SimPieceSegment> segments) {
        ArrayList<SimPieceSegment> oldSegments = mergePerB(segments);
        TreeMap<Double, HashMap<Double, ArrayList<Long>>> tree = new TreeMap<>();
        for (SimPieceSegment segment : oldSegments) {
            double a = segment.getA();
            double bi = segment.getB();
            long t = segment.getInitTimestamp();
            if (!tree.containsKey(bi)) tree.put(bi, new HashMap<>());
            if (!tree.get(bi).containsKey(a)) tree.get(bi).put(a, new ArrayList<>());
            tree.get(bi).get(a).add(t);
        }

        int high = states.size() - 1;
        int low = high / 2;
        State found = states.get(high);

        while (low <= high) {
            int mid = low  + ((high - low) / 2);
            State state = states.get(mid);
            if (findNumberOfGroups(initTimestamp, state.aMax, state.aMin, b, segments, previousNoOfGroups, oldSegments, tree)) {
                break;
            } else {
                found = state;
                low = mid + 1;
            }
        }
        return found;
    }

    private class State {

        int idx;

        double aMin;

        double aMax;

        public State(final int idx, final double aMin, final double aMax) {
            this.idx = idx;
            this.aMin = aMin;
            this.aMax = aMax;
        }

        @Override
        public String toString() {
            return String.format("%d, %f, %f, %f", idx, aMin, aMax, aMax - aMin);
        }
    }
    private int findNumberOfGroups(long initTimestamp, double aMax, double aMin, double b, ArrayList<SimPieceSegment> segments) {
        ArrayList<SimPieceSegment> tempSegments = new ArrayList<>(segments);
        tempSegments.add(new SimPieceSegment(initTimestamp, aMin, aMax, b));
        tempSegments = mergePerB(tempSegments);

        TreeMap<Double, HashMap<Double, ArrayList<Long>>> input = new TreeMap<>();
        for (SimPieceSegment segment : tempSegments) {
            double a = segment.getA();
            double bi = segment.getB();
            long t = segment.getInitTimestamp();
            if (!input.containsKey(bi)) input.put(bi, new HashMap<>());
            if (!input.get(bi).containsKey(a)) input.get(bi).put(a, new ArrayList<>());
            input.get(bi).get(a).add(t);
        }
        int groups = 0;
        for (Map.Entry<Double, HashMap<Double, ArrayList<Long>>> bSegments : input.entrySet())
            groups += bSegments.getValue().size();
        return groups;
    }

    private boolean findNumberOfGroups(long initTimestamp, double aMax, double aMin, double b, ArrayList<SimPieceSegment> segments,
                                       int previousNoOfGroups, ArrayList<SimPieceSegment> oldSegments,
                                       TreeMap<Double, HashMap<Double, ArrayList<Long>>> tree) {
        double alpha = oldSegments.stream().filter(s -> s.getB() == b && s.getAMin() <= aMax && s.getAMax() >= aMin).mapToDouble(s -> s.getAMax() - s.getAMin()).max().orElse(-1.0);

        ArrayList<SimPieceSegment> tempSegments = new ArrayList<>(segments);
        tempSegments.add(new SimPieceSegment(initTimestamp, aMin, aMax, b));
        tempSegments = mergePerB(tempSegments);
        double value = 0;
        TreeMap<Double, HashMap<Double, ArrayList<Long>>> input = new TreeMap<>();
        for (SimPieceSegment segment : tempSegments) {
            double a = segment.getA();
            double bi = segment.getB();
            long t = segment.getInitTimestamp();
            if (initTimestamp == t) {
//                System.out.println(String.format("A: %f, A_new: %f, B: %f, A/A_new: %f", alpha, (segment.getAMax() - segment.getAMin()), (aMax - aMin),
//                        alpha / (segment.getAMax() - segment.getAMin())));
                value = alpha / (segment.getAMax() - segment.getAMin());
            }
            if (!input.containsKey(bi)) input.put(bi, new HashMap<>());
            if (!input.get(bi).containsKey(a)) input.get(bi).put(a, new ArrayList<>());
            input.get(bi).get(a).add(t);
        }
        int groups = 0;
        for (Map.Entry<Double, HashMap<Double, ArrayList<Long>>> bSegments : input.entrySet())
            groups += bSegments.getValue().size();

        return groups > previousNoOfGroups;// || value > 10;
    }


    private boolean additionalGroup(int groups, ArrayList<SimPieceSegment> segments, SimPieceSegment segment) {
        ArrayList<SimPieceSegment> copy = new ArrayList<>(segments);
        copy.add(segment);
        return mergePerB(copy).size() > groups;
    }

    private boolean noOverlap(ArrayList<SimPieceSegment> segments, double b, double aMin, double aMax) {
        if (segments.isEmpty()) {
            return false;
        }
        int count = 0;
        for (SimPieceSegment segment : segments) {
            if (segment.getB() == b && aMax > segment.getAMin() &&  aMin < segment.getAMax()) {
                count++;
            }
        }
        if (count >= 1) {
            return false;
        }
        return true;
    }

    private ArrayList<SimPieceSegment> compress(List<Point> points) {
        Map<Integer, List<SimPieceSegment>> possibleSegments = new TreeMap<>();
        for (int i = 0; i <= points.size() - 1; i++) {
            List<SimPieceSegment> segmentsFromStartIdx = createSegmentsFromStartIdx(i, points);
            System.out.println(String.format("StartIdx: %d: %d", i, segmentsFromStartIdx.size()));
            possibleSegments.put(i, segmentsFromStartIdx);
        }
        getCandidates(possibleSegments, 0, "", points.size());

        return segments;
    }

    private void getCandidates(Map<Integer, List<SimPieceSegment>> possibleSegments, int idx, String sb, int size) {
        if (idx >= size) {
            calculateAndPrintSize(sb, possibleSegments);
            return;
        }
        List<SimPieceSegment> candidates = possibleSegments.get(idx);
        int counter = idx + 1;
        for (SimPieceSegment candidate : candidates) {
            String segment = sb + String.format("%d-%d,", idx, counter - idx);
            if (counter < size) {
                getCandidates(possibleSegments, counter + 1, segment, size);
            } else {
                calculateAndPrintSize(segment, possibleSegments);
            }
            counter++;
        }
    }

    private void calculateAndPrintSize(String segment, Map<Integer, List<SimPieceSegment>> possibleSegments) {
        this.segments = new ArrayList<>();
        String[] segmentSplits = segment.split(",");
        for (String segmentSplit : segmentSplits) {
            if (!segmentSplit.isEmpty()) {
                String[] split = segmentSplit.split("-");
                this.segments.add(possibleSegments.get(Integer.parseInt(split[0])).get(Integer.parseInt(split[1])-1));
            }
        }
        this.segments = mergePerB(this.segments);
        try {
            int size = toByteArray(false, false).length;
            if (size <= bestSize) {
                this.bestSize = size;
                this.bestSegments = this.segments;
                System.out.printf("CR: %.2f (%d bytes) : %s\n", 50 * 8.0 / size, size, segment);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<SimPieceSegment> mergePerB(ArrayList<SimPieceSegment> segments) {
        double aMinTemp = -Double.MAX_VALUE;
        double aMaxTemp = Double.MAX_VALUE;
        double b = Double.NaN;
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<SimPieceSegment> mergedSegments = new ArrayList<>();

        segments.sort(Comparator.comparingDouble(SimPieceSegment::getB).thenComparingDouble(SimPieceSegment::getA));
        for (int i = 0; i < segments.size(); i++) {
            if (b != segments.get(i).getB()) {
                if (timestamps.size() == 1)
                    mergedSegments.add(new SimPieceSegment(timestamps.get(0), aMinTemp, aMaxTemp, b));
                else {
                    for (Long timestamp : timestamps)
                        mergedSegments.add(new SimPieceSegment(timestamp, aMinTemp, aMaxTemp, b));
                }
                timestamps.clear();
                timestamps.add(segments.get(i).getInitTimestamp());
                aMinTemp = segments.get(i).getAMin();
                aMaxTemp = segments.get(i).getAMax();
                b = segments.get(i).getB();
                continue;
            }
            if (segments.get(i).getAMin() <= aMaxTemp && segments.get(i).getAMax() >= aMinTemp) {
                timestamps.add(segments.get(i).getInitTimestamp());
                aMinTemp = Math.max(aMinTemp, segments.get(i).getAMin());
                aMaxTemp = Math.min(aMaxTemp, segments.get(i).getAMax());
            } else {
                if (timestamps.size() == 1) mergedSegments.add(segments.get(i - 1));
                else {
                    for (long timestamp : timestamps)
                        mergedSegments.add(new SimPieceSegment(timestamp, aMinTemp, aMaxTemp, b));
                }
                timestamps.clear();
                timestamps.add(segments.get(i).getInitTimestamp());
                aMinTemp = segments.get(i).getAMin();
                aMaxTemp = segments.get(i).getAMax();
            }
        }
        if (!timestamps.isEmpty()) {
            if (timestamps.size() == 1)
                mergedSegments.add(new SimPieceSegment(timestamps.get(0), aMinTemp, aMaxTemp, b));
            else {
                for (long timestamp : timestamps)
                    mergedSegments.add(new SimPieceSegment(timestamp, aMinTemp, aMaxTemp, b));
            }
        }

        return mergedSegments;
    }

    public List<Point> decompress() {
        segments.sort(Comparator.comparingLong(SimPieceSegment::getInitTimestamp));
        List<Point> points = new ArrayList<>();
        long currentTimeStamp = segments.get(0).getInitTimestamp();


        for (int i = 0; i < segments.size() - 1; i++) {
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

    private void toByteArrayPerBSegments(ArrayList<SimPieceSegment> segments, boolean variableByte, ByteArrayOutputStream outStream) throws IOException {
        TreeMap<Integer, HashMap<Double, ArrayList<Long>>> input = new TreeMap<>();
        for (SimPieceSegment segment : segments) {
            double a = segment.getA();
            int b = (int) Math.round(segment.getB() / epsilon);
            long t = segment.getInitTimestamp();
            if (!input.containsKey(b)) input.put(b, new HashMap<>());
            if (!input.get(b).containsKey(a)) input.get(b).put(a, new ArrayList<>());
            input.get(b).get(a).add(t);
        }

        VariableByteEncoder.write(input.size(), outStream);
        if (input.isEmpty()) return;
        int previousB = input.firstKey();
        VariableByteEncoder.write(previousB, outStream);
        for (Map.Entry<Integer, HashMap<Double, ArrayList<Long>>> bSegments : input.entrySet()) {
            VariableByteEncoder.write(bSegments.getKey() - previousB, outStream);
            previousB = bSegments.getKey();
            VariableByteEncoder.write(bSegments.getValue().size(), outStream);
            for (Map.Entry<Double, ArrayList<Long>> aSegment : bSegments.getValue().entrySet()) {
                FloatEncoder.write(aSegment.getKey().floatValue(), outStream);
                if (variableByte) Collections.sort(aSegment.getValue());
                VariableByteEncoder.write(aSegment.getValue().size(), outStream);
                long previousTS = 0;
                for (Long timestamp : aSegment.getValue()) {
                    if (variableByte) VariableByteEncoder.write((int) (timestamp - previousTS), outStream);
                    else UIntEncoder.write(timestamp, outStream);
                    previousTS = timestamp;
                }
            }
        }
    }


    public byte[] toByteArray(boolean variableByte, boolean zstd) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] bytes;

        FloatEncoder.write((float) epsilon, outStream);

        toByteArrayPerBSegments(segments, variableByte, outStream);

        if (variableByte) VariableByteEncoder.write((int) lastTimeStamp, outStream);
        else UIntEncoder.write(lastTimeStamp, outStream);

        if (zstd) bytes = Zstd.compress(outStream.toByteArray());
        else bytes = outStream.toByteArray();

        outStream.close();

        return bytes;
    }

    private ArrayList<SimPieceSegment> readMergedPerBSegments(boolean variableByte, ByteArrayInputStream inStream) throws IOException {
        ArrayList<SimPieceSegment> segments = new ArrayList<>();
        long numB = VariableByteEncoder.read(inStream);
        if (numB == 0) return segments;
        int previousB = VariableByteEncoder.read(inStream);
        for (int i = 0; i < numB; i++) {
            int b = VariableByteEncoder.read(inStream) + previousB;
            previousB = b;
            int numA = VariableByteEncoder.read(inStream);
            for (int j = 0; j < numA; j++) {
                float a = FloatEncoder.read(inStream);
                int numTimestamps = VariableByteEncoder.read(inStream);
                long timestamp = 0;
                for (int k = 0; k < numTimestamps; k++) {
                    if (variableByte) timestamp += VariableByteEncoder.read(inStream);
                    else timestamp = UIntEncoder.read(inStream);
                    segments.add(new SimPieceSegment(timestamp, a, (float) (b * epsilon)));
                }
            }
        }

        return segments;
    }

    private void readByteArray(byte[] input, boolean variableByte, boolean zstd) throws IOException {
        byte[] binary;
        if (zstd) binary = Zstd.decompress(input, input.length * 2); //TODO: How to know apriori original size?
        else binary = input;
        ByteArrayInputStream inStream = new ByteArrayInputStream(binary);

        this.epsilon = FloatEncoder.read(inStream);
        this.segments = readMergedPerBSegments(variableByte, inStream);
        if (variableByte) this.lastTimeStamp = VariableByteEncoder.read(inStream);
        else this.lastTimeStamp = UIntEncoder.read(inStream);
        inStream.close();
    }
}
