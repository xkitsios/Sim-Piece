package gr.aueb.delorean.simpiece;

import gr.aueb.delorean.util.Point;

import java.util.*;

public class SimPieceCompressor {
    private static double quantization(double b, double epsilon) {
        return ((int) (b / epsilon)) * epsilon;
    }

    public static List<SimPieceSegment> filter(List<Point> points, double epsilon) {
        List<SimPieceSegment> swingSegments = new ArrayList<>();
        List<Integer> recordings = new ArrayList<>();
        double aMin = Double.MIN_VALUE;
        double aMax = Double.MAX_VALUE;

        int start = (int) points.get(0).getTimestamp();
        int idx = start;
        for (Point point : points) {
            double upValue = point.getValue() + epsilon;
            double downValue = point.getValue() - epsilon;

            if (recordings.isEmpty()) {
                aMin = Double.MIN_VALUE;
                aMax = Double.MAX_VALUE;
                recordings.add(idx++);
            } else {
                double b = quantization(points.get(recordings.get(0) - start).getValue(), epsilon);
                if (recordings.size() > 1) {
                    double upLim = aMax * (idx - recordings.get(0)) + b;
                    double downLim = aMin * (idx - recordings.get(0)) + b;
                    if (downValue > upLim || upValue < downLim) {
                        swingSegments.add(new SimPieceSegment(recordings.get(0), aMin, aMax, b));
                        recordings.clear();
                        recordings.add(idx++);
                        continue;
                    }
                }

                double aMaxTemp = (upValue - quantization(points.get(recordings.get(0) - start).getValue(), epsilon)) / (idx - recordings.get(0));
                double aMinTemp = (downValue - quantization(points.get(recordings.get(0) - start).getValue(), epsilon)) / (idx - recordings.get(0));

                if (recordings.size() == 1) {
                    aMax = aMaxTemp;
                    aMin = aMinTemp;
                } else {
                    double upLim = aMax * (idx - recordings.get(0)) + b;
                    double downLim = aMin * (idx - recordings.get(0)) + b;
                    if (upValue < upLim)
                        aMax = Math.max(aMaxTemp, aMin);
                    if (downValue > downLim)
                        aMin = Math.min(aMinTemp, aMax);
                }
                recordings.add(idx++);
            }
        }
        if (!recordings.isEmpty()) {
            double b = quantization(points.get(recordings.get(0) - start).getValue(), epsilon);
            swingSegments.add(new SimPieceSegment(recordings.get(0), aMin, aMax, b));
        }

        return swingSegments;
    }


    public static List<SimPieceSegment> mergeSegments(List<SimPieceSegment> constants) {
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<SimPieceSegment> segmentsMerged = new ArrayList<>();
        SimPieceSegment mergedSeg;

        TreeMap<Double, List<SimPieceSegment>> segmentsPerB = new TreeMap<>();
        for (SimPieceSegment segment : constants) {
            List<SimPieceSegment> temp = segmentsPerB.getOrDefault(segment.getB(), new ArrayList<>());
            temp.add(segment);
            segmentsPerB.put(segment.getB(), temp);
        }
        for (Map.Entry<Double, List<SimPieceSegment>> bSegments : segmentsPerB.entrySet()) {
            List<SimPieceSegment> seg = bSegments.getValue().stream().sorted(Comparator.comparingDouble(SimPieceSegment::getAMin)).toList();
            Iterator<SimPieceSegment> it = seg.iterator();
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

        return segmentsMerged.stream().sorted(Comparator.comparingLong(SimPieceSegment::getInitTimestamp)).toList();
    }
}
