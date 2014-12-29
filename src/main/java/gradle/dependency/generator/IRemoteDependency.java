package gradle.dependency.generator;

public interface IRemoteDependency {

	public String getName();

	public String getGroup();

	public String getVersion();

	/*
	 * return the dependency in the format in which the dependency is usually
	 * specified
	 */
	public String getDefinition();

}
