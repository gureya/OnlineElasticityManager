package actuator;

import org.apache.log4j.Logger;

public class Actuator {
	static Logger log = Logger.getLogger(Actuator.class);

	public static int getNewNumberOfServers(double[] primalVariables,
			double rpredictedValue, double wpredictedValue,
			int NUMBER_OF_SERVERS, double currentDatasize) {

		int NEW_NUMBER_OF_SERVERS = 0;
		// variables needed to calculate the equation of the
		// System model
		double w1 = primalVariables[0];
		double w2 = primalVariables[1];
		double w3 = primalVariables[2];
		double b = primalVariables[3];
		log.debug("[SYSTEM PRIMAL VARIABLES]\tw1 " + w1 + "\tw2 " + w2
				+ "\tw3 " + w3 + "\tb " + b);
		double xmin = primalVariables[4]; // determine the system bounds
		double xmax = primalVariables[5];

		double xx0 = xmin;
		double xx1 = xmax;
		double yy0 = (1 / w2) * ((-w1 * xx0) - b);
		double yy1 = (1 / w2) * ((-w1 * xx1) - b);
		double zz0 = (1 / w3) * ((-w1 * xx0) - (-w2 * yy0) - b);
		double zz1 = (1 / w3) * ((-w1 * xx1) - (-w2 * yy1) - b);

		double k0 = (xx1 - xx0);
		double k1 = (yy1 - yy0);
		// parametric Equation of the system model in 3-D
		// x = xx0 + (s*k0);
		// y = yy0 + (s*k1);
		// z = zz0 + (s*(zz1 - zz0));

		// Get the equation of your current point connected to
		// the origin
		// The current point
		double x0 = rpredictedValue;
		double y0 = wpredictedValue;
		double z0 = currentDatasize; // Assume that the datasize will not change

		// The origin
		double x1 = 0;
		double y1 = 0;
		double z1 = currentDatasize;

		double p0 = (x1 - x0);
		double p1 = (y1 - y0);
		// parametric Equation in 3-D
		// x = x0 + (t*p0);
		// y = y0 + (t*p1);
		// z = z0 + (t*(z1 - z0));

		// Compute the Intersection point from the two equations
		double t = ((k0 * (yy0 - y0)) + (k1 * (x0 - xx0)))
				/ ((k0 * p1) - (k1 * p0));
		double s = ((x0 + (t * p0) - xx0)) / k0;
		// Compute the Intersection point from the s and t
		double xIntersect = x0 + (t * p0); // Optimized reads
		double yIntersect = y0 + (t * p1); // Optimized writes
		double zIntersect = z0 + (t * (z1 - z0)); // current datasize
		log.debug("Optimized read throughput: " + xIntersect
				+ " Optimized write throughput: " + yIntersect
				+ " current datasize: " + zIntersect);

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
