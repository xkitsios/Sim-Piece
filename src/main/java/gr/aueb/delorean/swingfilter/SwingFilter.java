package gr.aueb.delorean.swingfilter;

import gr.aueb.delorean.util.Encoding.FloatEncoder;
import gr.aueb.delorean.util.Encoding.UIntEncoder;
import gr.aueb.delorean.util.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SwingFilter {
    private final List<SwingFilterSegment> segments;

    public SwingFilter(List<Point> points, double epsilon) {
        this.segments = new ArrayList<>();
        compress(points, epsilon);
    }


    public SwingFilter(byte[] bytes) throws IOException {
        this.segments = new ArrayList<>();
        readByteArray(bytes);
    }

    private void compress(List<Point> points, double epsilon) {
        Point first;
        Linear uiOld;
        Linear liOld;

        Iterator<Point> iterator = points.iterator();

        Point previous = first = iterator.next();
        Point current = iterator.next();
        uiOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() + epsilon);
        liOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() - epsilon);

        while (true) {
            if (!iterator.hasNext()) {
                segments.add(new SwingFilterSegment(first.getTimestamp(), first.getValue(), current.getTimestamp(), (uiOld.get(current.getTimestamp()) + liOld.get(current.getTimestamp())) / 2));
                return;
            }
            previous = current;
            current = iterator.next();
            if (uiOld.get(current.getTimestamp()) < current.getValue() - epsilon || liOld.get(current.getTimestamp()) > current.getValue() + epsilon) {
                Linear line = new Linear(first.getTimestamp(), first.getValue(), previous.getTimestamp(), (uiOld.get(previous.getTimestamp()) + liOld.get(previous.getTimestamp())) / 2);
                segments.add(new SwingFilterSegment(first.getTimestamp(), first.getValue(), previous.getTimestamp(), (uiOld.get(previous.getTimestamp()) + liOld.get(previous.getTimestamp())) / 2));
                previous = first = new Point(previous.getTimestamp(), line.get(previous.getTimestamp()));
                uiOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() + epsilon);
                liOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() - epsilon);
            } else {
                Linear uiNew = new Linear(first.getTimestamp(), first.getValue(), current.getTimestamp(), current.getValue() + epsilon);
                Linear liNew = new Linear(first.getTimestamp(), first.getValue(), current.getTimestamp(), current.getValue() - epsilon);
                if (uiOld.get(current.getTimestamp()) > uiNew.get(current.getTimestamp())) uiOld = uiNew;
                if (liOld.get(current.getTimestamp()) < liNew.get(current.getTimestamp())) liOld = liNew;
            }
        }
    }

    public List<Point> decompress() {
        List<Point> points = new ArrayList<>();
        long currentTimeStamp = segments.get(0).getInitialTimestamp();

        for (SwingFilterSegment currentSegment : segments) {
            double a = (currentSegment.getLastValue() - currentSegment.getInitialValue()) / (currentSegment.getLastTimestamp() - currentSegment.getInitialTimestamp());
            double b = currentSegment.getInitialValue() - a * currentSegment.getInitialTimestamp();
            while (currentTimeStamp <= currentSegment.getLastTimestamp()) {
                points.add(new Point(currentTimeStamp, a * currentTimeStamp + b));
                currentTimeStamp++;
            }
        }

        return points;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes;

        UIntEncoder.write(segments.size(), outputStream);
        for (SwingFilterSegment segment : segments) {
            UIntEncoder.write(segment.getInitialTimestamp(), outputStream);
            FloatEncoder.write((float) segment.getInitialValue(), outputStream);
        }

        UIntEncoder.write(segments.get(segments.size() - 1).getLastTimestamp(), outputStream);
        FloatEncoder.write((float) segments.get(segments.size() - 1).getLastValue(), outputStream);
        bytes = outputStream.toByteArray();
        outputStream.close();

        return bytes;
    }

    private void readByteArray(byte[] binary) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);

        long totalSegments = UIntEncoder.read(inputStream);
        if (totalSegments == 0) return;

        long initialTimestamp = UIntEncoder.read(inputStream);
        float initialValue = FloatEncoder.read(inputStream);
        for (int i = 1; i < totalSegments; i++) {
            long lastTimestamp = UIntEncoder.read(inputStream);
            float lastValue = FloatEncoder.read(inputStream);
            segments.add(new SwingFilterSegment(initialTimestamp, initialValue, lastTimestamp, lastValue));
            initialTimestamp = lastTimestamp;
            initialValue = lastValue;
        }
        long lastTimestamp = UIntEncoder.read(inputStream);
        float lastValue = FloatEncoder.read(inputStream);
        segments.add(new SwingFilterSegment(initialTimestamp, initialValue, lastTimestamp, lastValue));
        inputStream.close();
        segments.sort(Comparator.comparingDouble(SwingFilterSegment::getInitialTimestamp));
    }

    static class Linear {
        public final double a, b;

        public Linear(long ts, double vs, long te, double ve) {
            this.a = (ve - vs) / (te - ts);
            this.b = vs - a * ts;
        }

        public double get(long ts) {
            return (this.a * ts + this.b);
        }
    }
}
