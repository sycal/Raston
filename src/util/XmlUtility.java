package util;

import org.w3c.dom.Element;

public class XmlUtility {

	public static String get(Element element, String attributeName, String defaultValue) {
		String rawValue = element.getAttribute(attributeName);
		return (rawValue.length() > 0) ? rawValue : defaultValue;
	}
	
	public static int get(Element element, String attributeName, int defaultValue) {
		String rawValue = element.getAttribute(attributeName);
		if (rawValue.length() > 0) {
			try {
				return Integer.parseInt(rawValue);
			} catch (NumberFormatException e) {}
		}
		return defaultValue;
	}
}
