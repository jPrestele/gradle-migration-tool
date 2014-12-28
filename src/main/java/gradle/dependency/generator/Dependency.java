package gradle.dependency.generator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Dependency {
	private JarFile jarFile;
	private ArrayList<String> exportedClasses = new ArrayList<String>();
	private String jarName;
	private boolean hasJar = false;

	private DependencyType dependencyType = DependencyType.COMPILE;
	private String dependencyGradleFormat;
	private String name;
	private String group;
	private String version;

	public Dependency(String dependencyGradleFormat) {
		this.dependencyGradleFormat = dependencyGradleFormat;
		populateGroupNameVersion();
		jarName = name + '-' + version + ".jar";
	}

	public String getPath() {
		return jarFile.getName();
	}

	/*
	 * Returns String of dependency definition in gradle format(
	 * group:name:version)
	 */
	public String getGradleFormat() {
		return dependencyGradleFormat;
	}

	public ArrayList<String> getClasses() {
		return exportedClasses;
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getJarName() {
		return jarName;
	}

	public DependencyType getDependencyType() {
		return this.dependencyType;
	}

	/*
	 * Default dependency type is COMPILE
	 */
	public void setDependencyType(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}

	public void setJar(JarFile jar) {
		this.jarFile = jar;
		populateExportedClasses();
		hasJar = true;
	}

	public boolean hasJar() {
		return hasJar;
	}

	private void populateGroupNameVersion() {
		String dependency = dependencyGradleFormat;
		for (int i = 1; i <= 2; i++) {
			int colonIndex = dependency.indexOf(':');
			String subString = dependency.substring(0, colonIndex);
			dependency = dependency.substring(colonIndex + 1, dependency.length());
			if (i == 1) {
				group = subString;
			}
			if (i == 2) {
				name = subString;
			}
		}
		version = dependency;
	}

	private void populateExportedClasses() {
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			String jarEntry = jarEntries.nextElement().getName();
			if (jarEntry.endsWith(".class")) {
				String classExportFormat = normalizeEntry(jarEntry);
				exportedClasses.add(classExportFormat);
			}
		}
	}

	private String normalizeEntry(String entry) {
		String entryNoSuffix = entry.replace(".class", "");
		String entryPackageFormat = entryNoSuffix.replace("/", ".");
		return entryPackageFormat;
	}

}
