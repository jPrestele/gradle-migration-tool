package gradle.dependency.generator;

import static org.junit.Assert.*;
import gradle.migration.tool.workspace.utility.PatternFileReader;

import java.io.File;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class FileReaderPatternTest {
	PatternFileReader fileReader;

	@Before
	public void initialize() {
		File testClass = new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1\\Class1.java");
		fileReader = new PatternFileReader(testClass);
	}

	@Test
	public void readLineTest() {
		String regexTest = ".*private static.*test[rR]ege[xX];";
		String expectedString = "	private static String testRegex;";
		assertEquals(expectedString, fileReader.getRegexMatch(regexTest));
	}

	@Test
	public void getMatchesTillStopCondition() {
		LinkedList<String> expectedList = new LinkedList<String>();
		expectedList.add("import irgendwas.zum.testen;");
		expectedList.add("import nochmal.irgendwas.zum.testen;");

		assertEquals(expectedList, fileReader.getMatchesUntilStopMatch("import.*", ".*public class.*"));
	}
}
