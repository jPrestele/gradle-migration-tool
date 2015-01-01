package gradle.migration.tool.workspace;

import gradle.migration.tool.workspace.utility.PatternFileReader;

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
		PatternFileReader fileReader = new PatternFileReader(file);
		imports = fileReader.getMatchesUntilStopMatch(".*import .*;", ".*public (class|interface|enum).*");
		normalizeImports();
	}

	private void normalizeImports() {
		int index = 0;
		for (String imp : imports) {
			imp = imp.trim();
			imp = removeAllOccurences(imp, "import ", ";", "static", ".*");
			imports.set(index, imp);
			index++;
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
