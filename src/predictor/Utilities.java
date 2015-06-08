package predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Utilities {

	public DataFormat readDataFile() {
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

		return new DataFormat(reads, writes);
	
	}
	
	//convert Integer into int
	public double[][] convertIntegers(ArrayList<Double> integers)
	{
	    double[][] ret = new double[integers.size()][1];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i][0] = integers.get(i).intValue();
	    }
	    return ret;
	}
	
	//==============================Mean
	public double mean(double[] p) {
	    double sum = 0;  // sum of all the elements
	    for (int i=0; i<p.length; i++) {
	        sum += p[i];
	    }
	    return sum / p.length;
	}//end method mean


}


