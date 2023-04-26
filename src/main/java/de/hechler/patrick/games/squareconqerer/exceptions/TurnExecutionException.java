package de.hechler.patrick.games.squareconqerer.exceptions;

import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;

public class TurnExecutionException extends Exception {
	
	private static final long serialVersionUID = -11215257583259828L;
	
	public final ErrorType type;
	
	public TurnExecutionException(ErrorType type) {
		super();
		this.type = type;
	}
	
}
