package de.hechler.patrick.games.squareconqerer.addons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import de.hechler.patrick.games.squareconqerer.addons.entities.AddonEntities;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.EntityImpl;
import de.hechler.patrick.games.squareconqerer.world.entity.MyBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.MyUnit;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;

/**
 * an addon can be used to add stuff to the base game<br>
 * for example units and buildings
 * <p>
 * every addon has its own {@link #license() license} and
 * {@link #help() help}/{@link #credits() credits} page and
 */
public abstract class SquareConquererAddon {
	
	public static final String GAME_ADDON_NAME = "Square Conquerer";
	
	/**
	 * this field stores the unique name of this add-on
	 */
	public final String name;
	private final int   oridinalLength;
	private int         oridinalOffset;
	
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
	public SquareConquererAddon(String name, int oridinalLength) {
		this.name           = name;
		this.oridinalLength = oridinalLength;
	}
	
	protected void initOridinalOffset(int offset) {
		if (oridinalOffset != 0) {
			throw new AssertionError("offset already has a non-zero value");
		}
		oridinalOffset = offset;
	}
	
	public int oridinalOffset() {
		if (oridinalOffset == 0) {
			throw new AssertionError("offset is not yet initialized");
		}
		return oridinalOffset;
	}
	
	private static volatile Map<String, SquareConquererAddon>                  addons;
	private static volatile TheGameAddon                                       theGame;
	private static volatile Map<Class<? extends Entity>, SquareConquererAddon> entity;
	
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
			Map<Class<? extends Entity>, SquareConquererAddon> m   = new HashMap<>();
			int                                                off = ((SquareConquererAddon) g).oridinalLength;
			for (SquareConquererAddon addon : new TreeMap<>(a).values()) {
				if (addon != g) {
					addon.initOridinalOffset(off);
					off += addon.oridinalLength;
				}
				for (Class<? extends Entity> cls : addon.entities().entityClassses().keySet()) {
					if (m.put(cls, addon) != null) {
						throw new AssertionError("multiple addons add the same entity class!");
					}
				}
			}
			addons  = Collections.unmodifiableMap(a);
			theGame = g;
			return a;
		}
	}
	
	public static SquareConquererAddon addon(Entity e) {
		Map<Class<? extends Entity>, SquareConquererAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (e instanceof EntityImpl && !(e instanceof MyUnit || e instanceof MyBuild)) {
			return theGame;
		}
		SquareConquererAddon res = es.get(e.getClass());
		if (res == null) throw new AssertionError("unknown entity class: " + e.getClass());
		return res;
	}
	
	public static SquareConquererAddon addon(Unit u) {
		Map<Class<? extends Entity>, SquareConquererAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (u instanceof EntityImpl && !(u instanceof MyUnit)) {
			return theGame;
		}
		SquareConquererAddon res = es.get(u.getClass());
		if (res == null) throw new AssertionError("unknown entity class: " + u.getClass());
		return res;
	}
	
	public static SquareConquererAddon addon(Building b) {
		Map<Class<? extends Entity>, SquareConquererAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (b instanceof EntityImpl && !(b instanceof MyUnit)) {
			return theGame;
		}
		SquareConquererAddon res = es.get(b.getClass());
		if (res == null) throw new AssertionError("unknown entity class: " + b.getClass());
		return res;
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
	
	public abstract AddonEntities entities();
	
}
