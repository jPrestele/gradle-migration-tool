package gradle.migration.tool.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FileReaderPattern {
	private File file;
	private BufferedReader buffRead;

	public FileReaderPattern(File file) {
		this.file = file;
		try {
			buffRead = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String readLine() {
		String currentLine = "";
		try {
			currentLine = buffRead.readLine();
		} catch (IOException e) {
			System.err.println("Could not read from " + file.getName());
		}
		return currentLine;
	}

	public String getRegexMatch(String regex) {
		String line = readLine();
		while (line != null) {
			if (line.matches(regex)) {
				return line;
			}
			line = readLine();
		}
		return null;
	}

	public ArrayList<String> getMatchesTillStopCondition(String matchRegex) {
		return getMatchesTillStopCondition(matchRegex, "");
	}

	public ArrayList<String> getMatchesTillStopCondition(String matchRegex, String stopRegex) {
		ArrayList<String> matches = new ArrayList<String>();
		String currentLine = getRegexMatch(matchRegex);
		while (currentLine != null) {
			if (Pattern.matches(stopRegex, currentLine)) {
				return matches;
			}
			matches.add(currentLine);
			currentLine = getRegexMatch(matchRegex);
		}
		return matches;
	}
}
