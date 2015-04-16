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
	
	private static Map<OnlineModelMetrics, Integer> modelMap = new HashMap<OnlineModelMetrics, Integer>();

	public static void main(String[] args) throws IOException {
        
		double rValue = 0;
        double wValue = 0;
        double dValue = 10;
        
        int valid = 1;
        int invalid = 0;
        
		/*///Anything to test in Javaa
		double one = 234.1;
        double two = 234.1;
        System.out.println(Double.compare(one, two));
        System.exit(0);*/
        
		// Test for Read statistics
		try {
			readLatencies = DataCollector.readData(readPath, charset);
			System.out.println("Read Latency List");
			System.out.print("\t" + readLatencies);

			Object[] inputArray = readLatencies.toArray();

			DataStatistics rdataStatistics = DataCollector.readStats(
					inputArray, WindowSize);
			System.out.println(" \nRead Statistics");
			System.out.print("\tMean: " + rdataStatistics.getAvgLatency()
					+ "\t Max: " + rdataStatistics.getMaxLatency() + "\t Min: "
					+ rdataStatistics.getMinLatency());
        rValue = rdataStatistics.getAvgLatency();
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
			System.out.print("\tMean: " + wdataStatistics.getAvgLatency()
					+ "\t Max: " + wdataStatistics.getMaxLatency() + "\t Min: "
					+ wdataStatistics.getMinLatency());
			wValue = wdataStatistics.getAvgLatency();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test for the OnlineModel
		OnlineModelMetrics modelMetrics = new OnlineModelMetrics(rValue, wValue, dValue);
		modelMap = OnlineModel.buildModel(modelMap, modelMetrics, valid);
		System.out.println("\nMap Elements @iter ");
		System.out.print("\t" + modelMap);
	}

}
