package gr.aueb.delorean.swingfilter;

import gr.aueb.delorean.util.Point;

import java.util.*;

public class SwingFilterCompressor {
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
    public static List<SwingFilterSegment> filter(Collection<Point> points, double epsilon) {

        List<SwingFilterSegment> swingFilterSegments = new ArrayList<>();

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
                swingFilterSegments.add(new SwingFilterSegment(first.getTimestamp(), first.getValue(), current.getTimestamp(), (uiOld.get(current.getTimestamp()) + liOld.get(current.getTimestamp())) / 2));
                return swingFilterSegments;
            }
            previous = current;
            current = iterator.next();
            if (uiOld.get(current.getTimestamp()) < current.getValue() - epsilon|| liOld.get(current.getTimestamp()) > current.getValue() + epsilon) {
                Linear line = new Linear(first.getTimestamp(), first.getValue(), previous.getTimestamp(), (uiOld.get(previous.getTimestamp()) + liOld.get(previous.getTimestamp())) / 2);
                swingFilterSegments.add(new SwingFilterSegment(first.getTimestamp(), first.getValue(), previous.getTimestamp(), (uiOld.get(previous.getTimestamp()) + liOld.get(previous.getTimestamp())) / 2));
                previous = first = new Point(previous.getTimestamp(), line.get(previous.getTimestamp()));
                uiOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() + epsilon);
                liOld = new Linear(previous.getTimestamp(), previous.getValue(), current.getTimestamp(), current.getValue() - epsilon);
            } else {
                Linear uiNew = new Linear(first.getTimestamp(), first.getValue(), current.getTimestamp(), current.getValue() + epsilon);
                Linear liNew = new Linear(first.getTimestamp(), first.getValue(), current.getTimestamp(), current.getValue() - epsilon);
                if (uiOld.get(current.getTimestamp()) > uiNew.get(current.getTimestamp()))
                    uiOld = uiNew;
                if (liOld.get(current.getTimestamp()) < liNew.get(current.getTimestamp()))
                    liOld = liNew;
            }
        }
    }
}
