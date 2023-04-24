package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;

public abstract non-sealed class MyBuild extends BuildingImpl {
	
	public MyBuild(int x, int y, User usr, int maxlives, EnumIntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives, neededResources);
	}
	
	public MyBuild(int x, int y, User usr, int maxlives, int lives, EnumIntMap<ProducableResourceType> neededResources, int remainingBuildTurns) {
		super(x, y, usr, maxlives, lives, neededResources, remainingBuildTurns);
	}
	
}
