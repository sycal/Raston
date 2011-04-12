package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import util.PropertyHandler;
import util.StringUtility;

public class Receiver extends Thread {
	
	private static Logger logger = Logger.getLogger(Receiver.class);
	
	private final static String COMMAND_SHUTDOWN = "shutdown";
	private final static int DEFAULT_CLIENT_TIMEOUT_SECS = 60;
	
	private Socket connectedSocket;
	private IServerHandler protocol;
	
	public Receiver(IServerHandler protocol, Socket connectedSocket) {
		this.protocol = protocol;
		this.connectedSocket = connectedSocket;
		int clientTimeout = PropertyHandler.getInt("Server.ClientTimeoutSecs", DEFAULT_CLIENT_TIMEOUT_SECS);
		if (clientTimeout > 0) {
			try {
				this.connectedSocket.setSoTimeout(clientTimeout * 1000);
			} catch (SocketException e) {
				logger.error(String.format("Failed to set client timeout to %dms", clientTimeout));
			}
		}
	}

	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		try {
		    out = new PrintWriter(connectedSocket.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
		    String inputLine = null;
		    do {
		    	String clientIP = connectedSocket.getInetAddress().getHostAddress();
		    	//logger.debug(String.format("CLIENT (%s): %s", clientIP, inputLine));
		    	if ((inputLine != null) && inputLine.equalsIgnoreCase(COMMAND_SHUTDOWN) && connectedSocket.getInetAddress().isLoopbackAddress()) {
		    		logger.debug("SHUTDOWN command received");
		    		System.exit(0);
		    		break;
		    	}
		    	ServerResponse response = this.protocol.processLine(inputLine, this.connectedSocket);
			    if (response.hasResponse()) {
			    	String responseText = response.toString();
			    	logger.debug(String.format("CLIENT (%s): %s", clientIP, inputLine));
			    	logger.debug(String.format("SERVER: %s", StringUtility.removeSuffix(responseText, StringUtility.CRLF)));
			    	out.print(responseText);
			    	out.flush();
			    }
			    if (response.isFinished()) break;
		    } while ((inputLine = in.readLine()) != null);
		    logger.debug("Client connection ended");
		} catch (SocketTimeoutException e) {
			logger.debug("Client timed out");
		} catch (Exception e) {
			logger.error("Failed in Receiver", e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				connectedSocket.close();
			} catch (IOException e) {
				logger.error("Failed whilst cleaning up Receiver", e);
			}
		}
	}
}
