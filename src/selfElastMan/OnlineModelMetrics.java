package selfElastMan;

public class OnlineModelMetrics implements Comparable<OnlineModelMetrics> {

	private double readLatency;
	private double writeLatency;
	private double dataSize;

	public OnlineModelMetrics(double readLatency, double writeLatency,
			double dataSize) {
		// super();
		this.readLatency = readLatency;
		this.writeLatency = writeLatency;
		this.dataSize = dataSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(dataSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(readLatency);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(writeLatency);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OnlineModelMetrics other = (OnlineModelMetrics) obj;
		if (Double.doubleToLongBits(dataSize) != Double
				.doubleToLongBits(other.dataSize))
			return false;
		if (Double.doubleToLongBits(readLatency) != Double
				.doubleToLongBits(other.readLatency))
			return false;
		if (Double.doubleToLongBits(writeLatency) != Double
				.doubleToLongBits(other.writeLatency))
			return false;
		return true;
	}

	public double getReadLatency() {
		return readLatency;
	}

	public double getWriteLatency() {
		return writeLatency;
	}

	public double getDataSize() {
		return dataSize;
	}

	@Override
	public int compareTo(OnlineModelMetrics o) {
		// TODO Auto-generated method stub
		if (o == null) {
			throw new NullPointerException("OM :: OnlineModelMetrics was null");
		}
		if ((Double.compare(this.readLatency, o.getReadLatency()) == 0)
				&& (Double.compare(this.writeLatency, o.getWriteLatency()) == 0)
				&& (Double.compare(this.dataSize, o.getDataSize()) == 0)) {
			return 0;
		} else {
			return -1;
		}
	}
}
