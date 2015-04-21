package selfElastMan;

import java.util.Queue;

public class OnlineModel {

	public static OnlineModelMetrics[] buildModel(
			OnlineModelMetrics[] dataPoints, OnlineModelMetrics omm) {
		for (int i = 0; i < dataPoints.length; i++) {
			if (dataPoints[i].getrThroughput() == 0
					&& dataPoints[i].getwThroughput() == 0
					&& dataPoints[i].getDatasize() == 0
					&& dataPoints[i].getLatency() == 0
					&& dataPoints[i].getlQueue() == null) {
				dataPoints[i] = omm;
				break;
			} else {
				// Update Its Queue!!!!!
			}
		}

		return dataPoints;
	}

}
