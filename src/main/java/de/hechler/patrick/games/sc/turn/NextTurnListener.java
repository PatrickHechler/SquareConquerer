package de.hechler.patrick.games.sc.turn;

/**
 * this interface is used to inform listeners about world changes
 * 
 * @author Patrick Hechler
 */
@FunctionalInterface
public interface NextTurnListener {
	
	/**
	 * informs the listener that the world has changed
	 * 
	 * @param turn the new current turn
	 * @param worldHash the world hash (only when a turn was executed or the game started)
	 * @param turnHash the turn hash (only when a turn was executed)
	 */
	void nextTurn(int turn, byte[] worldHash, byte[] turnHash);
	
}
