package gradle.migration.tool.workspace.utility;

import gradle.migration.tool.workspace.Dependency;
import gradle.migration.tool.workspace.Project;
import gradle.migration.tool.workspace.Workspace;

import java.util.ArrayList;

import dnl.utils.text.table.TextTable;

public class WorkspacePrinter {

	private Workspace workspace;

	public WorkspacePrinter(Workspace workspace) {
		this.workspace = workspace;
	}

	public void printDependencyMatrix() {
		ArrayList<Project> projects = workspace.getProjects();
		ArrayList<Dependency> externalLibraries = workspace.getDependencies();
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
					if (projects.get(i).getProjectDependencies().contains(projects.get(j - 1))) {
						data[i][j] = new Integer(1);
					}
				} else {
					// add file dependencies
					if (projects.get(i).getDependencies().contains(externalLibraries.get(j - projects.size() - 1))) {
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
}
