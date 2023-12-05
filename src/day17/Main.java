package day17;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FileUtil;
import common.geometry.XY;

public class Main {
	
	static final char SOLID = '#';
	static final char EMPTY = '.';
	
	/**
	 * Represents a shape as a 2-dimensional character array.
	 * '#' indicates a solid portion.  (x,y) is defined such that
	 * (0,0) is the bottom left corner.
	 */
	static class Shape {
		
		char [][] shape;
		int width;
		int height;
		
		public Shape(char [][] shape) {
			this.shape = shape;
			this.height = shape.length;
			this.width = shape[0].length;
		}
		
		public boolean isSolid(int x, int y) {
			return shape[y][x] == SOLID;
		}
		
		/**
		 * Creates a shape from an array of text strings. The order of
		 * the strings is such that the first row is the top.  We reverse
		 * this into our (x, y) coordinates where row 0 is the bottom
		 * automatically.
		 */
		public static Shape fromLines(String [] lines) {
			char [][] shape = new char[lines.length][];
			for (int i=0; i<lines.length; i++)
				shape[lines.length-i-1] = lines[i].toCharArray();
			return new Shape(shape);
		}
		
	}
	
	static final Shape SHAPE1 = Shape.fromLines(new String [] {
			"####"
	});

	static final Shape SHAPE2 = Shape.fromLines(new String [] {
			".#.",
			"###",
			".#."
	});
	static final Shape SHAPE3 = Shape.fromLines(new String [] {
			"..#",
			"..#",
			"###"
	});
	static final Shape SHAPE4 = Shape.fromLines(new String [] {
			"#",
			"#",
			"#",
			"#"
		});
	static final Shape SHAPE5 = Shape.fromLines(new String [] {
			"##",
			"##"
		});
	
	static class Piece {
		
		Shape shape;
		int x;
		int y;
		
		public Piece(Shape shape, int x, int y) {
			this.shape = shape;
			this.x = x;
			this.y = y;
		}
		
	}
	
	static class World {
		
		Map<XY<Integer>, Character> grid = new HashMap<>();
		
		List<Piece> pieces = new ArrayList<>();
		
		Shape [] ALL_SHAPES = new Shape [] {SHAPE1, SHAPE2, SHAPE3, SHAPE4, SHAPE5};
		int nextShapeIndex = 0;
		
		public void set(int x, int y, char value) {
			grid.put(new XY<Integer>(x,y), value);
		}
		
		public Character get(int x, int y) {
			return grid.get(new XY<Integer>(x, y));
		}
		
		public int getMaxHeight() {
			int max = 0;
			for (Piece piece : pieces) {
				int top = piece.y + piece.shape.height;
				max = Math.max(max, top);
			}
			return max;
		}
		
		public Piece createNextPiece() {
			Shape shape = ALL_SHAPES[nextShapeIndex];
			nextShapeIndex++;
			if (nextShapeIndex >= ALL_SHAPES.length)
				nextShapeIndex=0;
			
			Piece piece = new Piece(shape, 2, getMaxHeight() + 3);
			pieces.add(piece);
			apply(piece, SOLID);
			return piece;
		}
		
		public void apply(Piece piece, char value) {
			for (int dx=0; dx<piece.shape.width; dx++) {
				for (int dy=0; dy<piece.shape.height; dy++) {
					if (piece.shape.isSolid(dx, dy))
						set(piece.x + dx, piece.y + dy, value);
				}
			}
		}
		
		protected boolean doesPieceFit(Piece piece, int x, int y) {
			for (int dx=0; dx<piece.shape.width; dx++) {
				for (int dy=0; dy<piece.shape.height; dy++) {
					if (piece.shape.isSolid(dx, dy) && isSolid(x + dx, y + dy))
						return false;
				}
			}
			return true;
		}
		
		public boolean isSolid(int x, int y) {
			Character ch = get(x, y);
			return (ch != null) && ch.charValue() == SOLID;
		}
		
		public void runProgram(String line, int pieceCount, boolean verbose, boolean part2) {
			Piece activePiece = createNextPiece();
			if (verbose)
				System.out.println(printString());
			
			char [] chars = line.toCharArray();
			int nextCharIndex = 0;
			
			boolean running = true;
			while(running) {
				char ch = chars[nextCharIndex++];
				if (nextCharIndex >= chars.length)
					nextCharIndex = 0;
				
				if (verbose) {
					System.out.println();
					System.out.println("------------------------------");
					System.out.println("Input: " + ch);
				}
				
				//apply wind:
				int dx = 0;
				if (ch == '>') {
					if (activePiece.x + activePiece.shape.width < 7)
						dx = 1;
				}
				else if (ch == '<') {
					if (activePiece.x > 0)
						dx = -1;
				}
				else {
					throw new IllegalArgumentException("Invalid input: " + ch);
				}
				
				if (dx != 0) {
					apply(activePiece, EMPTY);
					if (doesPieceFit(activePiece, activePiece.x + dx, activePiece.y))
						activePiece.x += dx;
					apply(activePiece, SOLID);
				}
				
				if (verbose) {
					System.out.println();
					System.out.println(printString());
				}
				
				//move down:
				
				apply(activePiece, EMPTY);	//turn off, or else piece hits itself
				
				if (activePiece.y > 0 && doesPieceFit(activePiece, activePiece.x, activePiece.y - 1)) {
					activePiece.y--;
					apply(activePiece, SOLID);
				} else {
					apply(activePiece, SOLID);
					if (pieces.size() == pieceCount) {
						System.out.println("All pieces simulated");
						running = false;
					}
					else {
						activePiece = createNextPiece();
					}
				}
				
				if (verbose) {
					System.out.println();
					System.out.println("------------------------------");
					System.out.println("Move down:");
					System.out.println();
					System.out.println(printString());
				}
				
			}
		}
		
		
		public String printString() {
			StringBuilder s = new StringBuilder();
			int yMax = getMaxHeight();
			for (int y=yMax; y>=0; y--) {
				for (int x=0; x<7; x++) {
					if (isSolid(x, y))
						s.append('#');
					else
						s.append('.');
				}
				s.append('\n');
			}
			return s.toString();
		}
		
	}
	
	public static void solvePart1(File file, int pieceCount, boolean verbose) throws Exception {
		World world = new World();
		String line = FileUtil.readLineFromFile(file);
		world.runProgram(line, pieceCount, verbose, false);
		System.out.println(world.printString());
		System.out.println("Height: " + world.getMaxHeight());
	}
	
	public static void getPart2CycleTime(File file) throws Exception {
		World world = new World();
		String line = FileUtil.readLineFromFile(file);
		world.runProgram(line, Integer.MAX_VALUE, false, true);
	}
	
	public static void solvePart2(File file) throws Exception {
		World world = new World();
		String line = FileUtil.readLineFromFile(file);
		world.runProgram(line, Integer.MAX_VALUE, false, true);
	}
	
	public static void main(String [] args) {
		try {
			File testFile = new File("files/day17/test.txt");
			//solvePart1(testFile, 10, true);
			//solvePart1(testFile, 2022, false);
			getPart2CycleTime(testFile);
			//solvePart2();
			
			File inputFile = new File("files/day17/input.txt");
			//solvePart1(inputFile, 2022, false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
