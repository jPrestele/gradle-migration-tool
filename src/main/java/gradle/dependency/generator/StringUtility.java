package gradle.dependency.generator;

public class StringUtility {

	public static String removeAllOccurences(String input, String... expressions) {
		String replacement = input;
		for (String expression : expressions) {
			replacement = replacement.replace(expression, "");
		}
		return replacement;
	}
}
