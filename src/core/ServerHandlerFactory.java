package core;

public class ServerHandlerFactory {
	
	public enum ServerProtocolType { Smtp, Web }
	
	public static IServerHandler createServerHandler(ServerProtocolType protocolType) throws SystemFailureException {
		switch (protocolType) {
		case Smtp:
			return new email.smtp.SmtpServerHandler();
		case Web:
			return new web.WebServerHandler();
		default:
			throw new SystemFailureException("Unknown protocol: %s", protocolType);
		}
	}
}
