package gradle.dependency.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WritableFile extends File {
	private static final long serialVersionUID = 1L;
	private String fileOutput = "";
	final static String newLine = System.lineSeparator();

	public WritableFile(String path) {
		super(path);
	}

	public WritableFile(File file) {
		super(file.getAbsolutePath());
	}

	public void append(String string) {
		fileOutput += string;
	}

	public void newLine() {
		fileOutput += newLine;
	}

	/*
	 * should only be used if append function was used
	 */
	public void write() {
		write(fileOutput);
	}

	public void write(String output) {
		File file = super.getAbsoluteFile();
		try {
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter(file));
			buffWrite.write(output);
			buffWrite.close();
		} catch (IOException e) {
			System.out.println("Could not write file : " + file);
			e.printStackTrace();
		}
	}

}