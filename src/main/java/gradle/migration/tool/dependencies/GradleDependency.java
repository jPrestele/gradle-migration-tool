package gradle.migration.tool.dependencies;

import gradle.migration.tool.workspace.Dependency;

public class GradleDependency extends Dependency implements IRemoteDependency {

	private String dependencyDefinition;
	private String name;
	private String group;
	private String version;
	private String jarFileName;

	public GradleDependency(String dependencyGradleFormat) {
		dependencyDefinition = dependencyGradleFormat;
		populateGroupNameVersion();
		jarFileName = name + '-' + version + ".jar";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getDependencyDefinition() {
		return dependencyDefinition;
	}

	public String getJarFileName() {
		return jarFileName;
	}

	private void populateGroupNameVersion() {
		String dependency = dependencyDefinition;
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
