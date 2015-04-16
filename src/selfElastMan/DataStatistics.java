package selfElastMan;

public class DataStatistics {
	private double avgLatency;
	private double minLatency;
	private double maxLatency;
	private double nfPctLatency;
	private double nnPctLatency;

	public DataStatistics(double avgLatency, double minLatency,
			double maxLatency, double nfPctLatency, double nnPctLatency) {
		// super();
		this.avgLatency = avgLatency;
		this.minLatency = minLatency;
		this.maxLatency = maxLatency;
		this.nfPctLatency = nfPctLatency;
		this.nnPctLatency = nnPctLatency;
	}

	public double getAvgLatency() {
		return avgLatency;
	}

	public double getMinLatency() {
		return minLatency;
	}

	public double getMaxLatency() {
		return maxLatency;
	}

	public double getNfPctLatency() {
		return nfPctLatency;
	}

	public double getNnPctLatency() {
		return nnPctLatency;
	}

}
