package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public sealed interface Building extends Entity permits BuildingImpl {
	
	boolean isFinishedBuild();
	
	void store(Unit u, int amount);
	
	void giveRes(Unit u, Resource res, int amount);
	
	void build(Unit u);
	
}
