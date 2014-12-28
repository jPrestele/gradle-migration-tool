package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleDependencyLibraryCreator {
	private ArrayList<Dependency> fileDependencies;
	private WritableFile outputFile;

	public GradleDependencyLibraryCreator(Workspace workspace) {
		String workspacePath = workspace.getWorkspaceRoot().getAbsolutePath();
		outputFile = new WritableFile(workspacePath + "\\" + "library.gradle");
		fileDependencies = workspace.getWorkspaceDependencies();
	}

	public void generateGradleFileDepLibrary() {
		generateFileBeginning();
	}

	private void generateFileBeginning() {
	}

}
