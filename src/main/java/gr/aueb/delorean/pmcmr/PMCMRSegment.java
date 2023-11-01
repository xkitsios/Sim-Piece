package gr.aueb.delorean.pmcmr;

public class PMCMRSegment {
    private final long initialTimestamp;
    private final Double value;

    PMCMRSegment(long initialTimestamp, double value) {
        this.initialTimestamp = initialTimestamp;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public long getInitialTimestamp() {
        return initialTimestamp;
    }
}
