package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class Carrier extends UnitImpl {
	
	public static final int NUMBER = 0x925D9B86;
	
	private static final int MAX_LIVES  = 3;
	private static final int VIEW_RANGE = 4;
	private static final int MAX_CARRY  = 5;
	
	public Carrier(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, VIEW_RANGE, MAX_CARRY);
	}
	
	public Carrier(int x, int y, User usr, int lives, Resource res, int carryAmount) {
		super(x, y, usr, MAX_LIVES, lives, VIEW_RANGE, MAX_CARRY, carryAmount, res);
	}
	
	@Override
	public int moveRange() {
		return 3 - (carryAmount >>> 1);
	}
	
	@Override
	public Carrier copy() {
		return new Carrier(x, y, owner(), lives(), carryResource, carryAmount);
	}
	
	@Override
	public int ordinal() { return 1; }
	
}
