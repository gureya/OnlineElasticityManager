package selfElastMan;

import java.util.Queue;

public class OnlineModelMetrics {
	private int rThroughput;
	private int wThroughput;
	private double datasize;
	private double latency;
	private boolean hasValue;
	private Queue<Double> lQueue;

	public OnlineModelMetrics(int rThroughput, int wThroughput,
			double datasize, double latency, boolean hasValue,
			Queue<Double> lQueue) {
		// super();
		this.rThroughput = rThroughput;
		this.wThroughput = wThroughput;
		this.datasize = datasize;
		this.latency = latency;
		this.hasValue = hasValue;
		this.lQueue = lQueue;
	}

	public boolean isHasValue() {
		return hasValue;
	}

	public int getrThroughput() {
		return rThroughput;
	}

	public int getwThroughput() {
		return wThroughput;
	}

	public double getDatasize() {
		return datasize;
	}

	public double getLatency() {
		return latency;
	}

	public Queue<Double> getlQueue() {
		return lQueue;
	}

}
