package gr.aueb.delorean.util;

import java.util.List;

public class TimeSeries {
    public List<Point> data;
    public double range;
    public int size;

    public TimeSeries(List<Point> data, double range) {
        this.data = data;
        this.range = range;
        this.size = data.size() * (4 + 4);
    }

    public int length() {
        return data.size();
    }
}
