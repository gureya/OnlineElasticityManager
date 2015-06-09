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
	public static int arrayLength(OnlineModelMetrics[][][] dataPoints) {
		int l = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++){
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
	// Add the points to a datastructure at runtime and later convert to an array
	// Returns the read values in a multidimensional array needed by matlab
	// scripts
	public static double[][] getReadDatapoints(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		double[][] reads = new double[PredictorUtilities
				.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++){
				for (int k = 0; k < dataPoints[i][j].length; k++) {
						if (dataPoints[i][j][k] != null) {
							reads[r][0] = dataPoints[i][j][k].getrThroughput();
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
			for (int j = 0; j < dataPoints[i].length; j++){
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
			for (int j = 0; j < dataPoints[i].length; j++){
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

}
