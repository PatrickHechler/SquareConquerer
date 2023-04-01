package de.hechler.patrick.games.squareconqerer.world.turn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.enums.Direction;

public record MoveTurn(List<Direction> dirs) implements EntityTurn {
	
	public MoveTurn(List<Direction> dirs) {
		this.dirs = Collections.unmodifiableList(new ArrayList<>(dirs));
	}
	
	public MoveTurn(Direction... dirs) {
		this(Arrays.asList(dirs));
	}
	
}
