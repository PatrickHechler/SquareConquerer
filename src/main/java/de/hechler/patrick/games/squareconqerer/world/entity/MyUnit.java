package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public abstract non-sealed class MyUnit extends UnitImpl {
	
	public MyUnit(int x, int y, User usr, int maxlives, int lives, int viewRange, int carryMaxAmount, int carryAmount, Resource res) {
		super(x, y, usr, maxlives, lives, viewRange, carryMaxAmount, carryAmount, res);
	}
	
	public MyUnit(int x, int y, User usr, int maxlives, int viewRange, int carryMaxAmount) {
		super(x, y, usr, maxlives, viewRange, carryMaxAmount);
	}
	
}
