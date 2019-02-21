package se.kth.castor.cores;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Core {
	public int n;
	public List<String> members = new ArrayList<>();

	public static double distance(Core c1, Core c2) {
		Set<String> intersection = new HashSet<>();
		Set<String> union = new HashSet<>();
		for(String s: c1.members) {
			if(c2.members.contains(s)) {
				intersection.add(s);
			}
		}
		union.addAll(c1.members);
		union.addAll(c2.members);
		return 1.0 - ( ((double) intersection.size()) / ((double) union.size()) );
	}

	public static double distance2(Core c1, Core c2) {
		Set<String> intersection = new HashSet<>();
		//Set<String> union = new HashSet<>();
		for(String s: c1.members) {
			if(c2.members.contains(s)) {
				intersection.add(s);
			}
		}
		//union.addAll(c1.members);
		//union.addAll(c2.members);
		return ( ((double) intersection.size()) / ((double) Math.max(c1.members.size(), c2.members.size())) );
	}

	public static double distance3(Core c1, Core c2) {
		//If you pick a random element from one set what is the probability that the other set does not contains it

		double s = c1.members.size() + c2.members.size();
		double diff = 0.0;
		for(String str: c1.members) {
			if(!c2.members.contains(str)) {
				diff += 1.0;
			}
		}
		for(String str: c2.members) {
			if(!c1.members.contains(str)) {
				diff += 1.0;
			}
		}

		return diff / s;
	}

	public static double isSame(Core c1, Core c2) {
		for(String str: c1.members) {
			if(!c2.members.contains(str)) {
				return 1.0;
			}
		}
		for(String str: c2.members) {
			if(!c1.members.contains(str)) {
				return 1.0;
			}
		}

		return 0.0;
	}

	public String toString() {
		return "{Core" + n + "(" + members.size() + "): " + members.stream().collect(Collectors.joining(",")) + "}";
	}
}
