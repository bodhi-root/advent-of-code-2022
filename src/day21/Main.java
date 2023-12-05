package day21;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FileUtil;

public class Main {
	
	static class Puzzle {
		
		Map<String, Node> nodes = new HashMap<>();
		
		public void add(Node node) {
			nodes.put(node.id, node);
		}
		public Node getNode(String id) {
			return nodes.get(id);
		}
		
		public long getValue(String text) {
			if (Character.isDigit(text.charAt(0)))
				return Long.parseLong(text);
			else
				return getNode(text).value(this);
		}
		
		public static Puzzle loadFrom(File file) throws IOException {
			Puzzle puzzle = new Puzzle();
			
			List<String> lines = FileUtil.readLinesFromFile(file);
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				//dbpl: 5
				//cczh: sllz + lgvd
				
				String [] parts = line.split("\\s+");
				String id = parts[0].substring(0, parts[0].length()-1);
				if (parts.length == 2) {
					long value = Long.parseLong(parts[1]);
					puzzle.add(new ValueNode(id, value));
				} else {
					String value1 = parts[1];
					String op = parts[2];
					String value2 = parts[3];
					puzzle.add(new OpNode(id, value1, op, value2));
				}
				
			}
			
			return puzzle;
		}
		
	}
	
	static abstract class Node {
		
		String id;
		
		public Node(String id) {
			this.id = id;
		}
		
		public abstract long value(Puzzle ctx);
		
		public abstract boolean hasAncestor(String id, Puzzle ctx);
		
	}
	
	static class ValueNode extends Node {
		
		long value;
		
		public ValueNode(String id, long value) {
			super(id);
			this.value = value;
		}
		
		public long value(Puzzle ctx) {
			return value;
		}
		
		public boolean hasAncestor(String id, Puzzle ctx) {
			return false;
		}
		
	}
	
	static class OpNode extends Node {
		
		String left;
		String op;
		String right;
		
		public OpNode(String id, String left, String op, String right) {
			super(id);
			this.left = left;
			this.op = op;
			this.right = right;
		}
		
		public long value(Puzzle ctx) {
			long x = ctx.getValue(left);
			long y = ctx.getValue(right);
			if (op.equals("+"))
				return x + y;
			else if (op.equals("-"))
				return x - y;
			else if (op.equals("*"))
				return x * y;
			else if (op.equals("/"))
				return x / y;
			else
				throw new IllegalStateException("Invalid op: " + op);
		}
		
		public boolean hasAncestor(String id, Puzzle ctx) {
			Node leftNode = ctx.getNode(left);
			Node rightNode = ctx.getNode(right);
			return (leftNode.id.equals(id) ||
				rightNode.id.equals(id) ||
				leftNode.hasAncestor(id, ctx) ||
				rightNode.hasAncestor(id, ctx));
		}
		
		public long findAncestorValueIfEquals(long target, String ancestorId, Puzzle ctx) {
			Node leftNode = ctx.getNode(left);
			Node rightNode = ctx.getNode(right);
			boolean leftChanges = leftNode.id.equals(ancestorId) || leftNode.hasAncestor(ancestorId, ctx);
			boolean rightChanges = rightNode.id.equals(ancestorId) || rightNode.hasAncestor(ancestorId, ctx);
			if (leftChanges == rightChanges)
				throw new IllegalStateException("One (and only one) side of operation can change");
			
			Node changeNode;
			long changeTarget;
			if (leftChanges) {
				changeNode = leftNode;
				long rightValue = rightNode.value(ctx);
				if (op.equals("+"))			//change + rightValue = target
					changeTarget = target - rightValue;
				else if (op.equals("-"))	//change - rightValue = target
					changeTarget = target + rightValue;
				else if (op.equals("*"))	//change * rightValue = target
					changeTarget = target / rightValue;
				else if (op.equals("/"))	//change / rightValue = target
					changeTarget = target * rightValue;
				else
					throw new IllegalStateException("Unknown op: " + op);
			} else {
				changeNode = rightNode;
				long leftValue = leftNode.value(ctx);
				if (op.equals("+"))			//leftValue + change = target
					changeTarget = target - leftValue;
				else if (op.equals("-"))	//leftValue - change = target
					changeTarget = leftValue - target;
				else if (op.equals("*"))	//leftValue * change = target
					changeTarget = target / leftValue;
				else if (op.equals("/"))	//leftValue / change = target
					changeTarget = leftValue / target;
				else
					throw new IllegalStateException("Unknown op: " + op);
			}
			
			if (changeNode.id.equals(ancestorId))
				return changeTarget;
			else
				return ((OpNode)changeNode).findAncestorValueIfEquals(changeTarget, ancestorId, ctx);
		}
		
	}
	
	
	public static void solvePart1(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		System.out.println(puzzle.getValue("root"));
	}
	
	public static void solvePart2(File file) throws Exception {
		Puzzle puzzle = Puzzle.loadFrom(file);
		
		OpNode root = (OpNode)puzzle.getNode("root");
		Node left = puzzle.getNode(root.left);
		Node right = puzzle.getNode(root.right);
		
		/*
		//try all values, starting at 0: too slow!
		ValueNode me = (ValueNode)puzzle.getNode("humn");
		me.value = 0;
		while (left.value(puzzle) != right.value(puzzle)) {
			me.value++;
			if (me.value % 10000 == 0)
				System.out.println(me.value);
		System.out.println("Answer: " + me.value);
		}
		*/
		
		//the following solution assumes the 'humn' node will never appear
		//on both sides of an operation
		boolean leftChanges = ((OpNode)left).hasAncestor("humn", puzzle);
		boolean rightChanges = ((OpNode)right).hasAncestor("humn", puzzle);
		
		if (leftChanges == rightChanges) {
			System.out.println("ERROR: Only 1 node can depend on humn");
			return;
		}
		
		OpNode node;
		long target;
		if (leftChanges) {
			node = (OpNode)left;
			target = right.value(puzzle);
		} else {
			node = (OpNode)right;
			target = left.value(puzzle);
		}
		
		long value = node.findAncestorValueIfEquals(target, "humn", puzzle);
		System.out.println("Answer: " + value);
	}
	
	static class ValueInfo {
		
		Map<String, Long> values = new HashMap<>();
		
		public void set(String id, long value) {
			values.put(id, value);
		}
		public long get(String id) {
			return values.get(id);
		}
		
	}
	
	public static void main(String [] args) {
		try {
			File testFile = new File("files/day21/test.txt");
			//solvePart1(testFile);
			solvePart2(testFile);
			
			File inputFile = new File("files/day21/input.txt");
			//solvePart1(inputFile);
			solvePart2(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
