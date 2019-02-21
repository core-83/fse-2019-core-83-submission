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

public class GetApiUsage {

	static String getLibraries = "SELECT l.id, l.coordinates, COUNT(d.clientid) as clients FROM library as l JOIN dependency as d ON l.id=d.libraryid GROUP BY d.libraryid;";
	static String getApiUsages = "SELECT m.libraryid, m.package, m.class, m.member, COUNT(u.clientid) " +
			"as usages FROM api_member as m JOIN api_usage as u ON m.id=u.apimemberid WHERE m.libraryid=? GROUP BY m.id;";

	public static void main(String[] args) throws SQLException {

		File f = new File ("api_usage.csv");
		try {
			FileUtils.write(f,"library,package,class,member,usages,clients\n", Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
									resultAPi.getString(5) + "," +
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
