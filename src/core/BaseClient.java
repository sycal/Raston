package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import util.StringUtility;

public abstract class BaseClient {
	
	private static Logger logger = Logger.getLogger(BaseClient.class);
	
	private Socket clientSocket;
	private boolean isConnected;
	
	public void connect(String host, int port) throws UnknownHostException, IOException, ProtocolException {
		logger.debug(String.format("Connecting to '%s' on port '%d'", host, port));
		this.clientSocket = new Socket(host, port);
		this.onConnected();
		this.isConnected = true;
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	public void close() throws IOException, ProtocolException {
		this.isConnected = false;
		if (this.clientSocket != null) {
			this.onClosing();
			this.clientSocket.close();
			this.clientSocket = null;
		}
	}
	
	protected void onConnected() throws IOException, ProtocolException {
	}
	
	protected void onClosing() throws IOException, ProtocolException {
	}

	protected ServerResponse sendAndReceive(String lineToSend, int expectedCode) throws IOException, ProtocolException {
		PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
		if (lineToSend != null) {
			out.write(lineToSend);
			if (!lineToSend.endsWith(StringUtility.CRLF)) out.write(StringUtility.CRLF);
			out.flush();
			logger.debug(StringUtility.cap("TX: " + lineToSend, 79, "..."));
		}
		ServerResponse finalResponse = null;
		if (expectedCode >= 0) {
			BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			StringBuilder responseText = new StringBuilder();
			String responseLineRawText;
			int actualCode = 0;
			while ((responseLineRawText = in.readLine()) != null) {
				logger.debug(StringUtility.cap("RX: " + responseLineRawText, 79, "..."));
				ServerResponse responseLine = ServerResponse.parseLine(responseLineRawText);
				actualCode = responseLine.getCode();
				if ((expectedCode > 0) && (actualCode != expectedCode)) {
					throw new ProtocolException("Expected code '%d'. Received '%s'.", expectedCode, responseLineRawText);
				}
				responseText.append(responseLine.getMessage() + StringUtility.NEW_LINE);
				if (!responseLine.isMultiline()) break;
			}
			finalResponse = new ServerResponse(actualCode, responseText.toString());
		}
		return finalResponse;
	}
}
