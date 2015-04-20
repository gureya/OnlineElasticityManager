package selfElastMan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.cassandra.service.DataStatistics;

public class DataCollector {
	public static DataStatistics[] collectCassandraStats() throws IOException {
		DataStatistics statsArray[] = new DataStatistics[2];
		String serverAddress = InetAddress.getLocalHost().getHostAddress()
				.trim();
		Socket s = new Socket(serverAddress, 9898);
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

		try {
			statsArray = (DataStatistics[]) ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return statsArray;
	}
}
