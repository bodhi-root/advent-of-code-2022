package day07;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import common.FileUtil;

public class Main {
	
	static enum FileType {FILE, DIRECTORY};
	
	static class FileNode {
		
		String name;
		FileType type;
		long size;
		
		FileNode parent;
		List<FileNode> children = new ArrayList<>();
		
		public FileNode(String name, FileType type, long size) {
			this.name = name;
			this.type = type;
			this.size = size;
		}
		
		public boolean isDirectory() {
			return type == FileType.DIRECTORY;
		}
		
		public FileNode addChild(String name, FileType type, long size) {
			FileNode child = new FileNode(name, type, size);
			child.parent = this;
			children.add(child);
			return child;
		}
		
		public FileNode getChild(String name) {
			for (FileNode child : children) {
				if (child.name.equals(name))
					return child;
			}
			return null;
		}
		
		public int getDepth() {
			int depth = 0;
			FileNode node = this;
			while (node.parent != null) {
				depth++;
				node = node.parent;
			}
			return depth;
		}
		
		public void visitChildrenDepthFirst(FileNodeVisitor visitor) {
			for (FileNode child : children) {
				visitor.visit(child);
				child.visitChildrenDepthFirst(visitor);
			}
		}
		
		public long getTotalSize() {
			if (this.type == FileType.DIRECTORY) {
				SizeCounter counter = new SizeCounter();
				visitChildrenDepthFirst(counter);
				return counter.sum;
			} else {
				return this.size;
			}
		}
		
	}
	
	static interface FileNodeVisitor {
		
		public void visit(FileNode node);
		
	}
	
	static class SizeCounter implements FileNodeVisitor {
		
		long sum = 0;
		
		public void visit(FileNode node) {
			sum += node.size;
		}
		
	}
	
	static class FileSystem {
		
		FileNode root;
		
		public FileSystem() {
			this.root = new FileNode("/", FileType.DIRECTORY, 0);
		}
		
		public FileNode getRoot() {
			return root;
		}
		
		public String printFileTree() {
			StringBuilder s = new StringBuilder();
			
			FileNodeVisitor printer = new FileNodeVisitor() {

				public void visit(FileNode node) {
					int depth = node.getDepth();
					for (int i=0; i<depth; i++)
						s.append("  ");
					s.append("- ");
					s.append(node.name);
					String typeText = node.type == FileType.DIRECTORY ? "dir" : "file";
					s.append(" (").append(typeText)
					 .append(", size=").append(node.size)
					 .append(")");
				    s.append("\n");
				}
				
			};
			
			visitAllDepthFirst(printer);
			return s.toString();
		}
		
		public void visitAllDepthFirst(FileNodeVisitor visitor) {
			visitor.visit(root);
			root.visitChildrenDepthFirst(visitor);
		}
		
	}
	
	
	public static FileSystem loadInput(File file) throws Exception {
		FileSystem system = new FileSystem();
		
		List<String> lines = FileUtil.readLinesFromFile(file);
		FileNode currentNode = null;
		
		String lastCommand = null;
		
		for (int i=0; i<lines.size(); i++) {
			String line = lines.get(i).trim();
			if (line.isEmpty())
				continue;
			
			//process command:
			if (line.charAt(0) == '$') {
				String [] cmd = line.substring(2).split("\\s+");
				
				//change directory:
				if (cmd[0].equals("cd")) {
					
					if (cmd[1].equals("/")) {
						currentNode = system.getRoot();
					} else if (cmd[1].equals("..")) {
						currentNode = currentNode.parent;
					} else {
						currentNode = currentNode.getChild(cmd[1]);
						if (currentNode == null)
							throw new IllegalStateException("Child does not exist: " + cmd[1]);
					}
					
					if (currentNode.type != FileType.DIRECTORY)
						throw new IllegalStateException("File is not a directory");
				} 
				
				//list files:
				else if (cmd[0].equals("ls")) {
					//do nothing
				}
				
				//other: error
				else {
					throw new IllegalStateException("Invalid command: " + cmd[0]);
				}
				
				lastCommand = cmd[0];
			}
			
			//read output:
			else {
				if (lastCommand.equals("ls")) {
					//parse file/directory info:
					//dir a
					//14848514 b.txt
					String [] parts = line.split("\\s+");
					String name = parts[1];
					if (parts[0].equals("dir")) {
						currentNode.addChild(name, FileType.DIRECTORY, 0);
					}
					else {
						long size = Long.parseLong(parts[0]);
						currentNode.addChild(name, FileType.FILE, size);
					}
				}
				
				else {
					throw new IllegalStateException("Unexpected output: " + line);
				}
			}
		}
		
		return system;
	}
	
	static class Part1Solver implements FileNodeVisitor {
		
		List<FileNode> nodes = new ArrayList<>();
		long totalSize = 0;
		
		public void visit(FileNode node) {
			if (node.isDirectory()) {
				long size = node.getTotalSize();
				if (size < 100000) {
					nodes.add(node);
					totalSize += size;
				}
			}
		}
		
	}
	
	static class DirAndSize {
		
		FileNode dir;
		long totalSize;
		
		public DirAndSize(FileNode dir, long totalSize) {
			this.dir = dir;
			this.totalSize = totalSize;
		}
		
	}
	
	static class Part2Solver implements FileNodeVisitor {
		
		List<DirAndSize> list = new ArrayList<>();
		
		public void visit(FileNode node) {
			if (node.isDirectory()) {
				long size = node.getTotalSize();
				list.add(new DirAndSize(node, size));
			}
		}
		
		public static void solve(FileSystem fs, long totalSize, long requiredFreeSize) {
			Part2Solver solver = new Part2Solver();
			fs.visitAllDepthFirst(solver);
			
			DirAndSize root = solver.list.get(0);
			System.out.println("Root size: " + root.totalSize);
			
			long freeSpace = totalSize - root.totalSize;
			System.out.println("Free space: " + freeSpace);
			
			long toDelete = requiredFreeSize - freeSpace;
			System.out.println("To delete: " + toDelete);
			
			solver.list.sort(new Comparator<DirAndSize> () {

				public int compare(DirAndSize o1, DirAndSize o2) {
					return Long.compare(o1.totalSize, o2.totalSize);
				}
				
			});
			
			for (DirAndSize entry : solver.list) {
				if (entry.totalSize >= toDelete) {
					System.out.println("Delete: " + entry.dir.name + ", size = " + entry.totalSize);
					break;
				}
			}
		}
		
	}
	
	public static void solvePart1(File file) throws Exception {
		FileSystem fs = loadInput(file);
		System.out.println(fs.printFileTree());
		
		Part1Solver solver = new Part1Solver();
		fs.visitAllDepthFirst(solver);
		System.out.println(solver.nodes.size() + " directories with total size = " + solver.totalSize);
	}
	
	public static void solvePart2(File file) throws Exception {
		FileSystem fs = loadInput(file);
		System.out.println(fs.printFileTree());
		
		Part2Solver.solve(fs, 70000000, 30000000);
	}
	
	public static void main(String [] args) {
		try {
			//File file = new File("files/day07/test.txt");
			File file = new File("files/day07/input.txt");
			//solvePart1(file);
			solvePart2(file);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
