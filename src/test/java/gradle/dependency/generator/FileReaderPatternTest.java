package gradle.dependency.generator;

import static org.junit.Assert.*;
import gradle.migration.tool.utility.FileReaderPattern;

import java.io.File;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class FileReaderPatternTest {
	FileReaderPattern fileReader;

	@Before
	public void initialize() {
		File testClass = new File("C:\\Program Files\\eclipse\\workspace\\gradle-dependency-generator\\src\\test\\resources\\project-test\\project\\test1\\Class1.java");
		fileReader = new FileReaderPattern(testClass);
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

		assertEquals(expectedList, fileReader.getMatchesTillStopCondition("import.*", ".*public class.*"));
	}
}
