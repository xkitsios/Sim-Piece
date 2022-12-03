package gr.aueb.delorean.pmcmr;

import gr.aueb.delorean.util.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PMCMRCompressor {
    public static List<PMCMRSegment> filter(Collection<Point> points, double epsilon) {

        List<PMCMRSegment> segment = new ArrayList<>();
        PMCMRSegment currentSegment = null;

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (Point point : points) {

            if (point.getValue() > max) max = point.getValue();
            if (point.getValue() < min) min = point.getValue();

            if (max - min <= epsilon && currentSegment != null) {
                currentSegment.setFinalTimestamp(point.getTimestamp());
                currentSegment.setValue(max - ((max - min) / 2));
            } else {
                if (currentSegment != null) segment.add(currentSegment);
                max = point.getValue();
                min = point.getValue();
                currentSegment = new PMCMRSegment();
                currentSegment.setInitialTimestamp(point.getTimestamp());
                currentSegment.setFinalTimestamp(point.getTimestamp());
                currentSegment.setValue(point.getValue());
            }
        }
        if (currentSegment != null) segment.add(currentSegment);

        return segment;
    }
}
