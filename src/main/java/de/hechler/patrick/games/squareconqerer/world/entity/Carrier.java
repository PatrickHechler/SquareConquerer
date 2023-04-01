package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;

public final class Carrier extends UnitImpl {
	
	public Carrier(int x, int y, User usr) {
		super(x, y, usr, 3, 5);
	}
	
	@Override
	public int moveRange() {
		return 3 - (carryAmount >>> 1);
	}
	
}
