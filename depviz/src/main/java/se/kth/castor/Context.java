package se.kth.castor;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Context {
	Map<String,Integer> apiMemberIds;
	Map<Integer, Integer> clientIds;
	int libraryId;
	Connection db;
	String libName;

	public Consumer<Integer> kill;

	public void setKill(Consumer<Integer> kill) {
		this.kill = kill;
	}

	public Context (Map<String,Integer> api, Map<Integer, Integer> client, int libraryId, Connection db, String libName) {
		this.apiMemberIds = api;
		this.clientIds = client;
		this.libraryId = libraryId;
		this.db = db;
		this.libName = libName;
	}

	static Context singleton;
	public static Context getContext() {
		return singleton;
	}
	public static void setContext(Context context) {
		singleton = context;
	}
}
