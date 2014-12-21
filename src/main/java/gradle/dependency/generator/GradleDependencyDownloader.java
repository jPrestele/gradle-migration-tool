package gradle.dependency.generator;

import java.io.File;
import java.util.ArrayList;

public class GradleDependencyDownloader {
	/*
	 * Class creates temporary build.gradle to download all the dependencies
	 */
	private ArrayList<GradleDependency> dependencies;
	private String repoUrl;
	private WritableFile tempFile = new WritableFile(tempFolderPath + "/build.gradle");
	private static String tempFolderPath = "gradleDependencyGeneratorTempFolder";

	/*
	 * If no repository is specified MavenCentral is used
	 */
	public GradleDependencyDownloader(String repositoryUrl, GradleDependency... fileDependencies) {
		dependencies = new ArrayList<GradleDependency>();
		repoUrl = repositoryUrl;
		for (GradleDependency dep : fileDependencies) {
			dependencies.add(dep);
		}
		activateGradleJavaPlugin();
		if (repoUrl == null) {
			setMavenCentralRepo();
		} else
			addRepo();
	}

	/*
	 * Constructor which should be used if MavenCentral repository should be
	 * used to donwload dependencies
	 */
	public GradleDependencyDownloader(GradleDependency... fileDependencies) {
		this(null, fileDependencies);
	}

	public void addRepo() {
		tempFile.append("repositories { maven { url '" + repoUrl + "' } }");
		tempFile.newLine();
	}

	/*
	 * uses gradle's mavenCentral() method
	 */
	public void setMavenCentralRepo() {
		tempFile.append("repositories { mavenCentral() }");
		tempFile.newLine();
	}

	public void generateDependencyEntries() {
		tempFile.append("dependencies { ");
		for (GradleDependency dependency : dependencies) {
			tempFile.append("compile '" + dependency.getDependencyInGradleFormat() + "'");
			tempFile.newLine();
		}
		tempFile.append("}");
	}

	public void downloadDependencies() {
		writeGradleDependencyFile();
		GradleCall gradleCall = new GradleCall(tempFolderPath, "dependencies");
		gradleCall.execGradleCommand();
		removeTempFiles();
	}

	// TODO: maybe remove check
	private void removeTempFiles() {
		tempFile.delete();
		File folder = new File(tempFolderPath);
		folder.delete();
	}

	/*
	 * add Java plugin to Gradle file so compile time configuration can be used
	 * for dependency
	 */
	private void activateGradleJavaPlugin() {
		tempFile.append("apply plugin: 'java'");
		tempFile.newLine();
	}

	private void writeGradleDependencyFile() {
		tempFile.getParentFile().mkdir();
		generateDependencyEntries();
		tempFile.write();
	}

	public static void main(String[] args) {
		GradleDependency dep1 = new GradleDependency("org.apache.httpcomponents:httpclient:4.3.5");
		GradleDependencyDownloader downloader = new GradleDependencyDownloader(dep1);
		downloader.downloadDependencies();
	}
}
