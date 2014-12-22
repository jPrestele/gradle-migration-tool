package gradle.dependency.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import dnl.utils.text.table.TextTable;

public class Workspace {
	private ArrayList<Project> projectList;
	private File workspaceRootFile;
	private ArrayList<GradleDependency> gradleDependencies;
	private ArrayList<FileDependency> fileDependencies;
	private File gradleDependendenciesCache;
	private String repositoryUrl = "http://repo1.maven.org/maven2/";
	private boolean transitiveDependencies = true;

	public Workspace(String workspaceRootPath) {
		this.workspaceRootFile = new File(workspaceRootPath);
		projectList = new ArrayList<Project>();
		gradleDependencies = new ArrayList<GradleDependency>();
		fileDependencies = new ArrayList<FileDependency>();
		String userHomeDir = System.getProperty("user.home");
		gradleDependendenciesCache = new File(userHomeDir + "\\.gradle\\caches\\modules-2\\files-2.1");
		populateProjects();
	}

	public ArrayList<Project> getProjects() {
		return projectList;
	}

	public File getWorkspaceRoot() {
		return workspaceRootFile;
	}

	public ArrayList<FileDependency> getFileDependencies() {
		return fileDependencies;
	}

	public void printDependencyMatrix() {
		int verticalSize = projectList.size();
		int horizontalSize = verticalSize + 1 + fileDependencies.size();
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
					if (projectList.get(i).getFileDependencies().contains(fileDependencies.get(j - projectList.size() - 1))) {
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
				columnNames[i] = fileDependencies.get(i - projectList.size() - 1).getName();
			}
		}
		TextTable tt = new TextTable(columnNames, data);
		tt.printTable();
	}

	public void workspaceConfigurationsFinished() {
		downloadDependencies();
		addGradleDependenciesToFileDependencies();
		populateProjectDependencies();
		populateProjectFileDependencies();
		if (transitiveDependencies) {
			makeDependenciesTransitive();
		}
	}

	/*
	 * Add single dependency or folder which contains dependencies. Will recurse
	 * through folder
	 */
	public void addFileDependencies(String... paths) {
		for (String path : paths) {
			File file = new File(path);
			if (file.isFile() && path.endsWith(".jar")) {
				addFileDependency(path);
			}
			if (file.isDirectory()) {
				String[] filesInDirectory = file.list();
				// only recurse if dir is not empty
				if (filesInDirectory != null) {
					addFileDependencies(filesInDirectory);
				}
			}
		}
	}

	public void addFileDependency(String dependency) {
		addFileDependency(dependency, DependencyType.COMPILE);
	}

	public void addFileDependency(String dependency, DependencyType type) {
		try {
			FileDependency fileDependency = new FileDependency(dependency);
			fileDependencies.add(fileDependency);
		} catch (IOException e) {
			System.err.println("Could not open jar : " + dependency);
		}
	}

	/*
	 * Use gradle dependency format : "group:name:version"
	 */
	public void addGradleDependencies(String... dependencies) {
		for (String dependency : dependencies) {
			addGradleDependency(dependency, DependencyType.COMPILE);
		}
	}

	public void addGradleDependency(String dependency) {
		addGradleDependency(dependency, DependencyType.COMPILE);
	}

	public void addGradleDependency(String dependency, DependencyType type) {
		GradleDependency gradleDependency = new GradleDependency(dependency);
		gradleDependency.setDependencyType(type);
		this.gradleDependencies.add(gradleDependency);
	}

	public void generateGradleSuprojectFiles() {
		for (Project project : projectList) {
			GradleFile gradleFile = new GradleFile(project.getFile());
			// don't create build.gradle if no dependencies exist for project
			if (project.getDependencies().isEmpty() == false || project.getFileDependencies().isEmpty() == false) {
				String newLine = System.lineSeparator();
				gradleFile.append("dependencies {" + newLine);
				for (Project projectDependency : project.getDependencies()) {
					gradleFile.append("\t" + projectDependency.getDependencyType().getType() + " project(':" + projectDependency.getName() + "')" + newLine);
				}
				for (FileDependency fileDependency : project.getFileDependencies()) {
					// check if it is local dependency or remote dependency
					if (fileDependency.isGradleDependency()) {
						gradleFile.append("\t" + fileDependency.getDependencyType().getType() + " '" + fileDependency.getGradleFormatDependency() + "'" + newLine);
					} else {
						gradleFile.append("\tcompile files('" + fileDependency.getPath() + "')" + newLine);
					}
				}
				gradleFile.append("}");
			}
			gradleFile.write();
		}
	}

	public void generateSettingsDotGradleFile() {
		SettingsGradleCreator.generateGradleSettingsFile(this);
	}

	/*
	 * Only needs to be set if gradle cache isn't in userhome/.gradle/...
	 */
	public void setGradleDependencyCache(String path) {
		gradleDependendenciesCache = new File(path);
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

	private void downloadDependencies() {
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(repositoryUrl);
		downloader.addDependencies(gradleDependencies);
		downloader.downloadDependencies();
	}

	// after Gradle downloaded Dependencies
	private void addGradleDependenciesToFileDependencies() {
		for (GradleDependency gradleDependency : gradleDependencies) {
			try {
				File jar = FileUtils.listFiles(gradleDependendenciesCache, FileFilterUtils.nameFileFilter(gradleDependency.getJarName()), TrueFileFilter.INSTANCE).iterator().next();
				FileDependency fileDependencyObj = new FileDependency(jar);
				fileDependencyObj.setGradleFormatDependency(gradleDependency.getGradleFormat());
				fileDependencyObj.setDependencyType(gradleDependency.getDependencyType());
				fileDependencies.add(fileDependencyObj);
			} catch (NoSuchElementException e) {
				System.err.println("Could not download dependency " + gradleDependency.getGradleFormat());
			} catch (IOException e) {
				System.err.println("Could not open dependency " + gradleDependency.getGradleFormat());
			}
		}

	}

	private void makeDependenciesTransitive() {
		// go to projects on which current project depend. If they have
		// the same dependencies as the parent project remove them in
		// currentProject
		for (Project project : projectList) {
			ArrayList<Project> projectDependencies = project.getDependencies();
			ArrayList<FileDependency> fileDependencies = project.getFileDependencies();
			ArrayList<Project> dependenciesIterateCopy = new ArrayList<Project>(projectDependencies);
			for (Project dependencyProject : dependenciesIterateCopy) {
				// remove duplicate projects
				for (Project dependencyProjectDependency : dependencyProject.getDependencies()) {
					projectDependencies.remove(dependencyProjectDependency);
				}
				// remove duplicate fileDependencies
				for (FileDependency dependencyProjectFileDependency : dependencyProject.getFileDependencies()) {
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
			for (FileDependency fileDep : fileDependencies) {
				ArrayList<String> imports = project.getImports();
				for (String exportClass : fileDep.getClasses()) {
					if (imports.contains(exportClass)) {
						project.addFileDependency(fileDep);
					}
				}
			}
		}
	}

	private void removeProjects(String... removeProjects) {
		int sizeBeforeRemoval = projectList.size();
		Iterator<Project> projectIterator = projectList.iterator();
		while (projectIterator.hasNext()) {
			String projectName = projectIterator.next().getName();
			for (String removeProject : removeProjects) {
				if (projectName.equals(removeProject)) {
					projectIterator.remove();
				}
			}
		}
		int sizeAfterRemoval = projectList.size();
		int numberProjectsRemoved = sizeBeforeRemoval - sizeAfterRemoval;
		System.out.println(numberProjectsRemoved + " projects removed");
	}

	public static void main(String[] args) throws InterruptedException {
		Workspace workspace = new Workspace("C:\\Users\\Jan\\Desktop\\git\\jbox2d");
		workspace.removeProjects(".git", ".gradle", "gradle");
		workspace.setTransitiveDependencies(true);
		// workspace.addFileDependencies("C:\\Users\\Jan\\.gradle\\caches\\modules-2\\files-2.1\\junit\\junit\\4.11\\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\\junit-4.11.jar");
		workspace.addGradleDependency("junit:junit:4.10", DependencyType.TESTCOMPILE);
		workspace.workspaceConfigurationsFinished();
		workspace.generateGradleSuprojectFiles();
		workspace.generateSettingsDotGradleFile();
		workspace.printDependencyMatrix();
	}
}
