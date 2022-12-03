package gr.aueb.delorean.simpiece;


import java.util.List;

public class SimPieceDecompressor {
    private final List<SimPieceSegment> segments;
    private double storedVal = 0;
    private int currentSegmentIdx = 0;
    private int currentTimestampOffset = 0;
    private SimPieceSegment currentSegment;
    private SimPieceSegment nextSegment;

    public SimPieceDecompressor(List<SimPieceSegment> segments) {
        this.segments = segments;
        this.currentSegment = segments.get(0);
        if (segments.size() > 1) this.nextSegment = segments.get(1);
        else this.nextSegment = null;
    }

    public Double readValue() {
        next();
        return storedVal;
    }

    private void next() {
        long finalTimestamp = nextSegment != null ? nextSegment.getInitTimestamp() : Long.MAX_VALUE;

        if (finalTimestamp > currentSegment.getInitTimestamp() + currentTimestampOffset) {
            storedVal = currentSegment.getA() * currentTimestampOffset + currentSegment.getB();
            currentTimestampOffset++;
        } else {
            currentSegmentIdx++;
            currentSegment = nextSegment;
            nextSegment = currentSegmentIdx + 1 < segments.size() ? segments.get(currentSegmentIdx + 1) : null;
            storedVal = currentSegment.getB();
            currentTimestampOffset = 1;
        }
    }
}
