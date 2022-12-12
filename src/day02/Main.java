package day02;

import java.io.File;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static enum Action {ROCK, PAPER, SCISSORS};
	static enum Outcome {LOSE, DRAW, WIN};
	
	static Action ALL_ACTIONS [] = Action.values();
	static Outcome ALL_OUTCOMES [] = Outcome.values();
	
	static String [] XYZ_VALUES = new String [] {"X", "Y", "Z"};
	static String [] ABC_VALUES = new String [] {"A", "B", "C"};
	
	public static void solvePart1(File file) throws Exception {
		int totalScore = 0;
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (String line : lines) {
			String [] parts = line.split("\\s+");
			String a = parts[0];
			String b = parts[1];
			
			System.out.print(a + ":" + b);
			
			Action theirs = ALL_ACTIONS[indexOf(a, ABC_VALUES)];
			Action mine = ALL_ACTIONS[indexOf(b, XYZ_VALUES)];
			
			int score = score(mine, theirs);
			System.out.println("=" + score);
			totalScore += score;
		}
		
		System.out.println("Total Score: " + totalScore);
	}
	
	public static int score(Action mine, Action theirs) {
		int score = 0;
		if (mine == Action.ROCK) {
			score += 1;
			if (theirs == Action.ROCK)
				score += 3;
			else if (theirs == Action.SCISSORS)
				score += 6;
		}
		else if (mine == Action.PAPER) {
			score += 2;
			if (theirs == Action.PAPER)
				score += 3;
			else if (theirs == Action.ROCK)
				score += 6;
		}
		else if (mine == Action.SCISSORS) {
			score += 3;
			if (theirs == Action.SCISSORS)
				score += 3;
			else if (theirs == Action.PAPER)
				score += 6;
		}
		return score;
	}
	
	public static int indexOf(String txt, String [] values) {
		for (int i=0; i<values.length; i++) {
			if (values[i].equals(txt))
				return i;
		}
		return -1;
	}
	
	
	
	public static void solvePart2(File file) throws Exception {
		int totalScore = 0;
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		for (String line : lines) {
			String [] parts = line.split("\\s+");
			String a = parts[0];
			String b = parts[1];
			
			System.out.print(a + ":" + b);
			
			Action theirs = ALL_ACTIONS[indexOf(a, ABC_VALUES)];
			Outcome outcome = ALL_OUTCOMES[indexOf(b, XYZ_VALUES)];
			Action mine = getMyAction(theirs, outcome);
			
			int score = score(mine, theirs);
			System.out.println("=" + score);
			totalScore += score;
		}
		
		System.out.println("Total Score: " + totalScore);
	}
	
	public static Action getMyAction(Action theirs, Outcome outcome) {
		if (theirs == Action.ROCK) {
			switch(outcome) {
			case DRAW: return Action.ROCK;
			case WIN: return Action.PAPER;
			case LOSE: return Action.SCISSORS;
			}
		}
		else if (theirs == Action.PAPER) {
			switch(outcome) {
			case DRAW: return Action.PAPER;
			case WIN: return Action.SCISSORS;
			case LOSE: return Action.ROCK;
			}
		}
		else if (theirs == Action.SCISSORS) {
			switch(outcome) {
			case DRAW: return Action.SCISSORS;
			case WIN: return Action.ROCK;
			case LOSE: return Action.PAPER;
			}
		}
		throw new IllegalArgumentException("Should not happen!");
	}
	
	public static void main(String [] args) {
		//File file = new File("files/day02/test.txt");
		File file = new File("files/day02/input.txt");
		try {
			//solvePart1(file);
			solvePart2(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
