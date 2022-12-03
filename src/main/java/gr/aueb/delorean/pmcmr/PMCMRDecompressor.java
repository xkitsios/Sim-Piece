package gr.aueb.delorean.pmcmr;

import java.util.List;

public class PMCMRDecompressor {

	private final List<PMCMRSegment> segments;
    private double storedVal = 0;
    private boolean endOfStream = false;
    private int currentElement = 0;
    private int currentTimestampOffset = 0;
    
    public PMCMRDecompressor(List<PMCMRSegment> constants) {
    	this.segments = constants;
    }

    public Double readValue() {
        next();
        if(endOfStream)
            return null;
        return storedVal;
    }

    private void next() {
		PMCMRSegment constant = segments.get(currentElement);
    	if (constant.getFinalTimestamp() >= (constant.getInitialTimestamp() + currentTimestampOffset)) {
    		storedVal = constant.getValue();
    		currentTimestampOffset++;
    	} else {
    		currentElement++;
    		if (currentElement < segments.size()) {
    			constant = segments.get(currentElement);
    			storedVal = constant.getValue();
    			currentTimestampOffset = 1;
    		} else {
    			endOfStream = true;
    		}
    	}
	}
}
