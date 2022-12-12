package day04;

import java.io.File;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class Range {
		
		int start;
		int end;
		
		public Range(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public boolean contains(Range range) {
			return range.start >= this.start && range.end <= this.end;
		}
		public boolean overlaps(Range range) {
			return (range.start >= this.start && range.start <= this.end) ||
				   (range.end   >= this.start && range.end   <= this.end) ||
				   (range.start < this.start && range.end > this.end);
		}
		
		public static Range from(String text) {
			String [] parts = text.split("-");
			return new Range(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		
	}
	
	public static void solve() throws Exception {
		int contains = 0;
		int overlaps = 0;
		
		//File file = new File("files/day04/test.txt");
		File file = new File("files/day04/input.txt");
		List<String> lines = FileUtil.readLinesFromFile(file);
		System.out.println("Read " + lines.size() + " lines");
		for (String line : lines) {
			String [] parts = line.split(",");
			Range left = Range.from(parts[0]);
			Range right = Range.from(parts[1]);
			if (left.overlaps(right)) {
				overlaps++;
				if (left.contains(right) || right.contains(left))
					contains++;
			}
		}
		
		System.out.println("Contains: " + contains);
		System.out.println("Overlaps: " + overlaps);
	}
	
	public static void main(String [] args) {
		try {
			solve();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
