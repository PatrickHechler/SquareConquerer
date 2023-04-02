package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.enums.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public sealed interface Building extends Entity permits BuildingImpl {
	
	boolean isFinishedBuild();
	
	int remainingBuildTurns();
	
	void store(Unit u, int amount) throws TurnExecutionException;
	
	EnumIntMap<ProducableResourceType> neededResources();
	
	void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException;
	
	void build(Unit u) throws TurnExecutionException;
	
	@Override
	Building copy();
	
}
