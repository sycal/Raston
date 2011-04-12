package email.smtp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import core.ProtocolException;
import core.SystemFailureException;
import email.Envelope;
import email.smtp.rules.SmtpRulesEngine;

import util.FileUtility;
import util.PropertyHandler;

@SuppressWarnings("serial")
public class SmtpForwardHandler implements Serializable {
	
	private static Logger logger = Logger.getLogger(SmtpForwardHandler.class);

	private static final String FILE_EXTENSION = ".txt";
	private static final String FORWARD_DATA_LIST_FILE = "index.data";
	private static final String PROPERTY_FORWARD_FOLDER_RULEQUEUE = "Forward.Folder.RuleQueue";
	private static final String PROPERTY_FORWARD_FOLDER_SENDQUEUE = "Forward.Folder.SendQueue";
	private static final String PROPERTY_FORWARD_FOLDER_SENT = "Forward.Folder.Sent";
	private static final String PROPERTY_FORWARD_FOLDER_FAILED = "Forward.Folder.Failed";
	private static final String PROPERTY_FORWARD_RETRIES_MAXFORWARDCOUNT = "Forward.Retries.MaxForwardCount";
	private static final String PROPERTY_FORWARD_RETRIES_INITIALBACKOFFMINS = "Forward.Retries.InitialBackoffMins";
	private static final String PROPERTY_FORWARD_RETRIES_SUBSEQUENTBACKOFFMINS = "Forward.Retries.SubsequentBackoffMins";
	private static final String PROPERTY_FORWARD_SMTPSERVER_HOST = "Forward.SmtpServer.Host";
	private static final String PROPERTY_FORWARD_SMTPSERVER_PORT = "Forward.SmtpServer.Port";
	private static final int DEFAULT_SMTP_PORT = 25;
	
	public static boolean queue(Envelope envelope) {
		try {
			String fileName = envelope.getEmail().getId() + FILE_EXTENSION;
			String filePath = buildFilePath(fileName, PROPERTY_FORWARD_FOLDER_RULEQUEUE);
			boolean success = FileUtility.saveObjectToFile(filePath, envelope);
			if (success) {
				logger.info(String.format("%s: Queued", FileUtility.removeExtension(fileName)));
			}
			return success;
		} catch (Exception e) {
			logger.error("Failed to queue email", e);
			return false;
		}
	}
	
	public static boolean queueForSendOnly(Envelope envelope) {
		try {
			String fileName = envelope.getEmail().getId() + FILE_EXTENSION;
			String filePath = buildFilePath(fileName, PROPERTY_FORWARD_FOLDER_SENDQUEUE);
			boolean success = FileUtility.saveObjectToFile(filePath, envelope);
			if (success) {
				logger.info(String.format("%s: Queued for send", FileUtility.removeExtension(fileName)));
			}
			return success;
		} catch (Exception e) {
			logger.error("Failed to queue email for send-only", e);
			return false;
		}	
	}
	
	public void runWithRules(SmtpRulesEngine rulesEngine) {
		this.run(rulesEngine);
	}
	
	public void runWithoutRules() {
		this.run(null);
	}
	
	private void run(SmtpRulesEngine rulesEngine) {
		try {
			if (rulesEngine == null) {
				this.bypassRules();
			} else {
				this.processRules(rulesEngine);
			}
			HashMap<String, ForwardData> itemsToForward = load();
			appendFiles(itemsToForward);
			if (processItems(itemsToForward) > 0) {
				save(itemsToForward);
			}
		} catch (Exception e) {
			logger.error("Failed during run", e);
		}
	}
	
	private void bypassRules() throws SystemFailureException {
		for (File file : listFiles(PROPERTY_FORWARD_FOLDER_RULEQUEUE)) {
			moveItem(file.getAbsolutePath(), PROPERTY_FORWARD_FOLDER_SENDQUEUE);
		}	
	}
	
	private void processRules(SmtpRulesEngine rulesEngine) throws SystemFailureException {
		for (File file : listFiles(PROPERTY_FORWARD_FOLDER_RULEQUEUE)) {
			try {
				String rawContents = FileUtility.loadAsciiFile(file.getAbsolutePath());
				Envelope envelope = Envelope.parse(rawContents);
				String id = FileUtility.removeExtension(file.getName());
				envelope.getEmail().setId(id);
				rulesEngine.processRules(envelope);
				file.delete();
			}
			catch (Exception e) {
				logger.error(String.format("%s: Failed to process rules for email file", file.getAbsolutePath()));
				moveItem(file.getAbsolutePath(), PROPERTY_FORWARD_FOLDER_FAILED);
			}
		}
	}
	
	private int processItems(HashMap<String, ForwardData> itemsToForward) throws ProtocolException, UnknownHostException, IOException, SystemFailureException {
		int itemsProcessed = 0;
		if (itemsToForward.size() > 0) {
			int maxForwardCount = PropertyHandler.getInt(PROPERTY_FORWARD_RETRIES_MAXFORWARDCOUNT);
			int initialBackoffMins = PropertyHandler.getInt(PROPERTY_FORWARD_RETRIES_INITIALBACKOFFMINS);
			int subsequentBackoffMins = PropertyHandler.getInt(PROPERTY_FORWARD_RETRIES_SUBSEQUENTBACKOFFMINS);
			String smtpServer = PropertyHandler.getNonEmptyString(PROPERTY_FORWARD_SMTPSERVER_HOST);
			int smtpServerPort = PropertyHandler.getInt(PROPERTY_FORWARD_SMTPSERVER_PORT, DEFAULT_SMTP_PORT);
			SmtpClient smtpClient = new SmtpClient();
			Iterator<Entry<String, ForwardData>> iterator = itemsToForward.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ForwardData> entry = iterator.next();
				ForwardData item = entry.getValue();
				if (shouldForward(item.nextAttemptTime)) {
					if (FileUtility.exists(item.filePath)) {
						String fileName = FileUtility.getFilename(item.filePath);						
						if (!smtpClient.isConnected()) {
							smtpClient.connect(smtpServer, smtpServerPort);
						}
						if (forwardItem(item, smtpClient)) {
							moveItem(item.filePath, PROPERTY_FORWARD_FOLDER_SENT);
							iterator.remove();
						} else {
							item.forwardAttempts++;
							logger.info(String.format("%s: Forward attempt count incremented to %d", fileName, item.forwardAttempts));
							if (item.forwardAttempts >= maxForwardCount) {
								moveItem(item.filePath, PROPERTY_FORWARD_FOLDER_FAILED);
								iterator.remove();
							} else {
								int nextRetryMins = (item.forwardAttempts > 1) ? subsequentBackoffMins : initialBackoffMins;
								Calendar nextAttemptTime = Calendar.getInstance();
								nextAttemptTime.add(Calendar.MINUTE, nextRetryMins);
								item.nextAttemptTime = nextAttemptTime.getTime();
								logger.info(String.format("%s: Set retry time to '%s' from mins '%d'", fileName, item.nextAttemptTime, nextRetryMins));
							}
						}
					} else {
						logger.error(String.format("File '%s' does not exist anymore, so removing from forward list", item.filePath));
						iterator.remove();
					}
					itemsProcessed++;
				}
			}
			if (smtpClient.isConnected()) {
				smtpClient.close();
			}
		}
//		if (itemsProcessed == 0) {
//			logger.debug("Nothing doing");
//		}
		return itemsProcessed;
	}
	
	private static boolean shouldForward(Date nextAttempt) {
		if (nextAttempt == null) return true;
		Calendar now = Calendar.getInstance();
		Calendar nextAttemptTime = Calendar.getInstance();
		nextAttemptTime.setTime(nextAttempt);
		return (!nextAttemptTime.after(now));
	}

	private static boolean forwardItem(ForwardData item, SmtpClient smtpClient) {
		try {
			String rawContents = FileUtility.loadAsciiFile(item.filePath);
			Envelope env = Envelope.parse(rawContents);
			String id = FileUtility.removeExtension(FileUtility.getFilename(item.filePath));
			env.getEmail().setId(id);
			smtpClient.sendEmail(env);
			logger.info(String.format("%s: Forwarded OK", env.getEmail().getId()));
			return true;
		} catch (Exception e) {
			logger.error(String.format("%s: Failed to forward", item.filePath), e);
			return false;
		}
	}
	
	private static void moveItem(String sourcefilePath, String destinationFolderPropertyName) throws SystemFailureException {
		String fileName = FileUtility.getFilename(sourcefilePath);
		File sourceFile = new File(sourcefilePath);
		File destFile = new File(buildFilePath(fileName, destinationFolderPropertyName));
		logger.debug(String.format("Moving '%s' to '%s'", fileName, destFile.getAbsoluteFile()));
		sourceFile.renameTo(destFile);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, ForwardData> load() {
		try {
			String filePath = buildFilePath(FORWARD_DATA_LIST_FILE, PROPERTY_FORWARD_FOLDER_SENDQUEUE);
			File file = new File(filePath);
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(filePath);
				ObjectInputStream ois = new ObjectInputStream(fis);
				HashMap<String, ForwardData> forwardItems = (LinkedHashMap<String, ForwardData>)ois.readObject();
				ois.close();
				return forwardItems;
			}
		} catch (Exception e) {
			logger.error("Failed to load forward data list", e);
		}
		return new LinkedHashMap<String, ForwardData>();
	}
	
	private static void save(HashMap<String, ForwardData> forwardItems) {
		try {
			String filePath = buildFilePath(FORWARD_DATA_LIST_FILE, PROPERTY_FORWARD_FOLDER_SENDQUEUE);
			FileOutputStream fos = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(forwardItems);
			oos.close();
		} catch (Exception e) {
			logger.error("Failed to save forward data list", e);
		}
	}
	
	private void appendFiles(HashMap<String, ForwardData> forwardItems) throws SystemFailureException {
		for (File file : listFiles(PROPERTY_FORWARD_FOLDER_SENDQUEUE)) {
			String fileNameLower = file.getName();
			if (!forwardItems.containsKey(fileNameLower)) {
				ForwardData item = new ForwardData();
				item.filePath = file.getAbsolutePath();
				item.forwardAttempts = 0;
				forwardItems.put(fileNameLower, item);
			}
		}
	}
	
	private static File[] listFiles(String folderPropertyName) throws SystemFailureException {
		File folder = getFolder(folderPropertyName);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(FILE_EXTENSION);
			}
		};
		return folder.listFiles(filter);
	}
	
	private static String buildFilePath(String fileName, String folderPropertyName) throws SystemFailureException {
		return getFolder(folderPropertyName).getAbsolutePath() + File.separatorChar + fileName;
	}
	
	private static File getFolder(String folderPropertyName) throws SystemFailureException {
		String queueFolder = PropertyHandler.getNonEmptyString(folderPropertyName);
		File folder = new File(queueFolder);
		if (!folder.exists()) folder.mkdir();
		return folder;
	}
	
	private class ForwardData implements Serializable {
		public String filePath;
		public int forwardAttempts;
		public Date nextAttemptTime;
	}
}
