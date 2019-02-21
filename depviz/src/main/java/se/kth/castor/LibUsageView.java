package se.kth.castor;

import processing.core.PApplet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LibUsageView extends PApplet {
	static int strokeLight = 0;
	public int screenWith;
	public int screenHeigth;

	Context context = Context.getContext();
	boolean done = false;
	boolean nbOrdiv = false;


	public void settings(){
		//size
		screenWith = context.clientIds.size();
		screenHeigth = context.apiMemberIds.size();
		size(screenWith, screenHeigth);
	}

	static String getApiUsageByClient = "SELECT u.clientid, m.packageid, m.class, SUM(u.nb) as nb, MAX(u.diversity) as diversity FROM api_usage as u JOIN api_member as m ON u.apimemberid=m.id WHERE m.libraryid=? GROUP BY u.clientid, m.packageid, m.class;";

	public void setup() {
		System.out.println(context.libName + " Start -----------------------------------------------------------------");
		background(255);

		fill(255, 0, 0);
		stroke(strokeLight);
		//rect(10F, 10F, 1F, 1F);
		try {
			Map<String,Integer> apiMemberIds = context.apiMemberIds;
			Map<Integer, Integer> clientIds = context.clientIds;

			PreparedStatement getUsages = context.db.prepareStatement(getApiUsageByClient);
			getUsages.setInt(1, context.libraryId);

			ResultSet resultSet = getUsages.executeQuery();
			while(resultSet.next()) {
				int client = resultSet.getInt("clientid");
				String usedClass = resultSet.getString("packageid") + "/" + resultSet.getString("class");
				int nb = resultSet.getInt("nb");
				int div = resultSet.getInt("diversity");

				if(clientIds.containsKey(client) && apiMemberIds.containsKey(usedClass)) {
					int x = clientIds.get(client);
					int y = apiMemberIds.get(usedClass);

					int[] c;
					if(nbOrdiv) {
						c = colorOf(nb);
					} else {
						c = colorOf(div);
					}
					stroke(c[0], c[1], c[2]);

					float fx = x;
					float fy = y;
					rect(fx, fy, 1F, 1F);
					//set(x,y,0xFF0000);
				} else {
					System.err.println("error with client: " + client + ", api member: " + usedClass);
				}
			}

			if(nbOrdiv) {
				save("./output-nb/" + context.libName + ".png");
			} else {
				save("./output-div/" + context.libName + ".png");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(context.libName + " done -----------------------------------------------------------------");
		System.out.flush();
		done = true;
		exit();
	}

	private static int[] colorOf(int i) {
		int r = 0;
		int g = 0;
		int b = 0;
		if(i == 1) {
			b = 255;
		} else if (i < 10) {
			r = 150;
			b = 255;
		} else if (i < 100) {
			r = 255;
			b = 210;
		} else if (i < 1000) {
			r = 255;
			b = 60;
		} else {
			r = 255;
		}
		return new int[]{r,g,b};
	}
}
