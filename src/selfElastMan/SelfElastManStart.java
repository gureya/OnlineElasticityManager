package selfElastMan;

import java.io.*;
import java.util.*;

import org.apache.cassandra.service.DataStatistics;

public class SelfElastManStart {

	Timer timer;
	public static int timerWindow = 5;

	private double readLModel[][][] = new double[500][500][500];
	
	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private int read;
	private int write;
	private static int scale = 50;
	
	public SelfElastManStart(int seconds) {
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

			//DataStatistics statsArray[] = new DataStatistics[2];

			/*
			 * ///Anything to test in Javaa double one = 234.1; double two =
			 * 234.1; System.out.println(Double.compare(one, two));
			 * System.exit(0);
			 */

			int rt = (int) (rThroughput/scale);
			int wt = (int) (wThroughput/scale);
			
			rstart = rt * scale;
			rend = rstart + scale;
			
			wstart = wt * scale;
			wend = wstart + scale;
			
			read = (rstart + rend)/2;
			write = (wstart + wend)/2;
			
			System.out.println(" \nRead Statistics");
			System.out.print("\tThroughput: " + rThroughput
					+ "\t 99th Percentile Latency: " + rPercentile);

			System.out.println(" \nWrite Statistics");
			System.out.print("\tThroughput: " + wThroughput);
			
			//Test for the OnlineModel
			readLModel = OnlineModel.buildModel(readLModel, read, write, dValue, rPercentile);
			
			for (int i = 0; i < readLModel.length; i++) {
				for (int j = 0; j < readLModel.length; j++) {
					for (int k = 0; k < readLModel.length; k++) {
						if(readLModel[i][j][k] != 0)
							System.out.println("\nRead: " + i + "\tWrite: " + j + "\tDatasize: " + k + "\tReadLatency: " + readLModel[i][j][k]);
					}
				}
			}

			System.out.println("\nTimer Task Finished..!%n");
			rThroughput = rThroughput + 25;
			wThroughput = wThroughput + 25;
		}
	}

}
