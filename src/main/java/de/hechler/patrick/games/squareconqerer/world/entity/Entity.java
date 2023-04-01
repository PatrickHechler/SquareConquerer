package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;

public sealed interface Entity permits Unit, Building, EntityImpl {
	
	int x();
	
	int y();
	
	User owner();
	
	int lives();
	
	int maxLives();
	
}
