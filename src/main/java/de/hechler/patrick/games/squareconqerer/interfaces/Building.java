package de.hechler.patrick.games.squareconqerer.interfaces;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.objects.Tile;

public interface Building {
	
	void use(Entety e) throws TurnExecutionException;
	
	boolean usable(Entety e);
	
	void act(Tile pos) throws TurnExecutionException;
	
	boolean actable(Tile pos);
	
	BuildingFactory factory();
	
	int buildLen();
	
	String toString();
	
	char infoLetter();
	
	Object snapshot();
	
	void rollback(Object sn);
	
}
