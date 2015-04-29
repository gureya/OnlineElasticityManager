package org.apache.cassandra.service;

import java.io.Serializable;

/**
 * @author GUREYA
 *
 */
@SuppressWarnings("serial")
public class DataStatistics implements Serializable {
	private double avgLatency;
	private double minLatency;
	private double maxLatency;
	private double nfPctLatency;
	private double nnPctLatency;
	private double throughput;
	private double dataSize;

	public DataStatistics(double avgLatency, double minLatency,
			double maxLatency, double nfPctLatency, double nnPctLatency,
			double throughput, double dataSize) {
		// super();
		this.avgLatency = avgLatency;
		this.minLatency = minLatency;
		this.maxLatency = maxLatency;
		this.nfPctLatency = nfPctLatency;
		this.nnPctLatency = nnPctLatency;
		this.throughput = throughput;
		this.dataSize = dataSize;
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

	public double getThroughput() {
		return throughput;
	}

	public double getDataSize() {
		return dataSize;
	}

}
