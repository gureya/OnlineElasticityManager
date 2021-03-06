package actuator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import onlineelastman.SelfElastManStart;

import org.apache.log4j.Logger;

public class Actuator {
	static Logger log = Logger.getLogger(Actuator.class);

	static Process _process = null;
	static InputStream _in = null;
	static OutputStream _out = null;
	static InputStream _err = null;

	/*
	 * TODO: WARNING: A lot of 3D VECTOR maths going on in this function; need a
	 * careful testing and error handling
	 */

	/**
	 * Get the Extra number of servers required to keep the Cassandra Cluster at
	 * optimal performance
	 * 
	 * @param primalVariables
	 * @param rpredictedValue
	 * @param wpredictedValue
	 * @param NUMBER_OF_SERVERS
	 * @param currentDatasize
	 * @return
	 */
	public static int getExtraNumberOfServers(double[] primalVariables,
			double rpredictedValue, double wpredictedValue,
			int NUMBER_OF_SERVERS, double currentDatasize) {

		int NEW_NUMBER_OF_SERVERS = 0;
		int extraServers = 0;

		/*
		 * variables needed to calculate the equation of the System model
		 */
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
		double zz0 = currentDatasize;
		double zz1 = currentDatasize;

		// double yy0 = (1 / w2) * ((-w1 * xx0) - b);
		// double yy1 = (1 / w2) * ((-w1 * xx1) - b);
		double yy0 = (1 / w2) * ((-w1 * xx0) - (-w3 * zz0) - b);
		double yy1 = (1 / w2) * ((-w1 * xx1) - (-w3 * zz1) - b);
		// double zz0 = (1 / w3) * ((-w1 * xx0) - (-w2 * yy0) - b);
		// double zz1 = (1 / w3) * ((-w1 * xx1) - (-w2 * yy1) - b);

		double k0 = (xx1 - xx0);
		double k1 = (yy1 - yy0);
		// parametric Equation of the system model in 3-D
		// x = xx0 + (s*k0);
		// y = yy0 + (s*k1);
		// z = zz0 + (s*(zz1 - zz0));

		/*
		 * Get the equation of your current point connected to the origin
		 */

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

		// double s = ((x0 + (t * p0) - xx0)) / k0;
		// Compute the Intersection point from s and t
		double xIntersect = x0 + (t * p0); // Optimized reads
		double yIntersect = y0 + (t * p1); // Optimized writes
		double zIntersect = z0 + (t * (z1 - z0)); // current datasize

		log.debug("Optimized read throughput: " + xIntersect
				+ " Optimized write throughput: " + yIntersect
				+ " current datasize: " + zIntersect);

		// Calculate the total throughput and the optimized throughput
		double predictedThroughput = (rpredictedValue + wpredictedValue);
		double predictedTotalThroughtput = ((predictedThroughput) * NUMBER_OF_SERVERS); // predicted
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
		/*
		 * TODO Set a threshold to only trigger actuation if there is a large
		 * change in throughput. Use the known target throughput per server to
		 * determine this e.g. 5% of the total throughput of one server
		 */
		int deadzone = Math
				.abs((int) (predictedThroughput - optimizedThroughput));

		// System.out.println("PredictedTotalThroughput: " +
		// predictedThroughput);
		// System.out.println("DeadZone is: " + deadzone);

		// Calculate the new number of servers
		NEW_NUMBER_OF_SERVERS = (int) Math
				.ceil((predictedTotalThroughtput / optimizedThroughput));
		log.debug("Calculated [NEW_NUMBER_OF_SERVERS], "
				+ NEW_NUMBER_OF_SERVERS);

		// if (deadzone > (0.05 * SelfElastManStart.targetThroughput)) {
		// check if the new set of servers exceed the minimum and maximum
		// number
		// of available servers

		if (NEW_NUMBER_OF_SERVERS <= SelfElastManStart.MIN_NUMBER_OF_SERVERS) {
			log.debug("New number of servers should not be less or equal to minimum number of servers to carry out the actuation");
			NEW_NUMBER_OF_SERVERS = SelfElastManStart.MIN_NUMBER_OF_SERVERS;
		} else if (NEW_NUMBER_OF_SERVERS > SelfElastManStart.MAX_NUMBER_OF_SERVERS) {
			log.info("New number of servers should not exceed the maximum number of servers to carry out the actuation");
			NEW_NUMBER_OF_SERVERS = SelfElastManStart.MAX_NUMBER_OF_SERVERS;
		} else
			log.info("New number of servers in the range of the cluster!");

		log.debug("Required [NEW_NUMBER_OF_SERVERS], " + NEW_NUMBER_OF_SERVERS);
		extraServers = NEW_NUMBER_OF_SERVERS - NUMBER_OF_SERVERS;
		// } else
		// log.info("Error in the Deadzone...Doing nothing!");

		log.debug("[Extra Servers Needed], " + extraServers);

		return extraServers;
	}

	/*
	 * TODO: Combine the following two functions as they are kind of repetitive
	 * passing the script to execute as an argument
	 */

	/**
	 * Decommission Cassandra Instances
	 * 
	 * @param instances
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void decommissionInstances(ArrayList<String> instances)
			throws IOException, InterruptedException {

		boolean processCompleted = false;
		String args = "";
		for (int i = 0; i < instances.size(); i++) {
			args = args + " " + instances.get(i);
		}

		String command = SelfElastManStart.actuatorScriptsPath
				+ "/removeInstances.sh" + args;
		log.debug("[Command to Execute], " + command);
		_process = Runtime.getRuntime().exec(command);
		// ... don't forget to initialise in, out, and error,
		// .... and consume the streams in separate threads!
		log.info("Starting a new actuation process..");

		final BufferedReader errReader = new BufferedReader(
				new InputStreamReader(_process.getErrorStream()));
		final BufferedReader inReader = new BufferedReader(
				new InputStreamReader(_process.getInputStream()));

		// read error and input streams as this would free up the buffers
		// free the error stream buffer
		Thread errThread = new Thread() {
			@Override
			public void run() {
				try {
					String line = errReader.readLine();
					while ((line != null) && !isInterrupted()) {
						System.err.println(line);
						line = errReader.readLine();
					}
				} catch (IOException ioe) {
					log.warn("Error reading the error stream", ioe);
				}
			}
		};
		Thread outThread = new Thread() {
			@Override
			public void run() {
				try {
					String line = inReader.readLine();
					while ((line != null) && !isInterrupted()) {
						System.out.println(line);
						line = inReader.readLine();
					}
				} catch (IOException ioe) {
					log.warn("Error reading the out stream", ioe);
				}
			}
		};
		try {
			errThread.start();
			outThread.start();
		} catch (IllegalStateException ise) {
		}

		// wait for the process to finish and check the exit code
		try {
			int exitCode = _process.waitFor();
			log.info("Actuation process exited with value: " + exitCode);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			processCompleted = false;
		}

		try {

			/*
			 * make sure that the error thread exits on Windows these threads
			 * sometimes get stuck and hang the execution timeout and join later
			 * after destroying the process.
			 */
			errThread.join();
			outThread.join();
			errReader.close();
			inReader.close();
		} catch (InterruptedException ie) {
			log.info(
					"ShellExecutor: Interrupted while reading the error/out stream",
					ie);
		} catch (IOException ioe) {
			log.warn("Error while closing the error/out stream", ioe);
		}
	}

	/**
	 * Spawn new Cassandra instances
	 * 
	 * @param instances
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void commissionInstances(ArrayList<String> instances)
			throws IOException, InterruptedException {

		boolean processCompleted = false;
		String args = "";
		for (int i = 0; i < instances.size(); i++) {
			args = args + " " + instances.get(i);
		}

		String command = SelfElastManStart.actuatorScriptsPath
				+ "/addInstances.sh" + args;
		log.debug("[Command to Execute], " + command);
		_process = Runtime.getRuntime().exec(command);
		// ... don't forget to initialise in, out, and error,
		// .... and consume the streams in separate threads!
		log.info("Starting a new actuation process..");

		final BufferedReader errReader = new BufferedReader(
				new InputStreamReader(_process.getErrorStream()));
		final BufferedReader inReader = new BufferedReader(
				new InputStreamReader(_process.getInputStream()));

		// read error and input streams as this would free up the buffers
		// free the error stream buffer
		Thread errThread = new Thread() {
			@Override
			public void run() {
				try {
					String line = errReader.readLine();
					while ((line != null) && !isInterrupted()) {
						System.err.println(line);
						line = errReader.readLine();
					}
				} catch (IOException ioe) {
					log.warn("Error reading the error stream", ioe);
				}
			}
		};
		Thread outThread = new Thread() {
			@Override
			public void run() {
				try {
					String line = inReader.readLine();
					while ((line != null) && !isInterrupted()) {
						System.out.println(line);
						line = inReader.readLine();
					}
				} catch (IOException ioe) {
					log.warn("Error reading the out stream", ioe);
				}
			}
		};
		try {
			errThread.start();
			outThread.start();
		} catch (IllegalStateException ise) {
		}

		// wait for the process to finish and check the exit code
		try {
			int exitCode = _process.waitFor();
			log.info("Actuation process exited with value: " + exitCode);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			processCompleted = false;
		}

		try {

			/*
			 * make sure that the error thread exits on Windows these threads
			 * sometimes get stuck and hang the execution timeout and join later
			 * after destroying the process.
			 */
			errThread.join();
			outThread.join();
			errReader.close();
			inReader.close();
		} catch (InterruptedException ie) {
			log.info(
					"ShellExecutor: Interrupted while reading the error/out stream",
					ie);
		} catch (IOException ioe) {
			log.warn("Error while closing the error/out stream", ioe);
		}

	}

	/**
	 * Get initial Cassandra instances from a configuration
	 * 
	 * @return nodesMap
	 */
	public static HashMap<String, Integer> getCassandraInstances() {
		String csvFile = "data/List_of_CassandraNodes.txt";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		HashMap<String, Integer> nodesMap = new HashMap<String, Integer>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] nodeData = line.split(cvsSplitBy);
				String nodeIP = nodeData[0];
				int nodeStatus = Integer.parseInt(nodeData[1]);
				nodesMap.put(nodeIP, nodeStatus);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return nodesMap;
	}

	/**
	 * Get the current number of active cassandra intances
	 * 
	 * @param nodesMap
	 * @return
	 */
	public static int getCurrentNoServers(HashMap<String, Integer> nodesMap) {
		int count = 0;
		for (int v : nodesMap.values()) {
			if (v > 0) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Update Cassandra instances after commissioning or decommissioning
	 * instances
	 * 
	 * @param updatedNodes
	 *            , instances to update
	 * @param nodesMap
	 *            , current nodes map
	 * @param value
	 *            , 0 for decommissioning and 1 for commissioning
	 * @return nodesMap, updated nodes map
	 */
	public static HashMap<String, Integer> updateCurrentNoservers(
			ArrayList<String> updatedNodes, HashMap<String, Integer> nodesMap,
			int value) {

		for (int i = 0; i < updatedNodes.size(); i++) {
			if (nodesMap.containsKey(updatedNodes.get(i)))
				nodesMap.put(updatedNodes.get(i), value);
		}
		return nodesMap;

	}

	/**
	 * Get the nodes to decommission
	 * 
	 * @param nodesMap
	 * @param extraServers
	 * @return ArrayList<String> nodesToDecommission
	 */
	public static ArrayList<String> getNodesToDecommission(
			HashMap<String, Integer> nodesMap, int extraServers) {
		ArrayList<String> nodesToDecommission = new ArrayList<String>();
		// Iterate through hashmap and get the nodes to decommission
		extraServers = extraServers * (-1); // make it a +ve integer
		int i = 0;
		for (Entry<String, Integer> entry : nodesMap.entrySet()) {
			// System.out.print("\t" + entry.getValue());
			if (entry.getValue() == 1) {
				nodesToDecommission.add(entry.getKey());
				i += 1;
			}
			if (i == extraServers)
				break;
		}
		return nodesToDecommission;
	}

	/**
	 * Get the nodes to Commission
	 * 
	 * @param nodesMap
	 * @param extraServers
	 * @return
	 */
	public static ArrayList<String> getNodesToCommission(
			HashMap<String, Integer> nodesMap, int extraServers) {
		ArrayList<String> nodesToCommission = new ArrayList<String>();
		// Iterate through hashmap and get the nodes to commission
		int i = 0;
		for (Entry<String, Integer> entry : nodesMap.entrySet()) {
			// System.out.print("\t" + entry.getValue());
			if (entry.getValue() == 0) {
				nodesToCommission.add(entry.getKey());
				i += 1;
			}
			if (i == extraServers)
				break;
		}
		return nodesToCommission;

	}

}
