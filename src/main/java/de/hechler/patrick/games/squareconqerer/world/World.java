package de.hechler.patrick.games.squareconqerer.world;

import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public sealed interface World permits RootWorld, RootWorld.Builder, RemoteWorld, UserWorld {
	
	User user();
	
	int xlen();
	
	int ylen();
	
	Tile tile(int x, int y);
	
	/**
	 * adds a new next turn listener.
	 * every time the world changes all next turn listeners are executed
	 * 
	 * @param listener the next turn listener to add
	 */
	void addNextTurnListener(Runnable listener);
	
	void removeNextTurnListener(Runnable listener);
	
	Map<User, List<Entity>> entities();
	
	void finish(Turn t);
	
}
