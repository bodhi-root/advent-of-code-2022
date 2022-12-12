package day01;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class Elf {
		
		int calories = 0;
		
		public void add(int calories) {
			this.calories += calories;
		}
		
	}
	
	public static List<Elf> loadInput(File file) throws IOException {
		List<String> lines = FileUtil.readLinesFromFile(file);
		
		List<Elf> elves = new ArrayList<>();
		Elf currentElf = new Elf();
		
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() && currentElf.calories > 0) {
				elves.add(currentElf);
				currentElf = new Elf();
			}
			else {
				currentElf.add(Integer.parseInt(line));
			}
		}
		
		if (currentElf.calories > 0)
			elves.add(currentElf);
		
		System.out.println("Loaded calories for " + elves.size() + " elves");
		return elves;
	}
	
	public static void solve() throws Exception {
		
		List<Elf> elves = loadInput(new File("files/day01/input.txt"));
		
		//find max
		Elf maxElf = elves.get(0);
		for (int i=1; i<elves.size(); i++) {
			Elf elf = elves.get(i);
			if (elf.calories > maxElf.calories)
				maxElf = elf;
		}
		
		System.out.println("Most calories: " + maxElf.calories);
		
		//find top 3:
		elves.sort(new Comparator<Elf>() {
			public int compare(Elf o1, Elf o2) {
				return o2.calories - o1.calories;
			}
		});
		
		int sum = 0;
		for (int i=0; i<3; i++) {
			sum += elves.get(i).calories;
		}
		System.out.println("Sum of top 3 calories: " + sum);
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
