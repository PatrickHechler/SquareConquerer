package de.hechler.patrick.games.squareconqerer.interfaces;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;

public interface Building {
	
	void use(Entety e);
	
	BuildingFactory factory();
	
	int buildLen();
	
}
