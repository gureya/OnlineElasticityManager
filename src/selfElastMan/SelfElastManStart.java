package selfElastMan;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class SelfElastManStart {
	/*
	 * Set the path where the read and write metrics are fetched TODO: Take this
	 * to configuration file and It should be executed periodically
	 */
	private static String folderPath = "/Users/GUREYA/cassandra";
	private static Path readPath = Paths.get(folderPath, "read-latencies.txt");
	private static Path writePath = Paths
			.get(folderPath, "write-latencies.txt");
	private static Charset charset = Charset.forName("UTF-8");

	/*
	 * WindowSize controls the number of values which contribute to the reported
	 * statistics. For example, if windowSize is set to 3 and the values
	 * {1,2,3,4,5} have been added in that order then the available values are
	 * {3,4,5} and all reported statistics will be based on these values
	 */
	public static final int WindowSize = 0;

	private static List<Double> readLatencies = new ArrayList<Double>();
	private static List<Double> writeLatencies = new ArrayList<Double>();

	public static void main(String[] args) throws IOException {

		// Test for Read statistics
		try {
			readLatencies = DataCollector.readData(readPath, charset);
			System.out.println("Read Latency List");
			System.out.print("\t" + readLatencies);

			Object[] inputArray = readLatencies.toArray();

			DataStatistics rdataStatistics = DataCollector.readStats(
					inputArray, WindowSize);
			System.out.println(" \nRead Statistics");
			System.out.print("\tMean: " + rdataStatistics.avgLatency
					+ "\t Max: " + rdataStatistics.maxLatency + "\t Min: "
					+ rdataStatistics.minLatency);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Test for Write statistics
		try {
			writeLatencies = DataCollector.readData(writePath, charset);
			System.out.println("\nWrite Latency List");
			System.out.print("\t" + writeLatencies);

			Object[] inputArray = writeLatencies.toArray();

			DataStatistics wdataStatistics = DataCollector.writeStats(
					inputArray, WindowSize);
			System.out.println(" \nWrite Statistics");
			System.out.print("\tMean: " + wdataStatistics.avgLatency
					+ "\t Max: " + wdataStatistics.maxLatency + "\t Min: "
					+ wdataStatistics.minLatency);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
