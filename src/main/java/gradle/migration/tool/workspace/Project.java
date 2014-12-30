package gradle.migration.tool.workspace;

import java.io.File;
import java.util.ArrayList;

public class Project {
	private File rootFile;
	private String projectName;
	private ArrayList<Package> packages;
	private ArrayList<String> imports;
	private ArrayList<String> exports;
	private ArrayList<Project> projectDependencies;
	private ArrayList<Dependency> fileDependencies;
	private DependencyType dependencyType = DependencyType.COMPILE;

	public Project(File rootFolder) {
		this.rootFile = rootFolder;
		projectName = rootFolder.getName();
		packages = new ArrayList<Package>();
		projectDependencies = new ArrayList<Project>();
		fileDependencies = new ArrayList<Dependency>();
		populatePackages(rootFile);
		populateImports();
		populateExports();
	}

	public ArrayList<Package> getPackages() {
		return packages;
	}

	public File getFile() {
		return rootFile;
	}

	public String getFilePath() {
		return rootFile.getAbsolutePath();
	}

	public ArrayList<String> getImports() {
		return imports;
	}

	public String getName() {
		return projectName;
	}

	public ArrayList<String> getExports() {
		return exports;
	}

	public ArrayList<Project> getDependencies() {
		return projectDependencies;
	}

	public ArrayList<Dependency> getFileDependencies() {
		return fileDependencies;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public void setDependencyType(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}

	public void addFileDependency(Dependency dependency) {
		fileDependencies.add(dependency);
	}

	/*
	 * projectDependencies must be populated before they can be used;
	 */
	public void addProjectDependency(Project project) {
		projectDependencies.add(project);
	}

	public void removeDependency(Project project) {
		projectDependencies.remove(project);
	}

	private void populatePackages(File file) {
		boolean parentIsAlreadyDeclaredPackage = false;
		for (File subFile : file.listFiles()) {
			if (subFile.isDirectory()) {
				populatePackages(subFile);
			}
			if (subFile.isFile() && subFile.getName().endsWith(".java")) {
				if (parentIsAlreadyDeclaredPackage == false) {
					parentIsAlreadyDeclaredPackage = true;
					File packageFile = subFile.getParentFile();
					Package packageObj = new Package(packageFile);
					packages.add(packageObj);

				}
			}
		}
	}

	private void populateImports() {
		imports = new ArrayList<String>();
		for (Package pack : packages) {
			imports.addAll(pack.getImports());
		}
	}

	private void populateExports() {
		exports = new ArrayList<String>();
		for (Package pack : packages) {
			exports.addAll(pack.getExports());
		}
	}

	@Override
	public String toString() {
		return projectName;
	}
}
