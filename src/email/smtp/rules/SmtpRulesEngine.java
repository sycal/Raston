package email.smtp.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import core.SystemFailureException;
import email.EmailAddress;
import email.Envelope;

public class SmtpRulesEngine {
	
	private static Logger logger = Logger.getLogger(SmtpRulesEngine.class);
	
	private List<SmtpRule> ruleList;
	private SmtpRule defaultRule;
	private Hashtable<String, List<EmailAddress>> emailGroups;
	
	private SmtpRulesEngine() {
	}

	public static SmtpRulesEngine createFromFile(String filePath) throws ParserConfigurationException, SAXException, IOException, SystemFailureException {
		SmtpRulesEngine engine = new SmtpRulesEngine();
		engine.loadRules(filePath);
		return engine;
	}
	
	private void loadRules(String filePath) throws ParserConfigurationException, SAXException, IOException, SystemFailureException {
		File file = new File(filePath);
		if (!file.exists()) throw new SystemFailureException("Cannot find rules file '%s'", filePath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		logger.info(String.format("Loading rules file '%s'", filePath));
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		this.loadRules(doc);
		this.loadGroups(doc);
	}
	
	private void loadRules(Document doc) throws SystemFailureException {
		this.ruleList = new ArrayList<SmtpRule>();
		this.defaultRule = null;
		NodeList nodeList = doc.getElementsByTagName("rule");
		for (int i=0; i<nodeList.getLength(); i++) {
			Element elNode = (Element)nodeList.item(i);
			String name = elNode.getAttribute("name");
			String envelopeFrom = elNode.getAttribute("envelopeFromRex");
			String envelopeTo = elNode.getAttribute("envelopeToRex");
			String ipFrom = elNode.getAttribute("ipFromRex");
			boolean isDefaultRule = elNode.getAttribute("isDefault").equalsIgnoreCase("true");
			boolean isFinalRule = elNode.getAttribute("isFinalRule").equalsIgnoreCase("true");
			SmtpRule newRule = new SmtpRule(name, envelopeFrom, envelopeTo, ipFrom, isFinalRule);
			if (isDefaultRule) {
				this.defaultRule = newRule;
			} else {
				this.ruleList.add(newRule);
			}
			NodeList nlChildTasks = elNode.getChildNodes();
			for (int i2=0; i2<nlChildTasks.getLength(); i2++) {
				Node childNode = nlChildTasks.item(i2);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element elTask = (Element)childNode;
					ISmtpRuleTask task = this.createSmtpRuleTask(elTask.getNodeName());
					task.initialise(elTask);
					newRule.getTaskList().add(task);
				}
			}
		}
	}
	
	private void loadGroups(Document doc) {
		this.emailGroups = new Hashtable<String, List<EmailAddress>>();
		NodeList nodeList = doc.getElementsByTagName("group");
		for (int i=0; i<nodeList.getLength(); i++) {
			Element elNode = (Element)nodeList.item(i);
			String groupName = elNode.getAttribute("name");
			NodeList nlChildTasks = elNode.getChildNodes();
			List<EmailAddress> emailAddressList = new ArrayList<EmailAddress>();
			for (int i2=0; i2<nlChildTasks.getLength(); i2++) {
				Node childNode = nlChildTasks.item(i2);
				if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equalsIgnoreCase("email")) {
					Element elTask = (Element)childNode;
					String emailAddress = elTask.getTextContent();
					emailAddressList.add(EmailAddress.parse(emailAddress));
				}
			}
			this.emailGroups.put(groupName, emailAddressList);
		}
	}
	
	public void processRules(Envelope envelope) throws Exception {
		if (this.ruleList == null) throw new SystemFailureException("Rules have not been loaded.");
		int rulesMatchedCount = 0;
		for (SmtpRule rule : this.ruleList) {
			if (rule.processRule(envelope, this.emailGroups)) {
				rulesMatchedCount++;
				if (rule.isFinalRule()) break;
			}
		}
		if (rulesMatchedCount == 0) {
			if (this.defaultRule != null) {
				logger.debug("No SMTP rules matched - executing default rule");
				this.defaultRule.processRule(envelope, this.emailGroups);
			} else {
				logger.debug("No SMTP rules matched and no default rule specified");
			}
		}
	}
	
	private ISmtpRuleTask createSmtpRuleTask(String taskName) throws SystemFailureException {
		if (taskName.equalsIgnoreCase("forward")) {
			return new ForwardTask();
		} else if (taskName.equalsIgnoreCase("fileStore")) {
			return new FileStoreTask();
		} else if (taskName.equalsIgnoreCase("header")) {
			return new HeaderTask();
		} else if (taskName.equalsIgnoreCase("databaseStore")) {
			return new DatabaseTask();
		} else if (taskName.equalsIgnoreCase("attachment")) {
			return new AttachmentTask();
		} else {
			throw new SystemFailureException("{Unknown SmtpRuleTask <%s>}", taskName);
		}
	}
}
