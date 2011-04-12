package email.smtp.rules;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import email.EmailAddress;
import email.Envelope;

public class HeaderTask extends SmtpRuleTaskBase implements ISmtpRuleTask {

	private static Logger logger = Logger.getLogger(HeaderTask.class);
	
	private String headerName;
	private String headerValue;
	
	@Override
	public void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) {
		this.setTokenMatchingObjects(envelope, emailGroups);
		String actualHeaderName = this.replaceTokens(this.headerName);
		String actualHeaderValue = this.replaceTokens(this.headerValue);
		logger.info(String.format("%s: Setting header '%s: %s'", envelope.getEmail().getId(), actualHeaderName, actualHeaderValue));
		envelope.getEmail().getHeaders().setHeader(actualHeaderName, actualHeaderValue);
	}

	@Override
	public void initialise(Element taskElement) {
		this.headerName = taskElement.getAttribute("name");
		this.headerValue = taskElement.getAttribute("value");
	}
}
