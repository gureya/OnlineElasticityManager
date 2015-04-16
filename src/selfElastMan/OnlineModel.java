package selfElastMan;

import java.util.Map;

public class OnlineModel {

	/**
	 * A function that tries to build the online model
	 * 
	 * @param modelMap
	 *            Existing ModelMap
	 * @param modelMetrics
	 *            [ReadLatency, WriteLatency, DataSize] = key(K) to the map;
	 * @param metricsClass
	 *            [valid/invalid] = value(V) to the map;
	 * @return Updated Map
	 */
	public static Map<OnlineModelMetrics, Integer> buildModel(
			Map<OnlineModelMetrics, Integer> modelMap,
			OnlineModelMetrics modelMetrics, int metricsClass) {

		modelMap.put(modelMetrics, metricsClass);

		return modelMap;
	}
}
