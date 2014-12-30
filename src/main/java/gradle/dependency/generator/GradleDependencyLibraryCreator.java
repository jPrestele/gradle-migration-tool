package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleDependencyLibraryCreator {
	private ArrayList<Dependency> fileDependencies;
	private WritableFile outputFile;

	public GradleDependencyLibraryCreator(Workspace workspace) {
		String workspacePath = workspace.getRootFile().getAbsolutePath();
		outputFile = new WritableFile(workspacePath + "\\" + "library.gradle");
		fileDependencies = workspace.getExternalLibraries();
	}

	public void generateGradleFileDepLibrary() {
		generateFileBeginning();
	}

	private void generateFileBeginning() {
	}

}
