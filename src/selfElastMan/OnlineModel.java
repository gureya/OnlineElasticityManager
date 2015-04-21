package selfElastMan;

public class OnlineModel {

	public static OnlineModelMetrics[] buildModel(
			OnlineModelMetrics[] dataPoints, OnlineModelMetrics omm) {
		boolean found = false;
		int index = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			if (dataPoints[i].isHasValue()
					&& Double.compare(dataPoints[i].getrThroughput(),
							omm.getrThroughput()) == 0
					&& Double.compare(dataPoints[i].getwThroughput(),
							omm.getwThroughput()) == 0
					&& Double.compare(dataPoints[i].getDatasize(),
							omm.getDatasize()) == 0) {
				System.out
						.println("\nPoint already exist,,,Noting its index for updating the Queue");
				found = true;
				index = i;
				break;
			} else {
				continue;
			}
		}

		if (found) {
			// Update the Queue
			if (dataPoints[index].getlQueue().size() > SelfElastManStart.queueLength) {
				// Remove the first element added to the queue
				dataPoints[index].getlQueue().remove();
				// Then update the queue with the current value
				dataPoints[index].getlQueue().add(omm.getLatency());
			} else {
				dataPoints[index].getlQueue().add(omm.getLatency());
			}
		} else {
			
			// Add to the data points, the first datapoint which is blank
			for (int i = 0; i < dataPoints.length; i++) {
				if (!dataPoints[i].isHasValue()) {
					System.out.println("\nNew Point adding it to the datapoints...");
					dataPoints[i] = omm;
					break;
				}
			}
		}

		return dataPoints;
	}

}
