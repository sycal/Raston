package email;

import junit.framework.TestCase;

public class EmailAddressTest extends TestCase {

	public void testConstructorWithToSimple() {
		EmailAddress addr = new EmailAddress("test@domain.com", "a friendly name");
		assertEquals("test@domain.com", addr.toSimpleAddress());
	}
	
	public void testConstructorWithToFriendly() {
		EmailAddress addr = new EmailAddress("test@domain.com", "a friendly name");
		assertEquals("\"a friendly name\" <test@domain.com>", addr.toFriendlyAddress());
	}
	
	public void testConstructorWithToEnvelope() {
		EmailAddress addr = new EmailAddress("test@domain.com", "a friendly name");
		assertEquals("<test@domain.com>", addr.toEnvelopeAddress());
	}
	
	public void testParseAddressOnly() {
		EmailAddress addr = EmailAddress.parse("test@domain.com");
		assertEquals(addr.toFriendlyAddress(), "<test@domain.com>");
	}
	
	public void testParseAddressWithBrackets() {
		EmailAddress addr = EmailAddress.parse("<test@domain.com>");
		assertEquals(addr.toFriendlyAddress(), "<test@domain.com>");
	}
	
	public void testParseFriendly() {
		EmailAddress addr = EmailAddress.parse("\"a friendly name\" <test@domain.com>");
		assertEquals(addr.toFriendlyAddress(), "\"a friendly name\" <test@domain.com>");
	}
	
	public void testParseFriendlyNoSpace() {
		EmailAddress addr = EmailAddress.parse("\"a friendly name\"<test@domain.com>");
		assertEquals(addr.toFriendlyAddress(), "\"a friendly name\" <test@domain.com>");
	}
	
	public void testParseFriendlyInvalidEmail() {
		EmailAddress addr = EmailAddress.parse("\"a friendly name\" test-domain.com");
		assertNull("Address should be NULL", addr);
	}
	
	public void testParseFriendlyComplex() {
		String complexEmail = "\"Qui & a compl ** \" <com-plex.te_123@my.SUB34.DOM-AIN.com>";
		EmailAddress addr = EmailAddress.parse(complexEmail);
		assertEquals(addr.toFriendlyAddress(), complexEmail);	
	}
	
	public void testParseAddressNoAt() {
		EmailAddress addr = EmailAddress.parse("test.domain.com");
		assertNull("Address should be NULL", addr);	
	}
	
	public void testParseAddressInvalidChar() {
		EmailAddress addr = EmailAddress.parse("test@dom£ain.com");
		assertNull("Address should be NULL", addr);	
	}
	
	public void testParseSingleQuote() {
		EmailAddress addr = EmailAddress.parse("test.o'name@domain.com");
		assertEquals(addr.toFriendlyAddress(), "<test.o'name@domain.com>");
	}
	
	public void testParseEmpty() {
		EmailAddress addr = EmailAddress.parse("");
		assertNull("Address should be NULL", addr);
	}
	
	public void testParseComplexLocalPart() {
		String complexEmail = "<!#$%&'*+-/=?^_`{|}~@domain.com>";
		EmailAddress addr = EmailAddress.parse(complexEmail);
		assertEquals(addr.toFriendlyAddress(), complexEmail);
	}
}
