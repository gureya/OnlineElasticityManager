package onlineelastman;

import java.util.Queue;

/**
 * @author GUREYA
 *
 */
public class OnlineModelMetrics {
	private int rThroughput; // Read Throughput(ops/sec)
	private int wThroughput; // Write Throughput(ops/sec)
	private int datasize;
	private double rlatency; // Read 99thPercentileLatency(us)
	private double wlatency; // Write 99thPercentileLatency(us)
	private boolean hasValue;
	private boolean valid; // true = not violate sla; false = violate sla
	private Queue<Integer> rQueue; // ReadLatenciesQueue
	private Queue<Integer> wQueue; // WriteLatenciesQueue

	public OnlineModelMetrics(int rThroughput, int wThroughput, int datasize,
			double rlatency, double wlatency, boolean hasValue,
			Queue<Integer> rQueue, Queue<Integer> wQueue, boolean valid) {
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

	public Queue<Integer> getrQueue() {
		return rQueue;
	}

	public Queue<Integer> getwQueue() {
		return wQueue;
	}

	public boolean isValid() {
		return valid;
	}

	// The setters are needed if you have a warm data
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void setrThroughput(int rThroughput) {
		this.rThroughput = rThroughput;
	}

	public void setwThroughput(int wThroughput) {
		this.wThroughput = wThroughput;
	}

	public void setDatasize(int datasize) {
		this.datasize = datasize;
	}

	public void setRlatency(double rlatency) {
		this.rlatency = rlatency;
	}

	public void setWlatency(double wlatency) {
		this.wlatency = wlatency;
	}

	public void setHasValue(boolean hasValue) {
		this.hasValue = hasValue;
	}

	public void setrQueue(Queue<Integer> rQueue) {
		this.rQueue = rQueue;
	}

	public void setwQueue(Queue<Integer> wQueue) {
		this.wQueue = wQueue;
	}
}
