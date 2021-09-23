package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.*;

/**
 * the Square of the Player.
 * 
 * for now it only delegates to {@link TheSquare},
 * 
 * but on server/client {@link TheSquare} would be the server
 * 
 * and {@link PlayersSquare} would be the client
 * 
 * even if on one devise with the two classes there is
 * 
 * the possiblility of a war cloud or something simmilar
 */
public class PlayersSquare {
	
	private final TheSquare square;
	private final Player player;
	
	public PlayersSquare(TheSquare square, Player player) {
		this.square = square;
		this.player = player;
	}
	
	public Tile getTile(int x, int y) {
		return square.getTile(x, y);
	}
	
	public int getXLen() {
		return square.getXLen();
	}
	
	public int getYLen() {
		return square.getYLen();
	}
	
	void died(Entety u) {
		square.died(u);
	}
	
	@Override
	public String toString() {
		return "playersSquare[player={" + player + "}; square={\n" + square.toString() + "}]";
	}
	
	public String squareString() {
		return square.toString();
	}

	public TurnExecutionException isValid(Turn t) {
		return square.isValid(t);
	}
	
}
