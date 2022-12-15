package day11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
	
	static class Item {
		
		long worry;
		
		public Item(long worry) {
			this.worry = worry;
		}
		
	}
	
	static class Monkey {
		
		List<Item> items = new ArrayList<>();
		MonkeyLogic logic;
		long inspectionCount = 0;
		
		public Monkey(int [] itemValues, MonkeyLogic logic) {
			for (int i=0; i<itemValues.length; i++)
				items.add(new Item(itemValues[i]));
			
			this.logic = logic;
		}
		
	}
	
	static class World {
		
		List<Monkey> monkeys = new ArrayList<>();
		
		public Monkey getMonkey(int index) {
			return monkeys.get(index);
		}
		
		public void addMonkey(Monkey monkey) {
			monkeys.add(monkey);
		}
		
		public void runRound(boolean part1, long modulus) {
			for (Monkey monkey : monkeys) {
				monkey.inspectionCount += monkey.items.size();
				monkey.logic.apply(this, monkey, part1, modulus);
			}
		}
		
		public long getMonkeyBusiness() {
			List<Monkey> sorted = new ArrayList<>();
			sorted.addAll(monkeys);
			sorted.sort(new Comparator<Monkey> () {

				@Override
				public int compare(Monkey o1, Monkey o2) {
					return Long.compare(o2.inspectionCount, o1.inspectionCount);
				}
				
			});
			
			return sorted.get(0).inspectionCount * sorted.get(1).inspectionCount;
		}
		
		public String getInspectionStateText() {
			StringBuilder s = new StringBuilder();
			for (int i=0; i<monkeys.size(); i++) {
				Monkey monkey = monkeys.get(i);
				s.append("Monkey " + i + ": ").append(monkey.inspectionCount).append("\n");
			}
			return s.toString();
		}
		
	}
	
	static abstract class MonkeyLogic {
		
		public void apply(World world, Monkey monkey, boolean part1, long modulus) {
			for (Item item : monkey.items) {
				item.worry = operation(item.worry);
				if (part1)
					item.worry = item.worry / 3;
				
				if (modulus > 0)
					item.worry = item.worry % modulus;
				
				int targetIdx = getTargetMonkey(item.worry);
				Monkey targetMonkey = world.getMonkey(targetIdx);
				targetMonkey.items.add(item);
			}
			monkey.items.clear();
		}
		
		protected abstract long operation(long old);
		
		protected abstract int getTargetMonkey(long value);
		
	}
	
	public static boolean isDivisible(long value, long divisor) {
		return value % divisor == 0;
	}
	
	public static void solvePart1() throws Exception {
		//World world = getTestWorld();
		World world = getInputWorld();
		for (int i=0; i<20; i++)
			world.runRound(true, 0);
		
		System.out.println(world.getMonkeyBusiness());
	}
	
	public static void solvePart2() throws Exception {
		//World world = getTestWorld();
		//long modulus = 23 * 19 * 13 * 17;
		
		World world = getInputWorld();
		long modulus = 19 * 5 * 11 * 17 * 7 * 13 * 3 * 2;
		
		for (int i=0; i<10000; i++)
			world.runRound(false, modulus);
		
		System.out.println(world.getInspectionStateText());

		System.out.println(world.getMonkeyBusiness());
	}
	
	public static void main(String [] args) {
		try {
			//solvePart1();
			solvePart2();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// --- Inputs -------------------------------------------------------------
	
	public static World getTestWorld() {
		World world = new World();
		
		world.addMonkey(new Monkey(
				new int [] {79, 98},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old * 19;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 23) ? 2 : 3;
					}
				}));
		
		world.addMonkey(new Monkey(
				new int [] {54, 65, 75, 74},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 6;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 19) ? 2 : 0;
					}
				}));
		
		world.addMonkey(new Monkey(
				new int [] {79, 60, 97},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old * old;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 13) ? 1 : 3;
					}
				}));
		
		world.addMonkey(new Monkey(
				new int [] {74},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 3;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 17) ? 0 : 1;
					}
				}));
		
		return world;
	}
	
	public static World getInputWorld() {
		World world = new World();
		
		//monkey 0:
		world.addMonkey(new Monkey(
				new int [] {91, 66},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old * 13;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 19) ? 6 : 2;
					}
				}));
		
		//monkey 1:
		world.addMonkey(new Monkey(
				new int [] {78, 97, 59},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 7;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 5) ? 0 : 3;
					}
				}));
		
		//monkey 2:
		world.addMonkey(new Monkey(
				new int [] {57, 59, 97, 84, 72, 83, 56, 76},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 6;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 11) ? 5 : 7;
					}
				}));
		
		//monkey 3:
		world.addMonkey(new Monkey(
				new int [] {81, 78, 70, 58, 84},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 5;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 17) ? 6 : 0;
					}
				}));
		
		//monkey 4:
		world.addMonkey(new Monkey(
				new int [] {60},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 8;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 7) ? 1 : 3;
					}
				}));
		
		//monkey 5:
		world.addMonkey(new Monkey(
				new int [] {57, 69, 63, 75, 62, 77, 72},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old * 5;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 13) ? 7 : 4;
					}
				}));
		
		//monkey 6:
		world.addMonkey(new Monkey(
				new int [] {73, 66, 86, 79, 98, 87},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old * old;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 3) ? 5 : 2;
					}
				}));
		
		//monkey 7:
		world.addMonkey(new Monkey(
				new int [] {95, 89, 63, 67},
				new MonkeyLogic() {
					protected long operation(long old) {
						return old + 2;
					}
					protected int getTargetMonkey(long value) {
						return isDivisible(value, 2) ? 1 : 4;
					}
				}));
		
		return world;
	}
	
}
