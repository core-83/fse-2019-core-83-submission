package se.kth.castor;

import javafx.collections.transformation.SortedList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VersionCompare {



	public static void main(String args[]) throws SQLException, IOException {
		//generateClientVersionUpdate();
		generateDependencyUpdate();
	}

	public static void generateDependencyUpdate() throws SQLException, IOException {
		String getLibs = "SELECT id FROM library";
		String getLibDep = "SELECT d.clientid, d.libraryid, COUNT(DISTINCT(t.id)) as nbElement, " +
				"COUNT(DISTINCT(t.class)) as nbClass, COUNT(DISTINCT(t.packageid)) as nbPackage, SUM(t.nb) as nbCall, " +
				"MAX(t.diversity) as diversity, AVG(t.diversity) as average_div " +
				"FROM dependency as d JOIN (" +
					"SELECT * FROM api_usage as u " +
						"JOIN api_member as m ON u.apimemberid=m.id WHERE m.libraryid=?" +
					") as t ON t.clientid=d.clientid AND t.libraryid=d.libraryid " +
				"WHERE d.libraryid=? GROUP BY d.clientid";
		File out = new File("update_dependency_info.sql");
		MariaDBWrapper db = new MariaDBWrapper();
		PreparedStatement getLibsStmt = db.getConnection().prepareStatement(getLibs);

		ResultSet resultSet = getLibsStmt.executeQuery();

		Set<Integer> libs = new HashSet<>();

		while(resultSet.next()) {
			libs.add(resultSet.getInt("id"));
		}

		for(Integer libID : libs) {
			PreparedStatement getLibVersionsStmt = db.getConnection().prepareStatement(getLibDep);
			getLibVersionsStmt.setInt(1, libID);
			getLibVersionsStmt.setInt(2, libID);

			ResultSet versionSet = getLibVersionsStmt.executeQuery();

			while (versionSet.next()) {
				Integer cliID = versionSet.getInt("clientid");
				Integer nbCall = versionSet.getInt("nbCall");
				Integer nbElement = versionSet.getInt("nbElement");
				Integer nbClass = versionSet.getInt("nbClass");
				Integer nbPackage = versionSet.getInt("nbPackage");
				Integer diversity = versionSet.getInt("diversity");
				Float avgDiversity = versionSet.getFloat("average_div");
				FileUtils.write(out, "UPDATE dependency SET intensity=" + nbCall + ", nbElement=" + nbElement +
							", nbClass=" + nbClass + ", nbPackage=" + nbPackage + ", diversity=" + diversity + ", avg_diversity=" + avgDiversity + " " +
						"WHERE clientid=" + cliID + " AND libraryid=" + libID + ";\n", true);
			}


			System.out.println(libID + " Done");
		}
	}

	public static void generateClientVersionUpdate() throws SQLException, IOException {
		String table = "client"; // "library"
		String getLibVersions = "SELECT id, groupid, artifactid, version FROM " + table + ";";
		File out = new File("update_" + table + "_version.sql");
		MariaDBWrapper db = new MariaDBWrapper();

		Map<String, List<Map.Entry<Integer,ComparableVersion>>> libsGAV = new HashMap<>();

		PreparedStatement getLibVersionsStmt = db.getConnection().prepareStatement(getLibVersions);
		ResultSet versionSet = getLibVersionsStmt.executeQuery();

		while(versionSet.next()) {
			Integer id = versionSet.getInt("id");
			String groupid = versionSet.getString("groupid");
			String artifactid = versionSet.getString("artifactid");
			String version = versionSet.getString("version");
			libsGAV.putIfAbsent(groupid + ":" + artifactid, new ArrayList<>());
			List<Map.Entry<Integer,ComparableVersion>> versions = libsGAV.get(groupid + ":" + artifactid);
			versions.add(new HashMap.SimpleEntry<>(id,new ComparableVersion(version)));
		}

		for(String ga : libsGAV.keySet()) {

			List<Map.Entry<Integer,ComparableVersion>> versions = libsGAV.get(ga);

			Collections.sort(versions, Comparator.comparing(Map.Entry::getValue));
			for(int i = 0; i < versions.size(); i++) {
				Map.Entry<Integer,ComparableVersion> v = versions .get(i);
				FileUtils.write(out,"UPDATE " + table + " SET versionInt=" + i + " WHERE id=" + v.getKey() +  ";\n",true);
			}
			System.out.println(ga + " Done");
		}
	}

	public static void generateLibraryVersionUpdate() throws SQLException, IOException {
		String table = "library"; // "library"
		String getLibs = "SELECT groupid, artifactid FROM " + table + " GROUP BY groupid, artifactid;";
		String getLibVersions = "SELECT id, version FROM " + table + " WHERE groupid=? AND artifactid=?;";
		File out = new File("update_" + table + "_version.sql");
		MariaDBWrapper db = new MariaDBWrapper();
		PreparedStatement getLibsStmt = db.getConnection().prepareStatement(getLibs);

		ResultSet resultSet = getLibsStmt.executeQuery();

		Set<Map.Entry<String,String>> libsGA = new HashSet<>();

		while(resultSet.next()) {
			String groupid = resultSet.getString("groupid");
			String artifactid = resultSet.getString("artifactid");
			libsGA.add(new HashMap.SimpleEntry<>(groupid,artifactid));
		}

		for(Map.Entry<String,String> ga : libsGA) {
			PreparedStatement getLibVersionsStmt = db.getConnection().prepareStatement(getLibVersions);
			getLibVersionsStmt.setString(1,ga.getKey());
			getLibVersionsStmt.setString(2,ga.getValue());

			ResultSet versionSet = getLibVersionsStmt.executeQuery();
			List<Map.Entry<Integer,ComparableVersion>> versions = new ArrayList<>();

			while(versionSet.next()) {
				Integer id = versionSet.getInt("id");
				String version = versionSet.getString("version");
				versions.add(new HashMap.SimpleEntry<>(id,new ComparableVersion(version)));
			}

			Collections.sort(versions, Comparator.comparing(Map.Entry::getValue));
			for(int i = 0; i < versions.size(); i++) {
				Map.Entry<Integer,ComparableVersion> v = versions .get(i);
				FileUtils.write(out,"UPDATE " + table + " SET versionInt=" + i + " WHERE id=" + v.getKey() +  ";\n",true);
			}
			System.out.println(ga.getKey() + ":" + ga.getValue() + " Done");
		}
	}
}
