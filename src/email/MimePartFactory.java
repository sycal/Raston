package email;

import core.HeaderManager;
import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import util.StringUtility;

public class MimePartFactory {
	
	private MimePartFactory() {}
	
	public static MimePart create(String rawContent) {
		String[] contentParts = rawContent.split(StringUtility.CRLFCRLF, 2);
		String rawBody;
		if (contentParts.length == 2) {
			rawBody = contentParts[1];
		} else {
			rawBody = null;
		}
		HeaderManager hm = HeaderManager.parse(contentParts[0]);
		String fileName = getFileName(hm.getHeader("Content-Disposition"));
		if (fileName != null) {
			return new Attachment(hm, rawBody, fileName);
		} else {
			return new MimePart(hm, rawBody);
		}
	}
	
	private static String getFileName(String contentDispositionHeader) {
		if (contentDispositionHeader != null) {
			System.out.println(contentDispositionHeader);
			Pattern contentDispositionPattern = new Pattern("(?:attachment|inline).*; ?filename=\"?({filename}[\\w\\-\\.]+)\"?", REFlags.IGNORE_CASE);
			Matcher m = contentDispositionPattern.matcher(contentDispositionHeader);
			if (m.find()) {
				String fileName = m.group("filename");
				return fileName;
			}
		}
		return null;
	}
}
