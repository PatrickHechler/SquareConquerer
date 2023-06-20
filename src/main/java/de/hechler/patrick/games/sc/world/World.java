package de.hechler.patrick.games.sc.world;

public interface World {
	
	int xlen();
	
	int ylen();
	
	Tile tile(int x, int y);
	
}
