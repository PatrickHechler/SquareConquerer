package de.hechler.patrick.games.sc.world.entity;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addable.EntityType;

public sealed interface Entity<M extends Entity<M, T>, T extends EntityType> extends Imagable permits Unit, Build {
	
	T type();
	
	M unmodifiable();
	
}
