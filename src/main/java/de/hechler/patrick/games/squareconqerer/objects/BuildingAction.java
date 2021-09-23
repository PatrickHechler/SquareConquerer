package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Building;

public abstract class BuildingAction extends Action {
	
	final Building b;
	
	public BuildingAction(Building b) {
		this.b = b;
	}
	
}
