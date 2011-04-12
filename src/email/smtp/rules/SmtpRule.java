package email.smtp.rules;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;

import org.apache.log4j.Logger;

import email.EmailAddress;
import email.Envelope;

import util.StringUtility;

public class SmtpRule {

	private static Logger logger = Logger.getLogger(SmtpRule.class);
	
	private String name;
	private Pattern reEnvelopeFrom;
	private Pattern reEnvelopeTo;
	private Pattern reIPFrom;
	private boolean finalRule;
	private List<ISmtpRuleTask> taskList;
	
	public SmtpRule(String name, String envelopeFrom, String envelopeTo, String ipFrom, boolean finalRule) {
		this.name = name;
		this.reEnvelopeFrom = this.compilePattern(envelopeFrom);
		this.reEnvelopeTo = this.compilePattern(envelopeTo);
		this.reIPFrom = this.compilePattern(ipFrom);
		this.taskList = new ArrayList<ISmtpRuleTask>();
		this.finalRule = finalRule;
	}
	
	public boolean processRule(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) throws Exception {
		if (this.isMatch(envelope)) {
			logger.info(String.format("%s: Matched rule <%s>", envelope.getEmail().getId(), this.name));
			for (ISmtpRuleTask task : this.taskList) {
				task.execute(envelope, emailGroups);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public List<ISmtpRuleTask> getTaskList() {
		return this.taskList;
	}
	
	public boolean isFinalRule() {
		return this.finalRule;
	}
	
	private Pattern compilePattern(String pattern) {
		return StringUtility.isNullOrEmpty(pattern) ? null : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}
	
	private boolean isMatch(Envelope envelope) {
		if (!this.isMatch(this.reEnvelopeFrom, envelope.getMailFrom().toSimpleAddress())) return false;
		if (this.reEnvelopeTo != null) {
			boolean envToMatched = false;
			for (EmailAddress envelopeTo : envelope.getRcptToList()) {
				if (this.isMatch(this.reEnvelopeTo, envelopeTo.toSimpleAddress())) {
					envToMatched = true;
					break;
				}
			}
			if (!envToMatched) return false;
		}
		if (!this.isMatch(this.reIPFrom, envelope.getSourceIPAddress())) return false;
		return true;
	}
	
	private boolean isMatch(Pattern compiledPattern, String itemToMatch) {
		if (compiledPattern == null) return true;
		Matcher m = compiledPattern.matcher(itemToMatch);
		return m.find();
	}
}
