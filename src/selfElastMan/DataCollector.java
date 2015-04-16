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
	 * Converts the content of a file into an Arraylist for manipulation
	 * 
	 * @param path
	 *            the path of the latency files
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
	 * A function that computes the read latencies statistics
	 * 
	 * @param inputArray
	 *            collection of all the observed read latencies
	 * @param windowSize
	 *            Size of read latencies to take into account
	 * @return DataStatistics object containing the computed statistics
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
		double mean = readStats.getMean(); // AverageLatency(us)
		double min = readStats.getMin(); // MinLatency(us)
		double max = readStats.getMax(); // MaxLatency(us)
		double nfpct = readStats.getPercentile(95); // 95thPercentileLatency(us)
		double nnpct = readStats.getPercentile(99); // 99thPercentileLatency(us)

		DataStatistics rdataStatistics = new DataStatistics(mean, min, max,
				nfpct, nnpct);

		return rdataStatistics;
	}

	/**
	 * A function that computes the write latencies statistics
	 * 
	 * @param inputArray
	 *            collection of all the observed write latencies
	 * @param windowSize
	 *            Size of write latencies to take into account
	 * @return DataStatistics object containing the computed statistics
	 */
	public static DataStatistics writeStats(Object[] inputArray, int windowSize) {
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
		double mean = writeStats.getMean(); // AverageLatency(us)
		double min = writeStats.getMin(); // MinLatency(us)
		double max = writeStats.getMax(); // MaxLatency(us)
		double nfpct = writeStats.getPercentile(95); // 95thPercentileLatency(us)
		double nnpct = writeStats.getPercentile(99); // 99thPercentileLatency(us)

		DataStatistics wdataStatistics = new DataStatistics(mean, min, max,
				nfpct, nnpct);

		return wdataStatistics;
	}
}
