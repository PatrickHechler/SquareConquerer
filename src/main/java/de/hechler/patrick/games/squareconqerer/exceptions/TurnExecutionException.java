package de.hechler.patrick.games.squareconqerer.exceptions;


public class TurnExecutionException extends Exception {
	
	/** UID */
	private static final long serialVersionUID = 7784312617465064191L;
	
	public TurnExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public TurnExecutionException(String message) {
		super(message);
	}
	
}
