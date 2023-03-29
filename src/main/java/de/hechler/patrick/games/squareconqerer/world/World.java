package de.hechler.patrick.games.squareconqerer.world;

import de.hechler.patrick.games.squareconqerer.User;

public interface World {
	
	User user();
	
	int xlen();
	
	int ylen();
	
	Tile tile(int x, int y);

}
