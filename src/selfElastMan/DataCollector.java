package selfElastMan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
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
			System.out.println("Success: pulling DataStatistics from the Cassandra node...");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed: pulling DataStatistics from the Cassandra noode..." + e.getMessage());
			e.printStackTrace();
		}
		catch (ConnectException conn){
			System.out.println("Failed: pulling DataStatistics from the Cassandra noode..." + conn.getMessage());
		}

		return statsArray;
	}
}
