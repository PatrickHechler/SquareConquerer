package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class Carrier extends UnitImpl {
	
	public static final String NAME   = "Carrier";
	public static final int    NUMBER = 0x925D9B86;
	
	private static final int MAX_LIVES  = 3;
	private static final int VIEW_RANGE = 4;
	private static final int MAX_CARRY  = 5;
	
	private static final int ORIDINAL_BASE_VALUE = 1;
	private static int       oridinal;
	
	public Carrier(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, VIEW_RANGE, MAX_CARRY);
	}
	
	public Carrier(int x, int y, User usr, int lives, Resource res, int carryAmount) {
		super(x, y, usr, MAX_LIVES, lives, VIEW_RANGE, MAX_CARRY, carryAmount, res);
	}
	
	@Override
	public int moveRange() {
		return 3 - (super.carryAmount >>> 1);
	}
	
	@Override
	public Carrier copy() {
		return new Carrier(super.x, super.y, owner(), lives(), super.carryResource, super.carryAmount);
	}
	
	@Override
	public int ordinal() {
		if (oridinal == 0) {
			oridinal = ORIDINAL_BASE_VALUE + SquareConquererAddon.theGame().oridinalOffsetUnit();
		}
		return oridinal;
	}
	
}
