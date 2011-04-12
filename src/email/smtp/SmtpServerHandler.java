package email.smtp;

import java.net.Socket;
import java.util.*;
import java.util.regex.*;

import core.*;
import email.Email;
import email.EmailAddress;
import email.Envelope;

import util.StringUtility;

public class SmtpServerHandler implements IServerHandler {
	
	private enum CommandState { Hello, MailFrom, RcptTo, Content }
	
	private final String REX_HELO = "HELO";
	private final String REX_EHLO = "EHLO ?(.*)?";
	private final String REX_MAIL_FROM = "MAIL FROM: ?(.*)";
	private final String REX_RCPT_TO = "RCPT TO: ?(.*)";
	private final String REX_DATA = "DATA";
	
	private CommandState commandState;
	private EmailAddress reversePath;
	private List<EmailAddress> forwardPaths;
	private StringBuilder content;
	private Matcher matched;
	
	public SmtpServerHandler() {
		this.setState(CommandState.Hello);
	}
	
	@Override
	public ServerResponse processLine(String line, Socket connectedSocket) throws SystemFailureException {
		if (line == null) return new ServerResponse(220, "Welcome to the Raston SMTP server");
		if (line.equalsIgnoreCase("QUIT")) return new ServerResponse(221, "Closing service channel", true);
		if (line.equalsIgnoreCase("RSET")) {
			this.setState(CommandState.Hello);
			return new ServerResponse(250, "OK reset.");
		}
		if (line.equalsIgnoreCase("HELP")) {
			return new ServerResponse(250, "This server supports the following commands:" + StringUtility.CRLF + "DATA, EHLO, HELO, MAIL, QUIT, RCPT, RSET");
		}
		switch (this.commandState)
		{
		case Hello:
			if (this.isCommand(this.REX_HELO, line)) {
				this.setState(CommandState.MailFrom);
				return new ServerResponse(250, "Hello");
			} else if (this.isCommand(this.REX_EHLO, line)) {
				this.setState(CommandState.MailFrom);
				return new ServerResponse(250, "Hello");
			} else {
				return new ServerResponse(503, "Send hello first");
			}
		case MailFrom:
			if (this.isCommand(this.REX_MAIL_FROM, line)) {
				EmailAddress testReversePath = EmailAddress.parse(this.matched.group(1));
				if (testReversePath != null) {
					this.reversePath = testReversePath;
					this.setState(CommandState.RcptTo);
					return new ServerResponse(250, "Sender OK");
				} else {
					return new ServerResponse(501, "Invalid address");
				}
			} else {
				return new ServerResponse(500, "Unrecognised command");
			}
		case RcptTo:
			if (this.isCommand(this.REX_RCPT_TO, line)) {
				EmailAddress testForwardPath = EmailAddress.parse(this.matched.group(1));
				if (testForwardPath != null) {
					this.forwardPaths.add(testForwardPath);
					return new ServerResponse(250, "Recipient OK");
				} else {
					return new ServerResponse(501, "Invalid address");
				}
			}
			if ((this.forwardPaths.size() >= 1) && (this.isCommand(this.REX_DATA, line))) {
				this.setState(CommandState.Content);
				return new ServerResponse(354,  "Start mail input, end with <CRLF>.<CRLF>");
			} else {
				return new ServerResponse(500, "Unrecognised command");
			}
		case Content:
			if (line.equalsIgnoreCase(".")) {
				String sourceIPAddress = connectedSocket.getInetAddress().getHostAddress();
				String hostName = connectedSocket.getLocalAddress().getHostName();
				String hostAddress = connectedSocket.getLocalAddress().getHostAddress();
				Email email = Email.parse(this.content.toString());
				email.getHeaders().insertHeader("Received", String.format("from (%s) by %s (%s) with Raston on (%s)", sourceIPAddress, hostName, hostAddress, new Date()), 0);
				Envelope envelope = new Envelope(sourceIPAddress, this.reversePath, this.forwardPaths, email);
				this.setState(CommandState.MailFrom);
				if (SmtpForwardHandler.queue(envelope)) {
					return new ServerResponse(250, "OK: queued as " + email.getId());
				} else {
					return new ServerResponse(554, "Transaction failed");
				}
			} else {
				this.content.append(line);
				this.content.append(StringUtility.CRLF);
				return new ServerResponse();
			}
		default:
			throw new SystemFailureException("Invalid state: %s", this.commandState);
		}
	}

	/**
	 * Sets the next state and initialises any required state variables.
	 * @param newState
	 */
	private void setState(CommandState newState) {
		switch (newState) {
		case MailFrom:
			this.reversePath = null;
			break;
		case RcptTo:
			this.forwardPaths = new ArrayList<EmailAddress>();
			break;
		case Content:
			this.content = new StringBuilder();
			break;
		}
		this.commandState = newState;
	}
	
	private Boolean isCommand(String commandPattern, String inputData) {
		Pattern p = Pattern.compile(commandPattern, Pattern.CASE_INSENSITIVE);
		this.matched = p.matcher(inputData);
		return this.matched.find();
	}
}
