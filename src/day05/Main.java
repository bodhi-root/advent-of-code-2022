package day05;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class World {
		
		List<Tower> towers;
		
		public World(int towerCount) {
			this.towers = new ArrayList<>(towerCount);
			for (int i=0; i<towerCount; i++)
				this.towers.add(new Tower());
		}
		
		public Tower getTower(int index) {
			return towers.get(index);
		}
		
		public String getTopValues() {
			StringBuilder s = new StringBuilder();
			for (Tower tower : towers) {
				if (tower.isEmpty())
					s.append(" ");
				else
					s.append(tower.getLast());
			}
			return s.toString();
		}
		
		/**
		 * Applies command such as: move 3 from 2 to 5
		 */
		public void runCommand(String text, boolean preserveOrder) {
			String [] parts = text.trim().split("\\s+");
			int amount = Integer.parseInt(parts[1]);
			int src = Integer.parseInt(parts[3])-1;
			int dst = Integer.parseInt(parts[5])-1;
			move(src, dst, amount, preserveOrder);
		}
		
		public void move(int src, int dst, int amount, boolean preserveOrder) {
			towers.get(src).moveTo(towers.get(dst), amount, preserveOrder);
		}
		
		/**
		 * Reads in state from lines such as:
		 * 
		 *     [D]    
         * [N] [C]    
         * [Z] [M] [P]
         *  1   2   3 
		 * 
		 */
		public static World readState(List<String> lines) {
			String lastLine = lines.get(lines.size() - 1);
			String [] parts = lastLine.trim().split("\\s+");
			int towerCount = parts.length;
			
			World world = new World(towerCount);
			for (int i=lines.size()-2; i>=0; i--) {
				String line = lines.get(i);
				for (int j=0; j<towerCount; j++) {
					int offset = j*4 + 1;
					String value = line.substring(offset, offset+1);
					if (!value.trim().isEmpty())
						world.getTower(j).add(value);
				}
			}
			return world;
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			
			int maxSize = 0;
			for (Tower tower : towers)
				maxSize = Math.max(maxSize, tower.size());
			
			for (int layer=maxSize-1; layer>=0; layer--) {
				for (int idxTower=0; idxTower<towers.size(); idxTower++) {
					if (idxTower>0)
						s.append(" ");
					
					Tower tower = towers.get(idxTower);
					if (tower.size() > layer)
						s.append("[").append(tower.get(layer)).append("]");
					else
						s.append("   ");
				}
				s.append("\n");
			}
			
			for (int i=0; i<towers.size(); i++) {
				if (i == 0)
					s.append(" ");
				else
					s.append("   ");
				s.append(i+1);
			}
			
			return s.toString();
		}
		
	}
	
	static class Tower {
		
		List<String> stack;
		
		public Tower() {
			stack = new ArrayList<>();
		}
		
		public boolean isEmpty() {
			return stack.isEmpty();
		}
		
		public int size() {
			return stack.size();
		}
		
		public void add(String value) {
			stack.add(value);
		}
		
		public String get(int idx) {
			return stack.get(idx);
		}
		public String getLast() {
			return stack.get(stack.size()-1);
		}
		public String removeLast() {
			return stack.remove(stack.size()-1);
		}
		
		public void moveTo(Tower dst, int amount, boolean preserveOrder) {
			if (preserveOrder) {
				String [] values = new String [amount];
				for (int i=0; i<amount; i++)
					values[amount-i-1] = this.removeLast();
				for (int i=0; i<amount; i++)
					dst.add(values[i]);	
			} else {
				for (int i=0; i<amount; i++)
					dst.add(this.removeLast());
			}
		}
		
	}
	
	public static void solve(File file, boolean preserveOrder, boolean verbose) throws Exception {
		List<String> lines = FileUtil.readLinesFromFile(file);
		
		int idxBlank=0;
		for (; idxBlank<lines.size(); idxBlank++) {
			if (lines.get(idxBlank).trim().isEmpty())
				break;
		}
		
		World world = World.readState(lines.subList(0, idxBlank));
		System.out.println(world.toString());
		
		for (int i=idxBlank+1; i<lines.size(); i++) {
			System.out.println(lines.get(i));
			world.runCommand(lines.get(i), preserveOrder);
			if (verbose)
				System.out.println(world.toString());
		}
		
		System.out.println(world.toString());
		System.out.println(world.getTopValues());
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day05/test.txt");
			//solve(file, false, true);
			//solve(file, true, true);
			
			File file = new File("files/day05/input.txt");
			solve(file, false, false);
			solve(file, true, false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
