// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.addons;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.addons.defaults.AddonDefaults;
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
public abstract class SCAddon {
	
	private static final String ADDONS_ARE_ALREADY_LOADED             = Messages.getString("SCAddon.addons-already-loaded");           //$NON-NLS-1$
	private static final Format DISABPLED_PROP_HAS_INVALID_ESCAPE     = Messages.getFormat("SCAddon.disabled-prop-invalid-escape");                  //$NON-NLS-1$
	private static final Format MULTIPLE_ADDONS_WITH_THE_SAME_NAME    = Messages.getFormat("SCAddon.multiple-addons-with-name");       //$NON-NLS-1$
	private static final Format UNKNOWN_ENTITY_CLASS                  = Messages.getFormat("SCAddon.unknown-entity");                  //$NON-NLS-1$
	private static final Format UNKNOWN_ADDON_NAME                    = Messages.getFormat("SCAddon.unknown-addon-name");              //$NON-NLS-1$
	private static final Format MULTIPLE_ADDONS_ADD_SAME_ENTITY_CLASS = Messages.getFormat("SCAddon.multiple-addons-add-same-entity"); //$NON-NLS-1$
	private static final Format MISSING_THE_GAME_ADDON_ADDONS         = Messages.getFormat("SCAddon.missing-the-game-addon");          //$NON-NLS-1$
	private static final String OFFSET_ALREADY_INITILIZED             = Messages.getString("SCAddon.offset-already-initilized");       //$NON-NLS-1$
	private static final Format BS_0_IN_NAME                          = Messages.getFormat("SCAddon.bs0-in-addon-name");               //$NON-NLS-1$
	
	/**
	 * the {@link #name} of {@link TheGameAddon}
	 */
	public static final String GAME_ADDON_NAME             = Messages.getString("SCAddon.the-game-addon-name"); //$NON-NLS-1$
	/**
	 * the property key ({@link System#getProperty(String)}) of a colon ({@code :}) separated list of all disabled addons. <br>
	 * to disable a addon with a colon in the name use a backslash before the colon ({@code \:})<br>
	 * to disable a addon with a backslash in the name use a backslash before the backslash ({@code \\})<br>
	 * on any other position a backslash is invalid and will let the load of the addons fail<br>
	 * if a non existing addon is disabled, it will be ignored
	 */
	public static final String DISABLED_ADDONS_PROPERTY    = "squareconquerer.addons.disabled";                 //$NON-NLS-1$
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
	public static final String RENAME_ADDON_PROPERTY_START = "squareconquerer.addons.rename.";                  //$NON-NLS-1$
	
	/**
	 * the name of the property which can be used to rename the {@link SCAddon} with the given name
	 * 
	 * @param cls the class of the {@link SCAddon}
	 * 
	 * @return the key of the property
	 * 
	 * @see #RENAME_ADDON_PROPERTY_START
	 */
	public static String renameAddonPropertyKey(Class<? extends SCAddon> cls) {
		String name = cls.getName();
		Module mod  = cls.getModule();
		if (mod.isNamed()) return RENAME_ADDON_PROPERTY_START + mod.getName() + ':' + name;
		return RENAME_ADDON_PROPERTY_START + name;
	}
	
	/**
	 * sets the rename property for the given class to the given value
	 * <p>
	 * note that the property change has no effect for already existing {@link SCAddon} instances
	 * 
	 * @param cls      the class to be renamed
	 * @param newValue the new {@link #name} value
	 * 
	 * @return the old value
	 * 
	 * @see #RENAME_ADDON_PROPERTY_START
	 */
	public static String renameAddonProperty(Class<? extends SCAddon> cls, String newValue) {
		return System.setProperty(renameAddonPropertyKey(cls), newValue);
	}
	
	/**
	 * disables the given addon name
	 * <p>
	 * note that disabling addons is only effective before the addons are loaded
	 * 
	 * @param name the name to be disabled
	 * @see #DISABLED_ADDONS_PROPERTY
	 * @throws IllegalStateException if the addons are already loaded
	 */
	public static void addDisableAddon(String name) throws IllegalStateException {
		if (addons != null) throw new IllegalStateException(ADDONS_ARE_ALREADY_LOADED);
		StringBuilder sb       = new StringBuilder();
		String        disabled = System.getProperty(DISABLED_ADDONS_PROPERTY);
		if (disabled != null) {
			sb.append(disabled);
			sb.append(':');
		}
		appendDisableName(name, sb);
		System.setProperty(DISABLED_ADDONS_PROPERTY, sb.toString());
	}
	
	/**
	 * disables the given addon names
	 * 
	 * @param names the names to be disabled
	 * @see #DISABLED_ADDONS_PROPERTY
	 * @throws IllegalStateException if the addons are already loaded
	 */
	public static void addAllDisableAddon(Iterable<String> names) throws IllegalStateException {
		if (addons != null) throw new IllegalStateException(ADDONS_ARE_ALREADY_LOADED);
		StringBuilder sb       = new StringBuilder();
		String        disabled = System.getProperty(DISABLED_ADDONS_PROPERTY);
		if (disabled != null) sb.append(disabled);
		Iterator<String> iter = names.iterator();
		if (!iter.hasNext()) return;
		
		System.setProperty(DISABLED_ADDONS_PROPERTY, sb.toString());
	}
	
	private static void appendDisableName(String name, StringBuilder sb) throws AssertionError {
		for (int i = 0;;) {
			int nb = name.indexOf('\\', i);
			int nc = name.indexOf(':', i);
			if (nb == -1 && nc == -1) {
				int len = name.length();
				if (i < len) {
					if (i == 0) sb.append(name);
					else sb.append(name, i, len);
				}
				break;
			}
			int ni = nb == -1 ? nc : nc == -1 ? nb : Math.min(nb, nc);
			sb.append(name, i, ni);
			switch (name.charAt(ni)) {
			case ':' -> sb.append("\\:"); //$NON-NLS-1$
			case '\\' -> sb.append("\\\\"); //$NON-NLS-1$
			default -> throw new AssertionError("invalid char: '" + name.charAt(ni) + '\''); //$NON-NLS-1$ this should never happen
			}
			i = ni + 1;
		}
	}
	
	/**
	 * returns a list of all disabled addon names
	 * <p>
	 * note that if the property changes after the addons are loaded the result of this method will change, but the addons will not be disabled
	 * 
	 * @return a list of all disabled addon names
	 * @see #DISABLED_ADDONS_PROPERTY
	 */
	public static List<String> getDisabledAddons() {
		return removeOrGetDisabledAddons(null);
	}
	
	@SuppressWarnings("null")
	private static List<String> removeOrGetDisabledAddons(Map<String, SCAddon> map) throws AssertionError {
		List<String> res      = map == null ? new ArrayList<>() : null;
		String       disabled = System.getProperty(DISABLED_ADDONS_PROPERTY);
		if (disabled != null) {
			int i = 0;
			while (true) {
				StringBuilder b = new StringBuilder();
				build: while (true) {
					if (disabled.length() == i) break /* build */;
					char c = disabled.charAt(i++);
					switch (c) {
					case '\\' -> {
						if (disabled.length() == i) {
							String cStr  = Character.toString(c);
							String index = Integer.toString(i - 1);
							String msg   = Messages.format(DISABPLED_PROP_HAS_INVALID_ESCAPE, DISABLED_ADDONS_PROPERTY, cStr, index);
							throw new AssertionError(msg);
						}
						c = disabled.charAt(i++);
						switch (c) {
						case '\\', ':' -> b.append(c);
						default -> {
							String cStr  = Character.toString(c);
							String index = Integer.toString(i - 1);
							String msg   = Messages.format(DISABPLED_PROP_HAS_INVALID_ESCAPE, DISABLED_ADDONS_PROPERTY, cStr, index);
							throw new AssertionError(msg);
						}
						}
					}
					case ':' -> {
						break build;
					}
					default -> b.append(c);
					}
				}
				if (map == null) res.add(b.toString());
				else map.remove(b.toString());
				if (++i > disabled.length()) break;
				// if the prop ends with a ':', remove the addon with an empty string as name
			}
		}
		return res;
	}
	
	/**
	 * this field stores the unique name of this add-on
	 */
	public final String name;
	/**
	 * this field stores the localized name of this add-on
	 */
	public final String localName;
	private int         oridinalUnitOffset;
	private int         oridinalBuildingOffset;
	
	/**
	 * creates a new {@link SCAddon} instance with the given {@code name}.
	 * <p>
	 * the name must be unique<br>
	 * this means, that the name {@value #GAME_ADDON_NAME} is not allowed to be used by any other add-on than the {@link TheGameAddon}.
	 * <p>
	 * if the System property {@link #renameAddonPropertyKey(Class)} is set, its value will be used as {@link #name} instead of <code>name</code>, which will be
	 * ignored.<br>
	 * see {@link #RENAME_ADDON_PROPERTY_START} for details.
	 * 
	 * @param name the {@link #name} and {@link #localName} of the add-on
	 */
	public SCAddon(String name) {
		String overwrittenName = System.getProperty(renameAddonPropertyKey(getClass()));
		if (overwrittenName != null) this.name = overwrittenName;
		else this.name = name;
		if (this.name.lastIndexOf('\0') != -1) throw new IllegalStateException(Messages.format(BS_0_IN_NAME, this.name));
		this.localName = this.name;
	}
	
	/**
	 * creates a new {@link SCAddon} instance with the given {@link #name} and {@link #localName}.
	 * <p>
	 * the name must be unique<br>
	 * this means, that the name {@value #GAME_ADDON_NAME} is not allowed to be used by any other add-on than the {@link TheGameAddon}.
	 * <p>
	 * if the System property {@link #renameAddonPropertyKey(Class)} is set, its value will be used as {@link #name} (and {@link #localName}) instead of
	 * <code>name</code> (and <code>localName</code>), which will be ignored.<br>
	 * see {@link #RENAME_ADDON_PROPERTY_START} for details.
	 * 
	 * @param name      the {@link #name} of the add-on
	 * @param localName the {@link #localName} of the add-on
	 */
	public SCAddon(String name, String localName) {
		String overwrittenName = System.getProperty(renameAddonPropertyKey(getClass()));
		if (overwrittenName != null) {
			this.name      = overwrittenName;
			this.localName = overwrittenName;
		} else {
			this.name      = name;
			this.localName = localName;
		}
		if (this.name.lastIndexOf('\0') != -1) throw new IllegalStateException(Messages.format(BS_0_IN_NAME, this.name));
	}
	
	private void initOridinalOffset(int unitOff, int buildOff) {
		if (this.oridinalUnitOffset != 0) { throw new AssertionError(OFFSET_ALREADY_INITILIZED); }
		this.oridinalUnitOffset     = unitOff;
		this.oridinalBuildingOffset = buildOff;
	}
	
	/**
	 * returns the ordinal unit offset of this {@link SCAddon}
	 * 
	 * @return the ordinal unit offset of this {@link SCAddon}
	 */
	public int oridinalOffsetUnit() {
		return this.oridinalUnitOffset;
	}
	
	/**
	 * returns the ordinal building offset of this {@link SCAddon}
	 * 
	 * @return the ordinal building offset of this {@link SCAddon}
	 */
	public int oridinalOffsetBuilding() {
		return this.oridinalBuildingOffset;
	}
	
	private static volatile Map<String, SCAddon>                  addons;
	private static volatile TheGameAddon                          theGame;
	private static volatile Map<Class<? extends Entity>, SCAddon> entity;
	
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
	public static Collection<SCAddon> onlyAddons() {
		Map<String, SCAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a.values();
	}
	
	/**
	 * returns a unmodifiable map containing almost all add-ons (excluding the game add-on)
	 * <p>
	 * the add-ons are mapped with their {@link #name} as keys
	 * 
	 * @return a unmodifiable map containing almost all add-ons (excluding the game add-on)
	 */
	public static Map<String, SCAddon> onlyAddonsMap() {
		Map<String, SCAddon> a = new HashMap<>(addonsMap());
		a.remove(GAME_ADDON_NAME);
		return a;
	}
	
	/**
	 * returns an unmodifiable collection containing all add-ons (including the game add-on)
	 * 
	 * @return an unmodifiable collection containing all add-ons (including the game add-on)
	 */
	public static Collection<SCAddon> addons() {
		Map<String, SCAddon> a = addons;
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
	public static Map<String, SCAddon> addonsMap() {
		Map<String, SCAddon> a = addons;
		if (a != null) return a;
		synchronized (SCAddon.class) {
			a = addons;
			if (a != null) return a;
			a = loadAddons();
			TheGameAddon g = (TheGameAddon) a.get(GAME_ADDON_NAME);
			if (g == null) {
				throw new AssertionError(Messages.format(MISSING_THE_GAME_ADDON_ADDONS, a));
			}
			Map<Class<? extends Entity>, SCAddon> es         = new HashMap<>();
			int                                   ooffUnit  = 1;              // null
			int                                   ooffBuild = 1;              // null
			for (SCAddon addon : new TreeMap<>(a).values()) {
				addon.initOridinalOffset(ooffUnit, ooffBuild);
				Map<Class<? extends Entity>, String> map        = addon.entities().entityClassses();
				Set<String>                          wasAlready = new HashSet<>();
				for (Entry<Class<? extends Entity>, String> e : map.entrySet()) {
					Class<? extends Entity> cls = e.getKey();
					if (es.put(cls, addon) != null) {
						throw new AssertionError(MULTIPLE_ADDONS_ADD_SAME_ENTITY_CLASS);
					}
					String clsName = e.getValue();
					if (wasAlready.add(clsName)) {
						if (Unit.class.isAssignableFrom(cls)) {
							ooffUnit++;
						} else if (Building.class.isAssignableFrom(cls)) {
							ooffBuild++;
						} else {
							throw new AssertionError("the entity class is no unit and no building: " + cls); //$NON-NLS-1$ this should never happen
						}
					}
				}
			}
			entity = Collections.unmodifiableMap(es);
			addons  = Collections.unmodifiableMap(a);
			theGame = g;
			return a;
		}
	}
	
	/**
	 * returns the {@link SCAddon} with the given name
	 * 
	 * @param name the {@link #name} of the {@link SCAddon}
	 * 
	 * @return the {@link SCAddon} with the given name
	 */
	public static SCAddon addon(String name) {
		Map<String, SCAddon> as = addons;
		if (as == null) {
			as = addonsMap();
		}
		SCAddon a = as.get(name);
		if (a == null) {
			throw new AssertionError(Messages.format(UNKNOWN_ADDON_NAME, name));
		}
		return a;
	}
	
	/**
	 * returns the {@link SCAddon} from the entity
	 * 
	 * @param e an entity from the {@link SCAddon}
	 * 
	 * @return the {@link SCAddon} from the entity
	 */
	public static SCAddon addon(Entity e) {
		Map<Class<? extends Entity>, SCAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (e instanceof EntityImpl && !(e instanceof MyUnit || e instanceof MyBuild)) return theGame;
		SCAddon res = es.get(e.getClass());
		if (res == null) throw new AssertionError(Messages.format(UNKNOWN_ENTITY_CLASS, e.getClass()));
		return res;
	}
	
	/**
	 * returns the {@link SCAddon} from the entity
	 * 
	 * @param e an entity from the {@link SCAddon}
	 * 
	 * @return the {@link SCAddon} from the entity
	 */
	public static SCAddon addon(Class<? extends Entity> e) {
		Map<Class<? extends Entity>, SCAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		}
		SCAddon res = es.get(e);
		if (res == null) throw new AssertionError(Messages.format(UNKNOWN_ENTITY_CLASS, e.getClass()));
		return res;
	}
	
	/**
	 * returns the {@link SCAddon} from the unit
	 * 
	 * @param u an unit from the {@link SCAddon}
	 * 
	 * @return the {@link SCAddon} from the unit
	 */
	public static SCAddon addon(Unit u) {
		Map<Class<? extends Entity>, SCAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (u instanceof EntityImpl && !(u instanceof MyUnit)) { return theGame; }
		SCAddon res = es.get(u.getClass());
		if (res == null) throw new AssertionError(Messages.format(UNKNOWN_ENTITY_CLASS, u.getClass()));
		return res;
	}
	
	/**
	 * returns the {@link SCAddon} from the building
	 * 
	 * @param b a building from the {@link SCAddon}
	 * 
	 * @return the {@link SCAddon} from the building
	 */
	public static SCAddon addon(Building b) {
		Map<Class<? extends Entity>, SCAddon> es = entity;
		if (es == null) {
			addonsMap();
			es = entity;
		} // potentially faster check for the game entities
		if (b instanceof EntityImpl && !(b instanceof MyUnit)) { return theGame; }
		SCAddon res = es.get(b.getClass());
		if (res == null) throw new AssertionError(Messages.format(UNKNOWN_ENTITY_CLASS, b.getClass()));
		return res;
	}
	
	private static Map<String, SCAddon> loadAddons() {
		Map<String, SCAddon>   addons = new HashMap<>();
		ServiceLoader<SCAddon> loader = ServiceLoader.load(SCAddon.class);
		for (SCAddon addon : loader) {
			if (addons.put(addon.name, addon) != null) throw new AssertionError(Messages.format(MULTIPLE_ADDONS_WITH_THE_SAME_NAME, addon.name));
		}
		removeOrGetDisabledAddons(addons);
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
	
	/**
	 * returns the {@link AddonDefaults} of this addon
	 * <p>
	 * every addon must have exactly one {@link AddonDefaults} instance, which does not belongs to any other addon
	 * 
	 * @return the {@link AddonDefaults} of this addon
	 */
	public abstract AddonDefaults defaults();
	
}
