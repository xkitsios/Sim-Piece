package gr.aueb.delorean.swingfilter;

import java.util.List;

public class SwingFilterDecompressor {
	private final List<SwingFilterSegment> segments;
	private double a;
	private double b;
    private double storedVal = 0;
    private boolean endOfStream = false;
    private int currentElement = 0;
    private int currentTimestampOffset = 0;
    private SwingFilterSegment currentSegment;
	private SwingFilterSegment nextSegment;
	private final long lastTimestamp;

    public SwingFilterDecompressor(List<SwingFilterSegment> swingSegments) {
    	this.segments = swingSegments;
    	this.currentSegment = swingSegments.get(0);
		this.a = (currentSegment.getLastValue() - currentSegment.getInitialValue()) / (currentSegment.getLastTimestamp() - currentSegment.getInitialTimestamp());
		this.b = currentSegment.getInitialValue() - a * currentSegment.getInitialTimestamp();
		if (swingSegments.size() > 1)
			this.nextSegment = swingSegments.get(1);
		else
			this.nextSegment = null;
		this.lastTimestamp = swingSegments.get(swingSegments.size() - 1).getLastTimestamp();

	}

    /**
     * Returns the next pair in the time series, if available.
     *
     * @return Pair if there's next value, null if series is done.
     */
    public Double readValue() {
        next();
        if (endOfStream) {
            return null;
        }
        return storedVal;
    }

    private void next() {
		long finalTimestamp = nextSegment != null ? nextSegment.getInitialTimestamp() : lastTimestamp + 1;

		if (finalTimestamp > currentSegment.getInitialTimestamp() + currentTimestampOffset) {
			storedVal = a * (currentSegment.getInitialTimestamp() + currentTimestampOffset) + b;
    		currentTimestampOffset++;
    	} else {
    		currentElement++;
    		if (currentElement < segments.size()) {
    			currentSegment = segments.get(currentElement);
				a = (currentSegment.getLastValue() - currentSegment.getInitialValue()) / (currentSegment.getLastTimestamp() - currentSegment.getInitialTimestamp());
				b = currentSegment.getInitialValue() - a * currentSegment.getInitialTimestamp();
				if (currentElement + 1 < segments.size())
					nextSegment = segments.get(currentElement + 1);
				else
					nextSegment = null;
				storedVal = a * currentSegment.getInitialTimestamp() + b;

    			currentTimestampOffset = 1;
    		} else {
    			endOfStream = true;
    		}
    	}
	}
}
