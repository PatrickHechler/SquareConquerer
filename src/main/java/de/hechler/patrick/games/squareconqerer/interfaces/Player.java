package de.hechler.patrick.games.squareconqerer.interfaces;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.objects.*;

public interface Player {
	
	/**
	 * sets the {@link PlayersSquare} of this Player
	 */
	void setMySquare(PlayersSquare mySquare);
	
	/**
	 * returns the {@link PlayersSquare} of this Player
	 */
	PlayersSquare getMySquare();
	
	/**
	 * starts the Turn of this Player.
	 */
	void startTurn();
	
	/**
	 * returns only {@code true}, when this Player is currently on his turn and did not finished it yet.
	 * 
	 * the Turn ends BEFORE the {@link #getTurn()}.
	 */
	boolean isOnTurn();
	
	/**
	 * returns the Turn, which had been done.<br>
	 * 
	 * this can be done only one time for each turn.
	 * 
	 * after this call {@link #startTurn} has to be called, before recalling {@link #getTurn()}
	 * 
	 * @return the Turn, which had been done
	 */
	Turn getTurn();
	
	/**
	 * return the Object which will be notified using {@link Object#notifyAll()} at the end of each turn
	 */
	Object getNotify();
	
	/**
	 * called if the execution of the turn cased an {@link TurnExecutionException}
	 * 
	 * @param tee
	 *            the thrown {@link TurnExecutionException}
	 */
	void invalidTurn(TurnExecutionException tee);
	
}
