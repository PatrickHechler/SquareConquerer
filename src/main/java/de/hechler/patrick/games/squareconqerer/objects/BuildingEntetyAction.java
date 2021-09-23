package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class BuildingEntetyAction extends EntetyAction {
	
	final BuildingFactory build;
	
	public BuildingEntetyAction(Entety e, BuildingFactory build) {
		super(e);
		this.build = build;
	}
	
	@Override
	public String toString() {
		return "CreatBuildingEntetyAction[entety=" + e + ", build=" + build + "]";
	}
	
}
