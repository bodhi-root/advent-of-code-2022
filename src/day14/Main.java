package day14;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FileUtil;

public class Main {
	
	static class XY {
		
		int x;
		int y;
		
		public XY(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int hashCode() {
			return x * 13 ^ 7 + y;
		}
		public boolean equals(Object o) {
			if (!(o instanceof XY))
				return false;
			
			XY that = (XY)o;
			return this.x == that.x && this.y == that.y;
		}
		
		public String toString() {
			return this.x + "," + this.y;
		}
		
	}
	
	static class World {
		
		Map<XY, Character> map = new HashMap<>(1000);
		
		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;
		
		public void set(XY xy, char value) {
			if (map.isEmpty()) {
				this.minX = xy.x;
				this.maxX = xy.x;
				this.minY = xy.y;
				this.maxY = xy.y;
			} else {
				this.minX = Math.min(this.minX, xy.x);
				this.maxX = Math.max(this.maxX, xy.x);
				this.minY = Math.min(this.minY, xy.y);
				this.maxY = Math.max(this.maxY, xy.y);
			}
			
			map.put(xy, value);
			
		}
		public void set(int x, int y, char value) {
			set(new XY(x, y), value);
		}
		
		public Character get(XY xy) {
			return map.get(xy);
		}
		public Character get(int x, int y) {
			return get(new XY(x, y));
		}
		
		public boolean isEmpty(XY xy) {
			return !map.containsKey(xy);
		}
		public boolean isEmpty(int x, int y) {
			return isEmpty(new XY(x, y));
		}
		
		public String printState() {
			StringBuilder s = new StringBuilder();
			for (int y=minY; y<=maxY; y++) {
				for (int x=minX; x<=maxX; x++) {
					Character ch = get(new XY(x, y));
					s.append(ch == null ? '.' : ch.charValue());
				}
				s.append("\n");
			}
			return s.toString();
		}
		
		public static World loadFromFile(File file) throws IOException {
			World world = new World();
			List<String> lines = FileUtil.readLinesFromFile(file);
			
			//parse lines such as: 498,4 -> 498,6 -> 496,6
			for (String line : lines) {
				//System.out.println("Drawing line: " + line);
				String [] parts = line.split("\\s+");
				XY xyLast = parseXY(parts[0]);
				for (int i=2; i<parts.length; i+=2) {
					XY xy = parseXY(parts[i]);
					
					int dx = (int)Math.signum(xy.x - xyLast.x);
					int dy = (int)Math.signum(xy.y - xyLast.y);
					
					//System.out.println("Connecting " + xyLast + " to " + xy + " (dx = " + dx + ", dy = " + dy + ")");
					
					int x = xyLast.x;
					int y = xyLast.y;
					world.set(x, y, '#');
					while (!(x == xy.x && y == xy.y)) {
						x += dx;
						y += dy;
						world.set(x, y, '#');
					}
					
					xyLast = xy;
				}
			}
			return world;
		}
		
		protected static XY parseXY(String text) {
			String [] coords = text.split(",");
			return new XY(
					Integer.parseInt(coords[0]), 
					Integer.parseInt(coords[1]));
		}
		
		public boolean dropSand(XY start) {
			int x = start.x;
			int y = start.y;
			
			while (y <= maxY) {
				if (isEmpty(x, y+1)) {
					y++;
				}
				else if (isEmpty(x-1, y+1)) {
					x--;
					y++;
				}
				else if (isEmpty(x+1, y+1)) {
					x++;
					y++;
				}
				else {
					set(x, y, 'O');
					return true;
				}
			}
			
			return false;
		}
		
		public boolean dropSandPart2(XY start, int floor) {

			if (!isEmpty(start))
				return false;
			
			int x = start.x;
			int y = start.y;
			
			while (true) {
				if (y + 1 == floor) {
					set(x, y, 'O');
					return true;
				}
				
				if (isEmpty(x, y+1)) {
					y++;
				}
				else if (isEmpty(x-1, y+1)) {
					x--;
					y++;
				}
				else if (isEmpty(x+1, y+1)) {
					x++;
					y++;
				}
				else {
					set(x, y, 'O');
					return true;
				}
			}
		}
		
	}
	
	public static void solve(File file, boolean part1) throws Exception {
		World world = World.loadFromFile(file);
		System.out.println(world.printState());
		
		XY sandStart = new XY(500, 0);
		int t = 0;
		if (part1) {
			while (world.dropSand(sandStart))
				t++;
		}
		else {
			int floor = world.maxY + 2;
			while (world.dropSandPart2(sandStart, floor))
				t++;
		}
		
		System.out.println("========================================================");
		System.out.println(world.printState());
		System.out.println("t = " + t);
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day14/test.txt");
			File file = new File("files/day14/input.txt");
			//solve(file, true);
			solve(file, false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
