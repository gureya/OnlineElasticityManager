package selfElastMan;

import java.io.*;
import java.util.*;

import org.apache.cassandra.service.DataStatistics;

public class SelfElastManStart {

	Timer timer;
	public static int timerWindow = 5;

	private static Map<OnlineModelMetrics, Integer> modelMap = new HashMap<OnlineModelMetrics, Integer>();
	DataStatistics statsArray[] = new DataStatistics[2];

	public SelfElastManStart(int seconds) {
		timer = new Timer();
		timer.schedule(new PeriodicExecutor(), 0, seconds * 1000);
	}

	public static void main(String[] args) throws IOException {
		new SelfElastManStart(timerWindow);
	}

	class PeriodicExecutor extends TimerTask {
		@Override
		public void run() {
			System.out.println("\nTimer Task Started..!%n");
			double rThroughput = 0;
			double wThroughput = 0;
			double rPercentile = 0;
			double dValue = 10;

			DataStatistics statsArray[] = new DataStatistics[2];

			/*
			 * ///Anything to test in Javaa double one = 234.1; double two =
			 * 234.1; System.out.println(Double.compare(one, two));
			 * System.exit(0);
			 */

			// Test for Read and Write statistics
			try {
				statsArray = DataCollector.collectCassandraStats();
				rThroughput = statsArray[0].getThroughput();
				wThroughput = statsArray[1].getThroughput();
				rPercentile = statsArray[0].getNnPctLatency();

				System.out.println(" \nRead Statistics");
				System.out.print("\tThroughput: " + rThroughput
						+ "\t 99th Percentile Latency: " + rPercentile);

				System.out.println(" \nWrite Statistics");
				System.out.print("\tThroughput: " + wThroughput);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Test for the OnlineModel
			/*
			 * OnlineModelMetrics modelMetrics = new
			 * OnlineModelMetrics(rThroughput, wThroughput, dValue); modelMap =
			 * OnlineModel.buildModel(modelMap, modelMetrics, valid);
			 * System.out.println("\nMap Elements @iter ");
			 * System.out.print("\t" + modelMap);
			 */
			System.out.println("\nTimer Task Finished..!%n");
		}
	}

}
