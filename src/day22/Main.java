package day22;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.FileUtil;

public class Main {
	
	public static final int RIGHT = 0;
	public static final int DOWN = 1;
	public static final int LEFT = 2;
	public static final int UP = 3;
	
	static class Loc {
		
		int row;
		int col;
		
		public Loc(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		public Loc adjacent(int dir) {
			switch(dir) {
				case RIGHT: return new Loc(row,   col+1);
				case DOWN:  return new Loc(row+1, col  );
				case LEFT:  return new Loc(row,   col-1);
				case UP:    return new Loc(row-1, col  );
				default: throw new IllegalStateException("Invalid direction: " + dir);
			}
		}
		
		public String toString() {
			return row + ", " + col;
		}
		
	}
	
	static class LocAndDir {
		
		Loc loc;
		int dir;
		
		public LocAndDir(Loc loc, int dir) {
			this.loc = loc;
			this.dir = dir;
		}
		public LocAndDir(int row, int col, int dir) {
			this(new Loc(row, col), dir);
		}
		
		public LocAndDir turnLeft() {
			int nextDir = this.dir - 1;
			if (nextDir < 0)
				nextDir += 4;
			return new LocAndDir(this.loc, nextDir);
		}
		public LocAndDir turnRight() {
			int nextDir = this.dir + 1;
			if (nextDir == 4)
				nextDir = 0;
			return new LocAndDir(this.loc, nextDir);
		}
		
		public long getPassword() {
			return (this.loc.row + 1) * 1000 + (this.loc.col + 1) * 4 + dir;
		}
		
		public String toString() {
			return this.loc.row + ", " + this.loc.col + ", dir=" + this.dir;
		}
		
	}
	
	static class Board {
		
		char [][] grid;
		int height;
		int width;
		LocAndDir [][][] connections;
		
		public Board(char [][] grid, String connectivity) {
			this.grid = grid;
			this.height = grid.length;
			this.width = grid[0].length;
			
			this.connections = new LocAndDir[height][width][4];
			if (connectivity.equals("part1"))
				initPart1Connectivity();
			else if (connectivity.equals("part2-test"))
				initPart2ConnectivityTest();
			else if (connectivity.equals("part2-mine"))
				initPart2ConnectivityMine();
			else
				throw new IllegalArgumentException("Unknown connectivity: " + connectivity);
		}
		
		protected void initPart1Connectivity() {
			
			for (int i=0; i<height; i++) {
				for (int j=0; j<width; j++) {
					
					Loc loc = new Loc(i, j);
					for (int dir=0; dir<4; dir++) {
						if (isValid(i, j)) {
							Loc nextLoc = loc.adjacent(dir);
							if (!isValid(nextLoc.row, nextLoc.col)) {
								
								switch(dir) {
								case RIGHT: nextLoc = new Loc(i, findExtremeLeft(i, j)); break;
								case DOWN:  nextLoc = new Loc(findExtremeTop(i, j), j); break;
								case LEFT:  nextLoc = new Loc(i, findExtremeRight(i, j)); break;
								case UP:    nextLoc = new Loc(findExtremeBottom(i, j), j); break;
								default: throw new IllegalStateException("Invalid direction: " + dir);
							}
								
							}
							connections[i][j][dir] = new LocAndDir(nextLoc, dir);
						}	
					}
					
				}
			}
		}
		
		protected void connect(Loc loc, int dir, Loc nextLoc, int nextDir) {
			this.connections[loc.row][loc.col][dir] = new LocAndDir(nextLoc, nextDir);
		}
		
		protected void initPart2ConnectivityTest() {
			//default values:
			for (int i=0; i<height; i++) {
				for (int j=0; j<width; j++) {
					if (isValid(i, j)) {
						Loc loc = new Loc(i, j);
						for (int dir=0; dir<4; dir++) {
							Loc nextLoc = loc.adjacent(dir);
							connect(loc, dir, nextLoc, dir); 
						}
					}
				}
			}
		
			/*
			 *          1111       <- row 0
			 *	        1111
			 * 	        1111
			 *	        1111
			 *	222233334444       <- row: sideLen
			 *	222233334444
			 *	222233334444
			 *	222233334444
			 *	        55556666   <- row: sideLen * 2
			 *	        55556666
			 *	        55556666
			 *	        55556666 
			 *
			 *  ^   ^   ^   ^
			 *  0   s   s*2 s*3
			 */
			
			//borders:
			int sideLen = this.height / 3;
			System.out.println(height + " x " + width + " sideLen = " + sideLen);
			for (int i=0; i<sideLen; i++) {
				
				//top2 to top1
				Loc top2 = new Loc(sideLen, i);
				Loc top1 = new Loc(0, sideLen*3-1-i);
				connect(top2, UP, top1, DOWN);
				connect(top1, UP, top2, DOWN);
				
				//left2 to bottom6:
				Loc left2 = new Loc(sideLen+i, 0);
				Loc bottom6 = new Loc(height-1, width-1-i);
				connect(left2, LEFT, bottom6, UP);
				connect(bottom6, DOWN, left2, RIGHT);
				
				//right4 to top6:
				Loc right4 = new Loc(sideLen+i, sideLen*3-1);
				Loc top6 = new Loc(sideLen*2, width-1-i);
				connect(right4, RIGHT, top6, DOWN);
				connect(top6, UP, right4, LEFT);
				
				//top3 to left1:
				Loc top3 = new Loc(sideLen, sideLen+i);
				Loc left1 = new Loc(i, sideLen*2);
				connect(top3, UP, left1, RIGHT);
				connect(left1, LEFT, top3, DOWN);
				
				//bottom3 to left5:
				Loc bottom3 = new Loc(sideLen*2-1, sideLen+i);
				Loc left5 = new Loc(height-1-i, sideLen*2);
				connect(bottom3, DOWN, left5, RIGHT);
				connect(left5, LEFT, bottom3, UP);
				
				//right1 to right6:
				Loc right1 = new Loc(i, sideLen*3-1);
				Loc right6 = new Loc(height-1-i, width-1);
				connect(right1, RIGHT, right6, LEFT);
				connect(right6, RIGHT, right1, LEFT);
				
				//bottom2 to bottom5:
				Loc bottom2 = new Loc(sideLen*2-1, i);
				Loc bottom5 = new Loc(height-1, sideLen*3-1-i);
				connect(bottom2, DOWN, bottom5, UP);
				connect(bottom5, DOWN, bottom2, UP);
			}
		}
		
		protected void initPart2ConnectivityMine() {
			//default values:
			for (int i=0; i<height; i++) {
				for (int j=0; j<width; j++) {
					if (isValid(i, j)) {
						Loc loc = new Loc(i, j);
						for (int dir=0; dir<4; dir++) {
							Loc nextLoc = loc.adjacent(dir);
							connect(loc, dir, nextLoc, dir); 
						}
					}
				}
			}
		
			/*
			 *      11112222   <- row 0
			 *	    11112222
			 * 	    11112222
			 *	    11112222
			 *	    3333       <- row: sideLen
			 *	    3333
			 *	    3333
			 *	    3333
			 *	44445555       <- row: sideLen * 2
			 *	44445555       
			 *	44445555       
			 *	44445555       
			 *  6666           <- row: sideLen *3
			 *  6666
			 *  6666
			 *  6666
			 *  ^   ^   ^   
			 *  0   s   s*2 
			 */
			
			//borders:
			int sideLen = this.width / 3;
			System.out.println(height + " x " + width + " sideLen = " + sideLen);
			for (int i=0; i<sideLen; i++) {
				
				//left3 to top4
				Loc left3 = new Loc(sideLen+i, sideLen);
				Loc top4 = new Loc(sideLen*2, i);
				connect(left3, LEFT, top4, DOWN);
				connect(top4, UP, left3, RIGHT);
				
				//bottom5 to right6
				Loc bottom5 = new Loc(sideLen*3-1, sideLen+i);
				Loc right6 = new Loc(sideLen*3+i, sideLen-1);
				connect(bottom5, DOWN, right6, LEFT);
				connect(right6, RIGHT, bottom5, UP);
				
				//bottom2 to right3
				Loc bottom2 = new Loc(sideLen-1, sideLen*2+i);
				Loc right3 = new Loc(sideLen+i, sideLen*2-1);
				connect(bottom2, DOWN, right3, LEFT);
				connect(right3, RIGHT, bottom2, UP);
				
				//left1 to left4
				Loc left1 = new Loc(i, sideLen);
				Loc left4 = new Loc(sideLen*3-1-i, 0);
				connect(left1, LEFT, left4, RIGHT);
				connect(left4, LEFT, left1, RIGHT);
				
				//top1 to left6
				Loc top1 = new Loc(0, sideLen+i);
				Loc left6 = new Loc(sideLen*3+i, 0);
				connect(top1, UP, left6, RIGHT);
				connect(left6, LEFT, top1, DOWN);
				
				//right2 to right5
				Loc right2 = new Loc(i, width-1);
				Loc right5 = new Loc(sideLen*3-1-i, sideLen*2-1);
				connect(right2, RIGHT, right5, LEFT);
				connect(right5, RIGHT, right2, LEFT);
				
				//top2 to bottom6
				Loc top2 = new Loc(0, sideLen*2+i);
				Loc bottom6 = new Loc(height-1, i);
				connect(top2, UP, bottom6, UP);
				connect(bottom6, DOWN, top2, DOWN);
			}
		}
		
		public char get(int i, int j) {
			return grid[i][j];
		}
		public char get(Loc loc) {
			return get(loc.row, loc.col);
		}
		
		public LocAndDir getNext(LocAndDir locDir) {
			LocAndDir result = this.connections[locDir.loc.row][locDir.loc.col][locDir.dir];
			if (result == null)
				throw new IllegalStateException("Move not defined for: " + locDir.toString());
			return result;
		}
		
		public boolean isValid(int i, int j) {
			return (i >= 0 && i < height &&
					j >= 0 && j < width &&
					grid[i][j] != ' ');
		}
		public boolean isValid(Loc loc) {
			return isValid(loc.row, loc.col);
		}
		
		public int getFirstValidColumn(int i) {
			for (int j=0; j<width; j++)
				if (grid[i][j] != ' ')
					return j;
			return -1;
		}
		public int getLastValidColumn(int i) {
			for (int j=width-1; j>=0; j--)
				if (grid[i][j] != ' ')
					return j;
			return -1;
		}
		
		public int findExtremeRight(int i, int j) {
			while (j+1 < width && grid[i][j+1] != ' ')
				j++;
			return j;
		}
		public int findExtremeLeft(int i, int j) {
			while (j-1 >= 0 && grid[i][j-1] != ' ')
				j--;
			return j;
		}
		public int findExtremeBottom(int i, int j) {
			while (i+1 < height && grid[i+1][j] != ' ')
				i++;
			return i;
		}
		public int findExtremeTop(int i, int j) {
			while (i-1 >= 0 && grid[i-1][j] != ' ')
				i--;
			return i;
		}
		
		public static Board loadFrom(List<String> lines, String connectivity) {
			int height = lines.size();
			int width = 0;
			for (String line : lines)
				width = Math.max(width, line.length());
			
			char [][] grid = new char[height][width];
			for (int i=0; i<height; i++) {
				String line = lines.get(i);
				for (int j=0; j<line.length(); j++)
					grid[i][j] = line.charAt(j);
				for (int j=line.length(); j<width; j++)
					grid[i][j] = ' ';
			}
			
			return new Board(grid, connectivity);
		}
		
	}
	
	static class Puzzle {
		
		Board board;
		char [][] path;
		String input;
		
		LocAndDir locDir;
		
		public static Puzzle loadFrom(File file, String connectivity) throws IOException {
			List<String> lines = FileUtil.readLinesFromFile(file);
			
			while (lines.get(lines.size()-1).trim().isEmpty())
				lines.remove(lines.size()-1);
			
			Puzzle puzzle = new Puzzle();
			puzzle.input = lines.remove(lines.size()-1);
			lines.remove(lines.size()-1);
			puzzle.board = Board.loadFrom(lines, connectivity);
			return puzzle;
		}
		
		public void init() {
			Loc start = new Loc(0, board.getFirstValidColumn(0));
			this.locDir = new LocAndDir(start, RIGHT);
			
			this.path = new char[this.board.height][this.board.width];
			for (int i=0; i<board.height; i++)
				Arrays.fill(this.path[i], ' ');
			
			updatePath();
		}
		
		public void process(String cmd) {
			if (cmd.equals("R")) {
				this.locDir = this.locDir.turnRight();
				updatePath();
			}
			else if (cmd.equals("L")) {
				this.locDir = this.locDir.turnLeft();
				updatePath();
			}
			else {
				advance(Integer.parseInt(cmd));
			}
		}
		
		protected void updatePath() {
			char ch = ' ';
			switch(this.locDir.dir) {
			case RIGHT: ch = '>'; break;
			case DOWN: ch = 'v'; break;
			case LEFT: ch = '<'; break;
			case UP: ch = '^'; break;
			default: throw new IllegalStateException("Invalid dir: " + this.locDir.dir);
			}
			
			this.path[this.locDir.loc.row][this.locDir.loc.col] = ch; 
		}
		
		public void advance(int steps) {
			for (int t=0; t<steps; t++) {
				System.out.print("Moving from " + locDir.toString());
				LocAndDir next = this.board.getNext(this.locDir);
				System.out.println(" to " + next.toString());
				if (board.get(next.loc) == '#')
					break;
				this.locDir = next;
				updatePath();
			}
		}
		
		public long getPassword() {
			return this.locDir.getPassword();
		}
		
		public String [] splitInput() {
			List<String> parts = new ArrayList<>();
			char [] chars = input.toCharArray();
			
			//10R5L5R10L4R5L5
			StringBuilder buff = new StringBuilder();
			for (int i=0; i<chars.length; i++) {
				if (chars[i] == 'L' || chars[i] == 'R') {
					parts.add(buff.toString());
					buff.setLength(0);
					
					parts.add(String.valueOf(chars[i]));
				} else {
					buff.append(chars[i]);
				}
			}
			if (buff.length() > 0)
				parts.add(buff.toString());			
			
			return parts.toArray(new String[parts.size()]);
		}
		
		public String printPath() {
			StringBuilder s = new StringBuilder();
			for (int i=0; i<board.height; i++) {
				for (int j=0; j<board.width; j++) {
					if (path[i][j] != ' ')
						s.append(path[i][j]);
					else
						s.append(board.grid[i][j]);
				}
				s.append('\n');
			}
			return s.toString();
		}
		
	}
	
	public static void solve(File file, String connectivity) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file, connectivity);
		puzzle.init();
		System.out.println("Starting at: " + puzzle.locDir.toString());
		
		String [] parts = puzzle.splitInput();
		for (String part : parts) {
			System.out.print(part + ": ");
			puzzle.process(part);
			System.out.println(puzzle.locDir.toString());
		}
		
		System.out.println();
		System.out.println(puzzle.printPath());
		System.out.println();
		
		System.out.println("Ended at: " + puzzle.locDir.toString());
		System.out.println("Password: " + puzzle.getPassword());
	}
	
	public static void main(String [] args) {
		try {
			//File testFile = new File("files/day22/test.txt");
			//solve(testFile, "part1");
			//solve(testFile, "part2-test");
			
			File inputFile = new File("files/day22/input.txt");
			//solve(inputFile, "part1");
			solve(inputFile, "part2-mine");
			
			//File testFile2 = new File("files/day22/test2.txt");
			//solve(testFile2, "part2-mine");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
