package de.hechler.patrick.games.squareconqerer.world.turn;

import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public record CarryTurn(Resource res, int amount) implements EntityTurn {
	
}
