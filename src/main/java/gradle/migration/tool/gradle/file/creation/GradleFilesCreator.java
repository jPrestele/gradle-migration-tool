package gradle.migration.tool.gradle.file.creation;

import gradle.migration.tool.dependencies.GradleDependency;
import gradle.migration.tool.utility.WritableFile;
import gradle.migration.tool.workspace.Dependency;
import gradle.migration.tool.workspace.Project;
import gradle.migration.tool.workspace.Workspace;

import java.util.ArrayList;

public class GradleFilesCreator {
	private Workspace workspace;
	private boolean useDependencyLibraries = false;

	public GradleFilesCreator(Workspace workspace) {
		this.workspace = workspace;
	}

	public void generateGradleSuprojectFiles() {
		for (Project project : workspace.getProjects()) {
			WritableFile gradleBuildFile = new WritableFile(project.getFile().getAbsolutePath() + "\\build.gradle");
			ArrayList<Dependency> fileDependencies = project.getDependencies();
			ArrayList<Project> projectDependencies = project.getProjectDependencies();
			// don't create build.gradle if no dependencies exist for project
			if (!projectDependencies.isEmpty() || !fileDependencies.isEmpty()) {
				gradleBuildFile.append("dependencies {").newLine();
				// Project dependency entries
				for (Project projectDependency : projectDependencies) {
					gradleBuildFile.append("\t" + projectDependency.getDependencyType().getType() + " project(':" + projectDependency.getName() + "')").newLine();
				}
				// only seperate project dependencies and file dependencies if
				// both are present
				if (!projectDependencies.isEmpty() && !fileDependencies.isEmpty()) {
					gradleBuildFile.newLine();
				}
				for (Dependency fileDependency : fileDependencies) {
					if (fileDependency instanceof GradleDependency) {
						GradleDependency gradleDependency = (GradleDependency) fileDependency;
						String dependencyEntry = deterimeDependencyEntry(gradleDependency);
						gradleBuildFile.append("\t" + gradleDependency.getDependencyType().getType() + " " + dependencyEntry).newLine();
					}
				}
				gradleBuildFile.append("}");
				gradleBuildFile.write();
			}
		}
	}

	/*
	 * if a dependency library is created the dependency entries in the build
	 * will refer to the created library
	 */
	private String deterimeDependencyEntry(GradleDependency dependency) {
		if (useDependencyLibraries) {
			return "libraries." + dependency.getName();
		} else {
			return "'" + dependency.getDependencyDefinition() + "'";
		}

	}

	public void generateGradleDependencylibrariesFile() {
		WritableFile librariesFile = new WritableFile(workspace.getRootFile() + "/libraries.gradle");
		librariesFile.append("ext {").newLine().append("\tlibraries = [").newLine();
		ArrayList<Dependency> dependencies = workspace.getDependencies();
		for (Dependency dependency : dependencies) {
			if (dependency instanceof GradleDependency) {
				GradleDependency gradleDependency = (GradleDependency) dependency;
				librariesFile.append("\t\t" + gradleDependency.getName() + " : \"" + gradleDependency.getDependencyDefinition() + "\"");
			}
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
		WritableFile file = new WritableFile(workspace.getRootFile() + "\\.settings");
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
