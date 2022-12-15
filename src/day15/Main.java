package day15;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.FileUtil;

public class Main {
	
	static class XY {
		
		long x;
		long y;
		
		public XY(long x, long y) {
			this.x = x;
			this.y = y;
		}
		
		public int hashCode() {
			return Long.hashCode(x * 13 ^ 7 + y);
		}
		public boolean equals(Object o) {
			if (!(o instanceof XY))
				return false;
			
			XY that = (XY)o;
			return this.x == that.x && this.y == that.y;
		}
		
		public String toString() {
			return x + ", " + y;
		}
		
		public long manhattanDistanceTo(XY loc) {
			return Math.abs(loc.x - this.x) + Math.abs(loc.y - this.y); 
		}
		
	}
	
	static class Sensor {
		
		XY location;
		XY closestBeaconLocation;
		
		/**
		 * Parse Sensor object from text:
		 * Sensor at x=2, y=18: closest beacon is at x=-2, y=15
		 */
		public static Sensor parseFrom(String text) {
			String [] parts = text.split(":");
			String locText = parts[0].substring("Sensor at ".length());
			String beaconText = parts[1].substring(" closest beacon is at ".length());
			
			//System.out.println(locText);
			//System.out.println(beaconText);
			
			Sensor sensor = new Sensor();
			sensor.location = parseXY(locText);
			sensor.closestBeaconLocation = parseXY(beaconText);
			return sensor;
		}
		public static XY parseXY(String text) {
			String [] parts = text.split(",\\s+");
			return new XY(
					Long.parseLong(parts[0].substring(2)),
					Long.parseLong(parts[1].substring(2)));
		}
		
		
		public long getDistanceToBeacon() {
			return location.manhattanDistanceTo(closestBeaconLocation);
		}
		
		public String toString() {
			return "Sensor at " + location.toString() + ": closest beacon is at " + closestBeaconLocation.toString();
		}
		
	}
	
	static void testParse() {
		String text = "Sensor at x=2, y=18: closest beacon is at x=-2, y=15";
		Sensor sensor = Sensor.parseFrom(text);
		System.out.println(sensor.toString());
	}
	
	static class Puzzle {
		
		List<Sensor> sensors = new ArrayList<>();
		
		public static Puzzle loadFrom(File file) throws IOException {
			Puzzle puzzle = new Puzzle();
			
			List<String> lines = FileUtil.readLinesFromFile(file);
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				puzzle.sensors.add(Sensor.parseFrom(line));
			}
			
			return puzzle;
		}
		
		public Set<XY> getCoveredPointsOnHorizontal(long y) {
			Set<XY> covered = new HashSet<>();
			
			for (Sensor sensor : sensors) {
				long maxLen = sensor.getDistanceToBeacon();
				if (y > sensor.location.y - maxLen && y < sensor.location.y + maxLen) {
					long len = maxLen - Math.abs(sensor.location.y - y);
					covered.add(new XY(sensor.location.x, y));
					for (int i=1; i<=len; i++) {
						covered.add(new XY(sensor.location.x + i, y));
						covered.add(new XY(sensor.location.x - i, y));
					}
				}
			}
			
			return covered;
		}
		
		public XY findUncoveredPointInRow(long y, long xMax) {
			//get list of ranges we cover:
			List<Range> covered = new ArrayList<>();
			for (Sensor sensor : sensors) {
				long maxLen = sensor.getDistanceToBeacon();
				if (y > sensor.location.y - maxLen && y < sensor.location.y + maxLen) {
					long len = maxLen - Math.abs(sensor.location.y - y);
					covered.add(new Range(sensor.location.x - len, sensor.location.x + len));
				}
			}
			
			//sort by length (biggest first to improve chances of covering entire row faster):
			covered.sort(new Comparator<Range>() {

				@Override
				public int compare(Range o1, Range o2) {
					return Long.compare(o2.length(), o1.length());
				}
				
			});
			
			//keep track of uncovered areas, removing from this list to see if anything remains:
			List<Range> uncovered = new ArrayList<>();
			uncovered.add(new Range(0, xMax));
			for (Range coverRange : covered) {
				
				for (int i=uncovered.size()-1; i>=0; i--) {
					Range uncoveredRange = uncovered.get(i);
					
					//we just covered up all possibilities: remove from list of possibilities
					if (coverRange.contains(uncoveredRange)) {
						uncovered.remove(i);
					}
					
					//covering up middle section: need to split range into two
					else if (coverRange.start > uncoveredRange.start && coverRange.end < uncoveredRange.end) {
						uncovered.remove(i);
						uncovered.add(new Range(uncoveredRange.start, coverRange.start-1));
						uncovered.add(new Range(coverRange.end+1, uncoveredRange.end));
					}
					
					//simple operation to trim range of possibilities
					else {
						if (!uncoveredRange.exclude(coverRange))
							throw new IllegalStateException("Should not happen!" +
						      " Uncovered: " + uncoveredRange.toString() + ", Covered: " + coverRange.toString());
					}
				}
				
				if (uncovered.isEmpty())
					return null;
			}
			
			if (uncovered.size() > 1)
				System.err.println("WARNING: More than 1 Range remains");
			
			Range range = uncovered.get(0);
			if (range.length() > 1)
				System.err.println("WARNING: Remaining range has more than 1 valid position");
			
			System.out.println("Remaining range: " + range.start + " - " + range.end);
			return new XY(range.start, y);
		}
		
	}
	
	/**
	 * Represents a continuous range of numbers: [start, end] inclusive
	 */
	static class Range {
		
		long start;
		long end;
		
		public Range(long start, long end) {
			this.start = start;
			this.end = end;
		}
		
		public long length() {
			return end - start + 1;
		}
		
		public boolean contains(Range range) {
			return range.start >= this.start && range.end <= this.end;
		}
		public boolean overlaps(Range range) {
			return (range.start >= this.start && range.start <= this.end) ||
				   (range.end   >= this.start && range.end   <= this.end) ||
				   (range.start < this.start && range.end > this.end);
		}
		
		/**
		 * Exclude the given range of numbers from this one.  This will 
		 * modify the start or end points of our range.  True is returned
		 * if the operation can be completed.  The operation will not
		 * be able to complete (and will return false) if either of the
		 * following are true:
		 * 
		 * 1. The range to exclude entirely contains this range.  In this
		 *    case all options would be excluded and no valid range
		 *    could be produced.
		 * 2. The range to exclude lies entirely within this range.  In 
		 *    this case (unless it covered the entire range, as in #1)
		 *    we would need to split our range into 2 ranges.
		 *    
		 * These cases will need to be handled by external logic.
		 */
		public boolean exclude(Range range) {
			//occurs entirely before or after this range: do nothing
			if (range.end < this.start || range.start > this.end)
				return true;
			
			//if entirely contains our range: can't do that
			if (range.start <= this.start && range.end >= this.end)
				return false;
			
			//if we need to break range into two, we can't do that:
			if (range.start > this.start && range.end < this.end)
				return false;
			
			//at this point we know we overlap on one end only
			
			//if start is contained in our range, keep left side:
			if (range.start > this.start && range.start <= this.end) {
				this.end = range.start - 1;
				return true;
			}
			
			//if end is contained in our range, keep right side:
			if (range.end >= this.start && range.end < this.end) {
				this.start = range.end + 1;
				return true;
			}
			
			throw new IllegalStateException("Should not happen! This range: " + this.toString() + ", exclude: " + range.toString());
		}
		
		public String toString() {
			return "Range(" + start + ", " + end + ")";
		}
		
	}
	
	public static void solvePart1(File file, long y, boolean print) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		Set<XY> points = puzzle.getCoveredPointsOnHorizontal(y);
		
		//remove beacon locations on given line:
		for (Sensor sensor : puzzle.sensors) {
			if (sensor.closestBeaconLocation.y == y)
				points.remove(sensor.closestBeaconLocation);
		}
		
		System.out.println(points.size());
		
		if (print) {
			List<Long> xList = new ArrayList<>();
			for (XY point : points)
				xList.add(point.x);
			xList.sort(Comparator.naturalOrder());
			System.out.println(xList.toString());
		}
	}
	
	public static void solvePart2(File file, long xyMax) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		
		XY solution = null;
		for (long y=0; y<=xyMax; y++) {
			XY xy = puzzle.findUncoveredPointInRow(y, xyMax);
			if (xy != null) {
				solution = xy;
				break;
			}
		}
		
		long freq = solution.x * 4000000  + solution.y;
		System.out.println("Solution = " + solution.toString() + " (tuning frequency = " + freq + ")");
	}
	
	public static void main(String [] args) {
		try {
			//testParse();
			
			//File testFile  = new File("files/day15/test.txt");
			File inputFile = new File("files/day15/input.txt");
			
			//solvePart1(testFile, 10, true);
			//solvePart1(inputFile, 2000000, false);
			
			//solvePart2(testFile, 20);
			solvePart2(inputFile, 4000000);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
