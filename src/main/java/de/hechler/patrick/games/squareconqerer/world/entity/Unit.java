package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public sealed interface Unit extends Entity permits UnitImpl {
	
	void xy(int newx, int newy);
	
	Resource carryRes();
	
	int carryAmount();
	
	int carryMaxAmount();
	
	void carry(Resource res, int amount);
	
	void uncarry(int amount);
	
	int moveRange();
	
}
