package gradle.migration.tool.workspace;

public enum DependencyType {
	COMPILE("compile"), TESTCOMPILE("testCompile"), RUNTIME("runtime");

	private final String type;

	private DependencyType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
