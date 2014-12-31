package gradle.migration.tool.workspace;

import java.io.File;
import java.util.ArrayList;

public class Project {
	private File file;
	private String name;
	private ArrayList<Package> packages;
	private ArrayList<String> imports;
	private ArrayList<String> exports;
	private ArrayList<Project> projectDependencies;
	private ArrayList<Dependency> dependencies;
	private DependencyType dependencyType = DependencyType.COMPILE;

	public Project(File rootFolder) {
		this.file = rootFolder;
		name = rootFolder.getName();
		packages = new ArrayList<Package>();
		projectDependencies = new ArrayList<Project>();
		dependencies = new ArrayList<Dependency>();
		populatePackages(file);
		populateImports();
		populateExports();
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Package> getPackages() {
		return packages;
	}

	/*
	 * returns all java import statements made in this project
	 */
	public ArrayList<String> getImports() {
		return imports;
	}

	/*
	 * returns all classes which are exported by this project
	 */
	public ArrayList<String> getExports() {
		return exports;
	}

	public ArrayList<Project> getProjectDependencies() {
		return projectDependencies;
	}

	public ArrayList<Dependency> getDependencies() {
		return dependencies;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public void setDependencyType(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}

	public void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public void addProjectDependency(Project project) {
		projectDependencies.add(project);
	}

	public void removeProjectDependency(Project project) {
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
		return name;
	}
}
