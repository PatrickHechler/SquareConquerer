package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.Direction;

public class MoveUnitAction extends Action {
	
	final Direction dir;
	final Unit u;
	
	public MoveUnitAction(Direction dir, Unit u) {
		this.dir = dir;
		this.u = u;
	}
	
}
