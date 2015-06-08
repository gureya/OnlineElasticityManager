package predictor;

import java.util.ArrayList;

public class DataFormat {
	ArrayList<Double> reads = new ArrayList<Double>();
	ArrayList<Double> writes = new ArrayList<Double>();

	public DataFormat(ArrayList<Double> reads, ArrayList<Double> writes) {
		super();
		this.reads = reads;
		this.writes = writes;
	}

	public ArrayList<Double> getReads() {
		return reads;
	}

	public ArrayList<Double> getWrites() {
		return writes;
	}

}
