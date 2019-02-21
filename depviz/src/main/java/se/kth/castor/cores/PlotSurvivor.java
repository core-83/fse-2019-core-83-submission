package se.kth.castor.cores;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotSurvivor {
	//static File output = new File("normalized-100-survivors.csv");
	static File output = new File("normalized-100-survivors-inv.csv");

	public static void main(String[] args) throws IOException {
		//File inDir = new File("survivor-class-2");
		File inDir = new File("survivor-class-inv");
		File list = new File("top100-gav-files");
		FileUtils.write(output,"Library,step,pop\n", false);
		for(File f: getSurvivorFiles(inDir,list)) {
			PlotSurvivor p = new PlotSurvivor(f);
			p.print(output);
		}
		System.out.println("Done");
	}

	public static List<File> getSurvivorFiles(File inDIr, File list) {
		List<File> result = new ArrayList<>();
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(list))) {
			String line;
			while((line = bufferedReader.readLine()) != null) {
				File f = new File(inDIr, line);
				result.add(f);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	String coordinates;
	int maxPop;
	Map<Integer, Integer> stepSurivors = new HashMap<>();
	double[] normailizedStepSurvivors = new double[101];

	public PlotSurvivor(File input) {

		String[] info = input.getName().split(",");
		String groupid = info[0];
		String artifactid = info[1];
		String version = info[2];
		coordinates = groupid + ":" + artifactid + ":" + version;
		int versionInt = Integer.parseInt(info[3].split("\\.")[0]);

		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(input))) {
			String line;
			bufferedReader.readLine();
			line = bufferedReader.readLine();
			String[] cells = line.split(",");
			maxPop = Integer.parseInt(cells[1]);
			while((line = bufferedReader.readLine()) != null) {
				cells = line.split(",");
				//String member = cells[2];
				int stepSurivor = Integer.parseInt(cells[1]);
				int step = Integer.parseInt(cells[0]);
				stepSurivors.put(step,stepSurivor);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//stepSurivors.put(0,maxPop);
		reformat();
	}

	public void reformat() {
		for(int i = 0; i <= 100; i++) {
			int s = (int) Math.floor(i / 100.0 * stepSurivors.size());
			if(s == 0) {
				normailizedStepSurvivors[i] = 100.0;
			} else {
				normailizedStepSurvivors[i] = stepSurivors.get(s) / ((double) maxPop) * 100.0;
			}
		}
	}

	public void print(File f) throws IOException {
		for(int i = 0; i <= 100; i++) {
			FileUtils.write(f,coordinates + "," + i + "," + normailizedStepSurvivors[i] + "\n", true);

		}
	}
}
