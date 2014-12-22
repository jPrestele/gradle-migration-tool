package gradle.dependency.generator;

import java.util.ArrayList;

public class GradleDependencyDownloader {
	/*
	 * Class creates temporary build.gradle to download all the dependencies
	 */
	private ArrayList<GradleDependency> dependencies;
	private String repoUrl;

	private static final String tempFolderPath = "src/main/resources/GradleDependencyDownloaderFolder";
	private static final WritableFile tempGradleBuildFile = new WritableFile(tempFolderPath + "/build.gradle");

	public GradleDependencyDownloader(String repositoryUrl) {
		dependencies = new ArrayList<GradleDependency>();
		repoUrl = repositoryUrl;
	}

	public void addDependency(GradleDependency dependency) {
		dependencies.add(dependency);
	}

	public void addDependencies(ArrayList<GradleDependency> dependencies) {
		this.dependencies.addAll(dependencies);
	}

	public void downloadDependencies() {
		appendGradleJavaPluginEntry();
		appendRepoEntry();
		writeGradleBuildFile();
		GradleCaller gradleCall = new GradleCaller(tempFolderPath, "cleanBuild");
		gradleCall.execGradleCommand();
		removeTempFiles();
	}

	private void appendRepoEntry() {
		tempGradleBuildFile.append("repositories { maven { url '" + repoUrl + "' } }");
		tempGradleBuildFile.newLine();
	}

	private void appendDependencyEntries() {
		tempGradleBuildFile.append("dependencies { ");
		for (GradleDependency dependency : dependencies) {
			tempGradleBuildFile.append("compile '" + dependency.getGradleFormat() + "'");
			tempGradleBuildFile.newLine();
		}
		tempGradleBuildFile.append("}");
	}

	private void removeTempFiles() {
		tempGradleBuildFile.delete();
	}

	private void appendGradleJavaPluginEntry() {
		tempGradleBuildFile.append("apply plugin: 'java'");
		tempGradleBuildFile.newLine();
	}

	private void writeGradleBuildFile() {
		tempGradleBuildFile.getParentFile().mkdir();
		appendDependencyEntries();
		tempGradleBuildFile.write();
	}

	// :TODO put in test
	public static void main(String[] args) {
		GradleDependency dep1 = new GradleDependency("org.apache.httpcomponents:httpclient:4.3.5");
		GradleDependency dep2 = new GradleDependency("junit:junit:4.10");
		String repo = "http://repo1.maven.org/maven2/";
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(repo);
		downloader.addDependency(dep2);
		downloader.downloadDependencies();
	}
}
