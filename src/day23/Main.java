package day23;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FileUtil;
import common.geometry.XY;

public class Main {
	
	static class Elf {
		
		XY<Integer> location;
		
		public Elf(int x, int y) {
			this.location = new XY<Integer>(x, y);
		}
		
	}
	
	static interface Rule {
		
		public XY<Integer> getLocation(Elf elf, Puzzle puzzle);
		
	}
	
	/**
	 * If there is no Elf in the N, NE, or NW adjacent positions, the Elf proposes moving north one step.
	 */
	static Rule RULE1 = new Rule() {
		
		public XY<Integer> getLocation(Elf elf, Puzzle puzzle) {
			if (puzzle.isEmpty(elf.location.x-1, elf.location.y-1) &&
				puzzle.isEmpty(elf.location.x, elf.location.y-1) &&
				puzzle.isEmpty(elf.location.x+1, elf.location.y-1))
				return new XY<Integer>(elf.location.x, elf.location.y-1);
			else
				return null;
		}
		
	};
	
	/**
	 * If there is no Elf in the S, SE, or SW adjacent positions, the Elf proposes moving south one step.
	 */
	static Rule RULE2 = new Rule() {
		
		public XY<Integer> getLocation(Elf elf, Puzzle puzzle) {
			if (puzzle.isEmpty(elf.location.x-1, elf.location.y+1) &&
				puzzle.isEmpty(elf.location.x, elf.location.y+1) &&
				puzzle.isEmpty(elf.location.x+1, elf.location.y+1))
				return new XY<Integer>(elf.location.x, elf.location.y+1);
			else
				return null;
		}
		
	};
	
	/**
	 * If there is no Elf in the W, NW, or SW adjacent positions, the Elf proposes moving west one step.
	 */
	static Rule RULE3 = new Rule() {
		
		public XY<Integer> getLocation(Elf elf, Puzzle puzzle) {
			if (puzzle.isEmpty(elf.location.x-1, elf.location.y-1) &&
				puzzle.isEmpty(elf.location.x-1, elf.location.y) &&
				puzzle.isEmpty(elf.location.x-1, elf.location.y+1))
				return new XY<Integer>(elf.location.x-1, elf.location.y);
			else
				return null;
		}
		
	};
	
	/**
	 * If there is no Elf in the E, NE, or SE adjacent positions, the Elf proposes moving east one step.
	 */
	static Rule RULE4 = new Rule() {
		
		public XY<Integer> getLocation(Elf elf, Puzzle puzzle) {
			if (puzzle.isEmpty(elf.location.x+1, elf.location.y-1) &&
				puzzle.isEmpty(elf.location.x+1, elf.location.y) &&
				puzzle.isEmpty(elf.location.x+1, elf.location.y+1))
				return new XY<Integer>(elf.location.x+1, elf.location.y);
			else
				return null;
		}
		
	};
	
	static Rule [] ALL_RULES = new Rule [] {RULE1, RULE2, RULE3, RULE4};
	
	static class Rectangle {
		
		XY<Integer> min;
		XY<Integer> max;
		
		public Rectangle(XY<Integer> min, XY<Integer> max) {
			this.min = min;
			this.max = max;
		}
		
	}
	
	static class Puzzle {
		
		List<Elf> elves = new ArrayList<>();
		Map<XY<Integer>, Elf> locations = new HashMap<>();
		int t = 0;
		int nextRuleOffset = 0;
		
		public Elf addElf(int x, int y) {
			Elf elf = new Elf(x, y);
			elves.add(elf);
			locations.put(elf.location, elf);
			return elf;
		}
		
		public void moveElf(Elf elf, XY<Integer> location) {
			locations.remove(elf.location);
			elf.location = location;
			locations.put(elf.location, elf);
		}
		
		public boolean isEmpty(int x, int y) {
			return !locations.containsKey(new XY<Integer>(x, y));
		}
		
		public boolean isEmptyAround(int x, int y) {
			return isEmpty(x-1, y-1) &&
					isEmpty(x-1, y) &&
					isEmpty(x-1, y+1) &&
					isEmpty(x, y+1) &&
					isEmpty(x+1, y+1) &&
					isEmpty(x+1, y) &&
					isEmpty(x+1, y-1) &&
					isEmpty(x, y-1);
		}
		public boolean isEmptyAround(XY<Integer> loc) {
			return isEmptyAround(loc.x, loc.y);
		}
		
		public int getEmptySpaceCount(Rectangle bounds) {
			int count = 0;
			for (int y=bounds.min.y; y<=bounds.max.y; y++) {
				for (int x=bounds.min.x; x<=bounds.max.x; x++) {
					if (isEmpty(x, y))
						count++;
				}
			}
			return count;
		}
		
		public int step() {
			Map<XY<Integer>, List<Elf>> proposedMoves = new HashMap<>();
			for (Elf elf : elves) {
				XY<Integer> nextLoc = getProposedMove(elf);
				if (nextLoc != null) {
					List<Elf> list = proposedMoves.get(nextLoc);
					if (list == null) {
						list = new ArrayList<>();
						proposedMoves.put(nextLoc, list);
					}
					list.add(elf);
				}
			}
			
			this.nextRuleOffset++;
			if (this.nextRuleOffset == ALL_RULES.length)
				this.nextRuleOffset = 0;
			
			int moves = 0;
			
			for (Map.Entry<XY<Integer>, List<Elf>> entry : proposedMoves.entrySet()) {
				XY<Integer> loc = entry.getKey();
				List<Elf> toMove = entry.getValue();
				if (toMove.size() == 1) {
					Elf elf = toMove.get(0);
					moveElf(elf, loc);
					moves++;
				}
			}
			
			this.t++;
			return moves;
		}
		
	
		public XY<Integer> getProposedMove(Elf elf) {
			if (isEmptyAround(elf.location))
				return null;
			
			for (int i=0; i<ALL_RULES.length; i++) {
				Rule rule = ALL_RULES[(nextRuleOffset + i) % ALL_RULES.length];
				XY<Integer> loc = rule.getLocation(elf, this);
				if (loc != null)
					return loc;
			}
			return null;
		}
		
		public static Puzzle loadFrom(File file) throws IOException {
			Puzzle puzzle = new Puzzle();
			
			List<String> lines = FileUtil.readLinesFromFile(file);
			for (int y=0; y<lines.size(); y++) {
				String line = lines.get(y).trim();
				if (line.isEmpty())
					continue;
				
				for (int x=0; x<line.length(); x++) {
					if (line.charAt(x) == '#')
						puzzle.addElf(x, y);
				}
			}
			
			return puzzle;
		}
		
		public Rectangle getBounds() {
			Elf elf = elves.get(0);
			int xMin = elf.location.x.intValue();
			int yMin = elf.location.y.intValue();
			int xMax = xMin;
			int yMax = yMin;
			
			for (int i=1; i<elves.size(); i++) {
				elf = elves.get(i);
				xMin = Math.min(xMin, elf.location.x.intValue());
				yMin = Math.min(yMin, elf.location.y.intValue());
				xMax = Math.max(xMax, elf.location.x.intValue());
				yMax = Math.max(yMax, elf.location.y.intValue());
			}
			
			return new Rectangle(
					new XY<Integer>(xMin, yMin), 
					new XY<Integer>(xMax, yMax)
				);
		}
		
		public String printString() {
			StringBuilder s = new StringBuilder();
			Rectangle bounds = getBounds();
			
			for (int y=bounds.min.y; y<=bounds.max.y; y++) {
				for (int x=bounds.min.x; x<=bounds.max.x; x++) {
					if (isEmpty(x, y))
						s.append('.');
					else
						s.append('#');
				}
				s.append('\n');
			}
			
			return s.toString();
		}
			
	}
	
	public static void runTest(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		System.out.println("t = 0");
		System.out.println(puzzle.printString());

		for (int i=0; i<5; i++) {
			puzzle.step();
			System.out.println("================================");
			System.out.println("t = " + puzzle.t);
			System.out.println(puzzle.printString());
		}
	}
	
	public static void solvePart1(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		for (int i=0; i<10; i++) {
			puzzle.step();
		}
		System.out.println("Answer = " + puzzle.getEmptySpaceCount(puzzle.getBounds()));
	}
	
	public static void solvePart2(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		while (puzzle.step() > 0) {};
		System.out.println("No one moved at t = " + puzzle.t);
	}
	
	public static void main(String [] args) {
		try {
			//File testFile = new File("files/day23/test.txt");
			//runTest(testFile);
			
			//File testFile2 = new File("files/day23/test2.txt");
			//solvePart1(testFile2);
			//solvePart2(testFile2);
			
			File inputFile = new File("files/day23/input.txt");
			//solvePart1(inputFile);
			solvePart2(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
