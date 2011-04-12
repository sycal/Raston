package util;

public class SqlUtility {

	private SqlUtility() {
	}
	
	public static String quote(String name) {
		return '[' + name + ']';
	}
	
	public static String stringQuote(String value) {
		return "'" + escape(value) + "'";
	}
	
	public static String escape(String value) {
		return value.replace("'", "''");
	}
}
