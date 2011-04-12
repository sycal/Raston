package email.smtp.rules;

import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.Element;

import core.SystemFailureException;
import email.EmailAddress;
import email.Envelope;

public interface ISmtpRuleTask {
	
	void initialise(Element taskElement) throws SystemFailureException;
	void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) throws Exception;
	
}
