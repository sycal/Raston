package email;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;

public class EmailAddress {
	
	private String baseEmailAddress;
	private String friendlyName;
	
	public EmailAddress(String baseEmailAddress, String friendlyName) {
		this.baseEmailAddress = baseEmailAddress;
		this.friendlyName = friendlyName;
	}
	
	public String toSimpleAddress() {
		return this.baseEmailAddress;
	}
	
	public String toFriendlyAddress() {
		if (friendlyName != null) {
			return String.format("\"%s\" <%s>", this.friendlyName, this.baseEmailAddress);
		} else {
			return String.format("<%s>", this.baseEmailAddress);
		}
	}
	
	public String toEnvelopeAddress() {
		return String.format("<%s>", this.baseEmailAddress);
	}

	public static EmailAddress parse(String text) {
		
		// ! # $ % & ' * + - / = ? ^ _ ` { | } ~
		// \\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~
		String localPartAtom = "(?:[\\w\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]+)";
		String localPartAtomDot = "(?:[\\w\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]+\\.)";
		String atom = "(?:[\\w\\-']+)";
		String atomDot = "(?:[\\w\\-']+\\.)";
		String emailAddress = String.format("%s*%s@%s*%s", localPartAtomDot, localPartAtom, atomDot, atom);
		String friendlyPattern = "(?:\"({friendly}.+?)\" ?)";
		String emailPattern = String.format("(?:<?({email}%s)>?)", emailAddress);
		String quotedPattern = String.format("^%s?%s$", friendlyPattern, emailPattern);
		Pattern p = new Pattern(quotedPattern, REFlags.IGNORE_CASE);
		Matcher m = p.matcher(text);
		if (m.find()) {
			String email = m.group("email");
			String friendlyName = m.group("friendly");
			return new EmailAddress(email, friendlyName);
		} else {
			return null;
		}
	}
}
