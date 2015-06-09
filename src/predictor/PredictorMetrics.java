package predictor;

import java.util.HashMap;

public class PredictorMetrics {
	double[] previousPredictions;
	HashMap<Integer, Integer> weights;
	double predictedValue;

	public PredictorMetrics(double[] previousPredictions,
			HashMap<Integer, Integer> weights, double predictedValue) {
		super();
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
