package de.hechler.patrick.games.squareconqerer.world.turn;

import de.hechler.patrick.games.squareconqerer.world.entity.Unit;

public record StoreTurn(Unit entity, int amount) implements EntityTurn {

}
