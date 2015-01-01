package gradle.migration.tool.workspace;

import gradle.migration.tool.dependencies.GradleDependency;
import gradle.migration.tool.gradle.file.creation.GradleFilesCreator;
import gradle.migration.tool.utility.GradleDependencyDownloader;
import gradle.migration.tool.workspace.utility.WorkspacePrinter;

import java.io.File;
import java.util.ArrayList;

public class Workspace {
	private File rootFile;
	private ArrayList<Project> projects;
	private ArrayList<Dependency> dependencies;
	private boolean transitiveDependencies = false;

	public Workspace(String rootPath) {
		this.rootFile = new File(rootPath);
		projects = new ArrayList<Project>();
		dependencies = new ArrayList<Dependency>();
		populateProjects();
	}

	public File getRootFile() {
		return rootFile;
	}

	public ArrayList<Project> getProjects() {
		return projects;
	}

	public ArrayList<Dependency> getDependencies() {
		return dependencies;
	}

	/*
	 * default is transitive
	 */
	public void setTransitiveDependencies(boolean transitive) {
		transitiveDependencies = transitive;
	}

	public void constructDependencyModel() {
		populateProjectDependencies();
		populateProjectFileDependencies();
		if (transitiveDependencies) {
			makeDependenciesTransitive();
		}
	}

	public void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public void addDependencies(ArrayList<Dependency> dependencies) {
		for (Dependency dependency : dependencies) {
			this.dependencies.add(dependency);
		}
	}

	public void removeProjects(String... projects) {
		for (String projectName : projects) {
			removeProject(projectName);
		}
	}

	/*
	 * returns true if project got removed, false if removal failed
	 */
	private boolean removeProject(String name) {
		for (Project project : projects) {
			if (project.getName().equals(name)) {
				projects.remove(project);
				System.out.println("Removed project: " + project.getName());
				return true;
			}
			System.out.println("Could not remove project " + project);
		}
		return false;
	}

	private void makeDependenciesTransitive() {
		// go to projects on which current project depend. If they have
		// the same dependencies as the current project remove them in
		// currentProject
		for (Project project : projects) {
			ArrayList<Project> projectDependencies = project.getProjectDependencies();
			ArrayList<Dependency> fileDependencies = project.getDependencies();
			ArrayList<Project> dependenciesIterateCopy = new ArrayList<Project>(projectDependencies);
			for (Project dependencyProject : dependenciesIterateCopy) {
				for (Project dependencyProjectDependency : dependencyProject.getProjectDependencies()) {
					projectDependencies.remove(dependencyProjectDependency);
				}
				for (Dependency dependencyProjectFileDependency : dependencyProject.getDependencies()) {
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
				System.out.println(project.getName() + " added to projects");
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
			for (Dependency fileDep : dependencies) {
				ArrayList<String> imports = project.getImports();
				for (String exportClass : fileDep.getClasses()) {
					if (imports.contains(exportClass)) {
						project.addDependency(fileDep);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return rootFile.getName();
	}

	public static void main(String[] args) throws InterruptedException {
		Workspace workspace = new Workspace("C:\\Users\\Jan\\Desktop\\git\\jbox2d");
		workspace.removeProjects(".git", ".gradle", "gradle");
		workspace.setTransitiveDependencies(true);

		ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
		GradleDependency junit = new GradleDependency("junit:junit:4.10");
		junit.setDependencyType(DependencyType.TESTCOMPILE);
		dependencies.add(junit);
		dependencies.add(new GradleDependency("org.apache.commons:commons-lang3:3.3.2"));
		dependencies.add(new GradleDependency("org.apache.httpcomponents:httpclient:4.3.6"));

		GradleDependencyDownloader downloader = new GradleDependencyDownloader("http://repo1.maven.org/maven2/");
		downloader.addDependencies(dependencies);
		downloader.downloadDependencies();

		workspace.addDependencies(dependencies);
		workspace.constructDependencyModel();

		WorkspacePrinter printer = new WorkspacePrinter(workspace);
		printer.printDependencyMatrix();

		GradleFilesCreator fileCreator = new GradleFilesCreator(workspace);
		fileCreator.generateGradleDependencylibrariesFile();
		fileCreator.generateGradleSettingsFiles();
		fileCreator.generateGradleSuprojectFiles();
	}
}
