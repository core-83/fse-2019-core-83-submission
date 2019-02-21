package se.kth.castor;

import org.apache.commons.lang3.ArrayUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LibUsageContextFactory {

	static Map<Integer, String> libs = new HashMap<>();
	static Connection db;

	static String getLibs = "SELECT id, coordinates FROM library;";
	public static Map<Integer, String> getLibsANdInit() throws SQLException {
		db = new MariaDBWrapper().getConnection();

		PreparedStatement getApi = db.prepareStatement(getLibs);
		ResultSet resultSet = getApi.executeQuery();
		while(resultSet.next()) {
			libs.put(resultSet.getInt("id"),resultSet.getString("coordinates"));
		}
		return libs;
	}

	//public static int[] PRESELECTED_LIB_IDS = new int[]{920,3460,3396,3652,5363,3415,2900,1814,1866,2485,1257,6,3541,3123,1365,575,3467,4298,2153,3751,4343,808,2528,1090,3497,3619,1662,413,5244,2518,3197,1062,84,4187,403,1977,4554,3315,3667,3561,3824,2841,1919,1678,2602,3375,4666,1851,3611,1491,3420,4954,965,3609,2486,2929,4698,5008,1417,3222,426,2142,5161,2362,1891,1969,905,4743,4194,1573,5322,2765,2914,2050,3148,4959,2429,2196,3071,4328,114,3477,521,1733,4235,4778,5098,4650,3607,4816,1451,507,370,2378,4758,4204,1101,1342,1235};
	public static int[] PRESELECTED_LIB_IDS = new int[]{1490, 3460, 4236, 289, 5363, 2884, 3099, 1814, 1866, 2485, 630, 640, 3541, 1565, 1365, 3922, 3683, 3918, 526, 2495, 4343, 808, 2528, 1090, 1798, 3619, 1662, 413, 5244, 2518, 3197, 1062, 2406, 2905, 403, 1977, 541, 3315, 4517, 3561, 3824, 2841, 4581, 1678, 3212, 4908, 4666, 1851, 3611, 2034, 3420, 4157, 1845, 2929, 4698, 5008, 576, 2804, 426, 5351, 4729, 4843, 751, 1969, 905, 4743, 4194, 2965, 833, 406, 2914, 4450, 3148, 4959, 2429, 4929, 3071, 4328, 114, 2599, 521, 2378, 1116, 2338, 4235, 2088, 4778, 4534, 704, 315, 1687, 1043, 611, 1342, 1235};

	static String getSelectedLibs = "SELECT id, coordinates FROM library;";
	public static Map<Integer, String> getLibsANdInit(int[] ids) throws SQLException {
		db = new MariaDBWrapper().getConnection();

		PreparedStatement getApi = db.prepareStatement(getLibs);
		ResultSet resultSet = getApi.executeQuery();
		while(resultSet.next()) {
			if(ArrayUtils.contains(ids,resultSet.getInt("id"))) {
				libs.put(resultSet.getInt("id"), resultSet.getString("coordinates"));
			}
		}
		return libs;
	}


	public static Map<Integer, String> justInit(Map<Integer, String> in) throws SQLException {
		db = new MariaDBWrapper().getConnection();
		libs = in;
		return libs;
	}


	static String getApiClassesQuery = "SELECT m.package, m.class FROM api_member_full as m WHERE m.libraryid=? GROUP BY m.package, m.class;";
	static String getClientUsingLibQuery = "SELECT DISTINCT(u.clientid) FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id WHERE m.libraryid=?;";


	public static Context build(int libraryid) throws SQLException {
		System.out.println("[" + libraryid +"] start " + libs.get(libraryid) + " ----------------------------------------");

		int apiIndex = 0;
		Map<String,Integer> apiMemberIds = new HashMap<>();

		PreparedStatement getApi = db.prepareStatement(getApiClassesQuery);
		getApi.setInt(1, libraryid);
		ResultSet resultSet = getApi.executeQuery();
		while(resultSet.next()) {
			String apiMember = resultSet.getString("package") + "/" + resultSet.getString("class");
			apiMemberIds.put(apiMember, apiIndex);
			apiIndex++;
		}

		int clientIndex = 0;
		Map<Integer, Integer> clientIds = new HashMap<>();

		PreparedStatement getClient = db.prepareStatement(getClientUsingLibQuery);
		getClient.setInt(1, libraryid);
		ResultSet resultSetClient = getClient.executeQuery();
		while(resultSetClient.next()) {
			int clientId = resultSetClient.getInt("clientid");
			clientIds.put(clientId, clientIndex);
			clientIndex++;
		}

		return new Context(apiMemberIds,clientIds,libraryid,db, libs.get(libraryid));
	}
}
