package day12;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class RowCol {
		
		int row;
		int col;
		
		public RowCol(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
	}
	
	static class Path {
		
		List<RowCol> path = new ArrayList<>();
		boolean metGoal = false;
		
		public void add(RowCol pos) {
			path.add(pos);
		}
		
		public int size() {
			return path.size();
		}
		public RowCol getEnd() {
			return path.get(path.size()-1);
		}
		
		public Path copy() {
			Path copy = new Path();
			copy.path.addAll(this.path);
			copy.metGoal = this.metGoal;
			return copy;
		}
		
	}
	
	static class Grid {
		
		char [][] values;
		int height;
		int width;
		
		public Grid(int height, int width) {
			this.values = new char[height][width];
			this.height = height;
			this.width = width;
		}
		
		public RowCol findValue(char target) {
			for (int i=0; i<height; i++) {
				for (int j=0; j<width; j++) {
					if (values[i][j] == target)
						return new RowCol(i, j);
				}
			}
			return null;
		}
		
		public char getValue(RowCol pos) {
			return values[pos.row][pos.col];
		}
		public void setValue(RowCol pos, char value) {
			values[pos.row][pos.col] = value; 
		}
		
		public static Grid loadFromFile(File file) throws IOException {
			List<String> lines = FileUtil.readLinesFromFile(file);
			while (lines.get(lines.size()-1).trim().isEmpty())
				lines.remove(lines.get(lines.size()-1));
			
			int height = lines.size();
			int width = lines.get(0).length();
			Grid grid = new Grid(height, width);
			
			for (int i=0; i<height; i++) {
				String line = lines.get(i);
				for (int j=0; j<width; j++) {
					grid.values[i][j] = line.charAt(j);
				}
			}
			
			return grid;
		}
		
		public List<RowCol> getNextSteps(RowCol pos) {
			char value = Character.toLowerCase(getValue(pos));
			
			List<RowCol> list = new ArrayList<>();
			list.add(new RowCol(pos.row-1, pos.col));
			list.add(new RowCol(pos.row+1, pos.col));
			list.add(new RowCol(pos.row, pos.col-1));
			list.add(new RowCol(pos.row, pos.col+1));
			for (int i=list.size()-1; i>=0; i--) {
				RowCol next = list.get(i);
				if (!(next.row >= 0 && next.row < height &&
					  next.col >= 0 && next.col < width &&
					  ((int)(getValue(next) - value)) <= 1)) {
					list.remove(i);
				}
			}
			return list;
		}
		
		public Path getShortestDistance(RowCol start, char goalChar, char finalChar) {
			Visitor visitor = new Visitor(this, start, goalChar, finalChar);
			
			return visitor.getShortestDistance();
		}
		
	}
	
	static class Visitor {
		
		Grid grid;
		Path [][] bestPathsBeforeGoal;
		Path [][] bestPathsAfterGoal;
		RowCol start;
		char goalChar;
		char finalChar;
		
		public Visitor(Grid grid, RowCol start, char goalChar, char finalChar) {
			this.grid = grid;
			this.bestPathsBeforeGoal = new Path[grid.height][grid.width];
			this.bestPathsAfterGoal = new Path[grid.height][grid.width];
			
			this.start = start;
			this.goalChar = goalChar;
			this.finalChar = finalChar;
		}
		
		public String getDistanceGridText() {
			StringBuilder s = new StringBuilder();
			for (int i=0; i<grid.height; i++) {
				for (int j=0; j<grid.width; j++) {
					String txt = String.valueOf(bestPathsBeforeGoal[i][j].size());
					for (int tmp=txt.length(); tmp<4; tmp++)
						s.append(' ');
					s.append(txt);
				}
				s.append("\n");
			}
			return s.toString();
		}
		
		public Path getShortestDistance() {
			Path path0 = new Path();
			path0.add(start);
			bestPathsBeforeGoal[start.row][start.col] = path0;
			
			List<Path> leaves = new ArrayList<>();
			leaves.add(path0);
			return expandBreadthFirst(leaves);
		}
		
		public Path expandBreadthFirst(List<Path> leaves) {
			List<Path> nextLeaves = new ArrayList<>();
			
			for (Path path : leaves) {
				RowCol lastPos = path.getEnd();
				List<RowCol> steps = grid.getNextSteps(lastPos);
				
				Path [][] activeGrid = path.metGoal ? bestPathsAfterGoal : bestPathsBeforeGoal;
				for (RowCol step : steps) {
					
					if (activeGrid[step.row][step.col] == null) {
						Path nextPath = path.copy();
						nextPath.add(step);
						char ch = grid.getValue(step);
						if (ch == goalChar)
							nextPath.metGoal = true;
						
						if (path.metGoal && ch == finalChar)
							return path;
						
						activeGrid[step.row][step.col] = nextPath;						
						nextLeaves.add(nextPath);
					}
				}
			}
			
			if (nextLeaves.isEmpty())
				return null;
			
			return expandBreadthFirst(nextLeaves);
		}
			
	}
	
	public static void solvePart1(File file) throws Exception {
		Grid grid = Grid.loadFromFile(file);
		
		RowCol start = grid.findValue('S');
		Path path = grid.getShortestDistance(start, 'z', 'E');
		System.out.println("length = " + path.size());
	}
	
	public static void solvePart2(File file) throws Exception {
		Grid grid = Grid.loadFromFile(file);
		Path bestPath = null;
		
		RowCol sLoc = grid.findValue('S');
		grid.setValue(sLoc, 'a');
		
		for (int i=0; i<grid.height; i++) {
			for (int j=0; j<grid.width; j++) {
				RowCol start = new RowCol(i, j);
				if (grid.getValue(start) == 'a') {
					Path path = grid.getShortestDistance(start, 'z', 'E');
					if (path != null) {
						if (bestPath == null || path.size() < bestPath.size())
							bestPath = path;
					}
				}
			}
		}
		
		System.out.println("length = " + bestPath.size());
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day12/test.txt");
			File file = new File("files/day12/input.txt");
			//solvePart1(file);
			solvePart2(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
