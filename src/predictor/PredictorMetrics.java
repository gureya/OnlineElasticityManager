package predictor;

import java.util.HashMap;

public class PredictorMetrics {
	double[] previousPredictions;
	HashMap<Integer, Integer> weights;
	double predictedValue;

	double[][] trainingLabels;
	double[][] dsz;
	double[][] writes;
	double[][] reads;

	/**
	 * @param trainingLabels
	 * @param dsz
	 * @param writes
	 * @param reads
	 */
	public PredictorMetrics(double[][] trainingLabels, double[][] dsz,
			double[][] writes, double[][] reads) {
		// super();
		this.trainingLabels = trainingLabels;
		this.dsz = dsz;
		this.writes = writes;
		this.reads = reads;
	}

	/**
	 * @return the trainingLabels
	 */
	public double[][] getTrainingLabels() {
		return trainingLabels;
	}

	/**
	 * @return the dsz
	 */
	public double[][] getDsz() {
		return dsz;
	}

	/**
	 * @return the writes
	 */
	public double[][] getWrites() {
		return writes;
	}

	/**
	 * @return the reads
	 */
	public double[][] getReads() {
		return reads;
	}

	/**
	 * @param previousPredictions
	 * @param weights
	 * @param predictedValue
	 */
	public PredictorMetrics(double[] previousPredictions,
			HashMap<Integer, Integer> weights, double predictedValue) {
		// super();
		this.previousPredictions = previousPredictions;
		this.weights = weights;
		this.predictedValue = predictedValue;
	}

	public double[] getPreviousPredictions() {
		return previousPredictions;
	}

	public HashMap<Integer, Integer> getWeights() {
		return weights;
	}

	public double getPredictedValue() {
		return predictedValue;
	}

}
