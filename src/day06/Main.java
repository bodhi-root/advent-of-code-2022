package day06;

import java.io.File;

import common.FileUtil;

public class Main {
	
	public static boolean isDifferent(char ch1, char ch2, char ch3, char ch4) {
		return !(ch1 == ch2 || ch1 == ch3 || ch1 == ch4 ||
			     ch2 == ch3 || ch2 == ch4 ||
			     ch3 == ch4);
	}
	
	public static boolean isDifferent(char [] chars, int offset, int len) {
		for (int start=0; start<len-1; start++) {
			for (int i=start+1; i<len; i++) {
				if (chars[offset+start] == chars[offset+i])
					return false;
			}
		}
		return true;
	}
	
	public static int findPacketStart(String message) {
		char [] chars = message.toCharArray();
		for (int i=3; i<chars.length; i++) {
			if (isDifferent(chars[i-3], chars[i-2], chars[i-1], chars[i]))
				return i;
		}
		return -1;
	}
	
	public static int findMessageStart(String message) {
		char [] chars = message.toCharArray();
		for (int i=13; i<chars.length; i++) {
			if (isDifferent(chars, i-13, 14))
				return i;
		}
		return -1;
	}
	
	public static void test() {
		String message = "mjqjpqmgbljsphdztnvjfqwrcgsmlb";
		System.out.println("Packet start: " + (findPacketStart(message) + 1));
		System.out.println("Message start: " + (findMessageStart(message) + 1));
	}
	
	public static void solve() throws Exception {
		String message = FileUtil.readLineFromFile(new File("files/day06/input.txt"));
		System.out.println("Packet start: " + (findPacketStart(message) + 1));
		System.out.println("Message start: " + (findMessageStart(message) + 1));
	}
	
	public static void main(String [] args) {
		try {
			test();
			solve();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
