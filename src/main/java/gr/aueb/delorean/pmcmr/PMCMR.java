package gr.aueb.delorean.pmcmr;

import gr.aueb.delorean.util.Encoding.FloatEncoder;
import gr.aueb.delorean.util.Encoding.UIntEncoder;
import gr.aueb.delorean.util.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PMCMR {

    private List<PMCMRSegment> segments;
    private long lastTimeStamp;

    public PMCMR(List<Point> points, double epsilon){
        this.segments = new ArrayList<>();
        this.lastTimeStamp = points.get(points.size() - 1).getTimestamp();
        compress(points, epsilon);
    }

    public PMCMR(byte[] bytes){
        this.segments = new ArrayList<>();
        readByteArray(bytes);
    }

    private void compress(List<Point> points, double epsilon) {
        segments.clear();
        PMCMRSegment currentSegment = null;

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (Point point : points) {

            if (point.getValue() > max) max = point.getValue();
            if (point.getValue() < min) min = point.getValue();

            if (max - min <= epsilon && currentSegment != null) {
                currentSegment.setValue(max - ((max - min) / 2));
            } else {
                if (currentSegment != null) segments.add(currentSegment);
                max = point.getValue();
                min = point.getValue();
                currentSegment = new PMCMRSegment();
                currentSegment.setInitialTimestamp(point.getTimestamp());
                currentSegment.setValue(point.getValue());
            }
        }
        if (currentSegment != null) segments.add(currentSegment);
    }

    public List<Point> decompress() {
        List<Point> points = new ArrayList<>();
        long currentTimeStamp = 0;

        for (int i = 0; i < segments.size() - 1; i++){
            while (currentTimeStamp < segments.get(i + 1).getInitialTimestamp()) {
                points.add(new Point(currentTimeStamp, segments.get(i).getValue()));
                currentTimeStamp++;
            }
        }

        while (currentTimeStamp <= lastTimeStamp) {
            points.add(new Point(currentTimeStamp, segments.get(segments.size() - 1).getValue()));
            currentTimeStamp++;
        }

        return points;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = null;

        try {
            UIntEncoder.write(segments.size(), outputStream);
            for (PMCMRSegment segment : segments) {
                UIntEncoder.write(segment.getInitialTimestamp(), outputStream);
                FloatEncoder.write((float) segment.getValue(), outputStream);
            }
            UIntEncoder.write(lastTimeStamp, outputStream);
            bytes = outputStream.toByteArray();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public void readByteArray(byte[] binary) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);

        try {
            long totalSegments = UIntEncoder.read(inputStream);
            for (int i = 0; i < totalSegments; i++) {
                PMCMRSegment segment = new PMCMRSegment();
                segment.setInitialTimestamp(UIntEncoder.read(inputStream));
                segment.setValue(FloatEncoder.read(inputStream));
                segments.add(segment);
            }
            lastTimeStamp = UIntEncoder.read(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        segments.sort(Comparator.comparingDouble(PMCMRSegment::getInitialTimestamp));
    }
}
