package predictor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import selfElastMan.OnlineModelMetrics;
import selfElastMan.SelfElastManStart;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

public class MatlabControl {

	static Logger log = Logger.getLogger(MatlabControl.class);

	public static double[] getPredictions(MatlabProxy proxy,
			OnlineModelMetrics[][] dataPoints, double[] currentPredictions)
			throws MatlabInvocationException {
		long start = System.nanoTime();

		// Convert reads and writes into two dimensional array to be sent to the
		// matlab scripts
		// Arrays in MATLAB are always at least two dimensions, so the
		// lowest
		// dimension Java array that can be sent to MATLAB is a double[][]
		double[][] reads = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		double[][] writes = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		System.out.println(PredictorUtilities
				.arrayLength(dataPoints));
		if(PredictorUtilities.arrayLength(dataPoints) > 0)
		{
		int r = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				if (dataPoints[i][j] != null) {
					reads[r][0] = dataPoints[i][j].getrThroughput();
					writes[r][0] = dataPoints[i][j].getwThroughput();
					r += 1;
				}
			}
		}
		// For testing purposes - Generate a time series
		double[][] timeseries = new double[reads.length][1];
		double k = 0;
		for (int i = 0; i < reads.length; i++) {
			timeseries[i][0] = k;
			// System.out.println("Timeseries\n" + timeseries[i][0]);
			k += 10;
		}

		double nextWindow = (timeseries[timeseries.length - 1][0] + 10);
		log.debug("[Next_WINDOW], " + nextWindow);
		//System.out.println(nextWindow);
		proxy.setVariable("nextWindow", nextWindow);

		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		processor.setNumericArray("reads", new MatlabNumericArray(reads, null));
		processor.setNumericArray("timeseries", new MatlabNumericArray(
				timeseries, null));

		// Add your scripts to Matlab path
		proxy.eval("addpath('/Users/GUREYA/Documents/workspace/ElasticityManager/src/predictor')");

		// Execute the prediction algorithms in Matlab via the proxy
		proxy.eval("[avg] = average(reads)");
		proxy.eval("[maxima] = maximum(reads)");
		proxy.eval("[fft_value, pattern] = fft_func(reads)");
		proxy.eval("[rt_value] = regression_tree(timeseries, reads, nextWindow)");
		proxy.eval("[svm_value, accuracy, decision_values] = svm(timeseries, reads, nextWindow)");

		// Get the current predictions for time t+1; order:[mean, max, fft,
		// reg_trees, libsvm]
		currentPredictions[0] = ((double[]) proxy.getVariable("avg"))[0];
		currentPredictions[1] = ((double[]) proxy.getVariable("maxima"))[0];
		currentPredictions[2] = ((double[]) proxy.getVariable("fft_value"))[0];
		currentPredictions[3] = ((double[]) proxy.getVariable("rt_value"))[0];
		currentPredictions[4] = ((double[]) proxy.getVariable("svm_value"))[0];
		
		// Print out for debugging
		/*
		 * System.out.println("Current Predictions for time t+1");
		 * System.out.println("avg: " + currentPredictions[0]);
		 * System.out.println("max: " + currentPredictions[1]);
		 * System.out.println("fft_value: " + currentPredictions[2]);
		 * System.out.println("rt_value: " + currentPredictions[3]);
		 * System.out.println("svm_value: " + currentPredictions[4]);
		 */
		
		// Time it takes to execute all the scripts
		log.debug("Elapsed Time(ms) for prediction: "
				+ +(System.nanoTime() - start) / 1000000);
		// System.out.println("Elapsed Time(ms): " + (System.nanoTime() - start)
		// / 1000000);
		}
		else
			log.debug("...Not enough training data available to make predictions...");
		return currentPredictions;
	}

	public static void runWMA(double[] previousPredictions,
			OnlineModelMetrics omm, double[] currentPredictions,
			HashMap<Integer, Integer> weights) {
		// Weighted Majority Algorithm
		// The actual values for the current prediction window
		int currentRead = omm.getrThroughput();
		int currentWrite = omm.getwThroughput();
		double rpredictedValue = 0;

		// Get the Winner for this round
		double value = 0;
		int keyOfMaxValue = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < previousPredictions.length; i++) {
			value = Math.abs(previousPredictions[i] - currentRead);
			map.put(i, (int) value);
		}

		double maxValueInMap = (Collections.min(map.values())); // The more
																// accurate
																// algorithm

		for (Entry<Integer, Integer> entry : map.entrySet()) { // Iterate
																// through
																// hashmap
			System.out.print("\t" + entry.getValue());
			if (entry.getValue() == maxValueInMap) {
				keyOfMaxValue = entry.getKey();
				System.out.println("\t" + keyOfMaxValue); // Print the key
															// with max
															// value
			}
		}
		// Arrays.sort(prediction_accuracy);
		// for (int i = 0; i < prediction_accuracy.length; i++) {
		System.out.print("\t" + maxValueInMap);
		// }
		// int winner = (prediction_accuracy.length - 1); // index with
		// Highest weight

		// Initialize the weights of all predictions to 1 [mean, max, fft,
		// reg_trees, libsvm]; Only at the beginning
		if (!SelfElastManStart.initialWeights) {
			for (int i = 0; i < SelfElastManStart.NUM_OF_ALGS; i++) {
				weights.put(i, 5); // Assign a five-star
			}
			rpredictedValue = PredictorUtilities.mean(currentPredictions);
			SelfElastManStart.initialWeights = true;
		}

		// Penalize each mistaken prediction by subtracting its weight by 1
		// Reward the Winner
		else {
			for (Entry<Integer, Integer> entry : weights.entrySet()) {
				if (entry.getKey() != keyOfMaxValue) {
					if (entry.getValue() != 0 && entry.getValue() <= 5) {
						int k1 = entry.getKey();
						int value1 = entry.getValue();
						value1 -= 1;
						weights.put(k1, value1);
					}
				} else {
					if (entry.getValue() < 5) {
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
					System.out.println("\tWinner: " + winner);
				}
			}

			rpredictedValue = currentPredictions[winner];
		}

		System.out.println("Predicted value: " + rpredictedValue);

		// Update the previous predictions
		System.arraycopy(currentPredictions, 0, previousPredictions, 0,
				previousPredictions.length);

		System.out.println("Previous Predictions:");
		for (int i = 0; i < previousPredictions.length; i++) {
			System.out.print("\t" + previousPredictions[i]);
		}

		System.out.println("\nWeights:");
		for (Entry<Integer, Integer> entry : weights.entrySet()) {
			System.out.print("\t" + entry.getValue());
		}
	}

}
