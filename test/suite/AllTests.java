package suite;

import util.StringUtilityTest;
import email.EmailAddressTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Raston tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(EmailAddressTest.class);
		suite.addTestSuite(StringUtilityTest.class);
		//$JUnit-END$
		return suite;
	}
}
