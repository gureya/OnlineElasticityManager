package selfElastMan;

import java.util.Queue;

public class OnlineModelMetrics {
	private double rThroughput;
	private double wThroughput;
	private double datasize;
	private double latency;
	private Queue<Double> lQueue;
	
	public OnlineModelMetrics(double rThroughput, double wThroughput,
			double datasize, double latency, Queue<Double> lQueue) {
		super();
		this.rThroughput = rThroughput;
		this.wThroughput = wThroughput;
		this.datasize = datasize;
		this.latency = latency;
		this.lQueue = lQueue;
	}
	
	public double getrThroughput() {
		return rThroughput;
	}

	public double getwThroughput() {
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
