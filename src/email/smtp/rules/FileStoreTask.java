package email.smtp.rules;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import email.EmailAddress;
import email.Envelope;

import util.FileUtility;

public class FileStoreTask extends SmtpRuleTaskBase implements ISmtpRuleTask {

	private static Logger logger = Logger.getLogger(FileStoreTask.class);
	
	private String filePath;
	
	@Override
	public void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) {
		this.setTokenMatchingObjects(envelope, emailGroups);
		String actualFilePath = this.replaceTokens(this.filePath);
		if (FileUtility.saveAsciiFile(actualFilePath, envelope.getEmail().toString())) {
			logger.info(String.format("Written email content to '%s'", actualFilePath));
		} else {
			logger.error(String.format("Failed to write to file: '%s'", actualFilePath));
		}		
	}

	@Override
	public void initialise(Element taskElement) {
		this.filePath = taskElement.getAttribute("filePath");
	}
}
