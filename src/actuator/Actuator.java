package actuator;

import org.apache.log4j.Logger;

public class Actuator {
	static Logger log = Logger.getLogger(Actuator.class);

	public static int getNewNumberOfServers(double[] primalVariables,
			double rpredictedValue, double wpredictedValue,
			int NUMBER_OF_SERVERS) {

		int NEW_NUMBER_OF_SERVERS = 0;
		// variables needed to calculate the equation of the
		// System model
		double w1 = primalVariables[0];
		double w2 = primalVariables[1];
		double w3 = primalVariables[2];
		double b = primalVariables[3];
		log.debug("[SYSTEM PRIMAL VARIABLES]\tw1 " + w1 + "\tw2 " + w2 + "\tw3 " + w3 + "\tb " + b);
		// Equation y = (-1/w2)*(w1*x + b);

		// Get the equation of your current point connected to
		// the origin
		// The current point
		double x1 = rpredictedValue;
		double y1 = wpredictedValue;

		// The origin
		double x2 = 0;
		double y2 = 0;
		double m = (y1 - y2) / (x1 - x2);
		// Equation = y = mx + b ; since b is zero then y = mx
		// Compute the Intersection point from the two equations
		double xIntersect = (-b / ((w2 * m) + w1)); // Optimized reads
		double yIntersect = (m * xIntersect); // Optimized writes

		// Calculate the total throughput and the optimized
		// throughput
		double predictedTotalThroughtput = ((rpredictedValue + wpredictedValue) * NUMBER_OF_SERVERS); // predicted
																										// total
																										// throughput
																										// for
																										// time
																										// t+1
		double optimizedThroughput = (xIntersect + yIntersect); // new
																// optimized
																// throughput
																// per
																// server
		// TODO Set a threshold to only trigger actuation if
		// there is a large change in throughput

		// Calculate the new number of servers
		NEW_NUMBER_OF_SERVERS = (int) (predictedTotalThroughtput / optimizedThroughput);
		// TODO Remember to update the current total number of
		// servers if actuation takes place.
		return NEW_NUMBER_OF_SERVERS;
	}

}
