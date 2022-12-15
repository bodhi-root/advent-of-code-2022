package day13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import common.FileUtil;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Main {
	
	/**
	 * Comparator that will return:
	 * 
	 * - 0 if objects are equal and no comparison can be made
	 * - negative values if objects are in the right order (first object should come first)
	 * - positive values if objects are in the wrong order
	 */
	static class CompareLogic implements Comparator<Object> {

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof Number && o2 instanceof Number) {
				Number n1 = (Number)o1;
				Number n2 = (Number)o2;
				return Long.compare(n1.longValue(), n2.longValue());
			}
			
			if (o1 instanceof Number) {
				List list = new ArrayList<>();
				list.add(o1);
				o1 = list;
			}
			if (o2 instanceof Number) {
				List list = new ArrayList<>();
				list.add(o2);
				o2 = list;
			}
			
			return compareLists((List)o1, (List)o2);
		}
		
		public int compareLists(List list1, List list2) {
			
			int len = Math.min(list1.size(), list2.size());
			for (int i=0; i<len; i++) {
				int result = compare(list1.get(i), list2.get(i));
				if (result != 0)
					return result;
			}
			
			if (list1.size() == list2.size())
				return 0;
			
			return list1.size() < list2.size() ? -1 : 1;
		}
		
	}
	
	/**
	 * Print objects (numbers or lists) with recursive components
	 */
	static class Printer {
		
		StringBuilder s = new StringBuilder();
		
		public void write(Object o) {
			if (o instanceof Number)
				s.append((Number)o);
			else if (o instanceof List) {
				List list = (List)o;
				s.append('[');
				for (int i=0; i<list.size(); i++) {
					if (i > 0)
						s.append(',');
					write(list.get(i));
				}
				s.append(']');
			}
		}
		
		public String toString() {
			return s.toString();
		}
		
		public static String print(Object o) {
			Printer printer = new Printer();
			printer.write(o);
			return printer.toString();
		}
		
	}
	
	
	
	static class InputPair {
		
		Comparator comparator = new CompareLogic();
		
		Object o1;
		Object o2;
		
		public InputPair(Object o1, Object o2) {
			this.o1 = o1;
			this.o2 = o2;
		}
		
		public boolean isCorrectOrder() {
			return comparator.compare(o1, o2) < 0;
		}
		
	}
	
	static Object parseLine(String line) {
		char [] chars = line.toCharArray();
		if (chars[0] != '[')
			throw new IllegalArgumentException("Value must start with '['");
		
		List result = new ArrayList<>();
		
		List<List> listStack = new ArrayList<>();
		listStack.add(result);
		
		StringBuilder buf = new StringBuilder();
		
		for (int i=1; i<chars.length; i++) {
			//System.out.println("Processing char " + i + " '" + chars[i] + "'");
			
			//skip comma:
			if (chars[i] == ',')
				i++;
			
			//start a new list: add to stack
			if (chars[i] == '[') {
				List list = new ArrayList<>();
				listStack.get(listStack.size()-1).add(list);
				
				listStack.add(list);
			}
			//end a list: pop from stack
			else if (chars[i] == ']') {
				listStack.remove(listStack.size()-1);
			}
			//number, terminated by a comma or ']'
			else if (Character.isDigit(chars[i])) {
				buf.append(chars[i]);
				while (i+1 < chars.length && Character.isDigit(chars[i+1])) {
					buf.append(chars[++i]);
				}
				Integer value = Integer.parseInt(buf.toString());
				buf.setLength(0);
				
				listStack.get(listStack.size()-1).add(value);
			}
			else {
				throw new IllegalArgumentException("Invalid input.  Unexpected char: '" + chars[i] + "': " + line);
			}
		}
		
		if (!listStack.isEmpty())
			throw new IllegalArgumentException("Invalid input.  Missing closing ']': " + line);
		
		return result;
	}
	
	public static List<InputPair> readInput(File file) throws IOException {
		List<InputPair> pairs = new ArrayList<>();
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (int i=0; i<lines.size(); i+=3) {
			String line1 = lines.get(i);
			String line2 = lines.get(i+1);
			
			Object o1 = parseLine(line1);
			Object o2 = parseLine(line2);
			
			//System.out.println(line1);
			//System.out.println(Printer.print(o1));
			//System.out.println(line2);
			//System.out.println(Printer.print(o2));
			//System.out.println();
			
			pairs.add(new InputPair(o1, o2));
		}
		
		return pairs;
	}
	
	public static void solvePart1(File file) throws Exception {
		List<InputPair> pairs = readInput(file);
		
		int sum = 0;
		for (int i=0; i<pairs.size(); i++) {
			InputPair pair = pairs.get(i);
			
			System.out.println("Pair " + (i+1));
			System.out.println(Printer.print(pair.o1));
			System.out.println(Printer.print(pair.o2));
			
			if (pair.isCorrectOrder()) {
				System.out.println("Adding: " + (i+1));
				sum += (i+1);
			}
			
			System.out.println();
		}
		
		System.out.println("Total: " + sum);
	}
	
	public static void solvePart2(File file) throws Exception {
		List<InputPair> pairs = readInput(file);
		
		List objects = new ArrayList<>();
		for (InputPair pair : pairs) {
			objects.add(pair.o1);
			objects.add(pair.o2);
		}
		objects.add(parseLine("[[2]]"));
		objects.add(parseLine("[[6]]"));
		
		objects.sort(new CompareLogic());
		
		long product = 1;
		for (int i=0; i<objects.size(); i++) {
			Object obj = objects.get(i);
			String txt = Printer.print(obj);
			
			if (txt.equals("[[2]]") || txt.equals("[[6]]")) {
				System.out.println("  Divider at index " + (i+1));
				product *= (i+1);
			}
			
			System.out.println(txt);
		}
		
		System.out.println("Decoder key: " + product);
	}
	
	public static void main(String [] args) {
		
		try {
			//File file = new File("files/day13/test.txt");
			File file = new File("files/day13/input.txt");
			//solvePart1(file);
			solvePart2(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
