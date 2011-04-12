package core;

import java.util.regex.*;

import util.StringUtility;

public class ServerResponse {

	private static final String REX_RESPONSE = "^(\\d{3})([ -])(.*)$";
	public static final char DELIM_MULTILINE = '-';
	public static final char DELIM_SINGLELINE = ' ';
	
	private int code;
	private char lastDelim;
	private String message;
	private Boolean finished;
	
	public ServerResponse() {
		this.code = 0;
		this.lastDelim = DELIM_SINGLELINE;
		this.message = "";
		this.finished = false;
	}
	
	public ServerResponse(String message) {
		this.code = 0;
		this.lastDelim = DELIM_SINGLELINE;
		this.message = message;
		this.finished = false;
	}
	
	public ServerResponse(int code, String message) {
		this.code = code;
		this.lastDelim = DELIM_SINGLELINE;
		this.message = message;
		this.finished = false;
	}
	
	public ServerResponse(int code, String message, Boolean quit) {
		this.code = code;
		this.lastDelim = DELIM_SINGLELINE;
		this.message = message;
		this.finished = quit;
	}
	
	private ServerResponse(int code, char delim, String message, Boolean quit) {
		this.code = code;
		this.lastDelim = delim;
		this.message = message;
		this.finished = quit;
	}
	
	public static ServerResponse parseLine(String serverResponseText) {
		Pattern p = Pattern.compile(REX_RESPONSE, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(serverResponseText);
		if (m.find()) {
			int code = Integer.parseInt(m.group(1));
			char delim = m.group(2).charAt(0);
			return new ServerResponse(code, delim, m.group(3), false);
		} else {
			return new ServerResponse(serverResponseText);
		}
	}

	public Boolean isFinished() {
		return this.finished;
	}
	
	public Boolean hasResponse() {
		return (this.code > 0) || (this.message.length() > 0);
	}
		
	public int getCode() {
		return this.code;
	}
	
	public boolean isMultiline() {
		return (this.lastDelim == DELIM_MULTILINE);
	}

	public String getMessage() {
		return this.message;
	}
	
	public String toString() {
		if (this.code > 0) {
			StringBuilder response = new StringBuilder();
			//String[] lines = StringUtility.removeSuffix(this.message, StringUtility.CRLF).split(StringUtility.CRLF);
			String[] lines = StringUtility.removeSuffix(this.message, StringUtility.CRLF).split(StringUtility.CRLF);
			int lastLineIndex = lines.length - 1;
			for (int i=0; i<lines.length; i++) {
				char delim = (i == lastLineIndex) ? DELIM_SINGLELINE : DELIM_MULTILINE;
				String formattedLine = String.format("%d%c%s", this.code, delim, lines[i]);
				response.append(StringUtility.setSuffix(formattedLine, StringUtility.CRLF));
			}
			return response.toString();
		} else {
			return StringUtility.setSuffix(this.message, StringUtility.CRLF); 
		}
	}
}
