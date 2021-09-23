package de.hechler.patrick.games.squareconqerer.exceptions;


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
