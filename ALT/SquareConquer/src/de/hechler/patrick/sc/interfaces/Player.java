package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.objects.World;

public interface Player {
	
	void init(final World world, final int myID);
	
	void makeTurn();
	
}
