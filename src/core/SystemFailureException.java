package core;

@SuppressWarnings("serial")
public class SystemFailureException extends Exception {
	
	public SystemFailureException(String message) {
		super(message);
	}
	
	public SystemFailureException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}
}
