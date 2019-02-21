package se.kth.castor;

import processing.core.PApplet;
import processing.javafx.PSurfaceFX;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws SQLException {
        if (args.length != 2) {
            String cmd = "java -jar target/dep-viz-1.0-SNAPSHOT-jar-with-dependencies.jar ";
            //Map<Integer, String> libs = LibUsageContextFactory.getLibsANdInit();
            Map<Integer, String> libs = LibUsageContextFactory.getLibsANdInit(LibUsageContextFactory.PRESELECTED_LIB_IDS);
            for (Integer libId : libs.keySet()) {
                System.out.println(cmd + libId + " " + "\"" + libs.get(libId) + "\"");
            }
        } else {
            int done = 1;
            Map<Integer, String> in = new HashMap<>();
            int libId = Integer.parseInt(args[0]);
            in.put(libId, args[1]);

            LibUsageContextFactory.justInit( in);
            Context context = LibUsageContextFactory.build(libId);
            Context.setContext(context);
            PApplet.main("se.kth.castor.LibUsageView");
        }
    }
}
