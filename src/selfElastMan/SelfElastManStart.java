package selfElastMan;

import java.io.*;
import java.util.*;

import org.apache.cassandra.service.DataStatistics;
import org.apache.log4j.Logger;

/**
 * @author GUREYA
 *
 */
public class SelfElastManStart {
	
	//Default configurations overwritten by the config properties
	public static int timerWindow = 5;
	public static OnlineModelMetrics[][] dataPoints;
	public static int scale = 50;
	public static int queueLength = 10;
	public static int maxReadTP = 500;
	public static int maxWriteTP = 500;
	public static int maxDataSize = 100;

	static Logger log = Logger.getLogger(SelfElastManStart.class);
	Timer timer;

	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private int fineRead;
	private int fineWrite;

	public SelfElastManStart(int seconds) {
		timer = new Timer();
		log.info("Starting the Autonomic Controller...");

		// Read from the config properties if any
		Utilities properties = new Utilities();
		try {
			properties.getProperties();
			timerWindow = Integer.parseInt(properties.timerWindow.trim());
			maxReadTP = Integer.parseInt(properties.maxReadTP.trim());
			maxWriteTP = Integer.parseInt(properties.maxWriteTP.trim());
			maxDataSize = Integer.parseInt(properties.maxDataSize.trim());
			scale = Integer.parseInt(properties.scale.trim());
			queueLength = Integer.parseInt(properties.queueLength.trim());
			dataPoints = new OnlineModelMetrics[maxReadTP][maxWriteTP];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String message = "Config Properties: timerWindow:" + timerWindow
				+ "\tmaxReaTP:" + maxReadTP + "\tmaxWriteTP:" + maxWriteTP
				+ "\tmaxDataSize:" + maxDataSize + "\tqueueLength:"
				+ queueLength;
		log.info(message);

		timer.schedule(new PeriodicExecutor(), 0, seconds * 1000);
	}

	public static void main(String[] args) throws IOException {
		new SelfElastManStart(timerWindow);
	}

	class PeriodicExecutor extends TimerTask {
		@Override
		public void run() {
			// System.out.println("\nTimer Task Started..!%n");
			log.debug("Timer Task Started..!%n...Collecting Periodic Statistics");
			double rThroughput = 0;
			double wThroughput = 0;
			double rPercentile = 0;
			double wPercentile = 0;

			// Average dataSize Need to find a way to get from the Cassandra
			// Cluster!
			double dataSize = 0; //Default

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
				if (!Double.isNaN(statsArray[0].getDataSize()))
					dataSize = statsArray[0].getDataSize();

				if (rThroughput == 0 && wThroughput == 0 && dataSize == 0) {
					log.info("No New dataStatitistics found...Zero operations reported");
					// System.out
					// .println("No New dataStatistics found...Zero operations reported");
				} else {
					int rt = (int) (rThroughput / scale);
					int wt = (int) (wThroughput / scale);

					rstart = rt * scale;
					rend = rstart + scale;

					wstart = wt * scale;
					wend = wstart + scale;

					fineRead = (rstart + rend) / 2;
					fineWrite = (wstart + wend) / 2;

					// System.out.println(" \nRead Statistics");
					// System.out.print("\tThroughput: " + rThroughput
					// + "\t 99th Percentile Latency: " + rPercentile);
					log.debug("Read Statistics:\tSum: "
							+ statsArray[0].getSum() + "\tnoRequests:"
							+ statsArray[0].getNoRequests() + "\tThroughput: "
							+ rThroughput + "\t 99th Percentile Latency: "
							+ rPercentile);

					// System.out.println(" \nWrite Statistics");
					// System.out.print("\tThroughput: " + wThroughput);
					log.debug("Write Statistics:\tSum: "
							+ statsArray[1].getSum() + "\tnoRequests:"
							+ statsArray[1].getNoRequests() + "\tThroughput: "
							+ wThroughput + "\t 99th Percentile Latency: "
							+ wPercentile);

					// Test for the OnlineModel
					Queue<Integer> rqe = new LinkedList<Integer>();
					Queue<Integer> wqe = new LinkedList<Integer>();

					rqe.add((int) rPercentile); // Read Queue is not null
					wqe.add((int) wPercentile); // Write Queue is not null

					OnlineModelMetrics omm = new OnlineModelMetrics(fineRead,
							fineWrite, (int) dataSize, rPercentile,
							wPercentile, true, rqe, wqe, true);

					// OnlineModelMetrics[] newdataPoints = new
					// OnlineModelMetrics[dataPoints.length];
					dataPoints = OnlineModel.buildModel(dataPoints, omm);
					// System.arraycopy(newdataPoints, 0, dataPoints, 0,
					// dataPoints.length);

					/*for (int i = 0; i < dataPoints.length; i++) {
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
					}*/
				}
				// System.out.println("\nTimer Task Finished..!%n");
				log.debug("Timer Task Finished..!%n...Collecting Periodic DataStatistics");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// System.out.println("\nTimer Task Aborted with Errors...!%n: "
				// + e.getMessage());
				log.debug("Timer Task Aborted with Errors...!%n: "
						+ e.getMessage());
				// e.printStackTrace();
			}
		}
	}

}
