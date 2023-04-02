package de.hechler.patrick.games.squareconqerer.exceptions;

import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;

@SuppressWarnings("serial")
public class TurnExecutionException extends Exception {
	
	public final ErrorType type;
	
	public TurnExecutionException(ErrorType type) {
		super();
		this.type = type;
	}
	
}
