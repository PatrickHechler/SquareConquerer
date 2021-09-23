package de.hechler.patrick.games.squareconqerer.interfaces;

public interface Entety {
	
	Player owner();
	
	boolean attacked(Entety e);
	
	void defended(Entety e);
	
	void selfkill();
	
	void setXY(int x, int y);
	
	int getX();
	
	int getY();
	
	int lives();
	
}