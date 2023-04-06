package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

public sealed interface Building extends Entity, ImageableObj permits BuildingImpl {

	static final int COUNT = 1;
	
	boolean isFinishedBuild();
	
	int remainingBuildTurns();
	
	void store(Unit u, int amount) throws TurnExecutionException;
	
	EnumIntMap<ProducableResourceType> neededResources();
	
	void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException;
	
	void build(Unit u) throws TurnExecutionException;
	
	@Override
	Building copy();
	
	static int ordinal(Building b) {
		return b == null ? 0 : b.ordinal();
	}
	
}
