package predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import selfElastMan.OnlineModelMetrics;

public class PredictorUtilities {

	public PredictorMetrics readDataFile() {
		String csvFile = "/Users/GUREYA/Documents/MATLAB/Experimental-Data/data-5050V1.txt";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		ArrayList<Double> reads = new ArrayList<Double>();
		ArrayList<Double> writes = new ArrayList<Double>();
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] datapoint = line.split(cvsSplitBy);
				int rth = Integer.parseInt(datapoint[0]);
				int wth = Integer.parseInt(datapoint[1]);
				int dsz = Integer.parseInt(datapoint[2]);
				int rl = Integer.parseInt(datapoint[3]);
				int slo = Integer.parseInt(datapoint[5]);

				reads.add((double) rth);
				writes.add((double) wth);

				/*
				 * System.out.println("RTH=" + datapoint[0] + " , WTH=" +
				 * datapoint[1] + " ,RL=" + datapoint[3] + " ,SLO=" +
				 * datapoint[5]);
				 */

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

		return new PredictorMetrics(reads, writes);

	}

	// convert Integer into int
	public double[][] convertIntegers(ArrayList<Double> integers) {
		double[][] ret = new double[integers.size()][1];
		for (int i = 0; i < ret.length; i++) {
			ret[i][0] = integers.get(i).intValue();
		}
		return ret;
	}

	// ==============================Mean
	public static double mean(double[] p) {
		double sum = 0; // sum of all the elements
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
		}
		return sum / p.length;
	}// end method mean

	// ========================Return the length of the array
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
