package de.hechler.patrick.games.squareconqerer.interfaces;

import de.hechler.patrick.zeugs.interfaces.Position2D;

public interface Entety extends Position2D {
	
	Player owner();
	
	void selfkill();
	
	int lives();
	
	void damage(int strengh);
	
	void heal(int strenght);
	
	String toString();
	
	Object snapshot();
	
	void rollback(Object sn);
	
}