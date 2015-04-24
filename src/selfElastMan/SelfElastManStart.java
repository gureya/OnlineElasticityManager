package selfElastMan;

import java.io.*;
import java.util.*;

import org.apache.cassandra.service.DataStatistics;

public class SelfElastManStart {

	Timer timer;
	public static int timerWindow = 5;

	public static OnlineModelMetrics[][] dataPoints = new OnlineModelMetrics[500][500];
	public static final int scale = 50;
	public static final int queueLength = 10;

	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private int fineRead;
	private int fineWrite;

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

			/*
			 * ///Anything to test in Javaa double one = 234.1; double two =
			 * 234.1; System.out.println(Double.compare(one, two));
			 * System.exit(0);
			 */
			double rThroughput = 0;
			double wThroughput = 0;
			double rPercentile = 0;
			double wPercentile = 0;

			// Average dataSize Need to find a way to get from the Cassandra
			// Cluster!
			double dataSize = 0;

			DataStatistics statsArray[];
			try {
				statsArray = DataCollector.collectCassandraStats();
				if (!Double.isNaN(statsArray[0].getThroughput()))
					rThroughput = statsArray[0].getThroughput();
				if (!Double.isNaN(statsArray[1].getThroughput()))
					wThroughput = statsArray[1].getThroughput();
				if (!Double.isNaN(statsArray[0].getNnPctLatency()))
					rPercentile = statsArray[0].getNnPctLatency();
				if (!Double.isNaN(statsArray[1].getNnPctLatency()))
					wPercentile = statsArray[1].getNnPctLatency();
				if(!Double.isNaN(statsArray[0].getDataSize()))
					dataSize = statsArray[0].getDataSize();
				
				if (rThroughput == 0 && wThroughput == 0 && dataSize == 0) {
					System.out.println("No New dataStatistics found...Zero operations reported");
				} else {
					int rt = (int) (rThroughput / scale);
					int wt = (int) (wThroughput / scale);

					rstart = rt * scale;
					rend = rstart + scale;

					wstart = wt * scale;
					wend = wstart + scale;

					fineRead = (rstart + rend) / 2;
					fineWrite = (wstart + wend) / 2;

					System.out.println(" \nRead Statistics");
					System.out.print("\tThroughput: " + rThroughput
							+ "\t 99th Percentile Latency: " + rPercentile);

					System.out.println(" \nWrite Statistics");
					System.out.print("\tThroughput: " + wThroughput);

					// Test for the OnlineModel
					Queue<Double> rqe = new LinkedList<Double>();
					Queue<Double> wqe = new LinkedList<Double>();

					rqe.add(rPercentile); // Read Queue is not null
					wqe.add(wPercentile); // Write Queue is not null

					OnlineModelMetrics omm = new OnlineModelMetrics(fineRead,
							fineWrite, (int) dataSize, rPercentile,
							wPercentile, true, rqe, wqe, true);

					// OnlineModelMetrics[] newdataPoints = new
					// OnlineModelMetrics[dataPoints.length];
					dataPoints = OnlineModel.buildModel(dataPoints, omm);
					// System.arraycopy(newdataPoints, 0, dataPoints, 0,
					// dataPoints.length);

					for (int i = 0; i < dataPoints.length; i++) {
						for (int j = 0; j < dataPoints[i].length; j++) {
							if (dataPoints[i][j] != null) {
								System.out.println("\nRead: "
										+ dataPoints[i][j].getrThroughput()
										+ "\tWrite: "
										+ dataPoints[i][j].getwThroughput()
										+ "\tDatasize: "
										+ dataPoints[i][j].getDatasize()
										+ "\tReadLatency: "
										+ dataPoints[i][j].getRlatency()
										+ "\tRead Queue: "
										+ dataPoints[i][j].getrQueue()
										+ "\tWrite Queue: "
										+ dataPoints[i][j].getwQueue());
							}
						}
					}
				}
				System.out.println("\nTimer Task Finished..!%n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
