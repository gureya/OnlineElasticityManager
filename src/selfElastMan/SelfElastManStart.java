package selfElastMan;

import java.io.*;
import java.util.*;

import org.apache.cassandra.service.DataStatistics;

public class SelfElastManStart {

	Timer timer;
	public static int timerWindow = 5;

	private double readLModel[][][] = new double[500][500][500];

	private OnlineModelMetrics[] dataPoints = new OnlineModelMetrics[50];

	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private int fineRead;
	private int fineWrite;
	private static int scale = 50;
	private static int queueLength = 10;

	public SelfElastManStart(int seconds) {
		for (int i = 0; i < dataPoints.length; i++) {
			dataPoints[i] = new OnlineModelMetrics(0, 0, 0, 0, null);
		}
		timer = new Timer();
		timer.schedule(new PeriodicExecutor(), 0, seconds * 1000);
	}

	public static void main(String[] args) throws IOException {
		new SelfElastManStart(timerWindow);
	}

	double rThroughput = 50;
	double wThroughput = 100;
	double rPercentile = 10;
	int dValue = 10;

	class PeriodicExecutor extends TimerTask {
		@Override
		public void run() {
			System.out.println("\nTimer Task Started..!%n");

			// DataStatistics statsArray[] = new DataStatistics[2];

			/*
			 * ///Anything to test in Javaa double one = 234.1; double two =
			 * 234.1; System.out.println(Double.compare(one, two));
			 * System.exit(0);
			 */

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
			Queue<Double> qe = new LinkedList<Double>();
			OnlineModelMetrics omm = new OnlineModelMetrics(fineRead,
					fineWrite, dValue, rPercentile, qe);
			dataPoints = OnlineModel.buildModel(dataPoints, omm);

			for (int i = 0; i < dataPoints.length; i++) {
				if (dataPoints[i].getrThroughput() != 0
						&& dataPoints[i].getwThroughput() != 0
						&& dataPoints[i].getDatasize() != 0 && dataPoints[i].getLatency() != 0 && dataPoints[i].getlQueue() != null){
					System.out.println("\nRead: "
							+ dataPoints[i].getrThroughput() + "\tWrite: "
							+ dataPoints[i].getwThroughput() + "\tDatasize: "
							+ dataPoints[i].getDatasize() + "\tReadLatency: "
							+ dataPoints[i].getLatency());
				}
			}


			System.out.println("\nTimer Task Finished..!%n");
			rThroughput = rThroughput + 25;
			wThroughput = wThroughput + 25;
		}
	}

}
