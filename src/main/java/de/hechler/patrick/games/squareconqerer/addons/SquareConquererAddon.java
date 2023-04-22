package de.hechler.patrick.games.squareconqerer.addons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import de.hechler.patrick.games.squareconqerer.addons.records.SCHelp;
import de.hechler.patrick.games.squareconqerer.addons.records.SCLicense;

public abstract class SquareConquererAddon {
	
	public static final String GAME_ADDON_NAME = "Square Conquerer";
	
	public final String name;
	
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
	
	public abstract Optional<SCLicense> license();
	
	public abstract SCHelp help();
	
}
