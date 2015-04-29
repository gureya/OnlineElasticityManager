package selfElastMan;

import org.apache.log4j.Logger;

/**
 * @author GUREYA
 *
 */
public class OnlineModel {
    static Logger log = Logger.getLogger(OnlineModel.class);
	public static OnlineModelMetrics[][] buildModel(
			OnlineModelMetrics[][] dataPoints, OnlineModelMetrics omm) {
		int i = omm.getrThroughput();
		int j = omm.getwThroughput();
		if (dataPoints[i][j] != null) {
			//System.out
			//		.println("\nPoint already exist,,,Just updating the respective Queues");
			log.debug("Point already exist,,,Just updating the respective Queues");
			// Update the Read Queue
			// TODO: Move this to a function
			if (dataPoints[i][j].getrQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[i][j].getrQueue().remove();
				// Then update the queue with the current value
				dataPoints[i][j].getrQueue().add(omm.getRlatency());
			} else {
				dataPoints[i][j].getrQueue().add(omm.getRlatency());
			}

			// Update the Write Queue
			// TODO: Move this to a function
			if (dataPoints[i][j].getwQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[i][j].getwQueue().remove();
				// Then update the queue with the current value
				dataPoints[i][j].getwQueue().add(omm.getWlatency());
			} else {
				dataPoints[i][j].getwQueue().add(omm.getWlatency());
			}
		} else {
			// Add to the data points
			log.debug("New data point,,,Adding to the datapoints");
			//System.out.println("New data point,,,Adding to the datapoints");
			dataPoints[i][j] = omm;
		}

		return dataPoints;
	}

}
