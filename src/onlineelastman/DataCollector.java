package onlineelastman;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.cassandra.service.DataStatistics;
import org.apache.log4j.Logger;

/**
 * @author GUREYA
 *
 */
public class DataCollector {
	static Logger log = Logger.getLogger(DataCollector.class);
	public static DataStatistics[] collectCassandraStats() throws IOException {
		DataStatistics statsArray[] = new DataStatistics[2];
		String serverAddress = InetAddress.getLocalHost().getHostAddress()
				.trim();
		Socket s = new Socket(serverAddress, 9898);
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

		try {
			statsArray = (DataStatistics[]) ois.readObject();
			log.info("Success: pulling DataStatitics from the Cassandra node...");
			//System.out.println("Success: pulling DataStatistics from the Cassandra node...");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.info("Failed: pulling DataStatistics from the Cassandra node..."+e.getMessage());
			//System.out.println("Failed: pulling DataStatistics from the Cassandra node..." + e.getMessage());
			e.printStackTrace();
		}
		catch (ConnectException conn){
			log.fatal("Failed: pulling DataStatistics from the Cassandra node..."+conn.getMessage());
			//System.out.println("Failed: pulling DataStatistics from the Cassandra node..." + conn.getMessage());
		}

		return statsArray;
	}
}
