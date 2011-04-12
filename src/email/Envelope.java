package email;

import java.util.ArrayList;
import java.util.List;


public class Envelope {

	private static final String HEADER_ENVELOPE_FROM = "x-raston-envelope-from";
	private static final String HEADER_ENVELOPE_TO = "x-raston-envelope-to";
	private static final String HEADER_ENVELOPE_IP = "x-raston-envelope-ip";
	
	private String sourceIPAddress;
	private EmailAddress mailFrom;
	private List<EmailAddress> rcptToList;
	private Email email;
	
	public Envelope(String sourceIPAddress, EmailAddress mailFrom, String rcptToList, Email email) {
		this(sourceIPAddress, mailFrom, EmailUtility.splitEmailAddresses(rcptToList), email);
	}
	public Envelope(String sourceIPAddress, EmailAddress mailFrom, EmailAddress rcptTo, Email email) {
		this.sourceIPAddress = sourceIPAddress;
		this.mailFrom = mailFrom;
		this.rcptToList = new ArrayList<EmailAddress>();
		this.rcptToList.add(rcptTo);
		this.email = email;
	}
	public Envelope(String sourceIPAddress, EmailAddress mailFrom, List<EmailAddress> rcptToList, Email email) {
		this.sourceIPAddress = sourceIPAddress;
		this.mailFrom = mailFrom;
		this.rcptToList = new ArrayList<EmailAddress>();
		this.rcptToList.addAll(rcptToList);
		this.email = email;
	}
	
	private Envelope() {
	}

	public EmailAddress getMailFrom() {
		return this.mailFrom;
	}
	
	public void setMailFrom(EmailAddress newValue) {
		this.mailFrom = newValue;
	}

	public List<EmailAddress> getRcptToList() {
		return this.rcptToList;
	}
	
	public void setRcptToList(String list) {
		this.rcptToList = EmailUtility.splitEmailAddresses(list);
	}

	public Email getEmail() {
		return this.email;
	}

	public String getSourceIPAddress() {
		return this.sourceIPAddress;
	}

	public static Envelope parse(String rawContents) {
		Envelope env = new Envelope();
		env.email = Email.parse(rawContents);
		String envFrom = env.email.getHeaders().getHeaderAndDelete(HEADER_ENVELOPE_FROM);
		env.mailFrom = EmailAddress.parse(envFrom);
		String envelopeToHeader = env.email.getHeaders().getHeaderAndDelete(HEADER_ENVELOPE_TO);
		env.rcptToList = EmailUtility.splitEmailAddresses(envelopeToHeader);
		env.sourceIPAddress = env.email.getHeaders().getHeaderAndDelete(HEADER_ENVELOPE_IP);
		return env;
	}

	@Override
	public String toString() {
		this.email.getHeaders().setHeader(HEADER_ENVELOPE_FROM, this.mailFrom.toSimpleAddress());
		String emailToList = EmailUtility.combineEmailAddresses(this.rcptToList);
		this.email.getHeaders().setHeader(HEADER_ENVELOPE_TO, emailToList);
		this.email.getHeaders().setHeader(HEADER_ENVELOPE_IP, this.sourceIPAddress);
		return this.email.toString();
	}
}
