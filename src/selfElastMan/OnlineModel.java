package selfElastMan;

public class OnlineModel {

	public static double[][][] buildModel(double[][][] readLModel, int read,
			int write, int datasize, double readLatency) {

		if (readLModel[read][write][datasize] == 0)
			readLModel[read][write][datasize] = readLatency;
		return readLModel;
	}

}
