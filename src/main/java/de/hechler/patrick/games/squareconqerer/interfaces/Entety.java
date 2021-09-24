package de.hechler.patrick.games.squareconqerer.interfaces;

import de.hechler.patrick.zeugs.interfaces.Position2D;

public interface Entety extends Position2D {
	
	Player owner();
	
	boolean attacked(Entety e);
	
	void defended(Entety e);
	
	void selfkill();
	
	void setXY(int x, int y);
	
	int getX();
	
	int getY();
	
	int lives();
	
	void heal(int strenght);

	String toString();
	
	Object snapshot();
	
	void rollback(Object sn);
	
}