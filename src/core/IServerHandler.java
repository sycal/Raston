package core;

import java.net.Socket;

public interface IServerHandler {
	
	ServerResponse processLine(String line, Socket connectedSocket) throws SystemFailureException;

}
