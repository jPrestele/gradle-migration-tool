package gradle.dependency.generator;

import static org.junit.Assert.*;
import gradle.migration.tool.workspace.JavaFile;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class JavaFileTest {
	JavaFile javaFile = new JavaFile("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1\\Class1.java");

	@Test
	public void getImportsTest() {
		LinkedList<String> expectedImports = new LinkedList<String>();
		expectedImports.add("irgendwas.zum.testen");
		expectedImports.add("nochmal.irgendwas.zum.testen");

		assertEquals(expectedImports, javaFile.getImports());
	}
}
