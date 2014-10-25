package gradle.dependency.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileDependency {
	private JarFile jarFile;
	private ArrayList<String> classes = new ArrayList<String>();
	private boolean isGradleDependency = false;
	private String gradleFormatDependency;
	private DependencyType dependencyType = DependencyType.COMPILE;

	/*
	 * Once an actual jar is added to the dependency the getClasses() methods
	 * can be used to get the exports
	 */
	public FileDependency(File file) throws IOException {
		jarFile = new JarFile(file);
		populateClasses();
	}

	private void populateClasses() {
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			String jarEntry = jarEntries.nextElement().getName();
			if (jarEntry.endsWith(".class")) {
				String classImportFormat = entryToImportFormat(jarEntry);
				classes.add(classImportFormat);
			}
		}
	}

	public ArrayList<String> getClasses() {
		return classes;
	}

	public String getName() {
		return new File(jarFile.getName()).getName();
	}

	public String getPath() {
		return jarFile.getName();
	}

	public String getGradleFormatDependency() {
		return gradleFormatDependency;
	}

	public void setGradleFormatDependency(String gradleFormatDependency) {
		this.gradleFormatDependency = gradleFormatDependency;
		isGradleDependency = true;
	}

	public boolean isGradleDependency() {
		return isGradleDependency;
	}

	private String entryToImportFormat(String entry) {
		String entryNoSuffix = entry.replace(".class", "");
		String entryPackageFormat = entryNoSuffix.replace("/", ".");
		return entryPackageFormat;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}

	/*
	 * Default dependency type is compile
	 */
	public void setDependencyType(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}
}
