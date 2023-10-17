package de.hechler.patrick.games.sc.turn;

import de.hechler.patrick.games.sc.world.entity.Entity;

public record Attack(Entity<?, ?> target) implements MoveAct {
	
}
