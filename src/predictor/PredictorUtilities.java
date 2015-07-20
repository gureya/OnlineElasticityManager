package predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import onlineelastman.OnlineModelMetrics;

public class PredictorUtilities {

	static Logger log = Logger.getLogger(PredictorUtilities.class);

	/**
	 * Reading warm up data if available from a file
	 * 
	 * @param datapoints
	 * @return
	 */
	public OnlineModelMetrics[][][] readDataFile(
			OnlineModelMetrics[][][] datapoints) {
		String csvFile = "dataFile.txt";
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
				// int dsz = 1;
				int rl = Integer.parseInt(datapoint[3]);
				int wl = Integer.parseInt(datapoint[4]);
				int slo = Integer.parseInt(datapoint[5]);
				boolean valid = (slo == 1) ? true : false;
				String rq = datapoint[6]
						.substring(1, datapoint[6].length() - 1);
				String[] rqArray = rq.split(";");
				Queue<Integer> rqe = new LinkedList<Integer>();
				Queue<Integer> wqe = new LinkedList<Integer>();
				
				wqe.add(0);
				for (int i = 0; i < rqArray.length; i++) {
					rqe.add(Integer.parseInt(rqArray[i].trim()));
				}
				// System.out.println("\trth " + rth + "\twth " + wth + "\tdsz "
				// + dsz + "\trl " + rl + "\twl " + wl + "\trqe" + rqe);

				OnlineModelMetrics point = new OnlineModelMetrics(rth, wth,
						dsz, rl, wl, true, rqe, wqe, valid);

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

	/**
	 * Get the mean of an array elements
	 * 
	 * @param p
	 * @return
	 */
	public static double mean(double[] p) {
		double sum = 0; // sum of all the elements
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
		}
		return sum / p.length;
	}

	/**
	 * Return the length of the current dataPoints without null ones
	 * 
	 * @param dataPoints
	 * @return
	 */
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

	// TODO: The loops can also be a performance bottleneck need to be
	// eliminated

	/**
	 * Returns the Training data in a multidimensional(2D) array needed by
	 * matlab scripts
	 * 
	 * @param dataPoints
	 * @return data2dArray
	 */
	public static PredictorMetrics getDataIn2DArray(
			OnlineModelMetrics[][][] dataPoints) {
		int r = 0;
		int size = PredictorUtilities.arrayLength(dataPoints);
		double[][] trainingLabels = new double[size][1];
		double[][] dsz = new double[size][1];
		double[][] writes = new double[size][1];
		double[][] reads = new double[size][1];
		for (int i = 0; i < dataPoints.length; i++) {
			for (int j = 0; j < dataPoints[i].length; j++) {
				for (int k = 0; k < dataPoints[i][j].length; k++) {
					if (dataPoints[i][j][k] != null) {
						trainingLabels[r][0] = (dataPoints[i][j][k].isValid()) ? 1
								: -1;
						reads[r][0] = dataPoints[i][j][k].getrThroughput();
						writes[r][0] = dataPoints[i][j][k].getwThroughput();
						dsz[r][0] = dataPoints[i][j][k].getDatasize();
						r += 1;
					}
				}
			}
		}

		PredictorMetrics data2dArray = new PredictorMetrics(trainingLabels,
				dsz, writes, reads);
		return data2dArray;
	}

}
