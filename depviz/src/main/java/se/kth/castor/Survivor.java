package se.kth.castor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Survivor {

	public static void main(String args[]) throws SQLException, IOException {

		int libs[] = new int[] {
				1490, 3460, 4236, 289, 5363, 2884, 3099, 1814, 1866, 2485, 630, 640, 3541, 1565, 1365, 3922, 3683, 3918, 526, 2495,
				4343, 808, 2528, 1090, 1798, 3619, 1662, 413, 5244, 2518, 3197, 1062, 2406, 2905, 403, 1977, 541, 3315, 4517, 3561,
				3824, 2841, 4581, 1678, 3212, 4908, 4666, 1851, 3611, 2034, 3420, 4157, 1845, 2929, 4698, 5008, 576, 2804, 426, 5351,
				4729, 4843, 751, 1969, 905, 4743, 4194, 2965, 833, 406, 2914, 4450, 3148, 4959, 2429, 4929, 3071, 4328, 114, 2599, 521,
				2378, 1116, 2338, 4235, 2088, 4778, 4534, 704, 315, 1687, 1043, 611, 1342, 1235
		};

		Connection db = new MariaDBWrapper().getConnection();

		Map<Integer, String> libCoordinates = new HashMap<>();
		String queryId = Arrays.toString(libs).substring(1);
		queryId = queryId.substring(0,queryId.length()-1);

		PreparedStatement getLibsStmt = db.prepareStatement("SELECT id, coordinates FROM library WHERE id IN (" + queryId + ")");
		ResultSet resultSet = getLibsStmt.executeQuery();
		while(resultSet.next()) {
			libCoordinates.put(resultSet.getInt("id"),resultSet.getString("coordinates"));
		}

		for(int i= 0; i < libs.length; i++) {
			/*Survivor s = new Survivor();
			s.init(db, libs[i]);
			s.computeSurvival();
			s.export(new File("./survivor/survivor-el-" + libs[i] + ".csv"));*/

			SurvivorClass c = new SurvivorClass();
			c.init(db, libs[i], libCoordinates.get(libs[i]));
			c.computeSurvival();
			c.export(new File("./survivor-class/" + libCoordinates.get(libs[i]) + ".csv"));
		}
	}


	List<Integer> elementIDs; //Sorted
	Set<Map.Entry<Integer,Integer>> clientsElementR;
	Map<Integer,Integer> clientsTimeOfDeath;


	static String getElements = "SELECT m.id, COUNT(DISTINCT(u.clientid)) as clients " +
			"FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id WHERE m.libraryid=? " +
			"GROUP BY m.id ORDER BY clients;"; // DESC
	static String getUsages = "SELECT m.id, u.clientid " +
			"FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id WHERE m.libraryid=? " +
			";";
	public void init(Connection db, int libraryid) throws SQLException {
		elementIDs = new ArrayList<>();
		clientsElementR = new HashSet<>();
		clientsTimeOfDeath = new HashMap<>();

		PreparedStatement getElementsStmt = db.prepareStatement(getElements);
		getElementsStmt.setInt(1,libraryid);
		ResultSet resultSet = getElementsStmt.executeQuery();
		while(resultSet.next()) {
			elementIDs.add(resultSet.getInt("id"));
		}

		PreparedStatement getUsagesStmt = db.prepareStatement(getUsages);
		getUsagesStmt.setInt(1,libraryid);
		ResultSet uResultSet = getUsagesStmt.executeQuery();
		while(uResultSet.next()) {
			clientsElementR.add(new HashMap.SimpleEntry<>(uResultSet.getInt("clientid"),uResultSet.getInt("id")));
		}
	}

	int[] deadPopulation;
	public void computeSurvival() {
		deadPopulation = new int[elementIDs.size()];
		for(int i = 0; i < elementIDs.size(); i++) {
			Integer element = elementIDs.get(i);
			Set<Map.Entry<Integer,Integer>> rToRemove = new HashSet<>();
			for(Map.Entry<Integer,Integer> r : clientsElementR) {
				if(r.getValue().equals(element)) {
					rToRemove.add(r);
				}
			}
			for(Map.Entry<Integer,Integer> r : rToRemove) {
				Integer client = r.getKey();
				clientsElementR.remove(r);
				if(!clientsTimeOfDeath.containsKey(client)) {
					clientsTimeOfDeath.put(client, i + 1);
				}
			}
			deadPopulation[i] = clientsTimeOfDeath.size();
			//System.out.println(i + ": " + clientsTimeOfDeath.size() + " clients are dead");
		}
	}

	public void export(File f) throws IOException {
		/*FileUtils.write(f,"clientid,time\n");
		for(Map.Entry<Integer,Integer> e: clientsTimeOfDeath.entrySet()) {
			FileUtils.write(f,e.getKey() + "," + e.getValue() + "\n", true);
		}*/
		if(elementIDs.size() > 0) {
			int maxPop = deadPopulation[elementIDs.size() - 1];
			FileUtils.write(f, "step,population,element\n");
			FileUtils.write(f, "0," + maxPop + ",NA\n", true);
			for (int i = 0; i < elementIDs.size(); i++) {
				FileUtils.write(f, (i + 1) + "," + (maxPop - deadPopulation[i]) + "," + elementIDs.get(i) + "\n", true);
			}
		} else {
			System.err.println("NO api element");
		}
	}
}
