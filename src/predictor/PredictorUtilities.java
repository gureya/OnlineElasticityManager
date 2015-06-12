package predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.apache.log4j.Logger;

import actuator.Actuator;
import onlineelastman.OnlineModel;
import onlineelastman.OnlineModelMetrics;

public class PredictorUtilities {

	static Logger log = Logger.getLogger(PredictorUtilities.class);

	// Reading warm up data if available from a file
	public OnlineModelMetrics[][][] readDataFile(
			OnlineModelMetrics[][][] datapoints) {
		String csvFile = "/Users/GUREYA/Documents/MATLAB/Experimental-Data/overall.txt";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] datapoint = line.split(cvsSplitBy);
				int rth = Integer.parseInt(datapoint[0]);
				int wth = Integer.parseInt(datapoint[1]);
				//int dsz = Integer.parseInt(datapoint[2]);
				int dsz = 1;
				int rl = Integer.parseInt(datapoint[3]);
				int wl = Integer.parseInt(datapoint[4]);
				int slo = Integer.parseInt(datapoint[5]);
				boolean valid = (slo == 1) ? true : false;
				String rq = datapoint[6]
						.substring(1, datapoint[6].length() - 1);
				String[] rqArray = rq.split(cvsSplitBy);
				Queue<Integer> rqe = new LinkedList<Integer>();
				for (int i = 0; i < rqArray.length; i++) {
					rqe.add(Integer.parseInt(rqArray[i]));
				}
				// System.out.println("\trth " + rth +"\twth " + wth + "\tdsz "
				// + dsz + "\trl " + rl + "\twl " + wl);

				OnlineModelMetrics point = new OnlineModelMetrics(rth, wth,
						dsz, rl, wl, true, rqe, null, valid);

				if (datapoints[rth][wth][dsz] != null) {
					datapoints[rth][wth][dsz].setrQueue(point.getrQueue());
					datapoints[rth][wth][dsz].setValid(point.isValid());
				} else
					datapoints[rth][wth][dsz] = point;

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return datapoints;

	}

	// Get the mean of an array elements
	public static double mean(double[] p) {
		double sum = 0; // sum of all the elements
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
		}
		return sum / p.length;
	}// end method mean

	// Return the length of the current datapoints without null ones
	public static int arrayLength(OnlineModelMetrics[][][] dataPoints) {
		int l = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						l += 1;
					}
				}
			}
		}
		return l;
	}

	// TODO: Combine the below functions into one function
	// The loops can also be a performance bottleneck need to be eliminated
	// Add the points to a datastructure at runtime and later convert to an
	// array

	// Returns the read values in a multidimensional array needed by matlab
	// scripts
	public static double[][] getReadDatapoints(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		double[][] reads = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						reads[r][0] = dataPoints[i][j][k].getrThroughput();
						r += 1;
					}
				}
			}
		}
		return reads;
	}

	// Returns the write values in a multidimensional array needed by matlab
	// scripts
	public static double[][] getWriteDatapoints(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		double[][] writes = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						writes[r][0] = dataPoints[i][j][k].getwThroughput();
						r += 1;
					}
				}
			}
		}
		return writes;
	}

	// Returns the DataSize values in a multidimensional array needed by matlab
	// scripts
	public static double[][] getDataSizeDatapoints(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		double[][] dsz = new double[PredictorUtilities.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						dsz[r][0] = dataPoints[i][j][k].getDatasize();
						r += 1;
					}
				}
			}
		}
		return dsz;
	}

	// Returns the TrainingLabels values in a multidimensional array needed by
	// matlab scripts
	public static double[][] getTrainingLabels(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		double[][] trainingLabels = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						trainingLabels[r][0] = (dataPoints[i][j][k].isValid()) ? 1
								: -1;
						r += 1;
					}
				}
			}
		}
		return trainingLabels;
	}

	// A testing function for predictor and system model
	public void testPredictorSystemModel(OnlineModelMetrics[][][] dataPoints,
			double[] rpreviousPredictions, double[] wpreviousPredictions,
			double[] rcurrentPredictions, double[] wcurrentPredictions,
			MatlabProxy proxy, boolean rinitialWeights,
			boolean winitialWeights, HashMap<Integer, Integer> rweights,
			HashMap<Integer, Integer> wweights, int NUMBER_OF_SERVERS,
			int NUM_OF_ALGS, int fineRead, int fineWrite) throws MatlabInvocationException {
		// Test for the Predictor
		// Get predictions for time t+1
		// Convert reads and writes into two dimensional array to be
		// sent to the matlab scripts
		// Arrays in MATLAB are always at least two dimensions, so
		// the lowest
		// dimension Java array that can be sent to MATLAB is a
		// double[][]
		double[][] reads = PredictorUtilities.getReadDatapoints(dataPoints);
		double[][] writes = PredictorUtilities.getWriteDatapoints(dataPoints);
		double[][] dszs = PredictorUtilities.getDataSizeDatapoints(dataPoints);
		double[][] trainingLabels = PredictorUtilities
				.getTrainingLabels(dataPoints);
		// Prediction for the read throughput
		// For Debugging
		String rpp = "Read Previous Predictions: ";
		for (int i = 0; i < rpreviousPredictions.length; i++) {
			rpp += "\t" + rpreviousPredictions[i];
		}
		log.debug(rpp);

		// Define the threshold of read data to atleast make a
		// prediction
		double rpredictedValue = 0;
		if (reads.length > 0) {
			rcurrentPredictions = MatlabControl.getPredictions(proxy,
					rcurrentPredictions, reads);
			log.debug("[Read Predictions], " + "\tavg: "
					+ rcurrentPredictions[0] + "\tmax: "
					+ rcurrentPredictions[1] + "\tfft_value: "
					+ rcurrentPredictions[2] + "\trt_value: "
					+ rcurrentPredictions[3] + "\tsvm_value: "
					+ rcurrentPredictions[4]);
			// Run the Weighted Majority Algorithm(WMA) and get the
			// predicted values
			// The actual values for the current window : fineRead &
			// fineWrite
			// Initialize the weights of all predictions to 1 [mean,
			// max, fft,
			// reg_trees, libsvm]; Only at the beginning

			if (!rinitialWeights) {
				for (int i = 0; i < NUM_OF_ALGS; i++) {
					rweights.put(i, 5); // Assign a five-star
										// initially.
										// All weights are equal
				}
				rpredictedValue = PredictorUtilities.mean(rcurrentPredictions);
				// Update the previous predictions
				System.arraycopy(rcurrentPredictions, 0, rpreviousPredictions,
						0, rpreviousPredictions.length);
				rinitialWeights = true;
			} else {
				PredictorMetrics rpm = MatlabControl.runWMA(
						rpreviousPredictions, rcurrentPredictions, rweights,
						fineRead);
				rweights = rpm.getWeights();
				rpreviousPredictions = rpm.getPreviousPredictions();
				rpredictedValue = rpm.getPredictedValue();
			}

			// For Debugging
			String rw = "\nWeights:";
			for (Entry<Integer, Integer> entry : rweights.entrySet()) {
				rw += "\t" + entry.getValue();
			}
			log.debug(rw);
			log.debug("Read predicted value for time t+1: " + rpredictedValue);
		} else
			log.debug("...Not enough training data available to make read predictions...");
		// Prediction & WMA for the write throughput

		// For Debugging
		String wpp = "Write Previous Predictions: ";
		for (int i = 0; i < wpreviousPredictions.length; i++) {
			wpp += "\t" + wpreviousPredictions[i];
		}
		log.debug(wpp);

		// Define the threshold of write data to atleast make a
		// prediction
		double wpredictedValue = 0;
		if (writes.length > 0) {
			wcurrentPredictions = MatlabControl.getPredictions(proxy,
					wcurrentPredictions, writes);
			log.debug("[Write Predictions], " + "\tavg: "
					+ wcurrentPredictions[0] + "\tmax: "
					+ wcurrentPredictions[1] + "\tfft_value: "
					+ wcurrentPredictions[2] + "\trt_value: "
					+ wcurrentPredictions[3] + "\tsvm_value: "
					+ wcurrentPredictions[4]);
			// Get the predictedValue
			if (!winitialWeights) {
				for (int i = 0; i < NUM_OF_ALGS; i++) {
					wweights.put(i, 5); // Assign a five-star
										// initially.
										// All weights are equal
				}
				wpredictedValue = PredictorUtilities.mean(wcurrentPredictions);
				// Update the previous predictions
				System.arraycopy(wcurrentPredictions, 0, wpreviousPredictions,
						0, wpreviousPredictions.length);
				winitialWeights = true;
			} else {
				PredictorMetrics wpm = MatlabControl.runWMA(
						wpreviousPredictions, wcurrentPredictions, wweights,
						fineWrite);
				wweights = wpm.getWeights();
				wpreviousPredictions = wpm.getPreviousPredictions();
				wpredictedValue = wpm.getPredictedValue();
			}

			// For Debugging
			String ww = "\nWeights:";
			for (Entry<Integer, Integer> entry : rweights.entrySet()) {
				ww += "\t" + entry.getValue();
			}
			log.debug(ww);
			log.debug("Write predicted value for time t+1: " + wpredictedValue);
		} else
			log.debug("...Not enough training data available to make write predictions...");

		// Testing the trained system model
		// At least determine a threshold for the training data to
		// get the system model
		if (reads.length > 0) {

			// Get the primal variables w, b from the model
			double[] primalVariables = OnlineModel.getUpdatedModel(proxy,
					reads, writes, dszs, trainingLabels);

			// Get the current number of servers
			int NEW_NUMBER_OF_SERVERS = Actuator.getNewNumberOfServers(
					primalVariables, rpredictedValue, wpredictedValue,
					NUMBER_OF_SERVERS);
			log.debug("[NEW_NUMBER_OF_SERVERS], " + NEW_NUMBER_OF_SERVERS);

		} else
			log.debug("...Not enough training data available to get the current system model...");
	}

}
