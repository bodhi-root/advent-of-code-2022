package day09;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.FileUtil;

public class Main {
	
	static class XY {
		
		int x;
		int y;
		
		public XY(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public XY copy() {
			return new XY(x, y);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof XY))
				return false;
			
			XY that = (XY)obj;
			return this.x == that.x && this.y == that.y;
		}
		
		@Override
		public int hashCode() {
			return this.x ^ 7 * 13 + this.y;
		}
		
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
		
	}
	
	static class Knot {
		
		XY position;
		Knot leader;
		
		public Knot() {
			position = new XY(0, 0);
		}
		
		public void move(int dx, int dy) {
			position.x += dx;
			position.y += dy;
		}
		
		public void updatePosition() {
			int dx = leader.position.x - position.x;
			int dy = leader.position.y - position.y;
			
			if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1)
				return;
			
			//signum guarantees we only move 1 step
			position.x += Math.signum(dx);
			position.y += Math.signum(dy);
		}
		
	}
	
	static class World {
		
		Knot head;
		List<Knot> tails;
		
		public World(int numTails) {
			head = new Knot();
			this.tails = new ArrayList<>(numTails);
			
			Knot leader = head;
			for (int i=0; i<numTails; i++) {
				Knot knot = new Knot();
				knot.leader = leader;
				tails.add(knot);
				
				leader = knot;
			}
		}
		
		public void moveHead(int dx, int dy) {
			head.move(dx, dy);
		}
		
		public void updateTails() {
			for (Knot knot : tails) {
				knot.updatePosition();
			}
		}
		
		public Knot getLastTail() {
			return tails.get(tails.size()-1);
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("Head: ").append(head.position.toString());
			
			s.append("Tails: [");
			for (int i=0; i<tails.size(); i++) {
				if (i > 0)
					s.append(", ");
				s.append(tails.get(i).position.toString());
			}
			s.append("]");
			return s.toString();
		}
		
	}
	
	public static void solve(File file, int numTails) throws Exception {
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		
		World world = new World(numTails);
		
		Set<XY> tailVisited = new HashSet<>();
		Knot tail = world.getLastTail();
		tailVisited.add(tail.position.copy());
				
		for (String line : lines) {
			//System.out.println("Input: " + line);
			String [] parts = line.split("\\s+");
			String dir = parts[0];
			int len = Integer.parseInt(parts[1]);
			
			int dx = 0;
			int dy = 0;
			if (dir.equals("U"))
				dy = +1;
			else if (dir.equals("D"))
				dy = -1;
			else if (dir.equals("L"))
				dx = -1;
			else if (dir.equals("R"))
				dx = +1;
			else
				throw new IllegalStateException("Invalid input: " + line);
			
			for (int i=0; i<len; i++) {
				world.moveHead(dx, dy);
				world.updateTails();
				tailVisited.add(tail.position.copy());
				//System.out.println(tail.position.toString());
			}
		}
		
		System.out.println("Tail visited: " + tailVisited.size() + " points");
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day09/test.txt");
			//File file = new File("files/day09/test2.txt");
			File file = new File("files/day09/input.txt");
			
			//solve(file, 1);
			solve(file, 9);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
