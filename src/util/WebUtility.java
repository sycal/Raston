package util;

public class WebUtility {
	
	private WebUtility() {}

	public static String urlEncode(String text) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == 32) {
				result.append('+');
			} else if ( ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) ) {
				result.append(c);
			} else if ((c == '-') || (c == '_') || (c == '.') || (c == '*')) {
				result.append(c);
			} else {
				result.append('%');
				String hex = Integer.toHexString(c).toUpperCase();
				if (hex.length() > 1) {
					result.append(hex);	
				} else {
					result.append('0');
					result.append(hex);
				}
			}
		}
		return result.toString();
	}
	
	public static String urlDecode(String text) {
		StringBuilder result = new StringBuilder();
		String temp = "";
		boolean decoding = false; 
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (decoding) {
				temp += c;
				if (temp.length() == 2) {
					int decoded = Integer.parseInt(temp, 16);
					result.append((char)decoded);
					decoding = false;
				}
			} else {
				switch (c) {
				case '+':
					result.append(' ');
					break;
				case '%':
					decoding = true;
					temp = "";
					break;
				default:
					result.append(c);
				}
			}
		}
		return result.toString();
	}
	
	public static String quotedPrintableEncode(String text) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if ((c >= 33) && (c <= 126) && (c != 61)) {
				result.append(c);
			} else {
				result.append('=');
				result.append(StringUtility.toHex(c, 2));
			}
		}
		return result.toString();
	}
}
