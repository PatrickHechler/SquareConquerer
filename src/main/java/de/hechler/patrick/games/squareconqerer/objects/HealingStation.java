package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class HealingStation implements Building {
	
	private int build = 2;
	
	@Override
	public void use(Entety e) {
		if (build > 0) {
			build -- ;
		} else {
			e.heal(1);
		}
	}
	
	@Override
	public BuildingFactory factory() {
		return BuildingFactory.healing_station;
	}
	
	@Override
	public int buildLen() {
		return build;
	}
	
	@Override
	public String toString() {
		return "HealingStation[build=" + build + "]";
	}
	
}
