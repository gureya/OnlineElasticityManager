package selfElastMan;

public class DataStatistics {
double avgLatency;
public double getAvgLatency() {
	return avgLatency;
}
public void setAvgLatency(double avgLatency) {
	this.avgLatency = avgLatency;
}
public double getMinLatency() {
	return minLatency;
}
public void setMinLatency(double minLatency) {
	this.minLatency = minLatency;
}
public double getMaxLatency() {
	return maxLatency;
}
public void setMaxLatency(double maxLatency) {
	this.maxLatency = maxLatency;
}
public double getNfPctLatency() {
	return nfPctLatency;
}
public void setNfPctLatency(double nfPctLatency) {
	this.nfPctLatency = nfPctLatency;
}
public double getNnPctLatency() {
	return nnPctLatency;
}
public void setNnPctLatency(double nnPctLatency) {
	this.nnPctLatency = nnPctLatency;
}
double minLatency;
double maxLatency;
double nfPctLatency;
double nnPctLatency;
}
