/**
 * 
 */
package onlineelastman;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author GUREYA
 *
 */
public class Utilities {
	String timerWindow;
	String maxReadTP;
	String maxWriteTP;
	String maxDataSize;
	String scale;
	String queueLength;
	String confLevel;
	String currentDataSize;
	String readResponseTime;
	String matlabPath;
	String actuatorScriptsPath;
	String targetThroughput;
	String minServers;
	String maxServers;

	static Logger log = Logger.getLogger(Utilities.class);

	public void getProperties() throws IOException {

		Properties prop = new Properties();
		String propFileName = "resources/config.properties";

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file' " + propFileName
					+ "' not found in the classpath");
		}

		// get the property values
		timerWindow = prop.getProperty("timerWindow");
		maxReadTP = prop.getProperty("maxReadTP");
		maxWriteTP = prop.getProperty("maxWriteTP");
		maxDataSize = prop.getProperty("maxDataSize");
		scale = prop.getProperty("scale");
		queueLength = prop.getProperty("queueLength");
		confLevel = prop.getProperty("confLevel");
		currentDataSize = prop.getProperty("currentDataSize");
		readResponseTime = prop.getProperty("readResponseTime");
		matlabPath = prop.getProperty("matlabPath");
		actuatorScriptsPath = prop.getProperty("actuatorScriptsPath");
		targetThroughput = prop.getProperty("targetThroughput");
		minServers = prop.getProperty("minServers");
		maxServers = prop.getProperty("maxServers");
	}
}
