package de.hechler.patrick.games.sc.error;


public class TurnExecutionException extends Exception {
	
	private static final long serialVersionUID = 1604005557407158450L;
	
	public final ErrorType type;
	
	public TurnExecutionException(ErrorType type) {
		this.type = type;
	}
	
}
