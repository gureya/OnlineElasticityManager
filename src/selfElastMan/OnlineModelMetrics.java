package selfElastMan;

import java.util.Queue;

/**
 * @author GUREYA
 *
 */
public class OnlineModelMetrics {
	private int rThroughput;
	private int wThroughput;
	private int datasize;
	private double rlatency; // Read latency 99th Percentile
	private double wlatency; // Write latency
	private boolean hasValue;
	private boolean valid;
	private Queue<Double> rQueue; // A Queue for the Read Latencies
	private Queue<Double> wQueue;

	public OnlineModelMetrics(int rThroughput, int wThroughput, int datasize,
			double rlatency, double wlatency, boolean hasValue,
			Queue<Double> rQueue, Queue<Double> wQueue, boolean valid) {
		super();
		this.rThroughput = rThroughput;
		this.wThroughput = wThroughput;
		this.datasize = datasize;
		this.rlatency = rlatency;
		this.wlatency = wlatency;
		this.hasValue = hasValue;
		this.rQueue = rQueue;
		this.wQueue = wQueue;
		this.valid = valid;
	}

	public int getrThroughput() {
		return rThroughput;
	}

	public int getwThroughput() {
		return wThroughput;
	}

	public int getDatasize() {
		return datasize;
	}

	public double getRlatency() {
		return rlatency;
	}

	public double getWlatency() {
		return wlatency;
	}

	public boolean isHasValue() {
		return hasValue;
	}

	public Queue<Double> getrQueue() {
		return rQueue;
	}

	public Queue<Double> getwQueue() {
		return wQueue;
	}

	public boolean isValid() {
		return valid;
	}

}
