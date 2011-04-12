package startup;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import core.ProtocolException;
import core.Server;
import core.ServerHandlerFactory;
import core.ServerThreadRunner;
import core.ShutdownClient;
import core.ShutdownHook;

import email.*;
import email.smtp.SmtpClient;
import email.smtp.SmtpForwardHandler;
import email.smtp.rules.SmtpRulesEngine;

import util.*;

@SuppressWarnings("unused")
public class RastonSmtpServer {
	
	private static Logger logger = Logger.getLogger(RastonSmtpServer.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//ripTest();
			//System.out.println(GnuPG.encodePassPhrase("jbossPrompt!\"34"));
			if (args.length == 0) {
				runServer();
			} else if (args[0].equalsIgnoreCase("shutdown")) {
				shutdownServer();
			}
		} catch (Exception e) {
			logger.error("Failed running main thread", e);
		}
		logger.info("Main thread - finished.");
	}

	private static void ripTest() {
		try {
			String filePath = "/var/raston/71577e83-20c1-4a21-b7e9-bf69b423268a.txt";
			//String filePath = "/var/raston/066be39c-2ea9-4c27-a595-e318e7a4dec2.txt";
			String rawContents = FileUtility.loadAsciiFile(filePath);
			if (rawContents == null) throw new Exception(String.format("Could not find file '%s'", filePath));
			Email email = Email.parse(rawContents);
			EmailAddress from = new EmailAddress("nowhere@sycal.org", "mr scott");
			EmailAddress to = new EmailAddress("decrypt@sycal.org", "to");
			Envelope env = new Envelope("127.0.0.1", from, to, email);
			SmtpRulesEngine engine = SmtpRulesEngine.createFromFile("smtp-rules.xml");
			engine.processRules(env);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void runServer() {
		try {
			logger.info("Started Raston SMTP server");
			int serverPort = PropertyHandler.getInt("Server.Port", 25);
			Server server = new Server(ServerHandlerFactory.ServerProtocolType.Smtp, serverPort);
			
			ShutdownHook hook = new ShutdownHook(server);
			Runtime.getRuntime().addShutdownHook(hook);
			
			Thread t = new Thread(new ServerThreadRunner(server));
			t.start();
			
			//sendTestEmail();

			SmtpForwardHandler fh = new SmtpForwardHandler();
			long lastModifiedRules = 0;
			String rulesFilePath = "smtp-rules.xml";
			SmtpRulesEngine rulesEngine = null; 
			while (server.isRunning()) {
				Thread.sleep(10000);
				try {
					long time = FileUtility.getLastModifiedTime(rulesFilePath);
					if ((rulesEngine == null) || (time > lastModifiedRules)) {
						lastModifiedRules = Math.max(time, lastModifiedRules);
						rulesEngine = SmtpRulesEngine.createFromFile(rulesFilePath);
					}
					fh.runWithRules(rulesEngine);
				} catch (Exception e) {
					logger.error("Failed during rule processing - will try again in one minute", e);
					rulesEngine = null;
					Thread.sleep(50000);
				}
			}
		} catch (Exception e) {
			logger.error("Server failure - shutting down", e);
		}
		System.exit(0);
	}
	
	private static void shutdownServer() throws UnknownHostException, IOException, ProtocolException {
		ShutdownClient client = new ShutdownClient();
		int serverPort = PropertyHandler.getInt("Server.Port", 25);
		try {
			client.connect("127.0.0.1", serverPort);
			client.shutdown();
		} catch (ConnectException e) {
			logger.error("Could not connect to local server for shutdown");
		}
	}
	
	private static void doEmailTest() {
		for (int i=0; i<500; i++) {
			sendTestEmail("The body is " + i);
		}
	}
	
	private static void sendTestEmail(String body) {
		try {
			Email email = new Email();
			email.setRawBody(body);
			email.setSubject("This is the subject");
			SmtpClient client = new SmtpClient();
			client.connect("pmraston", 1025);
			client.sendEmail("nowhere@nowhere.com", "nowhere@sycal.org", email.toString());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Envelope createEnvelope(String from, String to) {
		Email email = createEmail();
		Envelope env = new Envelope("127.0.0.1", EmailAddress.parse(from), EmailUtility.splitEmailAddresses(to), email);
		return env;
	}
	
	private static Email createEmail() {
		Email email = new Email();
		email.setRawBody("This is the body");
		email.setSubject("This is the subject");
		return email;
	}
}
