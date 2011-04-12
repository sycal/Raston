package email.smtp.rules;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import core.SystemFailureException;
import core.TokenBase;
import email.Attachment;
import email.Email;
import email.EmailAddress;
import email.Envelope;

import util.FileUtility;
import util.GnuPG;
import util.PropertyHandler;
import util.StringUtility;

public class AttachmentTask extends SmtpRuleTaskBase implements ISmtpRuleTask {
	
	private static Logger logger = Logger.getLogger(AttachmentTask.class);

	private String fileNameRex;
	private String contentTypeRex;
	private String destinationFilePath;
	private String fileName;
	private List<IAttachmentSubTask> subTaskList;
	
	@Override
	public void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) throws Exception {
		Email email = envelope.getEmail();
		Pattern fileNamePattern = setPattern(this.fileNameRex);
		Pattern contentTypePattern = setPattern(this.contentTypeRex);
		int totalAttachments = 0;
		int processedAttachments = 0;
		this.setTokenMatchingObjects(envelope, emailGroups);
		for (Attachment attachment : email.getAttachments()) {
			totalAttachments++;
			this.fileName = attachment.getFileName();
			String contentType = attachment.getContentType();
			if (isMatched(fileNamePattern, this.fileName) && isMatched(contentTypePattern, contentType)) {
				processedAttachments++;
				logger.info(String.format("Found attachment '%s'", this.fileName));
				String filePath = this.replaceTokens(this.destinationFilePath);
				attachment.save(filePath);
				this.executeSubTasks(filePath);
			}
		}
		logger.info(String.format("Processed %d of %d attachments", processedAttachments, totalAttachments));
	}

	@Override
	public void initialise(Element taskElement) throws SystemFailureException {
		this.fileNameRex = taskElement.getAttribute("fileNameRex");
		this.contentTypeRex = taskElement.getAttribute("contentTypeRex");
		this.destinationFilePath = taskElement.getAttribute("destinationFilePath");
		this.subTaskList = new ArrayList<IAttachmentSubTask>();
		NodeList nlChildTasks = taskElement.getChildNodes();
		for (int i=0; i<nlChildTasks.getLength(); i++) {
			Node childNode = nlChildTasks.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element subTaskElement = (Element)childNode;
				IAttachmentSubTask subTask = this.createSubTask(subTaskElement.getNodeName());
				subTask.initialise(subTaskElement);
				this.subTaskList.add(subTask);
			}
		}
	}
	
	@Override
	protected String onEvaluateTokenName(String tokenName) {
		if (tokenName.equalsIgnoreCase("filename")) {
			return this.fileName;
		} else {
			return super.onEvaluateTokenName(tokenName);
		}
	}
	
	private static Pattern setPattern(String regex) {
		return StringUtility.has(regex) ? new Pattern(regex, REFlags.IGNORE_CASE) : null;
	}
	
	private static boolean isMatched(Pattern pattern, String value) {
		if (pattern == null) {
			return true;
		}
		Matcher m = pattern.matcher(value);
		return m.find();
	}
	
	private void executeSubTasks(String filePath) {
		for (IAttachmentSubTask subTask : this.subTaskList) {
			filePath = subTask.processFile(filePath);
			if (filePath == null) {
				logger.error("Cannot continue with sub task processing as the last task returned NULL");
				break;
			}
		}
	}
	
	private IAttachmentSubTask createSubTask(String subTaskName) throws SystemFailureException {
		if (subTaskName.equalsIgnoreCase("decryptfile")) {
			return new AttachmentSubTaskDecrypt();
		} else if (subTaskName.equalsIgnoreCase("copyfile")) {
			return new AttachmentSubTaskCopy();
		} else if (subTaskName.equalsIgnoreCase("delete")) {
			return new AttachmentSubTaskDelete();
		} else {
			throw new SystemFailureException("{Unknown Attachment sub task '%s'}", subTaskName);
		}
	}
	
	private class AttachmentSubTaskDecrypt extends AttachmentSubTask implements IAttachmentSubTask
	{
		private String passPhrase;
		private String gpgExecutableFilePath;
		
		@Override
		public void initialise(Element subTaskElement) throws SystemFailureException {
			this.passPhrase = subTaskElement.getAttribute("passPhrase");
			this.destinationFilePath = subTaskElement.getAttribute("destinationFilePath");
			this.gpgExecutableFilePath = PropertyHandler.getNonEmptyString("GPG.FilePath");
		}
		
		@Override
		public String processFile(String filePath) {
			this.sourceFilePath = filePath;
			String decodedDestinationFilePath = this.replaceTokens(this.destinationFilePath);
			logger.info(String.format("Decrypting '%s'", filePath));
			GnuPG gpg = new GnuPG(gpgExecutableFilePath);
			boolean success = gpg.decryptFile(filePath, StringUtility.nullIfEmpty(this.passPhrase), decodedDestinationFilePath);
			if (success) {
				return decodedDestinationFilePath;
			} else {
				logger.error(String.format("Failed to decrypt '%s'", filePath));
				return null;
			}
		}
	}
	
	private class AttachmentSubTaskCopy extends AttachmentSubTask implements IAttachmentSubTask
	{	
		@Override
		public void initialise(Element subTaskElement) {
			this.destinationFilePath = subTaskElement.getAttribute("destinationFilePath");
		}
		
		@Override
		public String processFile(String filePath) {
			this.sourceFilePath = filePath;
			String decodedDestinationFilePath = this.replaceTokens(this.destinationFilePath);
			logger.info(String.format("Copying '%s' to '%s'", filePath, decodedDestinationFilePath));
			FileUtility.copy(filePath, decodedDestinationFilePath);
			return decodedDestinationFilePath;
		}
	}
	
	private class AttachmentSubTaskDelete extends AttachmentSubTask implements IAttachmentSubTask
	{	
		@Override
		public void initialise(Element subTaskElement) {
			this.destinationFilePath = subTaskElement.getAttribute("path");
		}
		
		@Override
		public String processFile(String filePath) {
			this.sourceFilePath = filePath;
			String deletePath = this.replaceTokens(this.destinationFilePath);
			logger.info(String.format("Deleting '%s'", deletePath));
			FileUtility.deleteFileOrTree(deletePath);
			return filePath;
		}
	}
	
	private class AttachmentSubTask extends TokenBase
	{
		protected String sourceFilePath;
		protected String destinationFilePath;
		
		@Override
		protected String onEvaluateTokenName(String tokenName) {
			if (tokenName.equalsIgnoreCase("filename")) {
				return FileUtility.getFilename(this.sourceFilePath);
			} else {
				return super.onEvaluateTokenName(tokenName);
			}
		}
	}

	private interface IAttachmentSubTask
	{
		void initialise(Element subTaskElement) throws SystemFailureException;
		String processFile(String filePath);
	}
}
