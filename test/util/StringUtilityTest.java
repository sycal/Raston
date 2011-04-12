package util;

import junit.framework.TestCase;

public class StringUtilityTest extends TestCase {

	public void testCappedStringUncapped() {
		String original = "0123456789";
		assertEquals(original, StringUtility.cap(original, original.length(), "..."));
	}
	
	public void testCappedStringCapped() {
		String original = "0123456789";
		String suffix = "...";
		String expected = "012345" + suffix;
		assertEquals(expected, StringUtility.cap(original, expected.length(), suffix));
	}
}
