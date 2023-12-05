package day18;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.FileUtil;
import common.geometry.XYZ;

public class Main {
	
	static class CoordinateSet {
		
		Set<XYZ<Integer>> coords = new HashSet<>();
		
		int xMin = 0;
		int yMin = 0;
		int zMin = 0;
		int xMax = 0;
		int yMax = 0;
		int zMax = 0;
		
		public boolean add(XYZ<Integer> coord) {
			boolean result = coords.add(coord);
			
			if (coords.size() == 1) {
				this.xMin = this.xMax = coord.x;
				this.yMin = this.yMax = coord.y;
				this.zMin = this.zMax = coord.z;
			} else {
				this.xMin = Math.min(this.xMin, coord.x);
				this.xMax = Math.max(this.xMax, coord.x);
				this.yMin = Math.min(this.yMin, coord.y);
				this.yMax = Math.max(this.yMax, coord.y);
				this.zMin = Math.min(this.zMin, coord.z);
				this.zMax = Math.max(this.zMax, coord.z);
			}
			
			return result;
		}
		public boolean add(int x, int y, int z) {
			return add(new XYZ<Integer>(x, y, z));
		}
		
		public boolean contains(XYZ<Integer> coord) {
			return coords.contains(coord);
		}
		public boolean contains(int x, int y, int z) {
			return contains(new XYZ<Integer>(x, y, z));
		}
		
		public void addAll(CoordinateSet set) {
			for (XYZ<Integer> xyz : set.coords)
				add(xyz);
		}
		
		public int getExposedSurfaceCount() {
			int count = coords.size() * 6;
			
			for (XYZ<Integer> xyz : coords) {
				if (contains(xyz.x+1, xyz.y, xyz.z))
					count--;
				if (contains(xyz.x-1, xyz.y, xyz.z))
					count--;
				if (contains(xyz.x, xyz.y+1, xyz.z))
					count--;
				if (contains(xyz.x, xyz.y-1, xyz.z))
					count--;
				if (contains(xyz.x, xyz.y, xyz.z+1))
					count--;
				if (contains(xyz.x, xyz.y, xyz.z-1))
					count--;
			}
			
			return count;
		}
		
		public int getExteriorSurfaceCount() {
			int count = getExposedSurfaceCount();
			
			//search for interior spaces and remove them:
			CoordinateSet visited = new CoordinateSet();
			for (int x=xMin; x<=xMax; x++) {
				for (int y=yMin; y<=yMax; y++) {
					
					int zStart = zMin;
					for (; zStart<=zMax; zStart++) {
						if (contains(x, y, zStart)) {
							break;
						}
					}
					
					if (zStart < zMax) {
						int zEnd = zMax;
						for (; zEnd>=0; zEnd--) {
							if (contains(x, y, zEnd)) {
								break;
							}
						}
						
						zStart++;
						zEnd--;
						for (int z=zStart; z<=zEnd; z++) {
							if (!contains(x, y, z) && !visited.contains(x, y, z)) {
								CoordinateSet emptySpace = getEmptySpaceFrom(x, y, z);
								visited.addAll(emptySpace);
								if (emptySpace.xMin > this.xMin && emptySpace.xMax < this.xMax &&
									emptySpace.yMin > this.yMin && emptySpace.yMax < this.yMax &&
									emptySpace.zMin > this.zMin && emptySpace.zMax < this.zMax) {
									
									System.out.println("Interior space found!");
									count -= emptySpace.getExposedSurfaceCount();
								}
							}
						}
					}
					
				}
			}
			
			return count;
		}
		
		public CoordinateSet getEmptySpaceFrom(int x, int y, int z) {
			CoordinateSet set = new CoordinateSet();
			set.add(new XYZ<Integer>(x, y, z));
			expandEmptySpace(set, x, y, z);
			return set;
		}
		
		protected void expandEmptySpace(CoordinateSet set, int x, int y, int z) {
			List<XYZ<Integer>> next = getAdjacentSpaces(new XYZ<Integer>(x, y, z));
			for (XYZ<Integer> xyz : next) {
				
				if (!this.contains(xyz)) {
					//don't add spaces outside of the min/max region
					if (xyz.x < xMin || xyz.x > xMax ||
						xyz.y < yMin || xyz.y > yMax ||
						xyz.z < zMin || xyz.z > zMax)
						continue;
					
					//if it's a new addition to the set, continue expanding
					if (set.add(xyz.x, xyz.y, xyz.z))
						expandEmptySpace(set, xyz.x, xyz.y, xyz.z);
				}
				
			}
		}
		
		public List<XYZ<Integer>> getAdjacentSpaces(XYZ<Integer> xyz) {
			List<XYZ<Integer>> next = new ArrayList<>();
			next.add(new XYZ<Integer>(xyz.x+1, xyz.y, xyz.z));
			next.add(new XYZ<Integer>(xyz.x-1, xyz.y, xyz.z));
			next.add(new XYZ<Integer>(xyz.x, xyz.y+1, xyz.z));
			next.add(new XYZ<Integer>(xyz.x, xyz.y-1, xyz.z));
			next.add(new XYZ<Integer>(xyz.x, xyz.y, xyz.z+1));
			next.add(new XYZ<Integer>(xyz.x, xyz.y, xyz.z-1));
			return next;
		}
		
		public static CoordinateSet loadFrom(File file) throws IOException {
			List<String> lines = FileUtil.readLinesFromFile(file);
			
			CoordinateSet coords = new CoordinateSet();
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				String [] parts = line.split(",");
				int x = Integer.parseInt(parts[0]);
				int y = Integer.parseInt(parts[1]);
				int z = Integer.parseInt(parts[2]);
				coords.add(new XYZ<Integer>(x, y, z));
			}
			return coords;
		}
		
	}
	
	public static void solvePart1(File file) throws Exception {
		CoordinateSet coords = CoordinateSet.loadFrom(file);
		System.out.println(coords.getExposedSurfaceCount());
	}
	
	public static void solvePart2(File file) throws Exception {
		CoordinateSet coords = CoordinateSet.loadFrom(file);
		System.out.println(coords.getExteriorSurfaceCount());
	}
	
	public static void main(String [] args) {
		try {
			File testFile = new File("files/day18/test.txt");
			//solvePart1(testFile);
			solvePart2(testFile);
			
			File inputFile = new File("files/day18/input.txt");
			//solvePart1(inputFile);
			solvePart2(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
