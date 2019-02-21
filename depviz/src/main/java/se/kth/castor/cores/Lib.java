package se.kth.castor.cores;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

public class Lib {
	String groupdID;
	String artifactID;
	String name;
	SortedMap<Integer, Core> versionCore = new TreeMap<>();

	public Lib(String g, String a) {
		groupdID = g;
		artifactID = a;
		name = g + ":" + a;
	}
	public void addVersion(File rawInfo) {
		addVersion(rawInfo, 0);
	}

	public void addVersion(File rawInfo, int minClients) {
		String[] info = rawInfo.getName().split(",");
		String version = info[2];
		int versionInt = Integer.parseInt(info[3].split("\\.")[0]);
		Core c = new Core();
		c.n = 50;
		boolean toAdd = false;

		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(rawInfo))) {
			String line;
			line = bufferedReader.readLine();
			line = bufferedReader.readLine();
			String[] cells = line.split(",");
			int pop = Integer.parseInt(cells[1]);
			if(pop >= minClients) toAdd=true;
			while((line = bufferedReader.readLine()) != null) {
				cells = line.split(",");
				String member = cells[2];
				boolean isCore50 = Boolean.parseBoolean(cells[6]);
				if(isCore50) {
					c.members.add(member);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(toAdd) {
			versionCore.put(versionInt, c);
		}
	}

	public static double median(List<Double> numArray) {
		if(numArray.isEmpty()) return Double.NaN;
		Collections.sort(numArray);
		double median;
		if (numArray.size() % 2 == 0)
			median = (numArray.get(numArray.size()/2) + numArray.get(numArray.size()/2 - 1))/2.0;
		else
			median = numArray.get(numArray.size()/2);
		return median;
	}

	public static double mean(List<Double> numArray) {
		if(numArray.isEmpty()) return Double.NaN;
		double sum = 0.0;
		for(Double d: numArray) sum += d;
		return sum/((double) numArray.size());
	}

	public List<Double> diversity(BiFunction<Core,Core,Double> distance) {
		List<Double> dists = new ArrayList<>();

		for(Core c1: versionCore.values()) {
			for(Core c2: versionCore.values()) {
				if(!c1.equals(c2)) {
					dists.add(distance.apply(c1,c2));
				}
			}
		}
		return dists;
	}

	public List<Double> sequenceDistance(BiFunction<Core,Core,Double> distance) {
		List<Double> dists = new ArrayList<>();
		if(versionCore.isEmpty()) return dists;
		Iterator<Core> it = versionCore.values().iterator();
		Core prev = it.next();
		while(it.hasNext()) {
			Core cur = it.next();
			dists.add(distance.apply(prev,cur));
			prev = cur;
		}
		return dists;
	}

	public static void printEvolutionHeader(File f) throws IOException {
		FileUtils.write(f,"Library,nbVersion,AvgSeqD1,MedSeqD1,AvgD1,MedD1,AvgSeqD3,MedSeqD3,AvgD3,MedD3\n",
				false);
	}

	public void printEvolution(File f) throws IOException {
		List<Double> seqD1 = sequenceDistance(Core::distance);
		List<Double> D1 = diversity(Core::distance);
		List<Double> seqD3 = sequenceDistance(Core::distance3);
		List<Double> D3 = diversity(Core::distance3);


		FileUtils.write(f,name + "," + versionCore.size()
						+ "," + mean(seqD1)
						+ "," + median(seqD1)
						+ "," + mean(D1)
						+ "," + median(D1)
						+ "," + mean(seqD3)
						+ "," + median(seqD3)
						+ "," + mean(D3)
						+ "," + median(D3) + "\n",
				true);
		/*FileUtils.write(f,name + "," + versionCore.size()
				+ "," + sequenceDistance(Core::distance)
				+ "," + diversity(Core::distance)
				+ "," + sequenceDistance(Core::distance3)
				+ "," + diversity(Core::distance3)
				+ "," + sequenceDistance(Core::isSame)
				+ "," + diversity(Core::isSame) + "\n",
				true);*/
	}

	public void printEvolution() {
		List<Double> seqD1 = sequenceDistance(Core::distance);
		List<Double> D1 = diversity(Core::distance);
		List<Double> seqD3 = sequenceDistance(Core::distance3);
		List<Double> D3 = diversity(Core::distance3);
		System.out.println(name + ": " + versionCore.size()
				+ "\nAVG D1 SEQ: " + mean(seqD1)
				+ "\nMed D1 SEQ: " + median(seqD1)
				+ "\nAVG D1: " + mean(D1)
				+ "\nMed D1: " + median(D1)
				+ "\nAVG D3 SEQ: " + mean(seqD3)
				+ "\nMed D3 SEQ: " + median(seqD3)
				+ "\nAVG D3: " + mean(D3)
				+ "\nMed D3: " + median(D3)
		);
	}

	public static void main(String[] args) throws IOException {
		File dir = new File("survivor-class-3");
		File output = new File("core-content-evolution-100+.csv");
		Map<String, Lib> libs = new HashMap<>();
		for(File f: dir.listFiles()) {
			if(f.getName().endsWith("csv")) {
				String[] info = f.getName().split(",");
				String groupdID = info[0];
				String artifactID = info[1];
				String name = groupdID + ":" + artifactID;
				libs.computeIfAbsent(name, s -> new Lib(groupdID,artifactID));
				Lib l = libs.get(name);
				//l.addVersion(f);
				l.addVersion(f,100);

			}
		}
		printEvolutionHeader(output);
		for(Lib l: libs.values()) {
			System.out.println("---------------------------------");
			l.printEvolution();
			l.printEvolution(output);
		}
		System.out.println("Done");
	}
}
