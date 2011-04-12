package email.smtp;

import java.io.*;
import java.net.*;

import core.BaseClient;
import core.ProtocolException;
import email.EmailAddress;
import email.Envelope;

public class SmtpClient extends BaseClient {

	@Override
	protected void onConnected() throws IOException, ProtocolException {
		this.sendAndReceive(null, 220);
		String localHostName = InetAddress.getLocalHost().getHostName();
		this.sendAndReceive("EHLO " + localHostName, 250);
	}

	public void sendEmail(String envelopeFrom, String envelopeTo, String content) throws IOException, ProtocolException {
		this.sendEmail(envelopeFrom, new String[] { envelopeTo }, content);
	}
	public void sendEmail(String envelopeFrom, String[] envelopeToList, String content) throws IOException, ProtocolException {
		this.sendAndReceive(String.format("MAIL FROM: %s", envelopeFrom), 250);
		for (String envelopeTo : envelopeToList) {
			this.sendAndReceive(String.format("RCPT TO: %s", envelopeTo), 250);
		}
		this.sendAndReceive("DATA", 354);
		this.sendAndReceive(content, -1);
		this.sendAndReceive(".", 250);
	}
	public void sendEmail(Envelope envelope) throws IOException, ProtocolException {
		this.sendAndReceive(String.format("MAIL FROM: %s", envelope.getMailFrom().toEnvelopeAddress()), 250);
		for (EmailAddress envelopeTo : envelope.getRcptToList()) {
			this.sendAndReceive(String.format("RCPT TO: %s", envelopeTo.toEnvelopeAddress()), 250);
		}
		this.sendAndReceive("DATA", 354);
		this.sendAndReceive(envelope.getEmail().toString(), -1);
		this.sendAndReceive(".", 250);	
	}
	
	@Override
	protected void onClosing() throws IOException, ProtocolException {
		this.sendAndReceive("QUIT", 221);
	}
}
