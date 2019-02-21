package se.kth.castor;

import org.apache.commons.io.FileUtils;

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

public class SurvivorClass {

	public static int[] coresToPrint = new int[]{1,10,50};
	//public static int[] coresToPrint;// = new int[]{1,2,3,4,5,6,7,8,9,};

	public static void main(String args[]) throws SQLException, IOException {
		/*coresToPrint = new int[100];
		for(int i =1; i <= 100; i++) coresToPrint[i-1]=i;*/
		/*SurvivorClass s = new SurvivorClass();
		s.init(new MariaDBWrapper().getConnection(), 521, "");
		s.computeSurvival();
		s.export(new File("./survivor-1040.csv"));*/
		String libSummary = "groupid,artifactid,version,versionInt,maxPop";

		for(int j = 0; j <= 100; j ++) {
			libSummary += ",core" + (j);
		}

		libSummary += ",UsedAPISize\n";
		FileUtils.write(coreSizeFile,libSummary);

		Connection db = new MariaDBWrapper().getConnection();

		Map<Integer, String> libCoordinates = new HashMap<>();

		PreparedStatement getLibsStmt = db.prepareStatement("SELECT id, coordinates, groupid, artifactid, version, versionInt FROM library WHERE id");
		ResultSet resultSet = getLibsStmt.executeQuery();
		while(resultSet.next()) {
			String key = resultSet.getString("groupid") + "," +
					resultSet.getString("artifactid") + "," +
					resultSet.getString("version") + "," +
					resultSet.getString("versionInt");
			libCoordinates.put(resultSet.getInt("id"), key);
		}

		for(Integer i: libCoordinates.keySet()) {
			/*Survivor s = new Survivor();
			s.init(db, libs[i]);
			s.computeSurvival();
			s.export(new File("./survivor/survivor-el-" + libs[i] + ".csv"));*/

			SurvivorClass c = new SurvivorClass();
			c.init(db, i, libCoordinates.get(i));
			c.computeSurvival();
			c.export(new File("./survivor-class-inv/" + libCoordinates.get(i) + ".csv"));//Add GAV
		}
	}

	//static File coreSizeFile = new File("./core-sizes.csv");
	static File coreSizeFile = new File("./core-n-sizes-inv.csv");


	List<String> elementIDs; //Sorted
	Set<Map.Entry<Integer,String>> clientsElementR;
	Map<Integer,Integer> clientsTimeOfDeath;
	int libraryid;
	String libName;


	static boolean desc = true;
	static String getElements = "SELECT p.package, m.class, COUNT(DISTINCT(u.clientid)) as clients " +
			"FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id JOIN package as p ON m.packageid=p.id WHERE m.libraryid=? " +
			"GROUP BY m.packageid, m.class ORDER BY clients"; // DESC
	static String getUsages = "SELECT p.package, m.class, u.clientid " +
			"FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id JOIN package as p ON m.packageid=p.id WHERE m.libraryid=? " +
			"GROUP BY u.clientid, m.packageid, m.class" +
			";";
	public void init(Connection db, int libraryid, String libName) throws SQLException {
		elementIDs = new ArrayList<>();
		clientsElementR = new HashSet<>();
		clientsTimeOfDeath = new HashMap<>();
		this.libraryid = libraryid;
		this.libName = libName;

		PreparedStatement getElementsStmt = db.prepareStatement(desc ? getElements + " DESC;" : getElements);
		getElementsStmt.setInt(1,libraryid);
		ResultSet resultSet = getElementsStmt.executeQuery();
		while(resultSet.next()) {
			elementIDs.add(resultSet.getString("package") + "/" + resultSet.getString("class"));
		}

		PreparedStatement getUsagesStmt = db.prepareStatement(getUsages);
		getUsagesStmt.setInt(1,libraryid);
		ResultSet uResultSet = getUsagesStmt.executeQuery();
		while(uResultSet.next()) {
			clientsElementR.add(new HashMap.SimpleEntry<>(
					uResultSet.getInt("clientid"),
					uResultSet.getString("package") + "/" + uResultSet.getString("class"))
			);
		}
	}

	int[] deadPopulation;
	public void computeSurvival() {
		deadPopulation = new int[elementIDs.size()];
		for(int i = 0; i < elementIDs.size(); i++) {
			int deadpop = clientsTimeOfDeath.size();
			String element = elementIDs.get(i);
			Set<Map.Entry<Integer,String>> rToRemove = new HashSet<>();
			for(Map.Entry<Integer,String> r : clientsElementR) {
				if(r.getValue().equals(element)) {
					rToRemove.add(r);
				}
			}
			for(Map.Entry<Integer,String> r : rToRemove) {
				Integer client = r.getKey();
				clientsElementR.remove(r);
				if(!clientsTimeOfDeath.containsKey(client)) {
					clientsTimeOfDeath.put(client, i + 1);
				}
			}
			deadPopulation[i] = clientsTimeOfDeath.size();
			int newDead = clientsTimeOfDeath.size() - deadpop;
			//System.out.println(i + ": " + clientsTimeOfDeath.size() + " clients are dead (" + newDead + ")");
		}
		//System.out.println("Done");
	}

	public static double threshold = 0.5;
	public static double threshold90 = 0.1;

	public void export(File f) throws IOException {
		/*FileUtils.write(f,"clientid,time\n");
		for(Map.Entry<Integer,Integer> e: clientsTimeOfDeath.entrySet()) {
			FileUtils.write(f,e.getKey() + "," + e.getValue() + "\n", true);
		}*/
		boolean found = false;
		boolean found90 = false;
		int core50Size = elementIDs.size();
		int core90Size = elementIDs.size();


		int[] coreNSize = new int[101];
		boolean[] foundN = new boolean[101];
		for(int i = 0; i < coreNSize.length; i ++) {
			coreNSize[i] = elementIDs.size();
			foundN[i] = false;
		}

		if(elementIDs.size() > 0) {
			int maxPop = deadPopulation[elementIDs.size()-1];
			double dMaxPop = (double) maxPop;

			String header = "step,population,element";
			String firstLine = "0," + maxPop + ",NA";
			for(int n: coresToPrint) {
				header += ",isCore"+n;
				firstLine += ",false";
			}
			FileUtils.write(f,header + "\n");
			FileUtils.write(f,firstLine + "\n", true);


			//FileUtils.write(f,"step,population,element,isCore50,isCore90\n");
			//FileUtils.write(f,"0," + maxPop + ",NA,false,false\n", true);
			for(int i = 0; i < elementIDs.size(); i++) {
				boolean isCore50 = deadPopulation[i] >= (threshold * maxPop);
				boolean isCore90 = deadPopulation[i] >= (threshold90 * maxPop);

				String line = (i + 1) + "," + (maxPop - deadPopulation[i]) + "," + elementIDs.get(i);
				for(int n: coresToPrint) {
					line += "," + isCore(n, deadPopulation[i], dMaxPop);
				}
				FileUtils.write(f,line + "\n", true);

				//FileUtils.write(f,(i + 1) + "," + (maxPop - deadPopulation[i]) + "," + elementIDs.get(i) + "," + isCore50 + "," + isCore90 + "\n", true);
				if(!found && isCore50) {
					found = true;
					System.out.println("Lib: " + libName +", Clients: " + deadPopulation[(i-1 > 0) ? i-1 : 0] + " / " + maxPop +
							" -> elements: " + i + " / " + elementIDs.size() + " (" + (((double) i) / ((double) elementIDs.size())) + ")");
				} else if(!found) {
					core50Size--;
				}
				if(!found90 && isCore90) {
					found90 = true;
				} else if(!found90) {
					core90Size--;
				}

				for(int j = 0; j < coreNSize.length; j ++) {
					if(!foundN[j] && isCore(j, deadPopulation[i], dMaxPop)) {
						foundN[j]= true;
					} else if(!foundN[j]) {
						coreNSize[j]--;
					}
				}
			}

			String libSummary = libName + "," + maxPop;
			for(int j = 0; j < coreNSize.length; j++) {
				libSummary += "," + coreNSize[j];
			}
			libSummary += "," + elementIDs.size() + "\n";
			FileUtils.write(coreSizeFile,libSummary, true);

			//FileUtils.write(coreSizeFile,libName + "," + maxPop + "," + core50Size + "," + core90Size + "," + elementIDs.size() + "\n", true);
		} else {
			System.err.println("NO api element");
		}
	}

	public static boolean isCore(int n, int deads, double maxPop) {
		return deads >= ((1.0 - ((double) n)/100.0) * maxPop);
	}
}