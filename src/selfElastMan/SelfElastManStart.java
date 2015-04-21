package selfElastMan;

import java.io.*;
import java.util.*;

public class SelfElastManStart {

	Timer timer;
	public static int timerWindow = 5;

	private static OnlineModelMetrics[] dataPoints = new OnlineModelMetrics[50];

	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private double fineRead;
	private double fineWrite;
	public static final int scale = 50;
	public static final int queueLength = 10;

	public SelfElastManStart(int seconds) {
		timer = new Timer();
		timer.schedule(new PeriodicExecutor(), 0, seconds * 1000);
	}

	public static void main(String[] args) throws IOException {
		// Initialize all the datapoints
		for (int i = 0; i < dataPoints.length; i++) {
			dataPoints[i] = new OnlineModelMetrics(0, 0, 0, 0, false, null);
		}

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
			qe.add(rPercentile); // Queue is not null
			OnlineModelMetrics omm = new OnlineModelMetrics(fineRead,
					fineWrite, dValue, rPercentile, true, qe);

			// OnlineModelMetrics[] newdataPoints = new
			// OnlineModelMetrics[dataPoints.length];
			dataPoints = OnlineModel.buildModel(dataPoints, omm);
			// System.arraycopy(newdataPoints, 0, dataPoints, 0,
			// dataPoints.length);

			for (int i = 0; i < dataPoints.length; i++) {
				if (dataPoints[i].isHasValue()) {
					System.out.println("\nRead: "
							+ dataPoints[i].getrThroughput() + "\tWrite: "
							+ dataPoints[i].getwThroughput() + "\tDatasize: "
							+ dataPoints[i].getDatasize() + "\tReadLatency: "
							+ dataPoints[i].getLatency() + "\tQueue: "
							+ dataPoints[i].getlQueue());
				}
			}

			System.out.println("\nTimer Task Finished..!%n");
			rThroughput = rThroughput + 25;
			wThroughput = wThroughput + 25;
		}
	}

}
