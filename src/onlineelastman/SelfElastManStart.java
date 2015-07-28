package onlineelastman;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

import org.apache.cassandra.service.DataStatistics;
import org.apache.log4j.Logger;

import actuator.Actuator;
import predictor.MatlabControl;
import predictor.PredictorMetrics;
import predictor.PredictorUtilities;

/**
 * @author GUREYA
 *
 */
public class SelfElastManStart {

	// Default configurations for Online Elastman overwritten by the config
	// properties
	public static int timerWindow = 5;
	public static OnlineModelMetrics[][][] dataPoints;
	public static int scale = 50;
	public static int queueLength = 10;
	public static int maxReadTP = 500;
	public static int maxWriteTP = 500;
	public static int maxDataSize = 100;
	public static double confLevel = 0.1;
	public static double currentDataSize = 1.0;
	public static String matlabPath = "/home/ubuntu/ElasticityManager/src/predictor";
	public static String actuatorScriptsPath = "/home/ubuntu/ElasticityManager/src/actuator";
	public static int readResponseTime = 5000;
	public static int targetThroughput = 1000; // target throughput per server

	// Variables used by Predictor Modules (r for reads && w for writes)
	// Initialized to the number of algorithms
	public static final int NUM_OF_ALGS = 6;
	public static double[] rpreviousPredictions = new double[NUM_OF_ALGS];
	public static double[] wpreviousPredictions = new double[NUM_OF_ALGS];
	public static double[] rcurrentPredictions = new double[NUM_OF_ALGS];
	public static double[] wcurrentPredictions = new double[NUM_OF_ALGS];
	public static boolean rinitialWeights = false;
	public static boolean winitialWeights = false;
	public static HashMap<Integer, Integer> rweights = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> wweights = new HashMap<Integer, Integer>();
	public static MatlabProxy proxy;
	
	//Testing the prediction with a sliding window
	public static List<Double> readList = new ArrayList<Double>();
	public static List<Double> writeList = new ArrayList<Double>();

	// Variables used by the Actuator
	public static int NUMBER_OF_SERVERS = 0;
	public static HashMap<String, Integer> nodesMap;
	public static int MIN_NUMBER_OF_SERVERS = 3;
	public static int MAX_NUMBER_OF_SERVERS = 10;

	static Logger log = Logger.getLogger(SelfElastManStart.class);
	Timer timer;

	// parameters for setting the datapoints grid - 3D (r(reads), w(writes),
	// d(datasize))
	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	// private int dstart;
	// private int dend;
	private int fineRead;
	private int fineWrite;
	private int fineDataSize;

	// for testing and debugging prediction
	public double rpredictedValue = 0;
	public double wpredictedValue = 0;
	long global_timeseries_counter = 0;

	/**
	 * @param timerWindow
	 * @throws MatlabInvocationException
	 */
	public SelfElastManStart(int timerWindow) throws MatlabInvocationException {
		timer = new Timer();
		log.info("Starting the Online Autonomic Controller...");

		// Read from the config properties if any
		Utilities properties = new Utilities();
		try {
			properties.getProperties();
			timerWindow = Integer.parseInt(properties.timerWindow.trim());
			maxReadTP = Integer.parseInt(properties.maxReadTP.trim());
			maxWriteTP = Integer.parseInt(properties.maxWriteTP.trim());
			maxDataSize = Integer.parseInt(properties.maxDataSize.trim());
			scale = Integer.parseInt(properties.scale.trim());
			queueLength = Integer.parseInt(properties.queueLength.trim());
			confLevel = Double.parseDouble(properties.confLevel.trim());
			currentDataSize = Double.parseDouble(properties.currentDataSize
					.trim());
			readResponseTime = Integer.parseInt(properties.readResponseTime
					.trim());
			matlabPath = properties.matlabPath;
			actuatorScriptsPath = properties.actuatorScriptsPath;
			targetThroughput = Integer.parseInt(properties.targetThroughput
					.trim());
			MIN_NUMBER_OF_SERVERS = Integer.parseInt(properties.minServers.trim());
			MAX_NUMBER_OF_SERVERS = Integer.parseInt(properties.maxServers.trim());
			SelfElastManStart.timerWindow = timerWindow;

			// Initialize the datapoint grids
			dataPoints = new OnlineModelMetrics[maxReadTP][maxWriteTP][maxDataSize];
			// Get initial number of servers for the all system
			nodesMap = Actuator.getCassandraInstances();
			// Set the maximum number of servers
			MAX_NUMBER_OF_SERVERS = nodesMap.size();
			// Initialize the active number of servers
			NUMBER_OF_SERVERS = Actuator.getCurrentNoServers(nodesMap);

			log.info("[Active Starting Number of Servers], "
					+ NUMBER_OF_SERVERS);
			log.info("[Minimum Number of Servers], " + MIN_NUMBER_OF_SERVERS);
			log.info("[Maximum Number of Servers], " + MAX_NUMBER_OF_SERVERS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String message = "[CONFIG PROPERTIES]: TimerWindow:" + timerWindow
				+ "\tMaxReadTP:" + maxReadTP + "\tMaxWriteTP:" + maxWriteTP
				+ "\tMaxDataSize:" + maxDataSize + "\tQueueLength:"
				+ queueLength + "\tConfLevel:" + confLevel + "\tGranularity:"
				+ scale + "\tMaxReadResponseTime:" + readResponseTime;
		log.info(message);

		try {
			startMatlabControl();
			log.info("MatlabControl successfully instantiated...");
		} catch (MatlabConnectionException e) { // TODO Auto-generated catch
												// block
			e.printStackTrace();
			log.fatal("Failed instantiating the MatlabControl...");
		}

		// /Testing the warm up phase // Testing the system with an existing
		// data
		PredictorUtilities pu = new PredictorUtilities();
		dataPoints = pu.readDataFile(dataPoints);

//		 for (int i = 0; i < dataPoints.length; i++) {
//		 for (int j = 0; j < dataPoints[i].length; j++) {
//		 for (int k = 0; k < dataPoints[i][j].length; k++) {
//		 if (dataPoints[i][j][k] != null) {
//		 int valid = (dataPoints[i][j][k].isValid()) ? 1 : -1;
//		 String data = dataPoints[i][j][k].getrThroughput()
//		 + "," + dataPoints[i][j][k].getwThroughput()
//		 + "," + dataPoints[i][j][k].getDatasize() + ","
//		 + (int) dataPoints[i][j][k].getRlatency() + ","
//		 + (int) dataPoints[i][j][k].getWlatency() + ","
//		 + valid + "," + dataPoints[i][j][k].getrQueue();
//		 System.out.println(data);
//		 // OnlineModel.printtoFile("dataFile.txt", data);
//		 }
//		 }
//		 }
//		 }
//		 System.exit(0);

		timer.schedule(new PeriodicExecutor(), 0, timerWindow * 1000);
	}

	public static void main(String[] args) throws IOException,
			MatlabInvocationException {
		new SelfElastManStart(timerWindow);
	}

	public static void startMatlabControl() throws MatlabConnectionException,
			MatlabInvocationException {
		// Set the matlab factory setting from opening every now and again
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setUsePreviouslyControlledSession(true).setHidden(true)
				.setMatlabLocation(null).build();

		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		proxy = factory.getProxy();

		// Add your scripts to Matlab path
		proxy.setVariable("matlabPath", matlabPath);
		log.debug("[MatlabPath], " + matlabPath);
		proxy.eval("addpath(matlabPath)");
	}

	class PeriodicExecutor extends TimerTask {
		@Override
		public void run() {
			log.debug("Timer Task Started..!%n...Collecting Periodic Statistics");
			double rThroughput = 0;
			double wThroughput = 0;
			double rPercentile = 0;
			double wPercentile = 0;
			int roperations = 0;
			int woperations = 0;

			// Average dataSize Need to find a way to get from the Cassandra
			// Cluster! currently just changing it from config!
			double dataSize = 1;

			DataStatistics statsArray[];
			try {
				statsArray = DataCollector.collectCassandraStats();
				if (!Double.isNaN(statsArray[0].getThroughput()))
					rThroughput = statsArray[0].getThroughput();
				if (!Double.isNaN(statsArray[1].getThroughput()))
					wThroughput = statsArray[1].getThroughput();
				if (!Double.isNaN(statsArray[0].getNnPctLatency()))
					rPercentile = statsArray[0].getNnPctLatency();
				if (!Double.isNaN(statsArray[1].getNnPctLatency()))
					wPercentile = statsArray[1].getNnPctLatency();
				if (!Double.isNaN(statsArray[0].getDataSize()))
					dataSize = statsArray[0].getDataSize();
				if (!Double.isNaN(statsArray[0].getNoRequests()))
					roperations = (int) statsArray[0].getNoRequests();
				if (!Double.isNaN(statsArray[1].getNoRequests()))
					woperations = (int) statsArray[1].getNoRequests();

				dataSize = currentDataSize; // Variation was done using the ycsb
											// client, Default 1KB
				if (roperations == 0 && woperations == 0) {
					log.info("No New dataStatitistics found...Zero operations reported");
				} else {
					// My throughput calculations here
					/*
					 * System.out
					 * .println("Timer Window just before calculating throughput: "
					 * + timerWindow);
					 */
					rThroughput = (roperations / timerWindow);

					wThroughput = (woperations / timerWindow);

					int rt = (int) (rThroughput / scale);
					int wt = (int) (wThroughput / scale);
					// int dsz = (int) (dataSize / scale);

					rstart = rt * scale;
					rend = rstart + scale;

					wstart = wt * scale;
					wend = wstart + scale;

					// dstart = dsz * scale;
					// dend = dstart + scale;

					fineRead = (rstart + rend) / 2;
					fineWrite = (wstart + wend) / 2;
					// fineDataSize = (dstart + dend) / 2;
					fineDataSize = (int) dataSize;

					log.debug("[Data Size(KB)], " + dataSize);
					log.debug("[READ], \tRunTime(us), "
							+ statsArray[0].getSum() + "\tOperations, "
							+ statsArray[0].getNoRequests()
							+ "\tThroughput(ops/sec), " + rThroughput
							+ "\t 99thPercentileLatency(us), " + rPercentile);

					log.debug("[UPDATE], \tRunTime(us), "
							+ statsArray[1].getSum() + "\tOperations, "
							+ statsArray[1].getNoRequests()
							+ "\tThroughput(ops/sec), " + wThroughput
							+ "\t 99thPercentileLatency(us), " + wPercentile);

					// Test for the OnlineModel --- Updating the Online Model
					Queue<Integer> rqe = new LinkedList<Integer>();
					Queue<Integer> wqe = new LinkedList<Integer>();

					rqe.add((int) rPercentile); // Read Queue is not null
					wqe.add((int) wPercentile); // Write Queue is not null

					OnlineModelMetrics omm = new OnlineModelMetrics(fineRead,
							fineWrite, fineDataSize, rPercentile, wPercentile,
							true, rqe, wqe, true);

					// OnlineModelMetrics[] newdataPoints = new
					// OnlineModelMetrics[dataPoints.length];
					dataPoints = OnlineModel.buildModel(dataPoints, omm,
							global_timeseries_counter);
					// System.arraycopy(newdataPoints, 0, dataPoints, 0,
					// dataPoints.length);
				}

				long start = System.nanoTime();
				// Test for the Predictor
				// Get predictions for time t+1
				// Convert reads and writes into two dimensional array to be
				// sent to the matlab scripts
				// Arrays in MATLAB are always at least two dimensions, so
				// the lowest
				// dimension Java array that can be sent to MATLAB is a
				// double[][]
				
				//Keep track of the read and write prediction in a list (Prediction_Data)
				readList.add((double) fineRead);
				writeList.add((double) fineWrite);
				double[][] readData = PredictorUtilities.convertIntegers(readList);
				double[][] writeData = PredictorUtilities.convertIntegers(writeList);
				
				// Print prediction data into a file for analysis
				// counter,realRead,predictedRead,realWrite,predictedWrite
				String pfile = "pdata.txt";
				String pdata = "";

				pdata = global_timeseries_counter + "," + fineRead + ","
						+ rpredictedValue + "," + fineWrite + ","
						+ wpredictedValue + "," + Math.round(rPercentile) + ","
						+ NUMBER_OF_SERVERS;
				OnlineModel.printtoFile(pfile, pdata);
				global_timeseries_counter += 1;

				// Prediction for the read throughput
				// For Debugging
				String rpp = "Read Previous Predictions: ";
				for (int i = 0; i < rpreviousPredictions.length; i++) {
					rpp += "\t" + rpreviousPredictions[i];
				}
				log.debug(rpp);

				// Define the threshold of read data to at least make a
				// prediction
				if (readData.length > 0) {
					rcurrentPredictions = MatlabControl.getPredictions(proxy,
							rcurrentPredictions, readData);
					log.debug("[Read Predictions], " + "\tavg: "
							+ rcurrentPredictions[0] + "\tmax: "
							+ rcurrentPredictions[1] + "\tfft_value: "
							+ rcurrentPredictions[2] + "\trt_value: "
							+ rcurrentPredictions[3] + "\tsvm_value: "
							+ rcurrentPredictions[4] + "\tminima: "
							+ rcurrentPredictions[5]);
					// Run the Weighted Majority Algorithm(WMA) and get the
					// predicted values
					// The actual values for the current window : fineRead &
					// fineWrite
					// Initialize the weights of all predictions to 1 [mean,
					// max, fft, reg_trees, libsvm, min]; Only at the beginning

					if (!rinitialWeights) {
						for (int i = 0; i < NUM_OF_ALGS; i++) {
							rweights.put(i, 3); // Assign a three-star
												// initially.
												// All weights are equal
						}
						rpredictedValue = PredictorUtilities
								.mean(rcurrentPredictions);
						// Update the previous predictions
						System.arraycopy(rcurrentPredictions, 0,
								rpreviousPredictions, 0,
								rpreviousPredictions.length);
						rinitialWeights = true;
					} else {
						PredictorMetrics rpm = MatlabControl.runWMA(
								rpreviousPredictions, rcurrentPredictions,
								rweights, fineRead);
						rweights = rpm.getWeights();
						rpreviousPredictions = rpm.getPreviousPredictions();
						rpredictedValue = rpm.getPredictedValue();
					}

					// For Debugging
					String rw = "\nWeights:";
					for (Entry<Integer, Integer> entry : rweights.entrySet()) {
						rw += "\t" + entry.getValue();
					}
					log.debug(rw);
					log.debug("Read predicted value for time t+1: "
							+ rpredictedValue);
				} else
					log.debug("...Not enough training data available to make read predictions...");

				// Prediction & WMA for the write throughput
				// For Debugging
				String wpp = "Write Previous Predictions: ";
				for (int i = 0; i < wpreviousPredictions.length; i++) {
					wpp += "\t" + wpreviousPredictions[i];
				}
				log.debug(wpp);

				// Define the threshold of write data to at least make a
				// prediction
				if (writeData.length > 0) {
					wcurrentPredictions = MatlabControl.getPredictions(proxy,
							wcurrentPredictions, writeData);
					log.debug("[Write Predictions], " + "\tavg: "
							+ wcurrentPredictions[0] + "\tmax: "
							+ wcurrentPredictions[1] + "\tfft_value: "
							+ wcurrentPredictions[2] + "\trt_value: "
							+ wcurrentPredictions[3] + "\tsvm_value: "
							+ wcurrentPredictions[4] + "\tminima: "
							+ wcurrentPredictions[5]);
					// Get the predictedValue
					if (!winitialWeights) {
						for (int i = 0; i < NUM_OF_ALGS; i++) {
							wweights.put(i, 3); // Assign a three-star
												// initially.
												// All weights are equal
						}
						wpredictedValue = PredictorUtilities
								.mean(wcurrentPredictions);
						// Update the previous predictions
						System.arraycopy(wcurrentPredictions, 0,
								wpreviousPredictions, 0,
								wpreviousPredictions.length);
						winitialWeights = true;
					} else {
						PredictorMetrics wpm = MatlabControl.runWMA(
								wpreviousPredictions, wcurrentPredictions,
								wweights, fineWrite);
						wweights = wpm.getWeights();
						wpreviousPredictions = wpm.getPreviousPredictions();
						wpredictedValue = wpm.getPredictedValue();
					}

					// For Debugging
					String ww = "\nWeights:";
					for (Entry<Integer, Integer> entry : wweights.entrySet()) {
						ww += "\t" + entry.getValue();
					}
					log.debug(ww);
					log.debug("Write predicted value for time t+1: "
							+ wpredictedValue);
				} else
					log.debug("...Not enough training data available to make write predictions...");

				// Time it takes to execute all the scripts
				log.debug("Elapsed Time(ms) for predictions: "
						+ ((System.nanoTime() - start) / 1000000));

				// Testing the trained system model
				// Model TrainingData - Reading from a config file
				PredictorMetrics data2dArray = PredictorUtilities
						.getDataIn2DArray(dataPoints);

				double[][] reads = data2dArray.getReads();
				double[][] writes = data2dArray.getWrites();
				double[][] dszs = data2dArray.getDsz();
				double[][] trainingLabels = data2dArray.getTrainingLabels();
				
				int extraServers = 0;
				log.debug("[Current Number of Servers] " + NUMBER_OF_SERVERS);

				// At least determine a threshold for the training data to get
				// the system model
				if (reads.length > 0) {
					// Get the primal variables w, b from the model
					double[] primalVariables = OnlineModel.getUpdatedModel(
							proxy, reads, writes, dszs, trainingLabels);

					if (primalVariables.length != 0) {
						// Get the extra number of servers needed
						extraServers = Actuator.getExtraNumberOfServers(
								primalVariables, rpredictedValue,
								wpredictedValue, NUMBER_OF_SERVERS, dataSize);

						// Testing the Actuator module
						if (extraServers < 0) {

							// Decommission the extra number of servers
							ArrayList<String> nodesToDecommission = Actuator
									.getNodesToDecommission(nodesMap,
											extraServers);
							if (nodesToDecommission != null) {
								log.debug("Starting decommissionig "
										+ extraServers + " servers");
								Actuator.decommissionInstances(nodesToDecommission);
								// Update the current nodesMap assuming
								// decommissioning happened sucessfully
								nodesMap = Actuator.updateCurrentNoservers(
										nodesToDecommission, nodesMap, 0);

								// Update the current number of servers
								NUMBER_OF_SERVERS = Actuator
										.getCurrentNoServers(nodesMap);

							} else
								log.debug("No enough instances to carry out Decommissioning...");

						} else if (extraServers > 0) {
							ArrayList<String> nodesToCommission = Actuator
									.getNodesToCommission(nodesMap,
											extraServers);
							if (nodesToCommission != null) {
								log.debug("Starting Commissionig "
										+ extraServers + " servers");
								Actuator.commissionInstances(nodesToCommission);

								// Update the current nodesMap assuming //
								// commissioning happened sucessfully
								nodesMap = Actuator.updateCurrentNoservers(
										nodesToCommission, nodesMap, 1);
								// Update the current number of servers
								NUMBER_OF_SERVERS = Actuator
										.getCurrentNoServers(nodesMap);

							} else
								log.debug("No enough instances to carry out Commissioning...");
						} else
							log.debug("Cassandra Cluster at its optimal performance, No actuation required");
					} else
						log.debug("Primal variables returned null..problem with the training data");

				} else
					log.debug("...Not enough training data available to get the current system model and carry out actuation...");

				log.debug("Timer Task Finished..!%n...Collecting Periodic DataStatistics");
			} catch (IOException | MatlabInvocationException | InterruptedException e) {
				// TODO Auto-generated catch block
				log.debug("Timer Task Aborted with Errors...!%n: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
