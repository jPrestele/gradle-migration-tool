package gradle.migration.tool.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Dependency {
	private JarFile jar;
	private boolean hasJar = false;
	private ArrayList<String> exportedClasses = new ArrayList<String>();
	private DependencyType dependencyType = DependencyType.COMPILE;

	public String getJarName() {
		return new File(jar.getName()).getName();
	}

	public ArrayList<String> getClasses() {
		return exportedClasses;
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

	/*
	 * Once a jar is set the exportedClasses will be populated
	 */
	public void setJar(JarFile jar) {
		this.jar = jar;
		hasJar = true;
		populateExportedClasses();
	}

	public boolean hasJar() {
		return hasJar;
	}

	private void populateExportedClasses() {
		Enumeration<JarEntry> jarEntries = jar.entries();
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
