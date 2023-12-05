package day20;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static class LinkedList {
		
		Node head;
		int len;
		
		public LinkedList(Node head, int len) {
			this.head = head;
			this.len = len;
		}
		
		public void multiplyNodesBy(long value) {
			Node node = this.head;
			node.value *= value;
			while (node.next != this.head) {
				node = node.next;
				node.value *= value;
			}
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(head.value);
			Node node = head;
			while (node.next != head) {
				node = node.next;
				s.append(", ").append(node.value);
			}
			return s.toString();
		}
		
		public static LinkedList from(List<Integer> values) {
			Node head = null;
			Node tail = null;
			for (int value : values) {
				Node newNode = new Node(value);
				if (head == null) {
					head = newNode;
					tail = newNode;
				}
				else {
					tail.insert(newNode);
					tail = newNode;
				}
			}
			
			tail.connectTo(head);
			return new LinkedList(head, values.size());
		}
		
		public List<Node> getNodesAsList() {
			List<Node> nodes = new ArrayList<>();
			nodes.add(head);
			Node node = head;
			while (node.next != head) {
				node = node.next;
				nodes.add(node);
			}
			return nodes;
		}
		
		public void mix(int times, boolean verbose) {
			if (verbose)
				System.out.println("Before: " + toString());
			
			List<Node> nodes = getNodesAsList();
			for (int i=0; i<times; i++) {

				for (Node node : nodes) {
					if (verbose)
						System.out.println("Node: " + node.value);
					
					long skip = node.value;
					Node insertAfter = node.previous;
					node.remove();

					if (skip > 0) {
						skip = skip % (this.len - 1);
						while (skip > 0) {
							insertAfter = insertAfter.next;
							skip--;
						}
					} 
					else if (skip < 0) {
						skip = -skip % (this.len - 1);
						while (skip > 0) {
							insertAfter = insertAfter.previous;
							skip--;
						}
					}

					insertAfter.insert(node);
					
					if (verbose)
						System.out.println(toString());
				}
			}
		}

		public long [] getCoordinates() {
			//find zero:
			Node zero = head;
			while (zero.value != 0)
				zero = zero.next;
			
			long [] coords = new long[3];
			Node node = zero;
			for (int i=0; i<3; i++) {
				//int offset = 1000 % this.len;
				node = node.getRelativeNode(1000);
				coords[i] = node.value;
			}
			
			return coords;
		}
		
	}
	
	static class Node {
		
		long value;
		Node next;
		Node previous;
		
		public Node(long value) {
			this.value = value;
		}
		
		public void insert(Node node) {
			if (this.next != null)
				node.connectTo(this.next);
			this.connectTo(node);
		}
		
		public void remove() {
			if (this.next != null)
				this.next.previous = this.previous;
			if (this.previous != null)
				this.previous.next = this.next;
			
			this.next = null;
			this.previous = null;
		}
		
		public void connectTo(Node node) {
			this.next = node;
			if (node != null)
				node.previous = this;
		}
		
		public Node getRelativeNode(int skip) {	
			Node node = this;
			if (skip > 0) {
				while (skip > 0) {
					node = node.next;
					skip--;
				}
			} 
			else if (skip < 0) {
				while (skip < 0) {
					node = node.previous;
					skip++;
				}
			}
			return node;
		}
		
	}
	
	public static LinkedList readInput(File file) throws IOException {
		List<String> lines = FileUtil.readLinesFromFile(file);
		List<Integer> values = new ArrayList<>();
		
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			values.add(Integer.parseInt(line));
		}
		
		return LinkedList.from(values);
	}
	
	public static void solve(File file, int multiplier, int mixCount, boolean verbose) throws Exception {
		LinkedList list = readInput(file);
		
		if (multiplier > 0)
			list.multiplyNodesBy(multiplier);
		
		//mix:
		System.out.println("Mixing...");
		list.mix(mixCount, verbose);
		
		//get coords:
		System.out.println("Getting coords...");
		long [] coords = list.getCoordinates();
		long sum = 0;
		for (int i=0; i<coords.length; i++) {
			System.out.println(i + ": " + coords[i]);
			sum += coords[i];
		}
		
		System.out.println("Sum: " + sum);
	}
	
	public static void main(String [] args) {
		try {
			//File testFile = new File("files/day20/test.txt");
			//solve(testFile, 1, 1, true);
			//solve(testFile, 811589153, 10, false);
			
			File inputFile = new File("files/day20/input.txt");
			//solve(inputFile, 1, 1, false);
			solve(inputFile, 811589153, 10, false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
