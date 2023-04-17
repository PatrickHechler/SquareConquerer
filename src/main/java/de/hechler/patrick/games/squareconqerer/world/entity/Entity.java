package de.hechler.patrick.games.squareconqerer.world.entity;

import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.User;

public sealed interface Entity permits Unit, Building, EntityImpl {
	
	int x();
	
	int y();
	
	User owner();
	
	int lives();
	
	int maxLives();
	
	int viewRange();
	
	Entity copy();
	
	/**
	 * returns an empty list
	 * <p>
	 * the returned list is sorted with the {@link Unit#compareTo(Unit)}
	 * 
	 * @return an empty list
	 */
	default List<Unit> units() {
		return Collections.emptyList();
	}
	
}
