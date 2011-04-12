package core;

import java.text.SimpleDateFormat;
import java.util.Date;

import util.FileUtility;
import util.NetUtility;

public abstract class TokenBase {

	protected String replaceTokens(String textWithTokens) {
		StringBuilder outputText = new StringBuilder();
		StringBuilder token = new StringBuilder();
		boolean tokenMode = false;
		for (int i=0; i<textWithTokens.length(); i++) {
			char c = textWithTokens.charAt(i);
			if (tokenMode) {
				if (c == ']') {
					if (token.length() > 0) outputText.append(this.evaluateFullToken(token.toString()));
					tokenMode = false;
				} else {
					token.append(c);
				}
			} else {
				if (c == '[') {
					token.setLength(0);
					tokenMode = true;
				} else {
					outputText.append(c);
				}
			}
		}
		return outputText.toString();
	}
	
	protected String onEvaluateTokenName(String tokenName) {
		if (tokenName.equalsIgnoreCase("now")) {
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			return sdf.format(now);
		} else if (tokenName.equalsIgnoreCase("hostname")) {
			return NetUtility.hostName();
		} else if (tokenName.equalsIgnoreCase("ipaddress")) {
			return NetUtility.ipAddress();
		} else {
			return "?" + tokenName + "?";
		}
	}
	
	protected String onEvaluateTokenAction(String tokenName, String tokenValue, String tokenAction) {
		if (tokenAction.equalsIgnoreCase("lower")) {
			return tokenValue.toLowerCase();
		} else if (tokenAction.equalsIgnoreCase("upper")) {
			return tokenValue.toUpperCase();
		} else if (tokenAction.equalsIgnoreCase("noExt")) {
			return FileUtility.removeExtension(tokenValue);
		} else {
			return "?" + tokenAction + "?";
		}
	}
	
	private String evaluateFullToken(String token) {
		String[] parts = token.split("\\.");
		String tokenName = parts[0];
		String tokenValue = this.onEvaluateTokenName(tokenName);
		for (int i=1; i<parts.length; i++) {
			String tokenAction = parts[i];
			tokenValue = this.onEvaluateTokenAction(tokenName, tokenValue, tokenAction);
		}
		return tokenValue;
	}
}
