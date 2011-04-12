package email;

import core.HeaderManager;

public class MimePart {
	
	protected HeaderManager headers;
	protected String rawBody;
	
	public MimePart(HeaderManager headers, String rawBody) {
		this.headers = headers;
		this.rawBody = rawBody;
	}
	
	public String getContentType() {
		return this.headers.getHeader("Content-Type");
	}
}
