package day08;

import java.io.File;
import java.io.IOException;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class Forest {
		
		int [][] grid;
		int height;
		int width;
		
		public Forest(int height, int width) {
			this.grid = new int[height][width];
			this.height = height;
			this.width = width;
		}
		
		public static Forest load(File file) throws IOException {
			List<String> lines = FileUtil.readLinesFromFile(file);
			
			while (lines.get(lines.size()-1).isEmpty())
				lines.remove(lines.size()-1);
			
			int height = lines.size();
			int width = lines.get(0).length();
			Forest forest = new Forest(height, width);
			
			for (int i=0; i<lines.size(); i++) {
				String line = lines.get(i);
				for (int j=0; j<width; j++) {
					forest.grid[i][j] = Integer.parseInt(line.substring(j,j+1));
				}
			}
			
			return forest;
		}
		
		public boolean isVisible(int i, int j) {
						
			if (i == 0 || j == 0 || i == height - 1 || j == width - 1)
				return true;
			
			return isVisibleFromLeft(i, j) || isVisibleFromRight(i, j) ||
				   isVisibleFromTop(i, j) || isVisibleFromBottom(i, j);
		}
		
		public boolean isVisibleFromTop(int i, int j) {
			for (int ii=0; ii<i; ii++) {
				if (grid[ii][j] >= grid[i][j])
					return false;
			}
			return true;
		}
		public boolean isVisibleFromBottom(int i, int j) {
			for (int ii=i+1; ii<height; ii++) {
				if (grid[ii][j] >= grid[i][j])
					return false;
			}
			return true;
		}
		public boolean isVisibleFromLeft(int i, int j) {
			for (int jj=0; jj<j; jj++) {
				if (grid[i][jj] >= grid[i][j])
					return false;
			}
			return true;
		}
		public boolean isVisibleFromRight(int i, int j) {
			for (int jj=j+1; jj<width; jj++) {
				if (grid[i][jj] >= grid[i][j])
					return false;
			}
			return true;
		}
		
		public int getVisibleTreeCount() {
			int height = grid.length;
			int width = grid[0].length;
			
			int count = height * 2 + width * 2 - 4;
			for (int i=1; i<height-1; i++) {
				for (int j=1; j<width-1; j++) {
					if (isVisible(i, j))
						count++;
				}
			}
			
			return count;
		}
		
		public int getScenicScore(int i, int j) {
			int left   = countVisibleDistance(i, j, 0, -1);
			int right  = countVisibleDistance(i, j, 0, +1);
			int top    = countVisibleDistance(i, j, -1, 0);
			int bottom = countVisibleDistance(i, j, +1, 0);
			return left * right * top * bottom;
		}
		
		public int countVisibleDistance(int i, int j, int di, int dj) {
			int count = 0;
			
			int ii = i + di;
			int jj = j + dj;
			while (ii >= 0 && ii < height && jj >= 0 && jj < width) {
				count++;
				if (grid[ii][jj] >= grid[i][j])
					break;
				
				ii += di;
				jj += dj;
			}
			
			return count;
		}
		
		public int getMaxScenicScore() {
			int max = 0;
			for (int i=0; i<height;i ++) {
				for (int j=0; j<width; j++) {
					int score = getScenicScore(i, j);
					//if (score > max) 
					//	System.out.println("New best (" + i + ", " + j + ") = " + score);
					max = Math.max(max, score);
				}
			}
			return max;
		}
		
	}
	
	public static void solve(File file) throws Exception {
		Forest forest = Forest.load(file);
		System.out.println("Visible trees: " + forest.getVisibleTreeCount());
		System.out.println("Best scenic score: " + forest.getMaxScenicScore());
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day08/test.txt");
			File file = new File("files/day08/input.txt");
			solve(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
