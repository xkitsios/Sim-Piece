package gr.aueb.delorean.util;

public class Point {

    private final long timestamp;
    private final double value;

    public Point(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }
}
