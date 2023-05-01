package de.hechler.patrick.games.squareconqerer.addons.defaults;

import java.util.Collection;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTraitWithVal;

public interface AddonDefaults {
	
	Map<String, Collection<Map<String, EntityTraitWithVal>>> startEntities();
	
}
