package de.hechler.patrick.games.squareconqerer.addons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
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
 * every addon has its own {@link #license() license} and {@link #help() help}/{@link #credits() credits} page and
 */
public abstract class SquareConquererAddon {
	
	/**
	 * the {@link #name} of {@link TheGameAddon}
	 */
	public static final String GAME_ADDON_NAME             = "Square Conquerer";
	/**
	 * the property key ({@link System#getProperty(String)}) of a colon ({@code :}) separated list of all disabled addons. <br>
	 * to disable a addon with a colon in the name use a backslash before the colon ({@code \:})<br>
	 * to disable a addon with a backslash in the name use a backslash before the backslash ({@code \\})<br>
	 * on any other position a backslash is invalid and will let the load of the addons fail<br>
	 * if a non existing addon is disabled, it will be ignored
	 */
	public static final String DISABLED_ADDONS_PROPERTY    = "squareconquerer.addons.disabled";
	/**
	 * the key start of the rename property.<br>
	 * If you have multiple addons with the same name, you can use this property to rename one:<br>
	 * set the System property <code>({@value #RENAME_ADDON_PROPERTY_START} ( + {@link Module#getName()} + ':' )? + {@link Class#getName()})</code> to the new
	 * name:<br>
	 * <code>{@link System#setProperty(String, String) System.setProperty}({@value #RENAME_ADDON_PROPERTY_START} ( + {@link Module#getName()} + ':' )? + {@link Class#getName()}, newName)</code>
	 * <p>
	 * the modules name must be inserted, when the class is in a named module ({@link Module#isNamed()}, otherwise the modules name must be omitted)<br>
	 * so even if there are two addons with the same name (class and addon), one of them can be renamed, if they are in different modules
	 * <p>
	 * the methods <code>renameAddonProperty(...)</code> and <code>renameAddonPropertyKey(...)</code> can help.<br>
	 * <code>renameAddonPropertyKey(...)</code> generates the key of the system property and <code>renameAddonProperty(...)</code> renames the system property
	 * <p>
	 * note that you have to use the exact class name (the name of a super class will only work for exact instances of the superclass)
	 */
	public static final String RENAME_ADDON_PROPERTY_START = "squareconquerer.addons.rename.";
	
	public static String renameAddonPropertyKey(Class<? extends SquareConquererAddon> cls) {
		String name = cls.getName();
		Module mod  = cls.getModule();
		if (mod.isNamed()) {
			return RENAME_ADDON_PROPERTY_START + mod.getName() + ':' + name;
		} else {
			return RENAME_ADDON_PROPERTY_START + name;
		}
	}
	
	public static String renameAddonProperty(Class<? extends SquareConquererAddon> cls, String newValue) {
		return System.setProperty(renameAddonPropertyKey(cls), newValue);
	}
	
	/**
	 * this field stores the unique name of this add-on
	 */
	public final String name;
	private int         oridinalUnitOffset;
	private int         oridinalBuildingOffset;
	
	/**
	 * creates a new {@link SquareConquererAddon} instance with the given {@code name}.
	 * <p>
	 * the name must be unique<br>
	 * this means, that the name {@value #GAME_ADDON_NAME} is not allowed to be used by any other add-on than the {@link TheGameAddon}.
	 * <p>
	 * if the System property {@link #renameAddonPropertyKey(Class)} is set, its value will be used as {@link #name} instead of <code>name</code>, which will be
	 * ignored.<br>
	 * see {@link #RENAME_ADDON_PROPERTY_START} for details.
	 * 
	 * @param name the {@link #this.name} of the add-on
	 */
	public SquareConquererAddon(String name) {
		String overwrittenName = System.getProperty(renameAddonPropertyKey(getClass()));
		if (overwrittenName != null) this.name = overwrittenName;
		else this.name = name;
	}
	
	protected void initOridinalOffset(int unitOff, int buildOff) {
		if (this.oridinalUnitOffset != 0) { throw new AssertionError("offset already has a non-zero value"); }
		this.oridinalUnitOffset     = unitOff;
		this.oridinalBuildingOffset = buildOff;
	}
	
	public int oridinalOffsetUnit() {
		return this.oridinalUnitOffset;
	}
	
	public int oridinalOffsetBuilding() {
		return this.oridinalBuildingOffset;
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
		TheGameAddon g = theGame;
		if (g != null) return g;
		addonsMap();
		return theGame;
	}
	
	/**
	 * returns an unmodifiable collection containing almost all add-ons (excluding the game add-on)
	 * 
	 * @return an unmodifiable collection containing almost all add-ons (excluding the game add-on)
	 */
	public static Collection<SquareConquererAddon> onlyAddons() {
		Map<String, SquareConquererAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a.values();
	}
	
	/**
	 * returns a unmodifiable map containing almost all add-ons (excluding the game add-on)
	 * <p>
	 * the add-ons are mapped with their {@link #this.name} as keys
	 * 
	 * @return a unmodifiable map containing almost all add-ons (excluding the game add-on)
	 */
	public static Map<String, SquareConquererAddon> onlyAddonsMap() {
		Map<String, SquareConquererAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a;
	}
	
	/**
	 * returns an unmodifiable collection containing all add-ons (including the game add-on)
	 * 
	 * @return an unmodifiable collection containing all add-ons (including the game add-on)
	 */
	public static Collection<SquareConquererAddon> addons() {
		Map<String, SquareConquererAddon> a = addons;
		if (a == null) return addonsMap().values();
		return a.values();
	}
	
	/**
	 * returns a unmodifiable map containing all add-ons (including the game add-on)
	 * <p>
	 * the add-ons are mapped with their {@link #this.name} as keys
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
				throw new AssertionError("I am missing the game addon! addons: " + a);
			}
			Map<Class<? extends Entity>, SquareConquererAddon> m         = new HashMap<>();
			int                                                ooffUnit  = 1;              // null
			int                                                ooffBuild = 1;              // null
			for (SquareConquererAddon addon : new TreeMap<>(a).values()) {
				addon.initOridinalOffset(ooffUnit, ooffBuild);
				Map<Class<? extends Entity>, String> map        = addon.entities().entityClassses();
				Set<String>                          wasAlready = new HashSet<>();
				for (Entry<Class<? extends Entity>, String> e : map.entrySet()) {
					Class<? extends Entity> cls = e.getKey();
					if (m.put(cls, addon) != null) {
						throw new AssertionError("multiple addons add the same entity class!");
					}
					String clsName = e.getValue();
					if (wasAlready.add(clsName)) {
						if (Unit.class.isAssignableFrom(cls)) {
							ooffUnit++;
						} else if (Building.class.isAssignableFrom(cls)) {
							ooffBuild++;
						} else {
							throw new AssertionError("the entity class is no unit and no building: " + cls);
						}
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
		if (e instanceof EntityImpl && !(e instanceof MyUnit || e instanceof MyBuild)) { return theGame; }
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
		if (u instanceof EntityImpl && !(u instanceof MyUnit)) { return theGame; }
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
		if (b instanceof EntityImpl && !(b instanceof MyUnit)) { return theGame; }
		SquareConquererAddon res = es.get(b.getClass());
		if (res == null) throw new AssertionError("unknown entity class: " + b.getClass());
		return res;
	}
	
	private static Map<String, SquareConquererAddon> loadAddons() {
		Map<String, SquareConquererAddon>   addons = new HashMap<>();
		ServiceLoader<SquareConquererAddon> loader = ServiceLoader.load(SquareConquererAddon.class);
		for (SquareConquererAddon addon : loader) {
			if (addons.put(addon.name, addon) != null) throw new AssertionError("multiple addons with the same name: '" + addon.name + "'");
		}
		String disabled = System.getProperty(DISABLED_ADDONS_PROPERTY);
		if (disabled != null) {
			int i = 0;
			while (true) {
				StringBuilder b = new StringBuilder();
				build: while (true) {
					if (disabled.length() == i) break /* build */;
					char c = disabled.charAt(i++);
					switch (c) {
					case '\\' -> {
						c = disabled.charAt(++i);
						switch (c) {
						case '\\', ':' -> b.append(c);
						default -> throw new AssertionError(
							"the property '" + DISABLED_ADDONS_PROPERTY + "' has an invalid escape sequence: \"\\" + c + "\" at char " + (i - 1));
						}
					}
					case ':' -> {
						break build;
					}
					default -> b.append(c);
					}
				}
				addons.remove(b.toString());
				if (++i > disabled.length()) break;
				// if the prop ends with a ':', remove the addon with an empty string as name
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
	
	/**
	 * returns the {@link AddonEntities} of this addon
	 * <p>
	 * every addon must have exactly one {@link AddonEntities} instance, which does not belongs to any other addon
	 * 
	 * @return the {@link AddonEntities} of this addon
	 */
	public abstract AddonEntities entities();
	
}
