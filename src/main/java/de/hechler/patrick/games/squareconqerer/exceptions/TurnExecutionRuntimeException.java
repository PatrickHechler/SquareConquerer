package de.hechler.patrick.games.squareconqerer.exceptions;


/**
 * used when no {@link Exception}s can be thrown and the catched and rethrown as {@link TurnExecutionException}
 * 
 * @author Patrick
 */
public class TurnExecutionRuntimeException extends RuntimeException {
	
	/** UID */
	private static final long serialVersionUID = 3390786633811374018L;
	
	public TurnExecutionRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public TurnExecutionRuntimeException(String message) {
		super(message);
	}
	
}
