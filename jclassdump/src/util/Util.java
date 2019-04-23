package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

//2018-11-21: check the code
public class Util {
	public static ArrayList<String> fromArraytoArrayList(String[] tmps) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < tmps.length; i++)
			result.add(tmps[i].trim());
		return result;
	}

	public static int findOccurences(String variable, String substring) {
		int index = variable.indexOf(substring);
		int count = 0;
		while (index >= 0) {
			index = variable.indexOf(substring, index + 1);
			count++;
		}
		return count;
	}

	public static String mergeToString(ArrayList<String> lines, int begin, int end) {
		String result = "";

		if (begin <= end && begin >= 0 && end >= 0 && begin < lines.size() && end < lines.size()) {
			for (int i = begin; i <= end; i++) {
				result += lines.get(i) + "\n";
			}
		}
		return result;
	}

	// Read a file into an array of lines
	public static ArrayList<String> readIntoLines(String classfilepath) {
		String temp = "";

		try {
			Scanner s = new Scanner(new File(classfilepath));
			while (s.hasNextLine()) {
				temp += s.nextLine() + "\n";
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return new ArrayList<String>(Arrays.asList(temp.trim().split("\n")));
	}

	public static String createNewPath(String filepath) {
		int i = 1;
		String newfilename = filepath + "$" + i;
		boolean find = true;

		while (find) {
			File f = new File(newfilename);
			if (f.exists() && !f.isDirectory()) {
				i++;
				newfilename = filepath + "$" + i;
			} else {
				return newfilename;
			}
		}

		assert (false); // we should arrive at here
		return "";
	}

	public static String changeCharInPosition(int position, char ch, String str) {
		String result;
		result = str.substring(0, position) + ch + str.substring(position + 1);
		return result;
	}

	public static HashSet<String> intersection(final HashSet<String> first, final HashSet<String> second) {
		final HashSet<String> copy = new HashSet<String>(first);
		copy.retainAll(second);
		return copy;
	}

	public static HashSet<String> union(final HashSet<String> first, final HashSet<String> second) {
		final HashSet<String> copy = new HashSet<String>(first);
		copy.addAll(second);
		return copy;
	}
}