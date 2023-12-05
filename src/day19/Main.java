package day19;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.FileUtil;


//4:17 - 6:45
//10:32 -
public class Main {
	
	public static final String ORE = "ORE";
	public static final String CLAY = "CLAY";
	public static final String OBSIDIAN = "OBSIDIAN";
	public static final String GEODE = "GEODE";
	
	public static final String ORE_ROBOT = "ORE_ROBOT";
	public static final String CLAY_ROBOT = "CLAY_ROBOT";
	public static final String OBSIDIAN_ROBOT = "OBSIDIAN_ROBOT";
	public static final String GEODE_ROBOT = "GEODE_ROBOT";
	
	static class Counter {
		
		int value;
		
		public Counter(int value) {
			this.value = value;
		}
		public Counter() {
			this(0);
		}
		
		public void add(int x) {
			this.value += x;
		}
		public void subtract(int x) {
			this.value -= x;
		}
		
		public Counter copy() {
			return new Counter(value);
		}
		
	}
	
	static class Resources {
		
		Map<String, Counter> map = new HashMap<>();
		
		public Counter getCounter(String key) {
			Counter counter = map.get(key);
			if (counter == null) {
				counter = new Counter();
				map.put(key, counter);
			}
			return counter;
		}
		
		public int add(String key, int value) {
			Counter counter = getCounter(key);
			counter.add(value);
			return counter.value;
		}
		public int subtract(String key, int value) {
			return add(key, -value);
		}
		public int getValue(String key) {
			return getCounter(key).value;
		}
		
		public Resources copy() {
			Resources copy = new Resources();
			for (Map.Entry<String, Counter> entry : this.map.entrySet())
				copy.map.put(entry.getKey(), entry.getValue().copy());
			return copy;
		}
		
	}
	
	static class BuildRequest {
		
		int oreRobots;
		int clayRobots;
		int obsidianRobots;
		int geodeRobots;
		
		public static final BuildRequest NONE               = new BuildRequest(0, 0, 0, 0);
		public static final BuildRequest ONE_ORE_ROBOT      = new BuildRequest(1, 0, 0, 0);
		public static final BuildRequest ONE_CLAY_ROBOT     = new BuildRequest(0, 1, 0, 0);
		public static final BuildRequest ONE_OBSIDIAN_ROBOT = new BuildRequest(0, 0, 1, 0);
		public static final BuildRequest ONE_GEODE_ROBOT    = new BuildRequest(0, 0, 0, 1);
		
		public BuildRequest(int ore, int clay, int obsidian, int geode) {
			this.oreRobots = ore;
			this.clayRobots = clay;
			this.obsidianRobots = obsidian;
			this.geodeRobots = geode;
		}
		
		public boolean isValid(Resources resources, Blueprint bp) {
			
			int ore = resources.getValue(ORE);
			int clay = resources.getValue(CLAY);
			int obsidian = resources.getValue(OBSIDIAN);
			
			return  !(
					ore < bp.oreForOreRobot * oreRobots ||
					ore < bp.oreForClayRobot * clayRobots ||
					ore < bp.oreForObsidianRobot * obsidianRobots ||
					ore < bp.oreForGeodeRobot * geodeRobots ||
					clay < bp.clayForObsidianRobot * obsidianRobots ||
					obsidian < bp.obsidianForGeodeRobot * geodeRobots
					);
		}
		
		public void apply(Resources resources, Blueprint bp) {
			
			//start building:
			if (geodeRobots > 0) {
				resources.subtract(OBSIDIAN, bp.obsidianForGeodeRobot * geodeRobots);
				resources.subtract(ORE, bp.oreForGeodeRobot * geodeRobots);
			}
			if (obsidianRobots > 0) {
				resources.subtract(CLAY, bp.clayForObsidianRobot * obsidianRobots);
				resources.subtract(ORE, bp.oreForObsidianRobot * obsidianRobots);
			}
			if (clayRobots > 0) {
				resources.subtract(ORE, bp.oreForClayRobot * clayRobots);
			}
			if (oreRobots > 0) {
				resources.subtract(ORE, bp.oreForOreRobot * oreRobots);
			}
			
			//mine resources:
			resources.add(ORE, resources.getValue(ORE_ROBOT));
			resources.add(CLAY, resources.getValue(CLAY_ROBOT));
			resources.add(OBSIDIAN, resources.getValue(OBSIDIAN_ROBOT));
			resources.add(GEODE, resources.getValue(GEODE_ROBOT));
			
			//finish builds:
			resources.add(ORE_ROBOT, oreRobots);
			resources.add(CLAY_ROBOT, clayRobots);
			resources.add(OBSIDIAN_ROBOT, obsidianRobots);
			resources.add(GEODE_ROBOT, geodeRobots);
		}
		
		public String toString() {
			return new StringBuilder()
					.append("(").append(oreRobots)
					.append(", ").append(clayRobots)
					.append(", ").append(obsidianRobots)
					.append(", ").append(geodeRobots)
					.append(")")
					.toString();
		}
		
	}
	
	static class Blueprint {
		
		int number;
		
		int oreForOreRobot;
		int oreForClayRobot;
		int oreForObsidianRobot;
		int oreForGeodeRobot;
		
		int clayForObsidianRobot;
		int obsidianForGeodeRobot;
		
		public int getBestOutcome(Resources resources, int finalT) {
			Part1Solver solver = new Part1Solver();
			exploreOutcomes(resources, BuildRequest.NONE, 1, finalT, solver);
			return solver.best;
		}
		
		protected void exploreOutcomes(Resources resources, BuildRequest request, int t, int finalT, Visitor visitor) {
		
			//System.out.println("t=" + t + ", request=" + request.toString());
			
			resources = resources.copy();
			request.apply(resources, this);
			//System.out.println("  resources=" + print(resources));
			
			if (!visitor.visit(this, resources, t, finalT))
				return;
			
			List<BuildRequest> newRequests = new ArrayList<>();
			
			int ore = resources.getValue(ORE);
			int clay = resources.getValue(CLAY);
			int obsidian = resources.getValue(OBSIDIAN);
			
			boolean canBuildGeode = obsidian >= obsidianForGeodeRobot && ore >= oreForGeodeRobot;
			boolean canBuildObsidian = clay >= clayForObsidianRobot && ore >= oreForObsidianRobot;
			boolean canBuildClay = ore >= oreForClayRobot;
			boolean canBuildOre = ore >= oreForOreRobot;
			
			//close: but i think we're missing cases where we need to
			//either switch the order build 2 at once.
			if (canBuildGeode)
				newRequests.add(BuildRequest.ONE_GEODE_ROBOT);
			if (canBuildObsidian)
				newRequests.add(BuildRequest.ONE_OBSIDIAN_ROBOT);
			if (canBuildClay)
				newRequests.add(BuildRequest.ONE_CLAY_ROBOT);
			if (canBuildOre) 
				newRequests.add(BuildRequest.ONE_ORE_ROBOT);
			
			
			/*
			if (canBuildGeode)
				newRequests.add(BuildRequest.ONE_GEODE_ROBOT);
			else {
				if (canBuildObsidian)
					newRequests.add(BuildRequest.ONE_OBSIDIAN_ROBOT);
				if (canBuildClay)
					newRequests.add(BuildRequest.ONE_CLAY_ROBOT);
				if (canBuildOre) 
					newRequests.add(BuildRequest.ONE_ORE_ROBOT);
			}
			*/
			
			/*
			//creates too many options (mostly ore and clay combos)
			for (int oreRobots=0; oreRobots<=1; oreRobots++) {
				for (int clayRobots=0; clayRobots<=1; clayRobots++) {
					for (int obsidianRobots=0; obsidianRobots<=1; obsidianRobots++) {
						for (int geodeRobots=0; geodeRobots<=1; geodeRobots++) {
							if (!(oreRobots == 0 && clayRobots == 0 && obsidianRobots == 0 && geodeRobots == 0)) {
								BuildRequest newRequest = new BuildRequest(oreRobots, clayRobots, obsidianRobots, geodeRobots);
								if (request.isValid(resources, this))
									newRequests.add(newRequest);
							}
						}
					}
				}
			}
			*/
			
			/*
			if (canBuildGeode) {
				newRequests.add(BuildRequest.ONE_GEODE_ROBOT);
			} 
			else if (canBuildObsidian) {
				newRequests.add(BuildRequest.ONE_OBSIDIAN_ROBOT);
			}
			else {
				for (int oreRobots=0; oreRobots<=1; oreRobots++) {
					for (int clayRobots=0; clayRobots<=1; clayRobots++) {
						if (!(oreRobots == 0 && clayRobots == 0)) {
							BuildRequest newRequest = new BuildRequest(oreRobots, clayRobots, 0, 0);
							if (request.isValid(resources, this))
								newRequests.add(newRequest);
						}
					}
				}
			}
			*/
			
			/*
			int oreRobots = resources.getValue(ORE_ROBOT);
			int clayRobots = resources.getValue(CLAY_ROBOT);
			int obsidianRobots = resources.getValue(OBSIDIAN_ROBOT);
			
			if (canBuildGeode) {
				newRequests.add(BuildRequest.ONE_GEODE_ROBOT);
			}
			else {
				double stepsToGeode = Integer.MAX_VALUE;
				if ()
			}
			else if (resources.getValue(CLAY_ROBOT) == 0) {
				
				double stepsToClay = oreForClayRobot / (double)oreRobots;
				double steps2 = (oreForClayRobot - oreForOreRobot + 1) / (double)oreRobots - 1;
				if (steps2 > stepsToClay) {
					newRequests.add(BuildRequest.ONE_CLAY_ROBOT);
				}
			}
			else if (resources.getValue(OBSIDIAN_ROBOT) == 0) {
				double stepsToObsidian = Math.max(
						oreForObsidianRobot / (double)ore, 
						clayForObsidianRobot / (double)clay);
				
			}
			else {
				
			}
			*/
			
			
			/*
			if (canBuildGeode) {
				newRequests.add(BuildRequest.ONE_GEODE_ROBOT);
			}
			else {
				
				int timeToGeode = Integer.MAX_VALUE;
				
				
				if (resources.getValue(OBSIDIAN_ROBOT) == 0) {
					
					if (resources.getValue(CLAY_ROBOT) == 0) {
						
					}
					
					int oreDiff = oreForObsidianRobot - ore;
					int clayDiff = clayForObsidianRobot - clay;
					
					
					
				}
				
				if (canBuildObsidian && resources.getValue(OBSIDIAN_ROBOT) == 0) {
					
				}
			}
			*/
			
			//if (newRequests.isEmpty())
			//	newRequests.add(BuildRequest.NONE);
			
			//if you can build all 4 robots: force them to build 1
			//if (newRequests.size() < 4)
			//	newRequests.add(BuildRequest.NONE);
			
			int maxOre = Math.max(Math.max(Math.max(oreForOreRobot, oreForClayRobot), oreForObsidianRobot), oreForGeodeRobot);
			int dt = finalT - t;
			if (newRequests.isEmpty() || (
					ore < maxOre * dt &&
					clay < clayForObsidianRobot * dt &&
					obsidian < obsidianForGeodeRobot * dt
					))
				newRequests.add(BuildRequest.NONE);
			
			for (BuildRequest next : newRequests) {
				exploreOutcomes(resources, next, t+1, finalT, visitor);
			}
		}
		
		public String toString() {
			return new StringBuilder()
					.append("oreForOreRobot: ").append(oreForOreRobot)
					.append(", oreForClayRoobot: ").append(oreForClayRobot)
					.append(", oreForObsidianRobot: ").append(oreForObsidianRobot)
					.append(", oreForGeodeRobot: ").append(oreForGeodeRobot)
					.append(", clayForObsidianRobot: ").append(clayForObsidianRobot)
					.append(", obsidianForGeodeRobot: ").append(obsidianForGeodeRobot)
					.toString();
		}
		
	}
	
	static interface Visitor {
		
		boolean visit(Blueprint bp, Resources resources, int t, int finalT);
		
	}
	
	static class Part1Solver implements Visitor {
		
		int best = -1;
		
		public boolean visit(Blueprint bp, Resources resources, int t, int finalT) {
			if (t == finalT) {
				int geodes = resources.getValue(GEODE);
				//System.out.println("Leaf: " + geodes);
				this.best = Math.max(best, geodes);
				//System.out.println("Best: " + best);
				return false;
			} else {
				if (best < 0)
					return true;
				
				//estimate max geodes we can get from this path
				//we'll use our current geode count + what we will mine from our current geode robot count
				int tRemaining = finalT - t;
				int geodeRobots = resources.getValue(GEODE_ROBOT);
				int maxGeodes = resources.getValue(GEODE) + (tRemaining * geodeRobots);

				//if we don't have a geode robot, estimate how long it will take to get one:
				/* //no, i don't think code below is right
				if (geodeRobots == 0) {
					int timeToRobot = Math.max(Math.max(
						bp.obsidianForGeodeRobot - resources.getValue(OBSIDIAN),
						bp.oreForGeodeRobot - resources.getValue(ORE)
					), 0);
				}
				*/

				while (tRemaining > 1) {
					maxGeodes += tRemaining;
					tRemaining--;
				}
				return maxGeodes > best;
			}
		}
		
	}
	
	static Pattern INPUT_PATTERN = Pattern.compile(
			"Blueprint (?<num>\\d+):" +
	        " Each ore robot costs (?<oreForOre>\\d+) ore." +
			" Each clay robot costs (?<oreForClay>\\d+) ore." +
	        " Each obsidian robot costs (?<oreForObsidian>\\d+) ore and (?<clayForObsidian>\\d+) clay." +
			" Each geode robot costs (?<oreForGeode>\\d+) ore and (?<obsidianForGeode>\\d+) obsidian."
			);
	
	/**
	 * Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 4 ore. Each obsidian robot costs 3 ore and 19 clay. Each geode robot costs 4 ore and 15 obsidian.
	 */
	public static Blueprint parseBlueprint(String text) {
		Matcher matcher = INPUT_PATTERN.matcher(text);
		if (!matcher.find())
			throw new IllegalArgumentException("Invalid input: " + text);
		
		Blueprint bp = new Blueprint();
		bp.number = Integer.parseInt(matcher.group("num"));
		bp.oreForOreRobot = Integer.parseInt(matcher.group("oreForOre"));
		bp.oreForClayRobot = Integer.parseInt(matcher.group("oreForClay"));
		bp.oreForObsidianRobot = Integer.parseInt(matcher.group("oreForObsidian"));
		bp.oreForGeodeRobot = Integer.parseInt(matcher.group("oreForGeode"));
		bp.clayForObsidianRobot = Integer.parseInt(matcher.group("clayForObsidian"));
		bp.obsidianForGeodeRobot = Integer.parseInt(matcher.group("obsidianForGeode"));
		return bp;
	}
	
	public static List<Blueprint> loadInput(File file) throws IOException {
		List<Blueprint> list = new ArrayList<>();
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (String line : lines)
			list.add(parseBlueprint(line));
		return list;
	}
	
	public static void testInput() {
		String text = "Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.";
		Blueprint bp = parseBlueprint(text);
		System.out.println(bp.toString());
		
		Resources resources = new Resources();
		resources.add(ORE_ROBOT, 1);
		
		int best = bp.getBestOutcome(resources, 24);
		System.out.println(best);
	}
	
	public static String print(Resources resources) {
		StringBuilder s = new StringBuilder();
		s.append("(ore, clay, obsidian, geode) = (")
		 .append(resources.getValue(ORE))
		 .append(", ").append(resources.getValue(CLAY))
		 .append(", ").append(resources.getValue(OBSIDIAN))
		 .append(", ").append(resources.getValue(GEODE))
		 .append(") robots = (")
		 .append(resources.getValue(ORE_ROBOT))
		 .append(", ").append(resources.getValue(CLAY_ROBOT))
		 .append(", ").append(resources.getValue(OBSIDIAN_ROBOT))
		 .append(", ").append(resources.getValue(GEODE_ROBOT))
		 .append(")");
		return s.toString();
	}
	
	public static void solvePart1(File file) throws Exception {
		List<Blueprint> blueprints = loadInput(file);
		
		long sum = 0;
		for (Blueprint bp : blueprints) {
			Resources resources = new Resources();
			resources.add(ORE_ROBOT, 1);
			
			int quality = bp.getBestOutcome(resources, 24);
			System.out.println("Blueprint " + bp.number + ": " + quality);	
			sum += quality * bp.number;
		}
		
		System.out.println("Sum: " + sum);
	}
	
	public static void solvePart2(File file) throws Exception {
		List<Blueprint> blueprints = loadInput(file);
		
		long product = 1;
		for (int i=0; i<3; i++) {
			Blueprint bp = blueprints.get(i);
			Resources resources = new Resources();
			resources.add(ORE_ROBOT, 1);
			
			int quality = bp.getBestOutcome(resources, 32);
			System.out.println("Blueprint " + bp.number + ": " + quality);	
			product *= quality;
		}
		
		System.out.println("Product: " + product);
	}
	
	public static void main(String [] args) {
		try {
			//testInput();
			
			File testFile = new File("files/day19/test.txt");
			//solvePart1(testFile);
			//solvePart2();

			File inputFile = new File("files/day19/input.txt");
			//solvePart1(inputFile);
			solvePart2(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
