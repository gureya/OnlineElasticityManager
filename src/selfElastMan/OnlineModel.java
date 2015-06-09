package selfElastMan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * @author GUREYA
 *
 */
public class OnlineModel {
	static Logger log = Logger.getLogger(OnlineModel.class);

	// for debugging
	private static PrintWriter out;

	public static OnlineModelMetrics[][][] buildModel(
			OnlineModelMetrics[][][] dataPoints, OnlineModelMetrics omm) {
		int i = omm.getrThroughput();
		int j = omm.getwThroughput();
		int k = omm.getDatasize();

		// for debugging
		String data = "";
		String file = "data.txt";
		// Assume that maximum datasize dont exceed maxReadTP or maxWriteTP
		if (i < SelfElastManStart.maxReadTP && j < SelfElastManStart.maxWriteTP) {
			if (dataPoints[i][j][k] != null) {

				log.debug("Point already exist,,,Just updating the respective Queues");
				int l = 0; // keep track of the violations
				// Update the Read Queue
				// TODO: Move this to a function
				if (dataPoints[i][j][k].getrQueue().size() >= SelfElastManStart.queueLength) {
					// Remove the first element added to the queue
					dataPoints[i][j][k].getrQueue().remove();
					// Then update the queue with the current value
					dataPoints[i][j][k].getrQueue()
							.add((int) omm.getRlatency());
					// Check if the point violates the sla with regard to the
					// confLevel
					for (Object object : dataPoints[i][j][k].getrQueue()) {
						if ((Integer) object > SelfElastManStart.readResponseTime)
							l++;
					}
					double cLevel = (double) l
							/ (double) dataPoints[i][j][k].getrQueue().size();
					if (cLevel > SelfElastManStart.confLevel) {
						dataPoints[i][j][k].setValid(false);
						log.debug("This point violates the sla...");
					} else {
						log.debug("This point does not violate the sla...");
					}

				} else {
					dataPoints[i][j][k].getrQueue()
							.add((int) omm.getRlatency());
					// Check if the point violates the sla with regard to the
					// confLevel
					for (Object object : dataPoints[i][j][k].getrQueue()) {
						if ((Integer) object > SelfElastManStart.readResponseTime)
							l++;
					}
					double cLevel = (double) l
							/ (double) dataPoints[i][j][k].getrQueue().size();
					if (cLevel > SelfElastManStart.confLevel) {
						dataPoints[i][j][k].setValid(false);
						log.debug("This point violates the sla...");
					} else {
						log.debug("This point does not violate the sla...");
					}
				}

				// Update the Write Queue
				// TODO: Update the sla violations for writes
				// TODO: Move this to a function
				if (dataPoints[i][j][k].getwQueue().size() >= SelfElastManStart.queueLength) {
					// Remove the first element added to the queue
					dataPoints[i][j][k].getwQueue().remove();
					// Then update the queue with the current value
					dataPoints[i][j][k].getwQueue()
							.add((int) omm.getWlatency());
				} else {
					dataPoints[i][j][k].getwQueue()
							.add((int) omm.getWlatency());
				}

				// for debugging...print each and every record to a file
				// (whether new or existing)!!
				int valid = (dataPoints[i][j][k].isValid()) ? 1 : 0;
				data = omm.getrThroughput() + "," + omm.getwThroughput() + ","
						+ omm.getDatasize() + "," + (int) omm.getRlatency()
						+ "," + (int) omm.getWlatency() + "," + valid + ","
						+ dataPoints[i][j][k].getrQueue();
				printtoFile(file, data);

			} else {
				// Add to the data points
				log.debug("New data point,,,Adding to the datapoints");

				dataPoints[i][j][k] = omm;
				// check for sla violations
				if (dataPoints[i][j][k].getRlatency() > SelfElastManStart.readResponseTime) {
					dataPoints[i][j][k].setValid(false);
					log.debug("This point violates the sla...");
				} else {
					log.debug("This point does not violate the sla...");
				}
				// TODO: check same for the writes

				// for debugging...print the datapoints to a file
				int valid = (dataPoints[i][j][k].isValid()) ? 1 : -1;
				data = dataPoints[i][j][k].getrThroughput() + ","
						+ dataPoints[i][j][k].getwThroughput() + ","
						+ dataPoints[i][j][k].getDatasize() + ","
						+ (int) dataPoints[i][j][k].getRlatency() + ","
						+ (int) dataPoints[i][j][k].getWlatency() + "," + valid
						+ "," + dataPoints[i][j][k].getrQueue();
				printtoFile(file, data);
			}
		}

		else {
			log.debug("Maximum dimensions metrics set has been exceeded...consider increasing them");
		}

		return dataPoints;
	}

	public static void getUpdatedModel(OnlineModelMetrics[][] datapoints) {

	}

	// for debugging print the datapoints to a file for analysis
	public static void printtoFile(String file, String data) {

		try {
			out = new PrintWriter(
					new BufferedWriter(new FileWriter(file, true)));
			out.println(data);
			// System.out.println(data);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e);
		}
		out.close(); // Important otherwise nothing will be written to the file
	}

}
