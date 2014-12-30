package gradle.dependency.generator;

import java.io.File;
import java.util.ArrayList;

import dnl.utils.text.table.TextTable;

public class Workspace {
	private File rootFile;
	private ArrayList<Project> projects;
	private ArrayList<Dependency> externalLibraries;
	private boolean transitiveDependencies = false;
	private String repositoryUrl;

	public Workspace(String rootPath) {
		this.rootFile = new File(rootPath);
		projects = new ArrayList<Project>();
		externalLibraries = new ArrayList<Dependency>();
		populateProjects();
	}

	public File getRootFile() {
		return rootFile;
	}

	public ArrayList<Project> getProjects() {
		return projects;
	}

	public ArrayList<Dependency> getExternalLibraries() {
		return externalLibraries;
	}

	public void setRepository(String repository) {
		this.repositoryUrl = repository;
	}

	/*
	 * default is transitive
	 */
	public void setTransitiveDependencies(boolean transitive) {
		transitiveDependencies = transitive;
	}

	public void workspaceConfigurationsFinished() {
		downloadDependencies();
		populateProjectDependencies();
		populateProjectFileDependencies();
		if (transitiveDependencies) {
			makeDependenciesTransitive();
		}
	}

	public void addGradleDependencies(String... dependencies) {
		for (String dependency : dependencies) {
			addGradleDependency(dependency);
		}
	}

	public void addGradleDependency(String dependency) {
		addGradleDependency(dependency, DependencyType.COMPILE);
	}

	public void addGradleDependency(String dependency, DependencyType type) {
		Dependency fileDependency = new RemoteGradleDependency(dependency);
		fileDependency.setDependencyType(type);
		this.externalLibraries.add(fileDependency);
	}

	public void printDependencyMatrix() {
		int verticalSize = projects.size();
		int horizontalSize = verticalSize + 1 + externalLibraries.size();
		Object[][] data = new Object[verticalSize][horizontalSize];
		for (int i = 0; i < verticalSize; i++) {
			for (int j = 0; j < horizontalSize; j++) {
				// add project names in first column
				if (j == 0) {
					data[i][j] = projects.get(i).getName();
				} else if (j < projects.size() + 1) {
					// add project dependencies
					if (projects.get(i).getDependencies().contains(projects.get(j - 1))) {
						data[i][j] = new Integer(1);
					}
				} else {
					// add file dependencies
					if (projects.get(i).getFileDependencies().contains(externalLibraries.get(j - projects.size() - 1))) {
						data[i][j] = new Integer(1);
					}
				}
			}
		}

		String[] columnNames = new String[horizontalSize];
		for (int i = 0; i < horizontalSize; i++) {
			if (i == 0) {
				columnNames[0] = "";
			} else if (i < projects.size() + 1) {
				// add projects names
				columnNames[i] = projects.get(i - 1).getName();
			} else {
				// add file names
				columnNames[i] = externalLibraries.get(i - projects.size() - 1).getJarName();
			}
		}
		TextTable tt = new TextTable(columnNames, data);
		tt.printTable();
	}

	public void removeProjects(String... projects) {
		for (String project : projects) {
			boolean removedSuccesfully = removeProject(project);
			if (!removedSuccesfully) {
				System.out.println("Could not remove project " + project);
			}
		}
	}

	private void downloadDependencies() {
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(repositoryUrl);
		downloader.addDependencies(externalLibraries);
		downloader.downloadDependencies();
	}

	private void makeDependenciesTransitive() {
		// go to projects on which current project depend. If they have
		// the same dependencies as the parent project remove them in
		// currentProject
		for (Project project : projects) {
			ArrayList<Project> projectDependencies = project.getDependencies();
			ArrayList<Dependency> fileDependencies = project.getFileDependencies();
			ArrayList<Project> dependenciesIterateCopy = new ArrayList<Project>(projectDependencies);
			for (Project dependencyProject : dependenciesIterateCopy) {
				// remove duplicate projects
				for (Project dependencyProjectDependency : dependencyProject.getDependencies()) {
					projectDependencies.remove(dependencyProjectDependency);
				}
				// remove duplicate fileDependencies
				for (Dependency dependencyProjectFileDependency : dependencyProject.getFileDependencies()) {
					fileDependencies.remove(dependencyProjectFileDependency);
				}
			}
		}
	}

	private void populateProjects() {
		System.out.println("PROJECTS");
		for (File workspaceFile : rootFile.listFiles()) {
			if (workspaceFile.isDirectory()) {
				Project project = new Project(workspaceFile);
				projects.add(project);
				System.out.println(project.getName());
			}
		}
		System.out.println("\n" + projects.size() + " projects found\n");
	}

	private void populateProjectDependencies() {
		for (Project currentProject : projects) {
			for (Project iterateProject : projects) {
				// skip own project
				if (currentProject.equals(iterateProject)) {
					continue;
				}
				ArrayList<String> exportsIterProject = iterateProject.getExports();
				for (String importCurrentProject : currentProject.getImports()) {
					if (exportsIterProject.contains(importCurrentProject)) {
						currentProject.addProjectDependency(iterateProject);
						break;
					}
				}
			}
		}
	}

	private void populateProjectFileDependencies() {
		for (Project project : projects) {
			for (Dependency fileDep : externalLibraries) {
				ArrayList<String> imports = project.getImports();
				for (String exportClass : fileDep.getClasses()) {
					if (imports.contains(exportClass)) {
						project.addFileDependency(fileDep);
					}
				}
			}
		}
	}

	/*
	 * returns true if project got removed
	 */
	private boolean removeProject(String name) {
		for (Project project : projects) {
			if (project.getName().equals(name)) {
				projects.remove(project);
				System.out.println("Removed project: " + project.getName());
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws InterruptedException {
		Workspace workspace = new Workspace("C:\\Users\\Jan\\Desktop\\git\\jbox2d");
		workspace.removeProjects(".git", ".gradle", "gradle");
		workspace.setRepository("http://repo1.maven.org/maven2/");
		workspace.setTransitiveDependencies(true);
		workspace.addGradleDependency("junit:junit:4.10", DependencyType.TESTCOMPILE);
		workspace.addGradleDependency("org.apache.commons:commons-lang3:3.3.2");
		workspace.addGradleDependency("org.apache.httpcomponents:httpclient:4.3.6");
		workspace.workspaceConfigurationsFinished();
		workspace.printDependencyMatrix();
		GradleFilesCreator fileCreator = new GradleFilesCreator(workspace);
		fileCreator.generateGradleDependencylibrariesFile();
		fileCreator.generateGradleSettingsFiles();
		fileCreator.generateGradleSuprojectFiles();
	}
}
