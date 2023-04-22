package de.hechler.patrick.games.squareconqerer.addons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import de.hechler.patrick.games.squareconqerer.addons.records.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.records.SCPage;

public abstract class SquareConquererAddon {
	
	public static final String GAME_ADDON_NAME = "Square Conquerer";
	
	/**
	 * this field stores the unique name of this add-on
	 */
	public final String name;
	
	/**
	 * creates a new {@link SquareConquererAddon} instance with the given
	 * {@code name}.
	 * <p>
	 * the name must be unique<br>
	 * this means, that the name {@value #GAME_ADDON_NAME} is not allowed to be used
	 * by any other add-on than the {@link TheGameAddon}.
	 * 
	 * @param name the {@link #name} of the add-on
	 */
	public SquareConquererAddon(String name) {
		this.name = name;
	}
	
	private static volatile Map<String, SquareConquererAddon> addons;
	private static volatile TheGameAddon                      theGame;
	
	/**
	 * returns the game add-on
	 * 
	 * @return the game add-on
	 */
	public static TheGameAddon theGame() {
		// the type TheGameAddon is not really useful for
		TheGameAddon g = theGame;
		if (g != null) return g;
		addonsMap();
		return theGame;
	}
	
	/**
	 * returns an unmodifiable collection containing almost all add-ons (excluding
	 * the game
	 * add-on)
	 * 
	 * @return an unmodifiable collection containing almost all add-ons (excluding
	 *         the game
	 *         add-on)
	 */
	public static Collection<SquareConquererAddon> onlyAddons() {
		Map<String, SquareConquererAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a.values();
	}
	
	/**
	 * returns a unmodifiable map containing almost all add-ons (excluding the game
	 * add-on)
	 * <p>
	 * the add-ons are mapped with their {@link #name} as keys
	 * 
	 * @return a unmodifiable map containing almost all add-ons (excluding the game
	 *         add-on)
	 */
	public static Map<String, SquareConquererAddon> onlyAddonsMap() {
		Map<String, SquareConquererAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a;
	}
	
	/**
	 * returns an unmodifiable collection containing all add-ons (including the game
	 * add-on)
	 * 
	 * @return an unmodifiable collection containing all add-ons (including the game
	 *         add-on)
	 */
	public static Collection<SquareConquererAddon> addons() {
		Map<String, SquareConquererAddon> a = addons;
		if (a == null) return addonsMap().values();
		return a.values();
	}
	
	/**
	 * returns a unmodifiable map containing all add-ons (including the game add-on)
	 * <p>
	 * the add-ons are mapped with their {@link #name} as keys
	 * 
	 * @return a unmodifiable map containing all add-ons (including the game add-on)
	 */
	public static Map<String, SquareConquererAddon> addonsMap() {
		Map<String, SquareConquererAddon> a = addons;
		if (a != null) return a;
		synchronized (SquareConquererAddon.class) {
			a = addons;
			if (a != null) return a;
			a = loadAddons();
			TheGameAddon g = (TheGameAddon) a.get(GAME_ADDON_NAME);
			if (g == null) {
				throw new AssertionError("I am missing the game addon!");
			}
			addons  = Collections.unmodifiableMap(a);
			theGame = g;
			return a;
		}
	}
	
	private static Map<String, SquareConquererAddon> loadAddons() {
		Map<String, SquareConquererAddon>   addons = new HashMap<>();
		ServiceLoader<SquareConquererAddon> loader = ServiceLoader.load(SquareConquererAddon.class);
		for (SquareConquererAddon addon : loader) {
			if (addons.put(addon.name, addon) != null) {
				throw new AssertionError("multiple addons with the same name: '" + addon.name + "'");
			}
		}
		return addons;
	}
	
	/**
	 * returns the license of this add-on
	 * 
	 * @return the license of this add-on
	 */
	public abstract SCLicense license();
	
	/**
	 * returns the help page of this add-on
	 * 
	 * @return the help page of this add-on
	 */
	public abstract SCPage help();
	
	/**
	 * returns the credits page of this add-on
	 * 
	 * @return the credits page of this add-on
	 */
	public abstract SCPage credits();
	
}
