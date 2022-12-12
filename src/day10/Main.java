package day10;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class Computer {
		
		int x = 1;
		int t = 0;
		
		int cursor = 0;
		
		List<Integer> signals = new ArrayList<>();
		
		public void step() {
			t++;
			
			if (cursor >= x - 1 && cursor <= x + 1)
				System.out.print('#');
			else
				System.out.print('.');
			
			cursor++;
			if (cursor >= 40) {
				cursor = 0;
				System.out.println();
			}
			
			
			
			
			if (t >= 20 && (t - 20) % 40 == 0)
				signals.add(t * x);
		}
		
		public void run(String cmd) {
			
			if (cmd.equals("noop")) {
				step();
			}
			else if (cmd.startsWith("addx")) {
				String [] parts = cmd.split("\\s+");
				int value = Integer.parseInt(parts[1]);
				step();
				step();
				x += value;
			}
		}
		
		public long getTotalSignalStrength() {
			long sum = 0;
			for (Integer x : signals)
				sum += x.intValue();
			return sum;
		}
		
	}
	
	public static void solve(File file) throws Exception {
		Computer computer = new Computer();
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (String line : lines)
			computer.run(line);
		
		System.out.println("Signal strength: " + computer.getTotalSignalStrength());
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day10/test.txt");
			File file = new File("files/day10/input.txt");
			solve(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
