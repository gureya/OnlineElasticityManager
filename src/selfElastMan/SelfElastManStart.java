package selfElastMan;

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
	public static OnlineModelMetrics[][] dataPoints;
	public static int scale = 50;
	public static int queueLength = 10;
	public static int maxReadTP = 500;
	public static int maxWriteTP = 500;
	public static int maxDataSize = 100;
	public static double confLevel = 0.1;
	public static int readResponseTime = 5000;

	// Variables used by Predictor Modules (r for reads && w for writes)
	// Initialized to the number of algorithms
	public static final int NUM_OF_ALGS = 5;
	public static double[] rpreviousPredictions = new double[NUM_OF_ALGS];
	public static double[] wpreviousPredictions = new double[NUM_OF_ALGS];
	public static double[] rcurrentPredictions = new double[NUM_OF_ALGS];
	public static double[] wcurrentPredictions = new double[NUM_OF_ALGS];
	public static boolean rinitialWeights = false;
	public static boolean winitialWeights = false;
	public static HashMap<Integer, Integer> rweights = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> wweights = new HashMap<Integer, Integer>();
	public static MatlabProxy proxy;

	static Logger log = Logger.getLogger(SelfElastManStart.class);
	Timer timer;

	// parameters for setting the datapoints grid
	private int rstart;
	private int wstart;
	private int rend;
	private int wend;
	private int fineRead;
	private int fineWrite;

	public SelfElastManStart(int timerWindow) {
		timer = new Timer();
		log.info("Starting the Autonomic Controller...");

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
			readResponseTime = Integer.parseInt(properties.readResponseTime
					.trim());
			dataPoints = new OnlineModelMetrics[maxReadTP][maxWriteTP];
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
		} catch (MatlabConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.fatal("Failed instantiating the MatlabControl...");
		}

		timer.schedule(new PeriodicExecutor(), 0, timerWindow * 1000);
	}

	public static void main(String[] args) throws IOException {
		new SelfElastManStart(timerWindow);
	}

	public static void startMatlabControl() throws MatlabConnectionException {
		// Set the matlab factory setting from opening every now and again
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setUsePreviouslyControlledSession(true).setHidden(true)
				.setMatlabLocation(null).build();

		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		proxy = factory.getProxy();
	}

	class PeriodicExecutor extends TimerTask {
		@Override
		public void run() {
			log.debug("Timer Task Started..!%n...Collecting Periodic Statistics");
			double rThroughput = 0;
			double wThroughput = 0;
			double rPercentile = 0;
			double wPercentile = 0;

			// Average dataSize Need to find a way to get from the Cassandra
			// Cluster!
			double dataSize = 0; // Variation was done using the ycsb client

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

				if (rThroughput == 0 && wThroughput == 0 && dataSize == 0) {
					log.info("No New dataStatitistics found...Zero operations reported");
				} else {
					// My throughput calculations here
					int roperations = (int) statsArray[0].getNoRequests();
					rThroughput = (roperations / timerWindow);

					int woperations = (int) statsArray[1].getNoRequests();
					wThroughput = (woperations / timerWindow);

					int rt = (int) (rThroughput / scale);
					int wt = (int) (wThroughput / scale);

					rstart = rt * scale;
					rend = rstart + scale;

					wstart = wt * scale;
					wend = wstart + scale;

					fineRead = (rstart + rend) / 2;
					fineWrite = (wstart + wend) / 2;

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
							fineWrite, (int) dataSize, rPercentile,
							wPercentile, true, rqe, wqe, true);

					// OnlineModelMetrics[] newdataPoints = new
					// OnlineModelMetrics[dataPoints.length];
					dataPoints = OnlineModel.buildModel(dataPoints, omm);
					// System.arraycopy(newdataPoints, 0, dataPoints, 0,
					// dataPoints.length);

					// Test for the Predictor
					// Get predictions for time t+1
					// Convert reads and writes into two dimensional array to be
					// sent to the matlab scripts
					// Arrays in MATLAB are always at least two dimensions, so
					// the lowest
					// dimension Java array that can be sent to MATLAB is a
					// double[][]
					double[][] reads = PredictorUtilities
							.getReadDatapoints(dataPoints);
					double[][] writes = PredictorUtilities
							.getWriteDatapoints(dataPoints);
					// Prediction for the read throughput
					rcurrentPredictions = MatlabControl.getPredictions(proxy,
							dataPoints, rcurrentPredictions, reads);
					log.debug("[Read Predictions], " + "\tavg: "
							+ rcurrentPredictions[0] + "\tmax: "
							+ rcurrentPredictions[1] + "\tfft_value: "
							+ rcurrentPredictions[2] + "\trt_value: "
							+ rcurrentPredictions[3] + "\tsvm_value: "
							+ rcurrentPredictions[4]);
					// Run the Weighted Majority Algorithm(WMA) and get the
					// predicted values
					// The actual values for the current window : fineRead &
					// fineWrite
					// Initialize the weights of all predictions to 1 [mean,
					// max, fft,
					// reg_trees, libsvm]; Only at the beginning

					// For Debugging
					String rpp = "Read Previous Predictions: ";
					for (int i = 0; i < rpreviousPredictions.length; i++) {
						rpp += "\t" + rpreviousPredictions[i];
					}
					log.debug(rpp);

					double rpredictedValue = 0;
					if (!rinitialWeights) {
						for (int i = 0; i < NUM_OF_ALGS; i++) {
							rweights.put(i, 5); // Assign a five-star initially.
												// All weights are equal
						}
						rpredictedValue = PredictorUtilities
								.mean(rcurrentPredictions);
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

					// Prediction & WMA for the write throughput

					// For Debugging
					String wpp = "Write Previous Predictions: ";
					for (int i = 0; i < wpreviousPredictions.length; i++) {
						wpp += "\t" + wpreviousPredictions[i];
					}
					log.debug(wpp);

					wcurrentPredictions = MatlabControl.getPredictions(proxy,
							dataPoints, wcurrentPredictions, writes);
					log.debug("[Write Predictions], " + "\tavg: "
							+ wcurrentPredictions[0] + "\tmax: "
							+ wcurrentPredictions[1] + "\tfft_value: "
							+ wcurrentPredictions[2] + "\trt_value: "
							+ wcurrentPredictions[3] + "\tsvm_value: "
							+ wcurrentPredictions[4]);
					// Get the predictedValue
					double wpredictedValue = 0;
					if (!winitialWeights) {
						for (int i = 0; i < NUM_OF_ALGS; i++) {
							wweights.put(i, 5); // Assign a five-star initially.
												// All weights are equal
						}
						wpredictedValue = PredictorUtilities
								.mean(wcurrentPredictions);
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
					for (Entry<Integer, Integer> entry : rweights.entrySet()) {
						ww += "\t" + entry.getValue();
					}
					log.debug(ww);
					log.debug("Write predicted value for time t+1: "
							+ wpredictedValue);

				}
				log.debug("Timer Task Finished..!%n...Collecting Periodic DataStatistics");
			} catch (IOException | MatlabInvocationException e) {
				// TODO Auto-generated catch block
				log.debug("Timer Task Aborted with Errors...!%n: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
