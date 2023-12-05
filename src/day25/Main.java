package day25;

import java.io.File;
import java.util.List;

import common.FileUtil;

public class Main {
	
	public static long snafuToDecimal(String snafu) {
		long sum = 0;
		
		char [] chars = snafu.toCharArray();
		long multiplier = 1;
		for (int i=chars.length-1; i>=0; i--) {
			char ch = chars[i];
			sum += multiplier * snafuToDecimal(ch);
			multiplier *= 5;
		}
		
		return sum;
	}
	
	public static long snafuToDecimal(char ch) {
		switch(ch) {
			case '2': return 2;
			case '1': return 1;
			case '0': return 0;
			case '-': return -1;
			case '=': return -2;
			default: throw new IllegalArgumentException("Invalid character: '" + ch + "'");
		}
	}
	
	public static String decimalToSnafu(long value) {
		StringBuilder s = new StringBuilder();
		
//		int index=0;
		long digitValue = 1;
		long maxValue = 2;
		while (value > maxValue) {
			digitValue *= 5;
			maxValue += digitValue * 2;
		}
		
		long outputValue = 0;
		while (true) {
			maxValue -= digitValue * 2;	//max value of all digits to the right
			
			boolean foundDigit = false;
			for (int digit=2; digit>=-2; digit--) {
				long total = outputValue + digit * digitValue;
				if (value >= total - maxValue && value <= total + maxValue) {
					foundDigit = true;
					
					char ch;
					switch(digit) {
					case 2: ch = '2'; break;
					case 1: ch = '1'; break;
					case 0: ch = '0'; break;
					case -1: ch = '-'; break;
					case -2: ch = '='; break;
					default: throw new IllegalStateException("Should not happen");
					}
					s.append(ch);
					
					outputValue += digit * digitValue;
					//System.out.println(" digit=" + digit + ", digitValue=" + digitValue + ", outputValue=" + outputValue);
					break;
				}
			}
			if (!foundDigit) {
				throw new IllegalStateException("Nope!");
			}
			
			if (digitValue == 1)
				break;
			
			digitValue /= 5;
		}
		
		return s.toString();
	}
	
	public static void solve(File file) throws Exception {
		List<String> lines = FileUtil.readLinesFromFile(file);
		
		long sum = 0;
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			
			long value = snafuToDecimal(line);
			System.out.println(line + " : " + value);
			sum += value;
		}
		
		System.out.println("Total: " + sum);
		System.out.println("Total (snafu): " + decimalToSnafu(sum));
	}
	
	public static void main(String [] args) {
		try {
			//File testFile = new File("files/day25/test.txt");
			//solve(testFile);
			
			File inputFile = new File("files/day25/input.txt");
			solve(inputFile);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
