package gradle.dependency.generator;

import java.io.File;
import java.util.ArrayList;

public class GradleDependencyDownloader {
	/*
	 * Class creates temporary build.gradle to download all the dependencies
	 */
	private ArrayList<GradleDependency> dependencies;
	private String repoUrl;

	private static final String tempFolderPath = "gradleDependencyGeneratorTempFolder";
	private static final WritableFile tempFile = new WritableFile(tempFolderPath + "/build.gradle");

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

	private void writeRepo() {
		tempFile.append("repositories { maven { url '" + repoUrl + "' } }");
		tempFile.newLine();
	}

	/*
	 * uses gradle's mavenCentral() method
	 */
	// public void setMavenCentralRepo() {
	// tempFile.append("repositories { mavenCentral() }");
	// tempFile.newLine();
	// }

	private void generateDependencyEntries() {
		tempFile.append("dependencies { ");
		for (GradleDependency dependency : dependencies) {
			tempFile.append("compile '" + dependency.getGradleFormat() + "'");
			tempFile.newLine();
		}
		tempFile.append("}");
	}

	public void downloadDependencies() {
		writeGradleJavaPlugin();
		writeRepo();
		writeGradleDependencies();
		GradleCall gradleCall = new GradleCall(tempFolderPath, "dependencies");
		gradleCall.execGradleCommand();
		removeTempFiles();
	}

	private void removeTempFiles() {
		tempFile.delete();
		File folder = new File(tempFolderPath);
		folder.delete();
	}

	/*
	 * add Java plugin to Gradle file so compile time configuration can be used
	 * for dependency
	 */
	private void writeGradleJavaPlugin() {
		tempFile.append("apply plugin: 'java'");
		tempFile.newLine();
	}

	private void writeGradleDependencies() {
		tempFile.getParentFile().mkdir();
		generateDependencyEntries();
		tempFile.write();
	}

	// put in test
	public static void main(String[] args) {
		GradleDependency dep1 = new GradleDependency("org.apache.httpcomponents:httpclient:4.3.5");
		GradleDependency dep2 = new GradleDependency("junit:junit:4.10");
		String repo = "http://repo1.maven.org/maven2/";
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(repo);
		downloader.addDependency(dep2);
		downloader.downloadDependencies();
	}
}
