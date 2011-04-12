package util;

import java.util.List;

public class StringUtility {

	private StringUtility() {}
	
	public static final String CRLF = "\r\n";
	public static final String CRLFCRLF = CRLF + CRLF;
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final String DOUBLE_QUOTE = "\"";
	
	public static boolean isNullOrEmpty(String valueToTest) {
		return valueToTest == null ? true : (valueToTest.length() == 0);
	}
	
	public static boolean has(String valueToTest) {
		return valueToTest == null ? false : (valueToTest.length() > 0);
	}
	
	public static String coalesceNull(String value1, String value2) {
		return (value1 == null) ? value2 : value1;
	}
	
	public static String coalesceEmpty(String value1, String value2) {
		return isNullOrEmpty(value1) ? value2 : value1;
	}
	
	public static String toFullLinePattern(String pattern) {
		String outputPattern = pattern.startsWith("^") ? pattern : '^' + pattern;
		return outputPattern.endsWith("$") ? outputPattern : outputPattern + '$';
	}
	
	public static String toFixedPattern(String pattern) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch (c) {
			case '.':
			case '\\':
			case '[':
			case ']':
			case '(':
			case ')':
			case '$':
			case '^':
			case '|':
				result.append('\\');
			}
			result.append(c);
		}
		return result.toString();
	}
	
	public static String emptyIfNull(String value) {
		return (value == null) ? "" : value;
	}
	
	public static String nullIfEmpty(String value) {
		return (value == null || value.isEmpty()) ? null : value;
	}
	
	public static String join(List<String> itemList, String delim) {
		StringBuilder output = new StringBuilder();
		for (int i=0; i<itemList.size(); i++) {
			if (i > 0) output.append(delim);
			output.append(itemList.get(i));
		}
		return output.toString();
	}

	public static String doubleQuote(String value) {
		return DOUBLE_QUOTE + value.replace(DOUBLE_QUOTE, DOUBLE_QUOTE + DOUBLE_QUOTE) + DOUBLE_QUOTE;
	}
	
	public static String setSuffix(String line, String suffix) {
		return line.endsWith(suffix) ? line : line + suffix;
	}
	
	public static String removeSuffix(String text, String suffix) {
		return text.endsWith(suffix) ? text.substring(0, text.length() - suffix.length()) : text;
	}
	
	public static boolean isIn(String testValue, String... items) {
		for (int i=0; i<items.length; i++) {
			if (testValue.equalsIgnoreCase(items[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static String cap(String text, int maxLength, String cappedSuffix) {
		if (text.length() > maxLength) {
			return text.substring(0, maxLength - cappedSuffix.length()) + cappedSuffix; 
		} else {
			return text;
		}
	}
	
	public static String toCsvLine(String[] values) {
		StringBuilder result = new StringBuilder();
		boolean isFirst = true;
		for (String value : values) {
			if (isFirst) {
				isFirst = false;
			} else {
				result.append(',');
			}
			result.append(toCsv(value));
		}
		return result.toString();
	}
	
	public static String toCsv(String value) {
		return requiresCsvQuoting(value) ? doubleQuote(value) : value;
	}
	
	private static boolean requiresCsvQuoting(String text) {
		if (text.startsWith(" ") || text.endsWith(" ")) {
			return true;
		}
		for (int i=0; i<text.length(); i++) {
			switch (text.charAt(i)) {
			case '\n':
			case '\r':
			case ',':
			case '"':
				return true;
			}
		}
		return false;
	}
	
	public static String toHex(int value, int totalWidth) {
		String hex = Integer.toHexString(value).toUpperCase();
		return padLeft(hex, totalWidth, '0');
	}
	
	public static String padLeft(String text, int totalWidth, char paddingChar) {
		int paddingCharTotal = totalWidth - text.length();
		StringBuilder result = new StringBuilder();
		for (int i=0; i<paddingCharTotal; i++) {
			result.append(paddingChar);
		}
		result.append(text);
		return result.toString();
	}
	
	public static String padRight(String text, int totalWidth, char paddingChar) {
		int paddingCharTotal = totalWidth - text.length();
		StringBuilder result = new StringBuilder(text);
		for (int i=0; i<paddingCharTotal; i++) {
			result.append(paddingChar);
		}
		return result.toString();
	}
}
