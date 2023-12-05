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

public class Main2 {
	
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
		
		public State createInitState() {
			Valve location = valves.get("AA");
			State state = new State(location);
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
		
		public void visitStatesDepthFirst(StateVisitor visitor) {
			State state = createInitState();
			visitor.visit(state);
			visitChildrenDepthFirst(state, visitor);
		}
		
		protected void visitChildrenDepthFirst(State state, StateVisitor visitor) {
							
			List<Action> actions = state.getNextSteps();
			for (Action action : actions) {
				State nextState = state.copy();
				nextState.apply(action);
				if (visitor.visit(nextState))
					visitChildrenDepthFirst(nextState, visitor);
			}

		}
		
	}
	
	static interface StateVisitor {
		
		public boolean visit(State state);
		
	}
	
	static class Solver implements StateVisitor {
		
		State bestLeaf = null;
		Map<String, Integer> statesAndScores = new HashMap<>();
		
		public boolean visit(State state) {
			if (state.getActionCount() == 30) {
				if (bestLeaf == null || state.releasedPressure > bestLeaf.releasedPressure) {
					bestLeaf = state;
					System.out.println("New best: " + state.releasedPressure);
				}
				return false;
			}
			
			String stateId = state.getCanonicalStateText();
			int maxFinalScore = state.getMaxFinalScore(30);
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
	
	static class State {
		
		Valve location;
		List<Action> actions = new ArrayList<>();
		
		Map<String, Valve> valves = new HashMap<>();
		Set<Valve> openedValves = new HashSet<>();
		int releasedPressure = 0;
				
		public State(Valve location) {
			this.location = location;
		}
		
		public void apply(Action action) {
			this.actions.add(action);
			
			for (Valve valve : openedValves)
				this.releasedPressure += valve.rate;
			
			if (action instanceof OpenValve) {
				OpenValve ov = (OpenValve)action;
				openedValves.add(ov.valve);
			}
			else if (action instanceof MoveToValve) {
				MoveToValve mv = (MoveToValve)action;
				this.location = mv.valve;
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
			int stepsRemaining = steps - actions.size();
			int score = this.releasedPressure;
			
			for (Valve valve : valves.values())
				score += valve.rate * stepsRemaining;
			
			return score;
		}
		
		public int getActionCount() {
			return actions.size();
		}
		
		public List<Action> getNextSteps() {
			List<Action> actions = new ArrayList<>();
			if (!openedValves.contains(location) && location.rate > 0)
				actions.add(new OpenValve(location));
			for (Valve nextValve : location.tunnelsTo)
				actions.add(new MoveToValve(nextValve));
			
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
						return mv2.valve.rate - mv1.valve.rate;
					}	
				}
				
			});
			
			return actions;
		}
		
		public State copy() {
			State copy = new State(this.location);
			copy.actions.addAll(this.actions);
			copy.valves.putAll(this.valves);
			copy.openedValves.addAll(this.openedValves);
			copy.releasedPressure = this.releasedPressure;
			return copy;
		}
		
		public String getCanonicalStateText() {
			StringBuilder s = new StringBuilder();
			s.append(location.id);
						
			List<String> openIds = new ArrayList<>();
			for (Valve valve : openedValves)
				openIds.add(valve.id);
			openIds.sort(Comparator.naturalOrder());
			
			for (String id : openIds)
				s.append(",").append(id);
			
			return s.toString();
		}
		
	}
	
	public static void solvePart1(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		Solver solver = new Solver();
		//puzzle.visitStatesBreadthFirst(solver);
		puzzle.visitStatesDepthFirst(solver);
		System.out.println("BEST: " + solver.bestLeaf.releasedPressure);
	}
	
	public static void solvePart2() throws Exception {
		
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day16/test.txt");
			File file = new File("files/day16/input.txt");
			solvePart1(file);
			//solvePart2();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
