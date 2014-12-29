package gradle.dependency.generator;

import java.io.File;
import java.util.ArrayList;

import dnl.utils.text.table.TextTable;

public class Workspace {
	private ArrayList<Project> projectList;
	private File workspaceRootFile;
	private ArrayList<Dependency> workrspaceDependencies;
	private String repositoryUrl = "http://repo1.maven.org/maven2/";
	private boolean transitiveDependencies = true;

	public Workspace(String workspaceRootPath) {
		this.workspaceRootFile = new File(workspaceRootPath);
		projectList = new ArrayList<Project>();
		workrspaceDependencies = new ArrayList<Dependency>();
		populateProjects();
	}

	public ArrayList<Project> getProjects() {
		return projectList;
	}

	public File getWorkspaceRoot() {
		return workspaceRootFile;
	}

	public ArrayList<Dependency> getWorkspaceDependencies() {
		return workrspaceDependencies;
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
			addGradleDependency(dependency, DependencyType.COMPILE);
		}
	}

	public void addGradleDependency(String dependency) {
		addGradleDependency(dependency, DependencyType.COMPILE);
	}

	public void addGradleDependency(String dependency, DependencyType type) {
		Dependency fileDependency = new RemoteGradleDependency(dependency);
		fileDependency.setDependencyType(type);
		this.workrspaceDependencies.add(fileDependency);
	}

	public void printDependencyMatrix() {
		int verticalSize = projectList.size();
		int horizontalSize = verticalSize + 1 + workrspaceDependencies.size();
		Object[][] data = new Object[verticalSize][horizontalSize];
		for (int i = 0; i < verticalSize; i++) {
			for (int j = 0; j < horizontalSize; j++) {
				// add project names in first column
				if (j == 0) {
					data[i][j] = projectList.get(i).getName();
				} else if (j < projectList.size() + 1) {
					// add project dependencies
					if (projectList.get(i).getDependencies().contains(projectList.get(j - 1))) {
						data[i][j] = new Integer(1);
					}
				} else {
					// add file dependencies
					if (projectList.get(i).getFileDependencies().contains(workrspaceDependencies.get(j - projectList.size() - 1))) {
						data[i][j] = new Integer(1);
					}
				}
			}
		}

		String[] columnNames = new String[horizontalSize];
		for (int i = 0; i < horizontalSize; i++) {
			if (i == 0) {
				columnNames[0] = "";
			} else if (i < projectList.size() + 1) {
				// add projects names
				columnNames[i] = projectList.get(i - 1).getName();
			} else {
				// add file names
				columnNames[i] = workrspaceDependencies.get(i - projectList.size() - 1).getJarName();
			}
		}
		TextTable tt = new TextTable(columnNames, data);
		tt.printTable();
	}

	public void removeProjects(String... removeProjects) {
		for (String name : removeProjects) {
			boolean removedSuccesfully = removeProject(name);
			if (!removedSuccesfully) {
				System.out.println("Could not remove project " + name);
			}
		}
	}

	private void downloadDependencies() {
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(repositoryUrl);
		downloader.addDependencies(workrspaceDependencies);
		downloader.downloadDependencies();
	}

	private void makeDependenciesTransitive() {
		// go to projects on which current project depend. If they have
		// the same dependencies as the parent project remove them in
		// currentProject
		for (Project project : projectList) {
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
		for (File workspaceFile : workspaceRootFile.listFiles()) {
			if (workspaceFile.isDirectory()) {
				Project project = new Project(workspaceFile);
				projectList.add(project);
			}
		}
		System.out.println(projectList.size() + " projects found");
	}

	private void populateProjectDependencies() {
		for (Project currentProject : projectList) {
			for (Project iterateProject : projectList) {
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
		for (Project project : projectList) {
			for (Dependency fileDep : workrspaceDependencies) {
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
		for (Project project : projectList) {
			if (project.getName().equals(name)) {
				projectList.remove(project);
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws InterruptedException {
		Workspace workspace = new Workspace("C:\\Users\\Jan\\Desktop\\git\\jbox2d");
		workspace.removeProjects(".git", ".gradle", "gradle");
		workspace.setTransitiveDependencies(true);
		// workspace.addFileDependencies("C:\\Users\\Jan\\.gradle\\caches\\modules-2\\files-2.1\\junit\\junit\\4.11\\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\\junit-4.11.jar");
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
