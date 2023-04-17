package de.hechler.patrick.games.squareconqerer.world.turn;

import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public record CarryTurn(Unit entity, Resource res, int amount) implements EntityTurn {
	
}
