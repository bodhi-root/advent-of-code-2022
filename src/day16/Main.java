package day16;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.FileUtil;

public class Main {
	
	static class Valve {
	
		String id;
		int rate;
		List<Valve> tunnelsTo = new ArrayList<>();
		
		public Valve(String id, int rate) {
			this.id = id;
			this.rate = rate;
		}
		
	}
	
	static class Puzzle {
		
		Map<String, Valve> valves = new HashMap<>();
		
		public void add(Valve valve) {
			valves.put(valve.id, valve);
		}
		public Valve getValve(String id) {
			return valves.get(id);
		}
		
		public State createInitState(int numPlayers) {
			State state = new State();
			
			Valve start = valves.get("AA");
			for (int i=0; i<numPlayers; i++)
				state.addPlayer(new Player(start));
			
			state.valves.putAll(this.valves);
			return state;
		}
		
		public static Puzzle loadFrom(File file) throws IOException {
			Puzzle puzzle = new Puzzle();
			
			//Valve AA has flow rate=0; tunnels lead to valves PW, ZT, XM, SK, HR
			//Valve HH has flow rate=22; tunnel leads to valve GG
			List<String> lines = FileUtil.readLinesFromFile(file);
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				String [] leftRight = line.split(";");
				String [] leftParts = leftRight[0].split("\\s+");
				String id = leftParts[1];
				int flowRate = Integer.parseInt(leftParts[4].substring(5));
				Valve valve = new Valve(id, flowRate);
				puzzle.add(valve);
			}
			
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				String [] leftRight = line.split(";");
				
				//get Valve:
				String [] leftParts = leftRight[0].split("\\s+");
				String id = leftParts[1];
				Valve valve = puzzle.getValve(id);
				if (valve == null)
					throw new IOException("Valve not found: " + id + " '" + line + "'");
				
				//get connections:
				String [] rightParts = leftRight[1].trim().split("\\s+");
								
				for (int i=4; i<rightParts.length; i++) {
					String connectId = rightParts[i];
					if (connectId.endsWith(","))
						connectId = connectId.substring(0, connectId.length()-1);
					
					Valve connect = puzzle.getValve(connectId);
					if (connect == null)
						throw new IOException("Valve not found: " + connectId + " '" + line + "'");
					
					valve.tunnelsTo.add(connect);
				}
				
			}
			
			return puzzle;
		}
		
	}
	
	static interface StateVisitor {
		
		public boolean visit(State state);
		
	}
	
	static class Solver implements StateVisitor {
		
		State bestLeaf = null;
		Map<String, State> statesAndScores = new HashMap<>(1000000);
		int finalT;
		
		public Solver(int finalT) {
			this.finalT = finalT;
		}
		
		public boolean visit(State state) {
			
			if (state.t == this.finalT) {
				if (bestLeaf == null || state.releasedPressure > bestLeaf.releasedPressure) {
					bestLeaf = state.copy();
					System.out.println("New best: " + state.releasedPressure);
				}
				return false;
			}
			
			String stateId = state.getCanonicalStateText();
			
			int score = state.getMinFinalScore(this.finalT);
			State bestState = statesAndScores.get(stateId);
			if (bestState == null) {
				statesAndScores.put(stateId, state.copy());
				return true;
			} else {
				int bestScore = bestState.getMinFinalScore(this.finalT);
				//if (score > bestScore || (score == bestScore && state.t < bestState.t)) {
				if (score >= bestScore) {
					statesAndScores.put(stateId, state.copy());
					return true;
				}
				return false;
			}
		}
		
	}
	
	static interface Action {
		
	}
	
	static class OpenValve implements Action {
		
		Valve valve;
		
		public OpenValve(Valve valve) {
			this.valve = valve;
		}
		
	}
	
	static class MoveToValve implements Action {
		
		final Valve src;
		final Valve dst;
		
		public MoveToValve(Valve src, Valve dst) {
			this.src = src;
			this.dst = dst;
		}
		
	}
	
	static class Player {
		
		Valve location;
		List<Action> actions = new ArrayList<>();
		
		public Player(Valve location) {
			this.location = location;
		}
		
		public Action removeLastAction() {
			return actions.remove(actions.size()-1);
		}
		
		public Player copy() {
			Player copy = new Player(location);
			copy.actions.addAll(this.actions);
			return copy;
		}
		
	}
	
	static class State {
		
		List<Player> players = new ArrayList<>();
		Map<String, Valve> valves = new HashMap<>();
		Set<Valve> openedValves = new HashSet<>();
		
		int t = 0;
		int releasedPressure = 0;
				
		public void addPlayer(Player player) {
			this.players.add(player);
		}
		
		public boolean isOpen(Valve valve) {
			return openedValves.contains(valve);
		}
		
		public void tick() {
			this.t++;
			for (Valve valve : openedValves)
				this.releasedPressure += valve.rate;
		}
		
		public void untick() {
			this.t--;
			for (Valve valve : openedValves)
				this.releasedPressure -= valve.rate;
		}
		
		public void apply(Action action, int playerIndex) {	
			Player player = players.get(playerIndex);
			player.actions.add(action);
			
			if (action instanceof OpenValve) {
				OpenValve ov = (OpenValve)action;
				openedValves.add(ov.valve);
			}
			else if (action instanceof MoveToValve) {
				MoveToValve mv = (MoveToValve)action;
				player.location = mv.dst;
			}
		}
		
		public void unapplyLastAction(int playerIndex) {
			Player player = players.get(playerIndex);
			Action action = player.removeLastAction();
			
			if (action instanceof OpenValve) {
				OpenValve ov = (OpenValve)action;
				openedValves.remove(ov.valve);
			}
			else if (action instanceof MoveToValve) {
				MoveToValve mv = (MoveToValve)action;
				player.location = mv.src;
			}
		}
		
		/**
		 * Get the maximum possible score we could obtain
		 * from the given state when we reach the terminal
		 * number of steps.  This is equal to the current value
		 * plus the flow we'd get if all valves turned on
		 * instantly and stayed on until completion.
		 */
		public int getMaxFinalScore(int steps) {
			int stepsRemaining = steps - t;
			int score = this.releasedPressure;
			
			for (Valve valve : valves.values())
				score += valve.rate * stepsRemaining;
			
			return score;
		}
		public int getMinFinalScore(int steps) {
			int stepsRemaining = steps - t;
			int score = this.releasedPressure;
			
			for (Valve valve : openedValves)
				score += valve.rate * stepsRemaining;
			
			return score;
		}
		
		public List<Action> getNextSteps(int playerIndex) {
			Player player = players.get(playerIndex);
			Valve location = player.location;
			
			List<Action> actions = new ArrayList<>();
			if (!openedValves.contains(location) && location.rate > 0)
				actions.add(new OpenValve(location));
			for (Valve nextValve : location.tunnelsTo)
				actions.add(new MoveToValve(location, nextValve));
			
			return actions;
		}
		
		public State copy() {
			State copy = new State();
			for (Player player : players)
				copy.addPlayer(player.copy());
			copy.valves.putAll(this.valves);
			copy.openedValves.addAll(this.openedValves);
			copy.t = this.t;
			copy.releasedPressure = this.releasedPressure;
			return copy;
		}
		
		public String getCanonicalStateText() {
			StringBuilder s = new StringBuilder();
			List<String> playerLocIds = new ArrayList<>();
			for (Player player : players)
				playerLocIds.add(player.location.id);
			playerLocIds.sort(Comparator.naturalOrder());
			
			for (int i=0; i<playerLocIds.size(); i++) {
				if (i > 0)
					s.append(',');
				s.append(playerLocIds.get(i));
			}
			s.append(':');
						
			List<String> openIds = new ArrayList<>();
			for (Valve valve : openedValves)
				openIds.add(valve.id);
			openIds.sort(Comparator.naturalOrder());
			
			for (int i=0; i<openIds.size(); i++) {
				if (i > 0)
					s.append(',');
				s.append(openIds.get(i));
			}
			
			return s.toString();
		}
		
		public void visitChildrenDepthFirstPart1(StateVisitor visitor) {
			
			int playerIndex = 0;	//only 1 player
			
			List<Action> actions = getNextSteps(playerIndex);
			sortActions(actions);
			for (Action action : actions) {
				tick();
				apply(action, playerIndex);
				if (visitor.visit(this))
					visitChildrenDepthFirstPart1(visitor);
				unapplyLastAction(playerIndex);
				untick();
			}

		}
		
		public static void visitChildrenBreadthFirstPart1(List<State> states, StateVisitor visitor) {
				
			System.out.println("t = " + states.get(0).t + ", size = " + states.size());
			
			int playerIndex = 0;
			List<State> nextStates = new ArrayList<>();
			
			for (State state : states) {
				
				List<Action> actions = state.getNextSteps(playerIndex);
				for (Action action : actions) {
					State nextState = state.copy();
					nextState.tick();
					nextState.apply(action, 0);
					if (visitor.visit(nextState))
						nextStates.add(nextState);
				}

			}
			
			if (!nextStates.isEmpty())
				visitChildrenBreadthFirstPart1(nextStates, visitor);
		}
		
		public void visitChildrenDepthFirstPart2(StateVisitor visitor) {
			
			String locationId1 = players.get(0).location.id;
			String locationId2 = players.get(1).location.id;
			
			//get actions lists for both players:
			List<Action> actions1 = getNextSteps(0);
			sortActions(actions1);
			
			List<Action> actions2 = getNextSteps(1);
			
			//try all (valid) action combinations:
			for (Action action1 : actions1) {
				
				this.tick();
				this.apply(action1, 0);
				this.sortActions(actions2);
				
				for (Action action2 : actions2) {
					
					if (locationId1.equals(locationId2)) {
						
						//don't let player2 open valve, just have player 1 do it
						//there will be at least one combination of player 1 opening
						//the valve and player 2 moving.
						if (action2 instanceof OpenValve)
							continue;
						
						//if starting in the same location, reduce the number
						//of next states by enforcing player1.location.id <= player2.location.id
						else if (action1 instanceof MoveToValve) {
							MoveToValve mv1 = (MoveToValve)action1;
							MoveToValve mv2 = (MoveToValve)action2;
							if (mv1.dst.id.compareTo(mv2.dst.id) > 0)
								continue;
						}
						
					}
					
					this.apply(action2, 1);
					if (visitor.visit(this))
						this.visitChildrenDepthFirstPart2(visitor);
					this.unapplyLastAction(1);
				}
				this.unapplyLastAction(0);
				this.untick();
			}
			
		}
		
		public static void visitChildrenBreadthFirstPart2(List<State> states, StateVisitor visitor) {
			
			System.out.println("t = " + states.get(0).t + ", size = " + states.size());
			
			Map<String, State> nextStates = new HashMap<>(100000);
			
			for (State state : states) {
				
				String locationId1 = state.players.get(0).location.id;
				String locationId2 = state.players.get(1).location.id;
				
				List<Action> actions1 = state.getNextSteps(0);
				List<Action> actions2 = state.getNextSteps(1);
				state.sortActions(actions1);
				
				//try all (valid) action combinations:
				for (Action action1 : actions1) {	
					
					state.tick();
					state.apply(action1, 0);
					
					state.sortActions(actions2);
					
					for (Action action2 : actions2) {
						
						if (locationId1.equals(locationId2)) {
							
							//don't let player2 open valve, just have player 1 do it
							if (action2 instanceof OpenValve)
								continue;
							
							//if starting in the same location, reduce the number
							//of next states by enforcing player1.location.id <= player2.location.id
							else if (action1 instanceof MoveToValve) {
								MoveToValve mv1 = (MoveToValve)action1;
								MoveToValve mv2 = (MoveToValve)action2;
								if (mv1.dst.id.compareTo(mv2.dst.id) > 0)
									continue;
							}
							
						}
						
						
						state.apply(action2, 1);
						
						String stateId = state.getCanonicalStateText();
						int score = state.releasedPressure;
						
						State bestNextState = nextStates.get(stateId);
						if (bestNextState == null || score > bestNextState.releasedPressure) {
							nextStates.put(stateId, state.copy());
						}
						
						state.unapplyLastAction(1);
					}
					
					state.unapplyLastAction(0);
					state.untick();
				}
				
			}
			
			System.out.println(" Map size: " + nextStates.size());
			
			//visit everyone:
			List<State> nextStateList = new ArrayList<>(nextStates.size());
			
			for (State nextState : nextStates.values()) {
				if (visitor.visit(nextState))
					nextStateList.add(nextState);
			}
			nextStates.clear();
			
			System.out.println(" List size: " + nextStateList.size());
			
			if (!nextStateList.isEmpty())
				visitChildrenBreadthFirstPart2(nextStateList, visitor);
		}
		
		protected void sortActions(List<Action> actions) {
			
			if (actions.size() <= 1)
				return;
			
			//score actions:
			List<ScoredAction> scoredList = new ArrayList<>();
			for (Action action : actions) {
				double score = scoreAction(action);
				scoredList.add(new ScoredAction(action, score));
			}
			
			//sort list (highest score to top):
			scoredList.sort(new Comparator<ScoredAction>() {

				@Override
				public int compare(ScoredAction o1, ScoredAction o2) {
					return Double.compare(o2.score, o1.score);
				}
				
			});
			
			//add sorted actions back to original list
			actions.clear();
			for (ScoredAction action : scoredList)
				actions.add(action.action);
		}
		
		protected double scoreAction(Action action) {
			
			double score = 0;
			int steps = 0;
			
			if (action instanceof OpenValve) {
				OpenValve ov = (OpenValve)action;
				steps++;
				
				if (!isOpen(ov.valve))
					score += ov.valve.rate;
				
				score += scoreNextBestMove(ov.valve, null);
				steps += 2;
			} else {
				MoveToValve mv = (MoveToValve)action;
				steps++;
				
				if (!isOpen(mv.dst)) {
					 score += mv.dst.rate;
					 steps++;
				}
				
				score += scoreNextBestMove(mv.dst, mv.src);
				steps += 2;
			}
			
			return score / (double)steps;
		}
		
		protected double scoreNextBestMove(Valve start, Valve avoid) {
			Valve best = null;
			for (Valve next : start.tunnelsTo) {
				if (avoid != null && best == avoid)
					continue;
				
				if (!isOpen(next) && (best == null || next.rate > best.rate))
					best = next;
			}
			
			return best == null ? 0 : best.rate;
		}
		
	}
	
	static class ScoredAction {
		
		Action action;
		double score;
		
		public ScoredAction(Action action, double score) {
			this.action = action;
			this.score = score;
		}
		
	}
	
	public static void solvePart1(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		State state = puzzle.createInitState(1);
		
		Solver solver = new Solver(30);
		solver.visit(state);
		
		//NOTE: both depth first and breadth first can solve our puzzle in less than 20 seconds
		//i think breadth-first is a little faster.
		
		//state.visitChildrenDepthFirstPart1(solver);
		
		List<State> states = new ArrayList<>();
		states.add(state);
		State.visitChildrenBreadthFirstPart1(states, solver);
		
		System.out.println("BEST: " + solver.bestLeaf.releasedPressure);
	}
	
	public static void solvePart2(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		State state = puzzle.createInitState(2);
		
		Solver solver = new Solver(26);
		solver.visit(state);
		
		//depth first solves test problem but not bigger one:
		
		//state.visitChildrenDepthFirstPart2(solver);
		
		List<State> states = new ArrayList<>();
		states.add(state);
		State.visitChildrenBreadthFirstPart2(states, solver);
		
		if (solver.bestLeaf == null)
			System.out.println("ERROR: No solution found");
		else
			System.out.println("BEST: " + solver.bestLeaf.releasedPressure);
	}
	
	public static void main(String [] args) {
		try {
			//System.out.println(66 * 66 / 2 * Math.pow(2, 15));
			
			File testFile = new File("files/day16/test.txt");
			//solvePart1(testFile);
			//solvePart2(testFile);
			
			File inputFile = new File("files/day16/input.txt");
			//solvePart1(inputFile);
			solvePart2(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
