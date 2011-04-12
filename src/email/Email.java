package email;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;

import org.apache.log4j.Logger;

import core.HeaderManager;

import util.StringUtility;

public class Email {
	
	private static final String HEADER_SUBJECT = "Subject";
	//private static final String HEADER_TO = "To";
	//private static final String HEADER_FROM = "From";
	
	private static Logger logger = Logger.getLogger(Email.class);

	private String id;
	private HeaderManager headers;
	private String rawBody;
	private List<MimePart> mimePartList;
	private boolean isBodyAnalysed;
	
	public Email() {
		this.id = UUID.randomUUID().toString();
		this.headers = new HeaderManager();
		this.rawBody = "";
		this.isBodyAnalysed = false;
	}
	
	public static Email parse(String rawContent) {
		logger.debug("Parsing email");
		Email email = new Email();
		String[] contentParts = rawContent.split(StringUtility.CRLFCRLF, 2);
		if (contentParts.length == 2) {
			email.rawBody = contentParts[1];
		} else {
			logger.debug("Header/body split not found. Parsing as headers only.");
			email.rawBody = null;
		}
		email.headers = HeaderManager.parse(contentParts[0]);
		return email;
	}
	
	public HeaderManager getHeaders() {
		return this.headers;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getSubject() {
		return this.headers.getHeader(HEADER_SUBJECT);
	}
	
	public void setSubject(String newValue) {
		this.headers.setHeader(HEADER_SUBJECT, newValue);
	}
	
	public String getRawBody() {
		return this.rawBody;
	}
	
	public List<Attachment> getAttachments() {
		this.checkBodyIsAnalysed();
		List<Attachment> list = new ArrayList<Attachment>();
		for (MimePart part : this.mimePartList) {
			if (part instanceof Attachment) {
				list.add((Attachment) part);
			}
		}
		return list;
	}

	public void setRawBody(String rawBody) {
		this.rawBody = rawBody;
		this.isBodyAnalysed = false;
		this.mimePartList = null;
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		if (this.headers.size() > 0) {
			output.append(this.headers);
			output.append(StringUtility.CRLF);
		}
		output.append(this.rawBody);
		return output.toString();
	}
	
	private void checkBodyIsAnalysed() {
		if (!this.isBodyAnalysed) {
			this.analyseBody();
		}
	}
	
	private void analyseBody() {
		logger.debug(String.format("Analysing '%s' for mime content", this.getId()));
		this.mimePartList = new ArrayList<MimePart>();
		String boundaryValue = this.findBoundaryValue();
		if (boundaryValue != null) {
			String fixedBoundaryPattern = StringUtility.CRLF + "--" + StringUtility.toFixedPattern(boundaryValue);
			String[] mimeParts = this.rawBody.split(fixedBoundaryPattern);
			for (int i=1; i<(mimeParts.length - 1); i++) {
				MimePart newMimePart = MimePartFactory.create(mimeParts[i]);
				this.mimePartList.add(newMimePart);
			}
		}
		this.isBodyAnalysed = true;
	}
	
	
	private String findBoundaryValue() {
		String mimeVersion = this.getHeaders().getHeader("Mime-Version");
		if (mimeVersion == null) {
			logger.debug(String.format("Email '%s' is not mime encoded", this.getId()));
			return null;
		}
		if (!mimeVersion.startsWith("1.0")) {
			logger.debug(String.format("Email '%s' appears to be mime encoded but reports version '%s'", this.getId(), mimeVersion));
			return null;
		}
		String contentType = this.getHeaders().getHeader("Content-Type");
		if (contentType == null) {
			logger.debug(String.format("Email '%s' has no content type", this.getId()));
			return null;	
		}
		Pattern p = new Pattern("multipart/mixed; boundary=\"?({boundary}[\\w\\-\\.=]+)\"?", REFlags.IGNORE_CASE);
		Matcher m = p.matcher(contentType);
		if (m.find()) {
			String boundary = m.group("boundary");
			logger.debug(String.format("Mime boundary value is '%s'", boundary));
			return boundary;
		} else {
			logger.debug(String.format("Email '%s' is not mime encoded", this.getId()));
			return null;
		}
	}
}
