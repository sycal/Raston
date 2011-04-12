package core;

@SuppressWarnings("serial")
public class ProtocolException extends Exception {

	public ProtocolException(String message) {
		super(message);
	}
	
	public ProtocolException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}
}
