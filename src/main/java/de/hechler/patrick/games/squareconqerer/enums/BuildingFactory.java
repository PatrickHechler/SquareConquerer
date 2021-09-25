package de.hechler.patrick.games.squareconqerer.enums;

import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.objects.ArrowTower;
import de.hechler.patrick.games.squareconqerer.objects.DefenceTower;
import de.hechler.patrick.games.squareconqerer.objects.HealingStation;
import de.hechler.patrick.games.squareconqerer.objects.UnitMaker;

public enum BuildingFactory {
	
	healing_station,
	
	unit_maker,
	
	defence_tower, arrow_tower,
	
	;
	
	// factory for buildings
	public Building create() {
		switch (this) {
		case healing_station:
			return new HealingStation();
		case unit_maker:
			return new UnitMaker();
		case defence_tower:
			return new DefenceTower();
		case arrow_tower:
			return new ArrowTower();
		default:
			throw new InternalError("unknown BuildArt: " + super.name());
		}
	}
	
	public char letter() {
		switch (this) {
		case healing_station:
			return 'H';
		case unit_maker:
			return 'U';
		case defence_tower:
			return 'D';
		case arrow_tower:
			return 'A';
		default:
			throw new InternalError("unknown BuildArt: " + super.name());
		}
	}
	
}
