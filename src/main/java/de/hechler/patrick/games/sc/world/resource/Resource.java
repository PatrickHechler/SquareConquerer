package de.hechler.patrick.games.sc.world.resource;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addable.ResourceType;

public interface Resource extends Imagable {
	
	ResourceType type();
	
	Resource unmodifiable();
	
}
