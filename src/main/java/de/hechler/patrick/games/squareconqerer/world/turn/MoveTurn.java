package de.hechler.patrick.games.squareconqerer.world.turn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.Direction;

public record MoveTurn(Unit entity, List<Direction> dirs) implements EntityTurn {
	
	public MoveTurn(Unit entity, List<Direction> dirs) {
		this.entity = entity;
		this.dirs = Collections.unmodifiableList(new ArrayList<>(dirs));
	}
	
	public MoveTurn(Unit entity, Direction... dirs) {
		this(entity, Arrays.asList(dirs));
	}
	
}
