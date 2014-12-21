package gradle.dependency.generator;

public class GradleDependency {
	private String dependencyGradleFormat;
	private String group;
	private String name;
	private String version;
	private String jarName;
	private DependencyType dependencyType = DependencyType.COMPILE;

	/*
	 * Gradle dependency format : "group:name:version"
	 */
	public GradleDependency(String dependencyGradleFormat) {
		this.dependencyGradleFormat = dependencyGradleFormat;
		populateGroupNameVersion();
		jarName = name + '-' + version + ".jar";
	}

	public String getDependencyInGradleFormat() {
		return dependencyGradleFormat;
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
}
