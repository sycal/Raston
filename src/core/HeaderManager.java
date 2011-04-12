package core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import util.StringUtility;

public class HeaderManager {
	
	private static Logger logger = Logger.getLogger(HeaderManager.class);

	private List<Header> headers;
	
	public HeaderManager() {
		this.headers = new ArrayList<Header>();
	}
	
	public String getHeader(String headerName) {
		for (Header header : this.headers) {
			if (headerName.equalsIgnoreCase(header.getName())) {
				return header.getValue();
			}
		}
		return null;
	}
	
	public String getHeaderAndDelete(String headerName) {
		for (int i=0; i<this.headers.size(); i++) {
			Header header = this.headers.get(i);
			if (headerName.equalsIgnoreCase(header.getName())) {
				this.headers.remove(i);
				return header.getValue();
			}	
		}
		return null;
	}
	
	public void deleteHeader(String headerName) {
		for (int i=0; i<this.headers.size(); i++) {
			Header header = this.headers.get(i);
			if (headerName.equalsIgnoreCase(header.getName())) {
				this.headers.remove(i);
				break;
			}	
		}
	}
	
	public void setHeader(String headerName, String headerValue) {
		for (Header header : this.headers) {
			if (headerName.equalsIgnoreCase(header.getName())) {
				header.setValue(headerValue);
				return;
			}
		}
		this.addHeader(headerName, headerValue);
	}
	
	public void addHeader(String headerName, String headerValue) {		
		this.insertHeader(headerName, headerValue, this.headers.size());
	}
	
	public void insertHeader(String headerName, String headerValue, int index) {
		logger.debug(String.format("Adding header index-%d '%s: %s'", index, headerName, headerValue));
		this.headers.add(index, new Header(headerName, headerValue));
	}
	
	public static HeaderManager parse(String rawHeaders) {
		HeaderManager hm = new HeaderManager();
		String unfoldedHeaders = unfoldHeaders(rawHeaders);
		String[] headerParts = unfoldedHeaders.split(StringUtility.CRLF);
		for (String headerLine : headerParts) {
			String[] headerLineParts = headerLine.split(":", 2);
			if (headerLineParts.length == 2) {
				String headerName = headerLineParts[0].trim();
				String headerValue = headerLineParts[1].trim();
				hm.addHeader(headerName, headerValue);
			} else {
				logger.warn(String.format("Invalid header line detected: '%s'", headerLine));
			}
		}
		return hm;
	}
	
	public int size() {
		return this.headers.size();
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		if (this.headers.size() > 0) {
			for (Header header : this.headers) {
				output.append(header.getName());
				output.append(": ");
				output.append(header.getValue());
				output.append(StringUtility.CRLF);
			}
		}
		return output.toString();
	}
	
	/**
	 * As per RFC822 3.1.1, a single header can span multiple lines, so this function
	 * merges them back into a single line to assist with analysis.
	 * @param headerBlock
	 * @return	The unfolded headers
	 * 
	 */
	private static String unfoldHeaders(String headerBlock) {
		return headerBlock.trim().replaceAll(StringUtility.CRLF + "\\s+", " ");
	}
}
