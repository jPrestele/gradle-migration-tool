package gradle.migration.tool.workspace;

import gradle.migration.tool.utility.FileReaderPattern;

import java.io.File;
import java.util.ArrayList;

public class Package {
	private File packageFile;
	private String javaPackage;
	private ArrayList<JavaFile> javaFiles;
	private ArrayList<String> exports;
	private ArrayList<String> imports;

	public Package(File packageFile) {
		this.packageFile = packageFile;
		populateJavaFiles();
		createJavaPackageName();
		populateExports();
		populateImports();
	}

	public File getPackageFile() {
		return packageFile;
	}

	public String getJavaPackageName() {
		return javaPackage;
	}

	public ArrayList<JavaFile> getJavaFiles() {
		return javaFiles;
	}

	public ArrayList<String> getExports() {
		return exports;
	}

	public ArrayList<String> getImports() {
		return imports;
	}

	private void populateJavaFiles() {
		javaFiles = new ArrayList<JavaFile>();
		for (File subFile : packageFile.listFiles()) {
			if (subFile.isFile() && subFile.getName().endsWith(".java")) {
				javaFiles.add(new JavaFile(subFile.getAbsolutePath()));
			}
		}
	}

	private void createJavaPackageName() {
		File firstJavaFileOfPackage = javaFiles.get(0);
		FileReaderPattern fileReader = new FileReaderPattern(firstJavaFileOfPackage);
		String firstLineJavaFile = fileReader.readLine();
		if (!firstLineJavaFile.contains("package ")) {
			javaPackage = "";
			return;
		}
		javaPackage = modifyToPackageString(firstLineJavaFile);
	}

	private String modifyToPackageString(String firstLineJavaFile) {
		String packageName = firstLineJavaFile.replace("package ", "");
		packageName = packageName.replace(";", "");
		return packageName;
	}

	private void populateExports() {
		exports = new ArrayList<String>();
		for (JavaFile javaFile : javaFiles) {
			String className = javaFile.getName().replace(".java", "");
			String export = javaPackage + '.' + className;
			exports.add(export);
		}
		// imports of the type "import xy.*" will look for standard package name
		// to resolve, so the stand package name is added to exports
		exports.add(javaPackage);
	}

	public void populateImports() {
		imports = new ArrayList<String>();
		for (JavaFile javaFile : javaFiles) {
			imports.addAll(javaFile.getImports());
		}
	}

	@Override
	public String toString() {
		return javaPackage;
	}
}
