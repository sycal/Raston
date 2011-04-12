package email;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import jregex.MatchResult;
import jregex.Pattern;
import jregex.REFlags;
import jregex.Replacer;
import jregex.Substitution;
import jregex.TextBuffer;

import org.apache.log4j.Logger;

import core.HeaderManager;

import util.Base64;
import util.StringUtility;

public class Attachment extends MimePart {
	
	private static Logger logger = Logger.getLogger(Attachment.class);
	
	private String fileName;

	public Attachment(HeaderManager headers, String rawBody, String fileName) {
		super(headers, rawBody);
		this.fileName = fileName;
	}

	public String getFileName() {
		return this.fileName;
	}
	
	public String getContentTransferEncoding() {
		return this.headers.getHeader("Content-Transfer-Encoding");
	}
	
	public boolean save(String filePath) {
		String cte = this.getContentTransferEncoding();
		if (cte == null) {
			logger.error("Content transfer encoding not set");
		} else if (cte.equalsIgnoreCase("7bit")) {
			logger.debug(String.format("Saving from 7bit to '%s'", filePath));
			return this.save7bit(filePath);
		} else if (cte.equalsIgnoreCase("quoted-printable")) {
			logger.debug(String.format("Saving from quoted-printable to '%s'", filePath));
			return this.saveQuotedPrintable(filePath);
		} else if (cte.equalsIgnoreCase("base64")) {
			logger.debug(String.format("Saving from base64 to '%s'", filePath));
			return this.saveBase64(filePath);
		} else {
			logger.error(String.format("Content transfer '%s' encoding not recognised", cte));
		}
		return false;
	}
	
	private boolean save7bit(String filePath) {
		File file = new File(filePath);
		File folder = new File(file.getParent());
		if (!folder.exists()) folder.mkdirs();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "US-ASCII"));
			String[] lines = this.rawBody.split(StringUtility.CRLF);
			for (int i=0; i<lines.length; i++) {
				String line = lines[i];
				out.write(line);
				out.write(StringUtility.CRLF);
			}
			out.close();
			return true;
		} catch (IOException e) {
			logger.error("Failed to write file", e);
			return false;
		}
	}
	
	private boolean saveBase64(String filePath) {
		File file = new File(filePath);
		File folder = new File(file.getParent());
		if (!folder.exists()) folder.mkdirs();
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			String[] lines = this.rawBody.split(StringUtility.CRLF);
			for (int i=0; i<lines.length; i++) {
				String line = lines[i];
				out.write(Base64.decode(line, Base64.DONT_GUNZIP));
			}
			out.close();
			return true;
		} catch (IOException e) {
			logger.error(String.format("Failed to write file '%s'", filePath), e);
			return false;
		}
	}
	
	private boolean saveQuotedPrintable(String filePath) {
		File file = new File(filePath);
		File folder = new File(file.getParent());
		if (!folder.exists()) folder.mkdirs();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "US-ASCII"));
			writeQuotedPrintable(out);
			out.close();
			return true;
		} catch (IOException e) {
			logger.error(String.format("Failed to write file '%s'", filePath), e);
			return false;
		}
	}
	
	private void writeQuotedPrintable(BufferedWriter out) throws IOException {
		Pattern p = new Pattern("=([0-9A-F]{2})", REFlags.IGNORE_CASE);
		Substitution decode = new Substitution() {
			@Override
			public void appendSubstitution(MatchResult capture, TextBuffer dest) {
				int decimal = Integer.parseInt(capture.group(1), 16);
				dest.append((char)decimal);
			}
		};
		Replacer r = p.replacer(decode);
		String[] lines = this.rawBody.split(StringUtility.CRLF);
		for (int i=0; i<lines.length; i++) {
			String line = lines[i];
			String decodedLine = r.replace(line);
			if (decodedLine.endsWith("=")) {
				out.append(decodedLine.substring(0, decodedLine.length() - 2));
			} else {
				out.append(decodedLine);
				out.append(StringUtility.CRLF);
			}
		}
	}
}
