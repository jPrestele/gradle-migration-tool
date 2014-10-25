package gradle.dependency.generator;

import java.io.File;
import java.util.ArrayList;

public class JavaFile extends File {
	private ArrayList<String> imports;

	public JavaFile(String path) {
		super(path);
		populateImports();
	}

	public ArrayList<String> getImports() {
		return imports;
	}

	private void populateImports() {
		FileReaderPattern fileReader = new FileReaderPattern(this);
		imports = fileReader.getMatchesTillStopCondition(".*import .*;", ".*public (class|interface).*");
		removeImportSyntax();
	}

	private void removeImportSyntax() {
		for (int i = 0; i < imports.size(); i++) {
			String impStatem = imports.get(i);
			impStatem = impStatem.trim();
			// .* replacement adds the package name as import
			impStatem = StringUtility.removeAllOccurences(impStatem, "import ", ";", "static", ".*");
			imports.set(i, impStatem);
		}
	}
}
