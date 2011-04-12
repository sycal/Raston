package web;

import java.net.Socket;

import org.apache.log4j.Logger;

import core.IServerHandler;
import core.ServerResponse;
import core.SystemFailureException;

public class WebServerHandler implements IServerHandler {
	
	private static Logger logger = Logger.getLogger(WebServerHandler.class);

	@Override
	public ServerResponse processLine(String line, Socket connectedSocket) throws SystemFailureException {
		logger.debug(line);
		return new ServerResponse();
	}

}
