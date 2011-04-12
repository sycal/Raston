package util;

import java.io.*;

import org.apache.log4j.Logger;

public class GnuPG {
	
	private static Logger logger = Logger.getLogger(GnuPG.class);

	private String gpgExecutableFilePath;
	
	public GnuPG(String gpgExecutableFilePath) {
		this.gpgExecutableFilePath = gpgExecutableFilePath;
	}
	
	public boolean decryptFile(String sourceFilePath, String passPhrase, String destinationFilePath) {
		FileUtility.deleteFileOrTree(destinationFilePath);
		String arguments = String.format("--batch --output %s --decrypt %s", destinationFilePath, sourceFilePath);
		if (passPhrase != null) {
			arguments = "--passphrase " + encodePassPhrase(passPhrase) + " " + arguments;
		}
		boolean success = executeGPG(arguments);
		if (success) {
			File resultFile = new File(destinationFilePath);
			return resultFile.exists();
		} else {
			return false;
		}
	}
	
	public static String encodePassPhrase(String phrase) {
		return phrase.replaceAll("([!\"])", "\\\\$1");
	}
	
	private boolean executeGPG(String commandArgs) {
		Process	p;
		String gpgCommand;
		gpgCommand = this.gpgExecutableFilePath + " " + commandArgs;
		logger.debug(gpgCommand);
		try {
			String[] shellCmd = { "/bin/bash", "-c", gpgCommand }; 
			p = Runtime.getRuntime().exec(shellCmd);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String s;
			while ((s = stdInput.readLine()) != null) {
				logger.debug("StdOut : " + s);
			}
			while ((s = stdError.readLine()) != null) {
				logger.debug("StdErr : " + s);
			}
		}
		catch (IOException e) {
			logger.error("Error during GPG execution", e);
			return false;
		}
		return true;
	}
}
