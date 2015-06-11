package predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.naming.ldap.Rdn;

import onlineelastman.OnlineModelMetrics;

public class PredictorUtilities {

	public OnlineModelMetrics[][][] readDataFile(OnlineModelMetrics[][][] datapoints) {
		String csvFile = "/Users/GUREYA/Documents/MATLAB/Experimental-Data/data-5050V1.txt";
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
				int dsz = Integer.parseInt(datapoint[2]);
				int rl = Integer.parseInt(datapoint[3]);
				int wl = Integer.parseInt(datapoint[4]);
				int slo = Integer.parseInt(datapoint[5]);
				boolean valid = (slo == 1) ? true : false;
				
				datapoints[rth][wth][dsz].setrThroughput(rth);
				datapoints[rth][wth][dsz].setwThroughput(wth);
				datapoints[rth][wth][dsz].setDatasize(dsz);
				datapoints[rth][wth][dsz].setRlatency(rl);
				datapoints[rth][wth][dsz].setWlatency(wl);
				datapoints[rth][wth][dsz].setValid(valid);
				
				
				String rq = datapoint[6].substring(1, datapoint[6].length()-1);
				String[] rqArray = rq.split(cvsSplitBy);
				Queue<Integer> rqe = new LinkedList<Integer>();
				for (int i = 0; i < rqArray.length; i++) {
					rqe.add(Integer.parseInt(rqArray[i]));
				}
				datapoints[rth][wth][dsz].setrQueue(rqe);

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
		double[][] trainingLabels = new double[PredictorUtilities.arrayLength(dataPoints)][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						trainingLabels[r][0] = (dataPoints[i][j][k].isValid()) ? 1 : -1;
						r += 1;
					}
				}
			}
		}
		return trainingLabels;
	}
	
	

}
