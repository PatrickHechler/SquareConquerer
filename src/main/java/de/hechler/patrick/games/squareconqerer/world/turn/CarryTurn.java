package de.hechler.patrick.games.squareconqerer.world.turn;

import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public record CarryTurn(Resource res, int amount) implements EntityTurn {
	
}
