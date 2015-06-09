package predictor;

import java.util.ArrayList;

import selfElastMan.OnlineModelMetrics;

public class PredictorUtilities {

	// Convert an array of Integer into int
	public double[][] convertIntegers(ArrayList<Double> integers) {
		double[][] ret = new double[integers.size()][1];
		for (int i = 0; i < ret.length; i++) {
			ret[i][0] = integers.get(i).intValue();
		}
		return ret;
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
	public static int arrayLength(OnlineModelMetrics[][] dataPoints) {
		int l = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				if (dataPoints[i][j] != null) {
					l += 1;
				}
			}
		}
		return l;
	}

	// Returns the read datapoints in a multidimensional array needed by matlab
	// scripts
	public static double[][] getReadDatapoints(OnlineModelMetrics[][] dataPoints) {
		int r = 0;
		double[][] reads = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				if (dataPoints[i][j] != null) {
					reads[r][0] = dataPoints[i][j].getrThroughput();
					r += 1;
				}
			}
		}
		return reads;
	}

	// Returns the write datapoints in a multidimensional array needed by matlab
	// scripts
	public static double[][] getWriteDatapoints(
			OnlineModelMetrics[][] dataPoints) {
		int r = 0;
		double[][] writes = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				if (dataPoints[i][j] != null) {
					writes[r][0] = dataPoints[i][j].getwThroughput();
					r += 1;
				}
			}
		}
		return writes;
	}

}
