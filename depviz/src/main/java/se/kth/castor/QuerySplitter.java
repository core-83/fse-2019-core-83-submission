package se.kth.castor;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QuerySplitter {
	private static final Options options = new Options();
	private static Connection db;
	private static File output;
	private static String getLibs = "SELECT * FROM library;";
	private static String query;
	private static int startIndex = 0;
	private static int n = 1;

	private static boolean indexSet = false;
	private static int[] indexes;

	public static void main(String[] args) throws ParseException, SQLException {

		options.addOption("h", "help", false, "Show help");
		options.addOption("f", "file", true, "Path to the output file stream. Optional");
		options.addOption("d", "db-properties", false, "Path to database properties file. Mandatory");
		options.addOption("q", "query", true, "Query to be split, containing ? for library id. Mandatory");
		options.addOption("s", "start-index", true, "Start index.");
		options.addOption("i", "indexes", true, "list of indexes separated with commas.");
		options.addOption("n", "number-substitution", true, "Number of ? to replace with libraryID.");

		CommandLineParser parser = new DefaultParser();

		for(String arg: args) {
			System.out.println("arg: \"" + arg + "\"");
		}
		CommandLine cmd = parser.parse(options, args);
		if (cmd.hasOption("h") || !cmd.hasOption("q")) {
			help();
		}
		if(cmd.hasOption("p")) {
			db = new MariaDBWrapper(new File(cmd.getOptionValue("p"))).getConnection();
		} else {
			db = new MariaDBWrapper().getConnection();
		}
		query = cmd.getOptionValue("q");
		if(!query.contains("?")) throw new SQLException("PreparedQuery does not contains ?");

		if(cmd.hasOption("f")) {
			System.out.println("f: \"" + cmd.getOptionValue("f") + "\"");
			output = new File(cmd.getOptionValue("f"));
		}
		if(cmd.hasOption("s")) {
			startIndex = Integer.parseInt(cmd.getOptionValue("s"));
		}
		if(cmd.hasOption("i")) {
			indexSet = true;
			String[] tmpIs = cmd.getOptionValue("i").split(",");
			indexes = new int[tmpIs.length];
			for(int i = 0; i < tmpIs.length; i++) {
				indexes[i] = Integer.parseInt(tmpIs[i]);
			}
		}

		if(cmd.hasOption("n")) {
			n = Integer.parseInt(cmd.getOptionValue("n"));
		}

		run();
		System.out.println("Everything went surprisingly well.");

	}

	private static void help() {

		HelpFormatter formater = new HelpFormatter();

		formater.printHelp("QuerySplitter", options);

		System.exit(0);

	}

	public static void run() throws SQLException {

		Map<Integer,String> libNames = new HashMap<>();

		PreparedStatement getLib = db.prepareStatement(getLibs);
		ResultSet resultSet = getLib.executeQuery();
		while(resultSet.next()) {
			libNames.put(resultSet.getInt("id"),resultSet.getString("coordinates"));
		}

		if(!indexSet) {
			for (int lib : libNames.keySet()) {
				if (lib >= startIndex) {
					iteration(lib, libNames.get(lib));
				}
			}
		} else {
			for (int lib : indexes) {
				iteration(lib, libNames.get(lib));
			}
		}
	}

	private static void iteration(int lib, String libName) throws SQLException {
		System.out.println("Lib: " + lib);
		PreparedStatement getApi = db.prepareStatement(query);
		//getApi.setInt(2, lib);
		for(int i = 1; i <= n; i++) {
			getApi.setInt(i, lib);
		}
		ResultSet resultAPi = getApi.executeQuery();
		try {
			while (resultAPi.next()) {
				if (output != null) {
					String line = libName;
					for (int i = 0; i < resultAPi.getMetaData().getColumnCount(); i++) {
						line += "," + resultAPi.getString(i + 1);
					}
					FileUtils.write(output, line + "\n", Charset.defaultCharset(), true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
