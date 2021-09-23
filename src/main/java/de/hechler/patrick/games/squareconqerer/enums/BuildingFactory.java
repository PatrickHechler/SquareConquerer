package de.hechler.patrick.games.squareconqerer.enums;

import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.objects.HealingStation;
import de.hechler.patrick.games.squareconqerer.objects.UnitMaker;

public enum BuildingFactory {
	
	healing_station,
	
	unit_maker,
	
	;
	
	// factory for buildings
	public Building create() {
		switch (this) {
		case healing_station:
			return new HealingStation();
		case unit_maker:
			return new UnitMaker();
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
		default:
			throw new InternalError("unknown BuildArt: " + super.name());
		}
	}
	
}
