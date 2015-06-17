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
	 * Get the Extra number of servers required to keep the Cassandra Cluster at
	 * optimal performance
	 */
	public static int getExtraNumberOfServers(double[] primalVariables,
			double rpredictedValue, double wpredictedValue,
			int NUMBER_OF_SERVERS, double currentDatasize) {

		int NEW_NUMBER_OF_SERVERS = 0;
		int extraServers = 0;
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
		// double s = ((x0 + (t * p0) - xx0)) / k0;
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
		extraServers = NEW_NUMBER_OF_SERVERS - NUMBER_OF_SERVERS;
		log.debug("[NEW_NUMBER_OF_SERVERS], " + NEW_NUMBER_OF_SERVERS);
		log.debug("[Extra Servers Needed], " + extraServers);
		// TODO Remember to update the current total number of
		// servers if actuation takes place.
		return extraServers;
	}

	/*
	 * Decommission Cassandra Instances
	 */
	public static void decommissionInstances(ArrayList<String> instances)
			throws IOException, InterruptedException {

		String args = "";
		for (int i = 0; i < instances.size(); i++) {
			args = args + " " + instances.get(i);
		}

		try {
			String command = SelfElastManStart.actuatorScriptsPath
					+ "/removeInstances.sh" + args;
			log.debug("[Command to Execute], " + command);
			_process = Runtime.getRuntime().exec(command);
			// ... don't forget to initialise in, out, and error,
			// .... and consume the streams in separate threads!
			_process.waitFor();

			_in = _process.getInputStream();
			InputStreamReader isr = new InputStreamReader(_in);
			_err = _process.getErrorStream();
			InputStreamReader esr = new InputStreamReader(_err);

			if (_in != null) {
				int n1;
				char[] c1 = new char[1024];
				StringBuffer standardOutput = new StringBuffer();
				while ((n1 = isr.read(c1)) > 0) {
					standardOutput.append(c1, 0, n1);
				}
				System.out.println("Standard Output: "
						+ standardOutput.toString());

			} else {
				int n2;
				char[] c2 = new char[1024];
				StringBuffer standardError = new StringBuffer();
				while ((n2 = esr.read(c2)) > 0) {
					standardError.append(c2, 0, n2);
				}
				System.out.println("Standard Error: "
						+ standardError.toString());
			}
		} finally {
			if (_process != null) {
				close(_process.getErrorStream());
				close(_process.getOutputStream());
				close(_process.getInputStream());
				_process.destroy();
				log.debug("[Process Exit Status], "+_process.exitValue());
			}
			close(_in);
			close(_out);
			close(_err);
		}
	}

	/*
	 * Spawn new Cassandra instances
	 */
	public static void commissionInstances(ArrayList<String> instances)
			throws IOException, InterruptedException {

		String args = "";
		for (int i = 0; i < instances.size(); i++) {
			args = args + " " + instances.get(i);
		}

		try {
			String command = SelfElastManStart.actuatorScriptsPath
					+ "/addInstances.sh" + args;
			_process = Runtime.getRuntime().exec(command);
			// ... don't forget to initialise in, out, and error,
			// .... and consume the streams in separate threads!
			_process.waitFor();

			_in = _process.getInputStream();
			InputStreamReader isr = new InputStreamReader(_in);
			_err = _process.getErrorStream();
			InputStreamReader esr = new InputStreamReader(_err);

			if (_in != null) {
				int n1;
				char[] c1 = new char[1024];
				StringBuffer standardOutput = new StringBuffer();
				while ((n1 = isr.read(c1)) > 0) {
					standardOutput.append(c1, 0, n1);
				}
				System.out.println("Standard Output: "
						+ standardOutput.toString());

			} else {
				int n2;
				char[] c2 = new char[1024];
				StringBuffer standardError = new StringBuffer();
				while ((n2 = esr.read(c2)) > 0) {
					standardError.append(c2, 0, n2);
				}
				System.out.println("Standard Error: "
						+ standardError.toString());
			}

		} finally {
			if (_process != null) {
				close(_process.getErrorStream());
				close(_process.getOutputStream());
				close(_process.getInputStream());
				_process.destroy();
				log.debug("[Process Exit Status], "+_process.exitValue());
			}
			close(_in);
			close(_out);
			close(_err);
		}
	}

	/*
	 * Currently available instances
	 */
	public static HashMap<String, Integer> getCassandraInstances() {
		String csvFile = "cassandra_nodes.txt";
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

	/*
	 * Get the nodes to decommission
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

	/*
	 * Get the nodes to Commission
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

	private static void close(InputStream anInput) {
		try {
			if (anInput != null) {
				anInput.close();
			}
		} catch (IOException anExc) {
			anExc.printStackTrace();
		}
	}

	private static void close(OutputStream anOutput) {
		try {
			if (anOutput != null) {
				anOutput.close();
			}
		} catch (IOException anExc) {
			anExc.printStackTrace();
		}
	}

}
