package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Building;

public class ActingBuildingAction extends BuildingAction {
	
	final int x, y;
	
	public ActingBuildingAction(Building b, int x, int y) {
		super(b);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "MakeUnitBuildingActio[building=" + b + "]";
	}
	
}
