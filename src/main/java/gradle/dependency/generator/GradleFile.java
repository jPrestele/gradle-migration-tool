package gradle.dependency.generator;

import java.io.File;

public class GradleFile {
	private File file;
	private StringBuilder output = new StringBuilder();

	public GradleFile(File file) {
		this.file = new File(file.getAbsolutePath() + "\\build.gradle");
	}

	public void append(String chars) {
		output.append(chars);
	}

	public void write() {
		WritableFile wFile = new WritableFile(file);
		wFile.create(output.toString());
	}
}
