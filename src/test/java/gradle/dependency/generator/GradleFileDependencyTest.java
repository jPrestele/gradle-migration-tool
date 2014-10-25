package gradle.dependency.generator;

import org.junit.Test;
import static org.junit.Assert.*;

public class GradleFileDependencyTest {
	GradleDependency dependency = new GradleDependency("testGroup:testName:testVersion");

	@Test
	public void getterTest() {
		assertEquals("testGroup", dependency.getGroup());
		assertEquals("testName", dependency.getName());
		assertEquals("testVersion", dependency.getVersion());
	}
}
