package gradle.migration.tool.workspace;

import gradle.migration.tool.utility.FileReaderPattern;

import java.io.File;
import java.util.ArrayList;

public class JavaFile {

	private File file;
	private ArrayList<String> imports;

	public JavaFile(String path) {
		file = new File(path);
		populateImports();
	}

	public ArrayList<String> getImports() {
		return imports;
	}

	public File getFile() {
		return file;
	}

	private void populateImports() {
		FileReaderPattern fileReader = new FileReaderPattern(file);
		imports = fileReader.getMatchesTillStopCondition(".*import .*;", ".*public (class|interface|enum).*");
		normalizeImports();
	}

	private void normalizeImports() {
		for (int i = 0; i < imports.size(); i++) {
			String impStatem = imports.get(i);
			impStatem = impStatem.trim();
			// .* replacement adds the package name as import
			impStatem = removeAllOccurences(impStatem, "import ", ";", "static", ".*");
			imports.set(i, impStatem);
		}
	}

	private String removeAllOccurences(String input, String... expressions) {
		String replacement = input;
		for (String expression : expressions) {
			replacement = replacement.replace(expression, "");
		}
		return replacement;
	}

	@Override
	public String toString() {
		return file.getName();
	}

}
