package de.hechler.patrick.games.squareconqerer.exceptions;

import de.hechler.patrick.games.squareconqerer.objects.TheSquare;
import de.hechler.patrick.games.squareconqerer.objects.Turn;

/**
 * used if an error occurs during the {@link Turn} {@link TheSquare#execute(Turn) execution}
 * 
 * @author Patrick
 */
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
