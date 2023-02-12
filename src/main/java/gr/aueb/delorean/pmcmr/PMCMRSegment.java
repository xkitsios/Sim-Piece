package gr.aueb.delorean.pmcmr;

public class PMCMRSegment {
    private long initialTimestamp;
    private Double value;

    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
