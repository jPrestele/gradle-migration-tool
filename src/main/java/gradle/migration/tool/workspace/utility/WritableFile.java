package gradle.migration.tool.workspace.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WritableFile extends File {
	private StringBuilder output;

	final static String newLine = System.lineSeparator();
	private static final long serialVersionUID = 1L;

	public WritableFile(String path) {
		super(path);
		output = new StringBuilder();
	}

	public WritableFile(File file) {
		super(file.getAbsolutePath());
	}

	public WritableFile append(String string) {
		output.append(string);
		return this;
	}

	public WritableFile newLine() {
		output.append(newLine);
		return this;
	}

	public void write() {
		File file = super.getAbsoluteFile();
		try {
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter(file));
			buffWrite.write(output.toString());
			buffWrite.close();
		} catch (IOException e) {
			System.out.println("Could not write file : " + file);
			e.printStackTrace();
		}
	}
}
