package gr.aueb.delorean.swingfilter;

public class SwingFilterSegment {

	private final long initialTimestamp;
	private final double initialValue;
	private final long lastTimestamp;
	private final double lastValue;

	public SwingFilterSegment(long initialTimestamp, double initialValue, long lastTimestamp, double lastValue) {
		this.initialTimestamp = initialTimestamp;
		this.initialValue = initialValue;
		this.lastTimestamp = lastTimestamp;
		this.lastValue = lastValue;
	}

	public long getInitialTimestamp() {
		return initialTimestamp;
	}

	public double getInitialValue() {
		return initialValue;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public double getLastValue() {
		return lastValue;
	}
}
