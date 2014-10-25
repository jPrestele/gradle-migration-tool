package gradle.dependency.generator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

public class ProjectTest {
	Project project = new Project("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test");

	@Test
	public void getImportsTests() {
		ArrayList<String> expectedImports = new ArrayList<String>();
		expectedImports.add("irgendwas.zum.testen");
		expectedImports.add("nochmal.irgendwas.zum.testen");
		expectedImports.add("something.to.test");

		ArrayList<String> importResults = project.getImports();

		assertEquals(expectedImports, importResults);
	}

	@Test
	public void getPackagesTest() {
		ArrayList<Package> expectedPackages = new ArrayList<Package>();
		Package pack1 = new Package(new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1"));
		Package pack2 = new Package(new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test2"));

		expectedPackages.add(pack1);
		expectedPackages.add(pack2);

		ArrayList<Package> resultPackages = project.getPackages();

		// for (int i = 0; i < expectedPackages.size(); i++) {
		// System.out.println(expectedPackages.get(i).getPackageFile());
		// System.out.println(resultPackages.get(i).getPackageFile());
		// System.out.println(expectedPackages.get(i).getJavaPackageName());
		// System.out.println(resultPackages.get(i).getJavaPackageName());
		// System.out.println(expectedPackages.get(i).getJavaFiles());
		// System.out.println(resultPackages.get(i).getJavaFiles());
		// }

		Package pack3 = new Package(new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1"));
		Package pack4 = new Package(new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1"));

		System.out.println(pack3.hashCode());
		System.out.println(pack4.hashCode());
		System.out.println(pack3 + "\n" + pack4);
		assertSame(expectedPackages, resultPackages);
	}
}
