package de.hechler.patrick.games.squareconqerer.enums;

import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.objects.HealingStation;

public enum BuildingFactory {
	
	healing_station
	
	;
	
	// factory for buildings
	public Building create() {
		switch (this) {
		case healing_station:
			return new HealingStation();
		default:
			throw new InternalError("unknown BuildArt: " + super.name());
		}
	}
	
	public char letter() {
		switch (this) {
		case healing_station:
			return 'H';
		default:
			throw new InternalError("unknown BuildArt: " + super.name());
		}
	}
	
}
