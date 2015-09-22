package predictor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

public class MatlabControl {

	static Logger log = Logger.getLogger(MatlabControl.class);

	public static double[] getPredictions(MatlabProxy proxy,
			double[] currentPredictions, double[][] predictionData)
			throws MatlabInvocationException {

		// For testing purposes - Generate a time series
		double[][] timeseries = new double[predictionData.length][1];
		double k = 0;
		for (int i = 0; i < predictionData.length; i++) {
			timeseries[i][0] = k;
			k += 10;
		}

		double nextWindow = (timeseries[timeseries.length - 1][0] + 10);
		log.debug("[Next_WINDOW], " + nextWindow);

		proxy.setVariable("nextWindow", nextWindow);

		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		processor.setNumericArray("reads", new MatlabNumericArray(
				predictionData, null));
		processor.setNumericArray("timeseries", new MatlabNumericArray(
				timeseries, null));

		// Execute the prediction algorithms in Matlab via the proxy
		/*proxy.eval("[avg] = average(reads)");
		proxy.eval("[maxima] = maximum(reads)");
		proxy.eval("[fft_value, pattern] = fft_func(reads)");
		proxy.eval("[rt_value] = regression_tree(timeseries, reads, nextWindow)");
		proxy.eval("[svm_value, accuracy, decision_values] = svm(timeseries, reads, nextWindow)");
		proxy.eval("[minima] = minimum(reads)");*/
		
		proxy.eval("[es] = exponentialSmoothing(reads)");
		proxy.eval("[foa] = firstOrderArima(reads)");
		proxy.eval("[rwa] = randomWalkArima(reads)");
		proxy.eval("[dfoa] = differencedFirstOrderArima(reads)");
		proxy.eval("[soa] = secondOrderArima(reads)");
		proxy.eval("[rt_value] = regression_tree(timeseries, reads, nextWindow)");

		// Get the current predictions for time t+1; order:[mean, max, fft,
		// reg_trees, libsvm, min]
		/*currentPredictions[0] = ((double[]) proxy.getVariable("avg"))[0];
		currentPredictions[1] = ((double[]) proxy.getVariable("maxima"))[0];
		currentPredictions[2] = ((double[]) proxy.getVariable("fft_value"))[0];
		currentPredictions[3] = ((double[]) proxy.getVariable("rt_value"))[0];
		currentPredictions[4] = ((double[]) proxy.getVariable("svm_value"))[0];
		currentPredictions[5] = ((double[]) proxy.getVariable("minima"))[0];*/
		
		// Get the current predictions for time t+1; order:[es, foa, rwa, dfoa, soa, reg_trees]!
		currentPredictions[0] = ((double[]) proxy.getVariable("es"))[0];
		currentPredictions[1] = ((double[]) proxy.getVariable("foa"))[0];
		currentPredictions[2] = ((double[]) proxy.getVariable("rwa"))[0];
		currentPredictions[3] = ((double[]) proxy.getVariable("dfoa"))[0];
		currentPredictions[4] = ((double[]) proxy.getVariable("soa"))[0];
		currentPredictions[5] = ((double[]) proxy.getVariable("rt_value"))[0];
		
		return currentPredictions;
	}

	public static PredictorMetrics runWMA(double[] previousPredictions,
			double[] currentPredictions, HashMap<Integer, Integer> weights,
			int currentValue) {
		// Weighted Majority Algorithm
		double predictedValue = 0;

		// Get the Winner for this round
		double value = 0;
		int keyOfMaxValue = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < previousPredictions.length; i++) {
			value = Math.abs(previousPredictions[i] - currentValue);
			map.put(i, (int) value);
		}

		// The more accurate algorithm
		double minValueInMap = (Collections.min(map.values()));

		// Iterate through hashmap and get the key with min value
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			// System.out.print("\t" + entry.getValue());
			if (entry.getValue() == minValueInMap) {
				keyOfMaxValue = entry.getKey();
				break; // pick the first one. fix this
			}
		}

		// Penalize each mistaken prediction by subtracting its weight by 1
		// Reward the Winner
		for (Entry<Integer, Integer> entry : weights.entrySet()) {
			if (entry.getKey() != keyOfMaxValue) {
				if (entry.getValue() != 0 && entry.getValue() <= 3) {
					int k1 = entry.getKey();
					int value1 = entry.getValue();
					value1 -= 1;
					weights.put(k1, value1);
				}
			} else {
				if (entry.getValue() < 3) {
					int k1 = entry.getKey();
					int value1 = entry.getValue();
					value1 += 1;
					weights.put(k1, value1);
				}
			}
		}
		// predictedValue = prediction corresponding to the highest
		// weight
		int winner = 0;
		int maxValueInWeights = (Collections.max(weights.values()));
		for (Entry<Integer, Integer> entry : weights.entrySet()) {
			if (entry.getValue() == maxValueInWeights) {
				winner = entry.getKey();
				break; // pick the first winner. fix this
			}
		}

		predictedValue = currentPredictions[winner];

		// Update the previous predictions
		System.arraycopy(currentPredictions, 0, previousPredictions, 0,
				previousPredictions.length);

		PredictorMetrics pm = new PredictorMetrics(previousPredictions,
				weights, predictedValue);
		return pm;
	}

}
