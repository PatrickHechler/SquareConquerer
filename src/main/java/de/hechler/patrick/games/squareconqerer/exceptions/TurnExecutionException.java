package de.hechler.patrick.games.squareconqerer.exceptions;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;

@SuppressWarnings("serial")
public class TurnExecutionException extends Exception {
	
	public final User      usr;
	public final ErrorType type;
	
	public TurnExecutionException(User usr, ErrorType type) {
		super();
		this.usr  = usr;
		this.type = type;
	}
	
}
