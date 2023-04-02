package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public sealed interface Unit extends Entity permits UnitImpl {
	
	void changePos(int newx, int newy, Tile checkcanEnter) throws TurnExecutionException;
	
	Resource carryRes();
	
	int carryAmount();
	
	int carryMaxAmount();
	
	void carry(Resource res, int amount) throws TurnExecutionException;
	
	void uncarry(int amount) throws TurnExecutionException;
	
	int moveRange();
	
	@Override
	Unit copy();
	
}
