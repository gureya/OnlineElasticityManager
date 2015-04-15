package selfElastMan;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DataCollector {

	// Get a DescriptiveStatistics instance to calculate Statistics
	static DescriptiveStatistics readStats = new DescriptiveStatistics();
	static DescriptiveStatistics writeStats = new DescriptiveStatistics();

	/**
	 * @param path
	 *            the path of the latency files to read from
	 * @param charset
	 *            default to "UTF-8"
	 * @return a list of read or write latencies
	 * @throws IOException
	 */
	public static List<Double> readData(Path path, Charset charset)
			throws IOException {
		String line;
		List<Double> latencyList = new ArrayList<Double>();
		try (BufferedReader br = Files.newBufferedReader(path, charset)) {
			while ((line = br.readLine()) != null) {
				String[] lineVariables = line.split(",");
				Double latency = Double.parseDouble(lineVariables[1].trim());
				latencyList.add(latency);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		return latencyList;
	}

	/**
	 * @param inputArray
	 */
	public static DataStatistics readStats(Object[] inputArray, int windowSize) {
		if (windowSize > 0)
			readStats.setWindowSize(windowSize);

		// Add the data from the array
		for (int i = 0; i < inputArray.length; i++) {
			readStats.addValue((double) inputArray[i]);
		}

		// TODO: Remove after testing
		/*
		 * double sum = 0; for (Object element : inputArray){ sum += (double)
		 * element; } System.out.println("\n Verified Average: " +
		 * sum/inputArray.length);
		 */

		// Compute some statistics
		double mean = readStats.getMean();
		double min = readStats.getMin();
		double max = readStats.getMax();
		double nfpct = readStats.getPercentile(95);
		double nnpct = readStats.getPercentile(99);
		double std = readStats.getStandardDeviation();
		double median = readStats.getPercentile(50);
		
		DataStatistics rdataStatistics = new DataStatistics();
		rdataStatistics.avgLatency = mean;
		rdataStatistics.maxLatency = max;
		rdataStatistics.minLatency = min;
		rdataStatistics.nfPctLatency = nfpct;
		rdataStatistics.nnPctLatency = nnpct;
		
		return rdataStatistics;
	}

	/**
	 * @param inputArray
	 */
	public static void writeStats(Object[] inputArray, int windowSize) {
		if (windowSize > 0)
			writeStats.setWindowSize(windowSize);
		
		// Add the data from the array
		for (int i = 0; i < inputArray.length; i++) {
			writeStats.addValue((double) inputArray[i]);
		}

		// TODO: Remove after testing
		/*
		 * double sum = 0; for (Object element : inputArray){ sum += (double)
		 * element; } System.out.println("\n Verified Average: " +
		 * sum/inputArray.length);
		 */

		// Compute some statistics
		double mean = writeStats.getMean();
		double std = writeStats.getStandardDeviation();
		double median = writeStats.getPercentile(50);

		System.out.println(" \nWrite Statistics");
		System.out.print("\t Mean: " + mean + "\t std: " + std + "\t median: "
				+ median);
	}
}
