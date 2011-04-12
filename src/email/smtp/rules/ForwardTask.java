package email.smtp.rules;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import email.EmailAddress;
import email.Envelope;
import email.smtp.SmtpForwardHandler;

import util.StringUtility;

public class ForwardTask extends SmtpRuleTaskBase implements ISmtpRuleTask {
	
	private static Logger logger = Logger.getLogger(ForwardTask.class);

	private String envelopeFrom;
	private String envelopeTo;
	
	@Override
	public void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) {
		String id = envelope.getEmail().getId();
		this.setTokenMatchingObjects(envelope, emailGroups);
		if (!StringUtility.isNullOrEmpty(this.envelopeFrom)) {
			EmailAddress mailFrom = EmailAddress.parse(this.replaceTokens(this.envelopeFrom));
			if (mailFrom != null) {
				logger.info(String.format("%s: Setting envelope-from : '%s'", id, mailFrom.toEnvelopeAddress()));
				envelope.setMailFrom(mailFrom);
			}
		}
		String envelopeToList = this.replaceTokens(this.envelopeTo);
		envelope.setRcptToList(envelopeToList);
		logger.info(String.format("%s: Forwarding to : '%s'", id, envelopeToList));
		SmtpForwardHandler.queueForSendOnly(envelope);
	}

	@Override
	public void initialise(Element taskElement) {
		this.envelopeFrom = taskElement.getAttribute("envelopeFrom");
		this.envelopeTo = taskElement.getAttribute("envelopeTo");
	}
}
