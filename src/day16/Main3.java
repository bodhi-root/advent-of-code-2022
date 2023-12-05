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

public class Main3 {
	
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
		
		/*
		public void visitStatesBreadthFirst(StateVisitor visitor) {
			State state = createInitState();
			visitor.visit(state);
			
			List<State> states = new ArrayList<>();
			states.add(state);
			visitChildrenBreadthFirst(states, visitor);
		}
		
		protected void visitChildrenBreadthFirst(List<State> states, StateVisitor visitor) {
				
			List<State> nextStates = new ArrayList<>();
			
			for (State state : states) {
				
				List<Action> actions = state.getNextSteps();
				for (Action action : actions) {
					State nextState = state.copy();
					nextState.apply(action);
					if (visitor.visit(nextState))
						nextStates.add(nextState);
				}

			}
			
			if (!nextStates.isEmpty())
				visitChildrenBreadthFirst(nextStates, visitor);
		}
		*/
		
		
		
	}
	
	static interface StateVisitor {
		
		public boolean visit(State state);
		
	}
	
	static class Solver implements StateVisitor {
		
		State bestLeaf = null;
		Map<String, Integer> statesAndScores = new HashMap<>();
		int finalT;
		
		public Solver(int finalT) {
			this.finalT = finalT;
		}
		
		public boolean visit(State state) {
			if (state.t == this.finalT) {
				if (bestLeaf == null || state.releasedPressure > bestLeaf.releasedPressure) {
					bestLeaf = state;
					System.out.println("New best: " + state.releasedPressure);
				}
				return false;
			}
			
			String stateId = state.getCanonicalStateText();
			int maxFinalScore = state.getMaxFinalScore(this.finalT);
			Integer bestMaxFinalScore = statesAndScores.get(stateId);
			if (bestMaxFinalScore == null || maxFinalScore > bestMaxFinalScore.intValue()) {
				statesAndScores.put(stateId, maxFinalScore);
				return true;
			} else {
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
		
		final Valve valve;
		
		public MoveToValve(Valve valve) {
			this.valve = valve;
		}
		
	}
	
	static class Player {
		
		Valve location;
		List<Action> actions = new ArrayList<>();
		
		public Player(Valve location) {
			this.location = location;
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
		
		public void tick() {
			this.t++;
			for (Valve valve : openedValves)
				this.releasedPressure += valve.rate;
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
				player.location = mv.valve;
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
		
		public List<Action> getNextSteps(int playerIndex) {
			Player player = players.get(playerIndex);
			Valve location = player.location;
			
			List<Action> actions = new ArrayList<>();
			if (!openedValves.contains(location) && location.rate > 0)
				actions.add(new OpenValve(location));
			for (Valve nextValve : location.tunnelsTo)
				actions.add(new MoveToValve(nextValve));
			
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
				State nextState = this.copy();
				nextState.tick();
				nextState.apply(action, playerIndex);
				if (visitor.visit(nextState))
					nextState.visitChildrenDepthFirstPart1(visitor);
			}

		}
		
		public void visitChildrenDepthFirstPart2(StateVisitor visitor) {
			
			//get actions lists for both players:
			List<Action> actions1 = getNextSteps(0);
			sortActions(actions1);
			
			List<Action> actions2 = getNextSteps(1);
			
			//try all (valid) action combinations:
			for (Action action1 : actions1) {
				
				State nextState1 = this.copy();
				nextState1.tick();
				nextState1.apply(action1, 0);
				nextState1.sortActions(actions2);
				
				for (Action action2 : actions2) {
					
					if (players.get(0).location.id.equals(players.get(1).location.id)) {
						
						//don't let player2 open valve, just have player 1 do it
						if (action2 instanceof OpenValve)
							continue;
						
						//if starting in the same location, reduce the number
						//of next states by enforcing player1.location.id <= player2.location.id
						else if (action1 instanceof MoveToValve) {
							MoveToValve mv1 = (MoveToValve)action1;
							MoveToValve mv2 = (MoveToValve)action2;
							if (mv1.valve.id.compareTo(mv2.valve.id) > 0)
								continue;
						}
						
					}
					
					State nextState2 = nextState1.copy();
					
					nextState2.apply(action2, 1);
					if (visitor.visit(nextState2))
						nextState2.visitChildrenDepthFirstPart2(visitor);
					
				}
			}
			
		}
		
		protected void sortActions(List<Action> actions) {
			//sort highest OpenValve before MoveToValve and then sort
			//by flow rate
			actions.sort(new Comparator<Action>() {

				@Override
				public int compare(Action o1, Action o2) {
					if (o1 instanceof OpenValve) {
						if (o2 instanceof OpenValve) {
							OpenValve ov1 = (OpenValve)o1;
							OpenValve ov2 = (OpenValve)o2;
							return ov2.valve.rate - ov1.valve.rate;
						} else {
							return -1;
						}
					} else if (o2 instanceof OpenValve) {
						return 1;
					} else {
						MoveToValve mv1 = (MoveToValve)o1;
						MoveToValve mv2 = (MoveToValve)o2;
						
						boolean open1 = openedValves.contains(mv1.valve);
						boolean open2 = openedValves.contains(mv2.valve);
						
						if (open1 == open2) {
							return mv2.valve.rate - mv1.valve.rate;
						} else if (open1) {
							return 1;
						} else {
							return -1;
						}
					}	
				}
				
			});
		}
		
	}
	
	public static void solvePart1(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		State state = puzzle.createInitState(1);
		Solver solver = new Solver(30);
		state.visitChildrenDepthFirstPart1(solver);
		System.out.println("BEST: " + solver.bestLeaf.releasedPressure);
	}
	
	public static void solvePart2(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		State state = puzzle.createInitState(2);
		Solver solver = new Solver(26);
		state.visitChildrenDepthFirstPart2(solver);
		System.out.println("BEST: " + solver.bestLeaf.releasedPressure);
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day16/test.txt");
			File file = new File("files/day16/input.txt");
			//solvePart1(file);
			solvePart2(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
