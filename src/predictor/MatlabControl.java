package predictor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

public class MatlabControl {

	// For Testing
	private static Scanner scanner;

	// Initialized to the number of algorithms
	public static final int NUM_OF_ALGS = 5;
	private static double[] previousPredictions = new double[NUM_OF_ALGS];
	private static double[] currentPredictions = new double[NUM_OF_ALGS];
	// private static double[] weights = new double[5];
	private static boolean initialWeights = false;
	private static double predictedValue = 0;
	private static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> weights = new HashMap<Integer, Integer>();
	private static MatlabProxy proxy;

	public MatlabControl() throws MatlabConnectionException {
		// Set the matlab factory setting from opening every now and again
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setUsePreviouslyControlledSession(true).setHidden(true)
				.setMatlabLocation(null).build();

		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		proxy = factory.getProxy();
	}

	public static void main(String[] args) throws MatlabConnectionException,
			MatlabInvocationException {
		// TODO Auto-generated method stub
		long start = System.nanoTime();

		new MatlabControl();
		// The actual values for the current prediction window
		double current_read = 345;

		// A while loop for Testing
		String again = "y";
		while (again.compareTo("y") == 0) {
			// ===============================
			// For testing purposes - reading data from a file: Should happen at
			// runtime
			Utilities dataFormat = new Utilities();
			DataFormat data = dataFormat.readDataFile();
			double[][] reads = dataFormat.convertIntegers(data.reads);

			// For testing purposes - Generate a time series
			double[][] timeseries = new double[reads.length][1];
			double k = 0;
			for (int i = 0; i < reads.length; i++) {
				timeseries[i][0] = k;
				// System.out.println("Timeseries\n" + timeseries[i][0]);
				k += 10;
			}

			double nextWindow = (timeseries[timeseries.length - 1][0] + 10);
			System.out.println(nextWindow);
			proxy.setVariable("nextWindow", nextWindow);

			// Arrays in MATLAB are always at least two dimensions, so the
			// lowest
			// dimension Java array that can be sent to MATLAB is a double[][]
			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
			processor.setNumericArray("reads", new MatlabNumericArray(reads,
					null));
			processor.setNumericArray("timeseries", new MatlabNumericArray(
					timeseries, null));

			// Add your scripts to Matlab path
			proxy.eval("addpath('/Users/GUREYA/Documents/workspace/ThesisTestBed/src/predictor')");

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
			System.out.println("avg: " + currentPredictions[0]);
			System.out.println("max: " + currentPredictions[1]);
			System.out.println("fft_value: " + currentPredictions[2]);
			System.out.println("rt_value: " + currentPredictions[3]);
			System.out.println("svm_value: " + currentPredictions[4]);

			// Time it takes to execute all the scripts
			System.out.println("Elapsed Time(ms): "
					+ (System.nanoTime() - start) / 1000000);

			// Disconnect the proxy from MATLAB
			// proxy.disconnect();

			// Weighted Majority Algorithm
			// Get the Winner for this round
			double value = 0;
			int keyOfMaxValue = 0;
			for (int i = 0; i < previousPredictions.length; i++) {
				value = Math.abs(previousPredictions[i] - current_read);
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
			if (!initialWeights) {
				for (int i = 0; i < NUM_OF_ALGS; i++) {
					weights.put(i, 5); // Assign a five-star
				}
				predictedValue = dataFormat.mean(currentPredictions);
				initialWeights = true;
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

				predictedValue = currentPredictions[winner];
			}

			System.out.println("Predicted value: " + predictedValue);

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
			// %==============end of While Testing=====================
			again = "n";
			scanner = new Scanner(System.in);
			System.out.print("\nDo you want to test again?(y==Yes, n==No): ");
			again = scanner.next();
		}
		// Disconnect the proxy from MATLAB
		System.out.print("Byee from the Matlab world...");
		proxy.disconnect();
		// =================================
	}

}
