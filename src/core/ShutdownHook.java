package core;

import java.io.IOException;

import org.apache.log4j.Logger;


public class ShutdownHook extends Thread {
	
	private static Logger logger = Logger.getLogger(ShutdownHook.class);
	
	private Server server;
	
	public ShutdownHook(Server server) {
		this.server = server;
	}
	
	public void run() {
		try {
			logger.debug("Stopping server ...");
			this.server.stop();
		} catch (IOException e) {
			logger.error("Failed to stop server", e);
		}
	}
}
