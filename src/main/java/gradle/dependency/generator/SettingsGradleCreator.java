package gradle.dependency.generator;

import java.util.ArrayList;

public class SettingsGradleCreator {
	private static String newLine = System.lineSeparator();
	private static final String fileName = "settings.gradle";

	public static void generateGradleSettingsFile(Workspace workspace) {
		StringBuilder output = new StringBuilder("include" + newLine);
		ArrayList<Project> projectList = workspace.getProjects();
		for (Project project : projectList) {
			output.append("\t" + "'" + project.getName() + "'," + newLine);
		}
		// remove final comma
		output.deleteCharAt(output.length() - newLine.length() - 1);
		WritableFile settingsGradle = new WritableFile(workspace.getWorkspaceRoot() + "\\" + fileName);
		settingsGradle.create(output.toString());
	}
}
