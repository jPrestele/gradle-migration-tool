package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleFilesCreator {
	private Workspace workspace;

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
				for (FileDependency fileDependency : project.getFileDependencies()) {
					if (fileDependency.isGradleDependency()) {
						gradleBuildFile.append("\t" + fileDependency.getDependencyType().getType() + " '" + fileDependency.getGradleFormatDependency() + "'").newLine();
					} else {
						gradleBuildFile.append("\tcompile files('" + fileDependency.getPath() + "')").newLine();
					}
				}
				gradleBuildFile.append("}");
			}
			gradleBuildFile.write();
		}
	}

	public void generateGradleSettingsFiles() {
		WritableFile file = new WritableFile(workspace.getWorkspaceRoot() + "\\.settings");
		file.append("include").newLine();
		for (Project project : workspace.getProjects()) {
			file.append("\t'" + project.getName() + "'");
			// ensure last entry in the file doesn't have a comma
			if (!isLastProject(project)) {
				file.append(",");
			}
			file.newLine();
		}
		file.write();
	}

	private boolean isLastProject(Project project) {
		ArrayList<Project> projectList = workspace.getProjects();
		int lastIndex = projectList.size() - 1;
		if (projectList.indexOf(project) == lastIndex) {
			return true;
		} else {
			return false;
		}
	}
}
