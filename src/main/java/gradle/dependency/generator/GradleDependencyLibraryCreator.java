package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleDependencyLibraryCreator {
	private ArrayList<FileDependency> fileDependencies;
	private WritableFile outputFile;

	public GradleDependencyLibraryCreator(Workspace workspace) {
		String workspacePath = workspace.getWorkspaceRoot().getAbsolutePath();
		outputFile = new WritableFile(workspacePath + "\\" + "library.gradle");
		fileDependencies = workspace.getFileDependencies();
	}

	public void generateGradleFileDepLibrary() {
		generateFileBeginning();
	}

	private void generateFileBeginning() {
	}

}
