package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

public sealed interface Unit extends Entity, ImageableObj permits UnitImpl {
	
	static final int COUNT = 1;
	
	void changePos(int newx, int newy, Tile checkcanEnter) throws TurnExecutionException;
	
	Resource carryRes();
	
	int carryAmount();
	
	int carryMaxAmount();
	
	void carry(Resource res, int amount) throws TurnExecutionException;
	
	void uncarry(int amount) throws TurnExecutionException;
	
	int moveRange();
	
	@Override
	Unit copy();
	
	static int ordinal(Unit u) {
		return u == null ? 0 : u.ordinal();
	}
	
}
