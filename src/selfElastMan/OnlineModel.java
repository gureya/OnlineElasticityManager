package selfElastMan;

public class OnlineModel {

	public static OnlineModelMetrics[][] buildModel(
			OnlineModelMetrics[][] dataPoints, OnlineModelMetrics omm) {
		int i = omm.getrThroughput();
		int j = omm.getwThroughput();
		if (dataPoints[i][j] != null) {
			System.out
					.println("\nPoint already exist,,,Just updating the Queue");
			// Update the Queue
			if (dataPoints[i][j].getlQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[i][j].getlQueue().remove();
				// Then update the queue with the current value
				dataPoints[i][j].getlQueue().add(omm.getLatency());
			} else {
				dataPoints[i][j].getlQueue().add(omm.getLatency());
			}
		} else {
			// Add to the data points
			System.out.println("New data point,,,Adding to the datapoints");
			dataPoints[i][j] = omm;
		}

		return dataPoints;
	}

}
