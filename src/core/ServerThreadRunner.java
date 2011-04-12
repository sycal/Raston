package core;

import org.apache.log4j.Logger;

public class ServerThreadRunner implements Runnable {

	private static Logger logger = Logger.getLogger(ServerThreadRunner.class);
	
	private Server server;
	
	public ServerThreadRunner(Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			this.server.run();
		} catch (Exception e) {
			logger.error("Failed whilst running server on thread", e);
		}
	}
}
