package de.hechler.patrick.games.sc.world.ground;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addable.GroundType;

public interface Ground extends Imagable {
	
	GroundType type();
	
	Ground unmodifiable();
	
}
