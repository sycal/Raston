package core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import core.ServerHandlerFactory.ServerProtocolType;

public class Server {
	
	private static Logger logger = Logger.getLogger(Server.class);
	
	private ServerSocket listener;
	private ServerProtocolType serverProtocol;
	
	public Server(ServerProtocolType serverProtocol, int port) throws SystemFailureException {
		this.serverProtocol = serverProtocol;
		try {
			this.listener = new ServerSocket(port);
		} catch (IOException e) {
			throw new SystemFailureException("Failed to start server: %s", e.toString());
		}
	}
	
	public void stop() throws IOException {
		if (this.listener != null) {
			this.listener.close();
		}
	}
	
	public boolean isRunning() {
		return !this.listener.isClosed();
	}
	
	public void run() throws IOException {
		try {
			while (true) {
				Socket newSocket = this.listener.accept();
				logger.info(String.format("Received connection from client socket %s", newSocket.getRemoteSocketAddress()));
				IServerHandler protocolHandler = ServerHandlerFactory.createServerHandler(this.serverProtocol);
				new Receiver(protocolHandler, newSocket).start();
			}
		} catch (SocketException e) {
			logger.debug("Server listener socket closed.");
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
