package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	public static String readLineFromFile(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			return in.readLine();
		}
		finally {
			in.close();
		}
	}
	
	public static List<String> readLinesFromFile(File file) throws IOException {
		List<String> lines = new ArrayList<>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			String line;
			while ((line = in.readLine()) != null)
				lines.add(line);
		}
		finally {
			in.close();
		}
		return lines;
	}
	
}
