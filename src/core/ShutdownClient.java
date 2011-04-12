package core;

import java.io.IOException;
import java.net.UnknownHostException;

public class ShutdownClient extends BaseClient {
	
	public void shutdown() throws UnknownHostException, IOException, ProtocolException {
		this.sendAndReceive(null, 0);
		this.sendAndReceive("SHUTDOWN", 0);
	}
}
