package email.smtp.rules;

import java.util.Hashtable;
import java.util.List;

import core.TokenBase;
import email.EmailAddress;
import email.EmailUtility;
import email.Envelope;
import util.StringUtility;

public abstract class SmtpRuleTaskBase extends TokenBase {

	private final String TOKEN_NAME_HEADER = "header:";
	private final String TOKEN_NAME_GROUP = "group:";
	
	private Envelope envelope;
	private Hashtable<String, List<EmailAddress>> emailGroups;
	
	protected void setTokenMatchingObjects(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) {
		this.envelope = envelope;
		this.emailGroups = emailGroups;
	}
	
	@Override
	protected String onEvaluateTokenName(String tokenName) {
		if (tokenName.equalsIgnoreCase("id")) {
			return this.envelope.getEmail().getId();
		} else if (tokenName.equalsIgnoreCase("envelopeFrom")) {
			return this.envelope.getMailFrom().toSimpleAddress();
		} else if (tokenName.equalsIgnoreCase("envelopeTo")) {
			return EmailUtility.combineEmailAddresses(this.envelope.getRcptToList());
		} else if (tokenName.equalsIgnoreCase("ipFrom")) {
			return this.envelope.getSourceIPAddress();
		} else if (tokenName.equalsIgnoreCase("subject")) {
			return StringUtility.emptyIfNull(this.envelope.getEmail().getSubject());
		} else if (tokenName.equalsIgnoreCase("email")) {
			return this.envelope.getEmail().toString();
		} else if (tokenName.equalsIgnoreCase("body")) {
			return this.envelope.getEmail().getRawBody();
		} else if (tokenName.startsWith(TOKEN_NAME_HEADER)) {
			String headerName = tokenName.substring(TOKEN_NAME_HEADER.length());
			return this.envelope.getEmail().getHeaders().getHeader(headerName);
		} else if (tokenName.startsWith(TOKEN_NAME_GROUP)) {
			String groupName = tokenName.substring(TOKEN_NAME_GROUP.length());
			if (this.emailGroups.containsKey(groupName)) {
				return EmailUtility.combineEmailAddresses(this.emailGroups.get(groupName));
			} else {
				return "";
			}
		} else {
			return super.onEvaluateTokenName(tokenName);
		}
	}
	
	@Override
	protected String onEvaluateTokenAction(String tokenName, String tokenValue, String tokenAction) {
		if (tokenAction.equalsIgnoreCase("name") && StringUtility.isIn(tokenName, "envelopeFrom", "envelopeTo")) {
			return EmailUtility.stripNameFromEmail(tokenValue);
		} else {
			return super.onEvaluateTokenAction(tokenName, tokenValue, tokenAction);
		}
	}
}
