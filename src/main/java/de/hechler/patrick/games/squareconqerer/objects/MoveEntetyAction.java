package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.Direction;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class MoveEntetyAction extends EntetyAction {
	
	final Direction dir;
	
	public MoveEntetyAction(Entety e, Direction dir) {
		super(e);
		this.dir = dir;
	}
	
	@Override
	public String toString() {
		return "MoveEntety[entety=" + e + ", dir=" + dir + "]";
	}
	
}
