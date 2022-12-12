package day03;

import java.io.File;
import java.util.List;

import common.FileUtil;
import java.util.Set;
import java.util.HashSet;

public class Main {
	
	static Set<String> intersection(Set<String> left, Set<String> right) {
		Set<String> set = new HashSet<>();
		set.addAll(left);
		set.retainAll(right);
		return set;
	}
	
	static Set<String> union(Set<String> left, Set<String> right) {
		Set<String> set = new HashSet<>();
		set.addAll(left);
		set.addAll(right);
		return set;
	}
	
	static class Sack {
		
		Set<String> left;
		Set<String> right;
		
		public Sack() {
			left = new HashSet<>();
			right = new HashSet<>();
		}
		
		public Set<String> getIntersect() {
			return intersection(left, right);
		}
		
		public Set<String> getUnion() {
			return union(left, right);
		}
		
		public static Sack fromText(String text) {
			Sack sack = new Sack();
			
			int len = text.length();
			int len2 = len / 2;
			for (int i=0; i<len2; i++) {
				sack.left.add(text.substring(i,i+1));
				sack.right.add(text.substring(len2+i, len2+i+1));
			}
			
			return sack;
		}
		
	}
	
	public static int getPriority(char ch) {
		if (ch >= 'a' && ch <= 'z')
			return (ch - 'a') + 1;
		if (ch >= 'A' && ch <= 'Z')
			return (ch - 'A') + 27;
		throw new IllegalArgumentException("Invalid symbol: " + ch);
	}
	
	public static void solvePart1() throws Exception {
		int sum = 0;
		
		//File file = new File("files/day03/test.txt");
		File file = new File("files/day03/input.txt");
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (String line : lines) {
			Sack sack = Sack.fromText(line);
			Set<String> common = sack.getIntersect();
			
			char ch = common.iterator().next().charAt(0);
			int value = getPriority(ch);
			sum += value;
			
			System.out.println(common.toString() + " : " + value);
		}
		
		System.out.println("Sum: " + sum);
	}
	
	public static void solvePart2() throws Exception {
		int sum = 0;
		
		//File file = new File("files/day03/test.txt");
		File file = new File("files/day03/input.txt");
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (int i=0; i<lines.size(); i+=3) {
			Sack sack1 = Sack.fromText(lines.get(i));
			Sack sack2 = Sack.fromText(lines.get(i+1));
			Sack sack3 = Sack.fromText(lines.get(i+2));
			
			Set<String> common = intersection(intersection(sack1.getUnion(), sack2.getUnion()), sack3.getUnion());
			
			char ch = common.iterator().next().charAt(0);
			int value = getPriority(ch);
			sum += value;
			
			System.out.println(common.toString() + " : " + value);
		}
		
		System.out.println("Sum: " + sum);
	}
	
	public static void main(String [] args) {
		try {
			//solvePart1();
			solvePart2();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
