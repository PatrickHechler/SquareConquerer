package de.hechler.patrick.games.squareconqerer.addons.defaults;

import java.util.Collection;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.entities.AddonEntities;
import de.hechler.patrick.games.squareconqerer.addons.entities.EntityTraitWithVal;

/**
 * this interface is used to let addons define default values
 * 
 * @author Patrick Hechler
 */
public interface AddonDefaults {
	
	/**
	 * returns the default start entities of this addon
	 * <p>
	 * the keys are the {@link AddonEntities#entityClassses() class names}<br>
	 * the values are collections, where each Collection element represents the traits (see
	 * {@link AddonEntities#createEntity(String, User, Map, int, int)})
	 * 
	 * @return the default start entities of this addon
	 */
	Map<String, Collection<Map<String, EntityTraitWithVal>>> startEntities();
	
}
