package de.hechler.patrick.games.squareconqerer.world.turn;

import de.hechler.patrick.games.squareconqerer.world.entity.Entity;

public sealed interface EntityTurn permits CarryTurn, MoveTurn, StoreTurn {
	
	Entity entity();
	
}
