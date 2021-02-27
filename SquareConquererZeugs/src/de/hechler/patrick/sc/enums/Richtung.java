package de.hechler.patrick.sc.enums;

import de.hechler.patrick.sc.objects.Position;

public enum Richtung {
	
	oben, unten,
	
	rechts, links
	
	;
	
	public Position ziel(Position pos) {
		switch (this) {
		case oben:
			return new Position(pos.x + 1, pos.y);
		case unten:
			return new Position(pos.x - 1, pos.y);
		case rechts:
			return new Position(pos.x, pos.y - 1);
		case links:
			return new Position(pos.x, pos.y - 1);
		default:
			throw new IllegalStateException("illegal line!");
		}
	}
	
}
