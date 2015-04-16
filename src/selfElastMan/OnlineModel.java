package selfElastMan;

import java.util.Map;

public class OnlineModel {

	public static Map<OnlineModelMetrics, Integer> buildModel(
			Map<OnlineModelMetrics, Integer> modelMap,
			OnlineModelMetrics modelMetrics, int metricsClass) {
		modelMap.put(modelMetrics, metricsClass);
		return modelMap;
	}
}
