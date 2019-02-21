package se.kth.castor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetApiNonUsage {

	static String getLibraries = "SELECT l.id, l.coordinates, COUNT(d.clientid) as clients FROM library as l JOIN dependency as d ON l.id=d.libraryid GROUP BY d.libraryid;";
	static String getApiUsages = "SELECT f.libraryid, f.package, f.class, f.member " +
			"FROM api_member_full as f LEFT JOIN api_member as m ON f.libraryid=m.libraryid AND f.package=m.package AND f.class=m.class AND f.member=m.member " +
			"WHERE f.libraryid=? AND m.id IS NULL;";

	public static void main(String[] args) throws SQLException {

		File f = new File ("tmp_api_usage.csv");
		MariaDBWrapper db = new MariaDBWrapper();

		Map<Integer,Integer> libs = new HashMap<>();
		Map<Integer,String> libNames = new HashMap<>();

		PreparedStatement getLib = db.getConnection().prepareStatement(getLibraries);
		ResultSet resultSet = getLib.executeQuery();
		while(resultSet.next()) {
			libs.put(resultSet.getInt("id"),resultSet.getInt("clients"));
			libNames.put(resultSet.getInt("id"),resultSet.getString("coordinates"));
		}

		for(int lib : libs.keySet()) {
			System.out.println("Lib: " + lib);
			PreparedStatement getApi = db.getConnection().prepareStatement(getApiUsages);
			getApi.setInt(1,lib);
			ResultSet resultAPi = getApi.executeQuery();
			try {
				while(resultAPi.next()) {
					FileUtils.write(f,
							libNames.get(resultAPi.getInt(1)) + "," +
									resultAPi.getString(2) + "," +
									resultAPi.getString(3) + "," +
									resultAPi.getString(4) + "," +
									"0," +
									libs.get(lib) +
									"\n", Charset.defaultCharset(), true);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done");
	}
}
