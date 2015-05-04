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
	
	//for debugging
	private static PrintWriter out;

	public static OnlineModelMetrics[][] buildModel(
			OnlineModelMetrics[][] dataPoints, OnlineModelMetrics omm) {
		int i = omm.getrThroughput();
		int j = omm.getwThroughput();

		// for debugging
		String data = "";
		String file = "data.txt";
        if(i < SelfElastManStart.maxReadTP && j < SelfElastManStart.maxWriteTP){
		if (dataPoints[i][j] != null) {
			// System.out
			// .println("\nPoint already exist,,,Just updating the respective Queues");
			log.debug("Point already exist,,,Just updating the respective Queues");

			// Update the Read Queue
			// TODO: Move this to a function
			if (dataPoints[i][j].getrQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[i][j].getrQueue().remove();
				// Then update the queue with the current value
				dataPoints[i][j].getrQueue().add((int) omm.getRlatency());
			} else {
				dataPoints[i][j].getrQueue().add((int) omm.getRlatency());
			}

			// Update the Write Queue
			// TODO: Move this to a function
			if (dataPoints[i][j].getwQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[i][j].getwQueue().remove();
				// Then update the queue with the current value
				dataPoints[i][j].getwQueue().add((int) omm.getWlatency());
			} else {
				dataPoints[i][j].getwQueue().add((int) omm.getWlatency());
			}
		} else {
			// Add to the data points
			log.debug("New data point,,,Adding to the datapoints");
			// System.out.println("New data point,,,Adding to the datapoints");
			dataPoints[i][j] = omm;
			
			//for debugging...print the datapoints to a file
			int valid = (omm.isValid()) ? 1 : 0;
			data = omm.getrThroughput() + "," + omm.getwThroughput() + ","
					+ omm.getDatasize() + "," + (int) omm.getRlatency() + ","
					+ (int) omm.getWlatency() + "," + valid;
			printtoFile(file, data);
		}
        }
        
        else{
        	log.debug("Maximum dimensions metrics set has been exceeded...consider increasing them");
        }

		return dataPoints;
	}
	
	//for debugging print the datapoints to a file for analysis
	public static void printtoFile(String file, String data){

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(data);
			//System.out.println(data);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e);
		}
		out.close(); // Important otherwise nothing will be written to the file
	}

}
