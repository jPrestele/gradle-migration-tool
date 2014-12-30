package gradle.migration.tool.utility;

import gradle.migration.tool.workspace.Dependency;
import gradle.migration.tool.workspace.RemoteGradleDependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class GradleDependencyDownloader {
	/*
	 * Class creates temporary build.gradle to download all the dependencies
	 */
	private ArrayList<Dependency> dependencies;
	private String repoUrl;
	private File gradleDependenciesCache;

	private static final String tempFolderPath = "src/main/resources/GradleDependencyDownloaderFolder";
	private static final WritableFile tempGradleBuildFile = new WritableFile(tempFolderPath + "/build.gradle");

	public GradleDependencyDownloader(String repositoryUrl) {
		dependencies = new ArrayList<Dependency>();
		repoUrl = repositoryUrl;
		String userHomeDir = System.getProperty("user.home");
		gradleDependenciesCache = new File(userHomeDir + "\\.gradle\\caches\\modules-2\\files-2.1");
	}

	public void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public void addDependencies(ArrayList<Dependency> dependencies) {
		this.dependencies.addAll(dependencies);
	}

	public void downloadDependencies() {
		appendGradleJavaPluginEntry();
		appendRepoEntry();
		writeGradleBuildFile();
		GradleCaller cleanCall = new GradleCaller(tempFolderPath, "clean");
		cleanCall.execGradleCommand();
		GradleCaller downloadCall = new GradleCaller(tempFolderPath, "build");
		downloadCall.execGradleCommand();
		removeTempFiles();
		searchAndSetDependencyJars();
	}

	/*
	 * searches in gradle's dependency cache for the downloaded dependencies
	 */
	private void searchAndSetDependencyJars() {
		for (Dependency dependency : dependencies) {
			if (dependency instanceof RemoteGradleDependency) {
				RemoteGradleDependency gradleDependency = (RemoteGradleDependency) dependency;
				try {
					File jar = FileUtils.listFiles(gradleDependenciesCache, FileFilterUtils.nameFileFilter(gradleDependency.getJarFileName()), TrueFileFilter.INSTANCE).iterator().next();
					dependency.setJar(new JarFile(jar));
				} catch (NoSuchElementException e) {
					System.err.println("Could not download dependency " + gradleDependency.getDependencyDefinition());
				} catch (IOException e) {
					System.err.println("Could not open dependency " + gradleDependency.getDependencyDefinition());
				}
			}
		}
	}

	private void appendRepoEntry() {
		tempGradleBuildFile.append("repositories { maven { url '" + repoUrl + "' } }");
		tempGradleBuildFile.newLine();
	}

	private void appendDependencyEntries() {
		tempGradleBuildFile.append("dependencies { ");
		for (Dependency dependency : dependencies) {
			if (dependency instanceof RemoteGradleDependency) {
				RemoteGradleDependency gradleDependency = (RemoteGradleDependency) dependency;
				tempGradleBuildFile.append("compile '" + gradleDependency.getDependencyDefinition() + "'");
				tempGradleBuildFile.newLine();
			}
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

	/*
	 * Only needs to be set if gradle cache isn't in userhome/.gradle/...
	 */
	public void setGradleDependencyCache(String path) {
		gradleDependenciesCache = new File(path);
	}

	// :TODO put in test
	// public static void main(String[] args) {
	// GradleDependency dep1 = new
	// GradleDependency("org.apache.httpcomponents:httpclient:4.3.5");
	// GradleDependency dep2 = new GradleDependency("junit:junit:4.10");
	// String repo = "http://repo1.maven.org/maven2/";
	// GradleDependencyDownloader downloader = new
	// GradleDependencyDownloader(repo);
	// downloader.addDependency(dep2);
	// downloader.downloadDependencies();
	// }
}
