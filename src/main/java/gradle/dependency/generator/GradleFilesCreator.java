package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleFilesCreator {
	private Workspace workspace;
	private boolean useDependencyLibraries = false;

	public GradleFilesCreator(Workspace workspace) {
		this.workspace = workspace;
	}

	public void generateGradleSuprojectFiles() {
		for (Project project : workspace.getProjects()) {
			WritableFile gradleBuildFile = new WritableFile(project.getFilePath() + "\\build.gradle");
			// don't create build.gradle if no dependencies exist for project
			if (project.getDependencies().isEmpty() == false || project.getFileDependencies().isEmpty() == false) {
				gradleBuildFile.append("dependencies {").newLine();
				for (Project projectDependency : project.getDependencies()) {
					gradleBuildFile.append("\t" + projectDependency.getDependencyType().getType() + " project(':" + projectDependency.getName() + "')").newLine();
				}
				gradleBuildFile.newLine();
				for (Dependency fileDependency : project.getFileDependencies()) {
					String dependencyEntry = deterimeDependencyEntry(fileDependency);
					gradleBuildFile.append("\t" + fileDependency.getDependencyType().getType() + " '" + dependencyEntry + "'").newLine();
				}
				gradleBuildFile.append("}");
			}
			gradleBuildFile.write();
		}
	}

	private String deterimeDependencyEntry(Dependency dependency) {
		if (useDependencyLibraries) {
			return "libraries." + dependency.getName();
		} else {
			return dependency.getGradleFormat();
		}

	}

	public void generateGradleDependencylibrariesFile() {
		WritableFile librariesFile = new WritableFile(workspace.getWorkspaceRoot() + "/libraries.gradle");
		librariesFile.append("ext {").newLine().append("\tlibraries = [").newLine();
		ArrayList<Dependency> dependencies = workspace.getWorkspaceDependencies();
		for (Dependency dependency : dependencies) {
			librariesFile.append("\t\t" + dependency.getName() + " : \"" + dependency.getGradleFormat() + "\"");
			if (!isLastItem(dependency, dependencies)) {
				librariesFile.append(",");
			}
			librariesFile.newLine();
		}
		librariesFile.append("\t]").newLine().append("}");
		librariesFile.write();
		useDependencyLibraries = true;
	}

	private <T> boolean isLastItem(T item, ArrayList<T> array) {
		int maxIndex = array.size() - 1;
		if (array.indexOf(item) == maxIndex) {
			return true;
		}
		return false;
	}

	public void generateGradleSettingsFiles() {
		WritableFile file = new WritableFile(workspace.getWorkspaceRoot() + "\\.settings");
		file.append("include").newLine();
		ArrayList<Project> projectList = workspace.getProjects();
		for (Project project : projectList) {
			file.append("\t'" + project.getName() + "'");
			// ensure last entry in the file doesn't have a comma
			if (!isLastItem(project, projectList)) {
				file.append(",");
			}
			file.newLine();
		}
		file.write();
	}
}
